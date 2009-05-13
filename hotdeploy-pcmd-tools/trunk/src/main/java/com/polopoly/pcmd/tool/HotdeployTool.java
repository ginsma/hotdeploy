package com.polopoly.pcmd.tool;

import static com.polopoly.pcmd.tool.HotdeployGenerateImportOrderTool.generateImportOrder;
import static com.polopoly.pcmd.tool.HotdeployGenerateImportOrderTool.writeFile;
import static example.deploy.hotdeploy.util.Plural.plural;

import java.io.IOException;
import java.util.List;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.pcmd.bootstrap.BootstrapFileGenerator;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.deployer.DefaultSingleFileDeployer;
import example.deploy.hotdeploy.deployer.FatalDeployException;
import example.deploy.hotdeploy.deployer.MultipleFileDeployer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrder;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFile;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.AlwaysChangedDirectoryState;
import example.deploy.hotdeploy.state.CouldNotUpdateStateException;
import example.deploy.hotdeploy.state.DirectoryState;
import example.deploy.hotdeploy.state.DirectoryStateFetcher;
import example.deploy.xml.parser.XmlParser;
import example.deploy.xml.parser.cache.CachingDeploymentFileParser;

public class HotdeployTool implements Tool<HotdeployParameters> {
    private final class LoggingSingleFileDeployer extends
    DefaultSingleFileDeployer {
        private LoggingSingleFileDeployer(PolicyCMServer server) {
            super(server);
        }

        @Override
        public boolean importAndHandleException(
                DeploymentFile fileToImport)
        throws FatalDeployException {
            System.out.println("Importing " + fileToImport + "...");

            return super.importAndHandleException(fileToImport);
        }
    }

    private CachingDeploymentFileParser cachingParser;

    public HotdeployTool() {
        cachingParser = new CachingDeploymentFileParser(new XmlParser());
    }

    public HotdeployParameters createParameters() {
        return new HotdeployParameters();
    }

    private boolean importOrderAvailable(FilesToDeployParameters parameters) {
        try {
            return new ImportOrderFile(parameters.getDirectory()).getFile().exists();
        } catch (IOException e) {
            System.err.println("Could not determine where to place import order: " + e.getMessage() + ". Not writing import order file.");

            return false;
        }
    }

    private boolean shouldCreateImportOrder(HotdeployParameters parameters,
            List<DeploymentFile> files) {
        return files.size() > 1 && (!importOrderAvailable(parameters) || parameters.isGenerateImportOrder());
    }

    private List<DeploymentFile> createImportOrder(
            HotdeployParameters parameters) {
        ImportOrder importOrder = generateImportOrder(cachingParser, parameters, parameters.isIgnorePresent());

        writeFile(importOrder);

        return importOrder;
    }

    private boolean shouldCreateBootstrap(HotdeployParameters parameters,
            boolean createImportOrder) {
        return createImportOrder || parameters.isGenerateBootstrap();
    }

    private void createBootstrap(HotdeployParameters parameters,
            List<DeploymentFile> files) {
        BootstrapFileGenerator generator = new BootstrapFileGenerator();

        generator.setForce(true);
        generator.setBootstrapNonCreated(parameters.isBootstrapNonCreated());
        generator.setIgnorePresent(parameters.isIgnorePresent());
        generator.setParser(cachingParser);

        generator.generateBootstrap(parameters.getDirectory(), files);
    }

    private DirectoryState getDirectoryState(PolopolyContext context, HotdeployParameters parameters) {
        if (parameters.isForce()) {
            return new AlwaysChangedDirectoryState();
        }
        else {
            return new DirectoryStateFetcher(context.getPolicyCMServer()).getDirectoryState();
        }
    }

    public void execute(PolopolyContext context,
            HotdeployParameters parameters) {
        DirectoryState directoryState = getDirectoryState(context, parameters);

        try {
            List<DeploymentFile> files = parameters.discoverFiles();

            boolean createImportOrder =
                shouldCreateImportOrder(parameters, files);

            if (createImportOrder) {
                files = createImportOrder(parameters);
            }

            boolean createBootstrap =
                shouldCreateBootstrap(parameters, createImportOrder);

            if (createBootstrap) {
                createBootstrap(parameters, files);
            }

            System.out.println(files.size() + " content file" + plural(files.size()) + " found...");

            MultipleFileDeployer deployer = new MultipleFileDeployer(
                    new LoggingSingleFileDeployer(context.getPolicyCMServer()),
                    parameters.getDirectory(),
                    directoryState);

            deployer.deploy(files);

            System.out.println(deployer.getResultMessage(files));
        } catch (FatalDeployException e) {
            System.err.println("Deployment could not be performed: " + e.getMessage());
        } finally {
            persistState(directoryState);
        }
    }

    private void persistState(DirectoryState directoryState) {
        try {
            directoryState.persist();
        } catch (CouldNotUpdateStateException e) {
            System.err.println("Error recording deployment state: " + e.getMessage());
        }
    }

    public String getHelp() {
        return "Imports content files that have been modified since last time they were imported (or were never imported).";
    }
}
