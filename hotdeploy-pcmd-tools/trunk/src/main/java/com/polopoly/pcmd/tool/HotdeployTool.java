package com.polopoly.pcmd.tool;

import static com.polopoly.pcmd.tool.HotdeployGenerateImportOrderTool.generateImportOrder;
import static com.polopoly.pcmd.tool.HotdeployGenerateImportOrderTool.writeFile;

import java.io.IOException;
import java.util.List;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.deployer.DefaultSingleFileDeployer;
import example.deploy.hotdeploy.deployer.FatalDeployException;
import example.deploy.hotdeploy.deployer.MultipleFileDeployer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrder;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFile;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.CouldNotUpdateStateException;
import example.deploy.hotdeploy.state.DirectoryState;
import example.deploy.hotdeploy.state.DirectoryStateFetcher;

public class HotdeployTool implements Tool<FilesToDeployParameters> {
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

    public FilesToDeployParameters createParameters() {
        return new FilesToDeployParameters();
    }

    public void execute(PolopolyContext context,
            FilesToDeployParameters parameters) {
        DirectoryState directoryState = new DirectoryStateFetcher(context.getPolicyCMServer()).getDirectoryState();

        try {
            List<DeploymentFile> files = parameters.discoverFiles();

            if (files.size() > 1 && !importOrderAvailable(parameters)) {
                ImportOrder importOrder = generateImportOrder(parameters);

                files = importOrder;

                ImportOrderFile importOrderFile = new ImportOrderFile(importOrder);
                writeFile(importOrderFile);


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
            try {
                directoryState.persist();
            } catch (CouldNotUpdateStateException e) {
                System.err.println("Error recording deployment state: " + e.getMessage());
            }
        }
    }

    private boolean importOrderAvailable(FilesToDeployParameters parameters) {
        try {
            return new ImportOrderFile(parameters.getDirectory()).getFile().exists();
        } catch (IOException e) {
            System.err.println("Could not determine where to place import order: " + e.getMessage() + ". Not writing import order file.");

            return false;
        }
    }

    private String plural(int size) {
        if (size == 1) {
            return "";
        }
        else {
            return "s";
        }
    }

    public String getHelp() {
        return "Imports content files that have been modified since last time they were imported (or were never imported).";
    }

}
