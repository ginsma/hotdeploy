package com.polopoly.pcmd.tool;

import static com.polopoly.pcmd.tool.HotdeployGenerateImportOrderTool.generateImportOrder;
import static com.polopoly.pcmd.tool.HotdeployGenerateImportOrderTool.writeFile;
import static example.deploy.hotdeploy.util.Plural.plural;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.pcmd.bootstrap.BootstrapFileGenerator;
import com.polopoly.pcmd.tool.parameters.FilesToDeployParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.deployer.DefaultSingleFileDeployer;
import example.deploy.hotdeploy.deployer.FatalDeployException;
import example.deploy.hotdeploy.deployer.MultipleFileDeployer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.discovery.ResourceFileDiscoverer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrder;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFile;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.CouldNotUpdateStateException;
import example.deploy.hotdeploy.state.DirectoryState;
import example.deploy.hotdeploy.state.DirectoryStateFetcher;
import example.deploy.hotdeploy.state.NoFilesImportedDirectoryState;
import example.deploy.xml.parser.ContentXmlParser;
import example.deploy.xml.parser.cache.CachingDeploymentFileParser;

public class HotdeployTool implements Tool<HotdeployParameters> {
    private final class LoggingSingleFileDeployer extends DefaultSingleFileDeployer {
        private int fileCount;
        private int imported;

        private LoggingSingleFileDeployer(PolicyCMServer server, int fileCount) {
            super(server);

            this.fileCount = fileCount;
        }

        @Override
        public boolean importAndHandleException(
                DeploymentFile fileToImport) throws FatalDeployException {
            StringBuffer message = new StringBuffer(100);

            message.append("Importing ");
            message.append(fileToImport);

            if (fileCount > 10) {
                message.append(" (" + getPercentage(imported++) + "%)");
            }

            message.append("...");

            System.out.println(message);

            return super.importAndHandleException(fileToImport);
        }

        private String getPercentage(int i) {
            return Integer.toString(100 * imported / fileCount);
        }
    }

    private CachingDeploymentFileParser cachingParser;
    private DirectoryState directoryState;
    private DirectoryStateFetcher directoryStateFetcher;

    public HotdeployTool() {
        cachingParser = new CachingDeploymentFileParser(new ContentXmlParser());
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
        ImportOrder importOrder =
            generateImportOrder(cachingParser, parameters, parameters.isIgnorePresent());

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
            return new NoFilesImportedDirectoryState();
        }
        else {
            directoryStateFetcher =
                new DirectoryStateFetcher(context.getPolicyCMServer());

            return directoryStateFetcher.getDirectoryState();
        }
    }

    public void execute(PolopolyContext context,
            HotdeployParameters parameters) {
        directoryState = getDirectoryState(context, parameters);

        try {
            if (parameters.isSearchResources()) {
                deployResourceContent(context);
            }

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

            System.out.println(files.size() + " content file" + plural(files.size()) +
                    " found in " + parameters.getFileOrDirectory().getAbsolutePath() + "...");

            MultipleFileDeployer deployer =
                createDeployer(context, parameters.getDirectory(), files.size());

            deployer.deploy(files);

            System.out.println(deployer.getResultMessage(files));
        } catch (FatalDeployException e) {
            System.err.println("Deployment could not be performed: " + e.getMessage());
        } finally {
            persistState(directoryState);
        }
    }

    private MultipleFileDeployer createDeployer(PolopolyContext context, File directory, int fileCount) {
        return new MultipleFileDeployer(
                new LoggingSingleFileDeployer(context.getPolicyCMServer(), fileCount),
                directory,
                directoryState);
    }

    private void deployResourceContent(PolopolyContext context) throws FatalDeployException {
        try {
            List<DeploymentFile> resourceFiles =
                new ResourceFileDiscoverer().getFilesToImport(getClass().getClassLoader());

            if (resourceFiles.isEmpty()) {
                return;
            }

            MultipleFileDeployer deployer =
                createDeployer(context, new File("."), resourceFiles.size());

            deployer.deploy(resourceFiles);

            if (!deployer.isAllFilesUnchanged()) {
                System.out.println("Content in the classpath: " +
                        deployer.getResultMessage(resourceFiles));
            }

            // we might have deployed the hotdeploy templates here. if that is the
            // case we can now (and only now) fetch the directory state.
            if (directoryStateFetcher != null) {
                directoryState = directoryStateFetcher.refreshAfterFailingToFetch();
            }
        } catch (NotApplicableException e) {
            // no resource content. fine.
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
