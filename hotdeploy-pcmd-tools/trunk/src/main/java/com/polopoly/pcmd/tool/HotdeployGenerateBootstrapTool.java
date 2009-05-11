package com.polopoly.pcmd.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.discovery.importorder.ImportOrderFile;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileParser;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileWriter;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.xml.bootstrap.Bootstrap;
import example.deploy.xml.bootstrap.BootstrapContent;
import example.deploy.xml.bootstrap.BootstrapFileWriter;
import example.deploy.xml.bootstrap.BootstrapGenerator;
import example.deploy.xml.consistency.PresentFileReader;
import example.deploy.xml.parser.XmlParser;

public class HotdeployGenerateBootstrapTool implements Tool<BootstrapParameters> {
    private static final Logger logger =
        Logger.getLogger(HotdeployGenerateBootstrapTool.class.getName());

    private static final String BOOTSTRAP_FILE_NAME = "bootstrap.xml";

    public BootstrapParameters createParameters() {
        return new BootstrapParameters();
    }

    private void checkFileDoesNotExist(File bootstrapFile) {
        if (bootstrapFile.exists()) {
            System.err.println("The bootstrap file " + bootstrapFile.getAbsolutePath() + " already exists. " +
    		"Use --" + BootstrapParameters.FORCE_PARAMETER + " to overwrite.");
            System.exit(1);
        }
    }

    private void writeBootstrapToFile(Bootstrap bootstrap, File bootstrapFile) {
        try {
            FileWriter writer = new FileWriter(bootstrapFile, false);
            new BootstrapFileWriter(bootstrap).write(writer);
            writer.close();
        } catch (IOException e) {
            System.err.println("Could not write to the new bootstrap file " + bootstrapFile.getAbsolutePath() + ": " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Wrote bootstrap content to " + bootstrapFile.getAbsolutePath() + ".");
    }

    private ImportOrderFile readImportOrder(
            File importOrderFile) throws IOException {
        ImportOrderFile importOrder =
            new ImportOrderFileParser(importOrderFile).parse();

        return importOrder;
    }

    private void writeImportOrder(ImportOrderFile importOrderFile)
            throws IOException {
        new ImportOrderFileWriter(importOrderFile).write();
    }

    private void addBootstrapToImportOrder(File importOrderFile, File bootstrapFile) {
        try {
            ImportOrderFile importOrder = readImportOrder(importOrderFile);

            FileDeploymentFile bootstrapFileAsDeploymentFile = new FileDeploymentFile(bootstrapFile);

            importOrder.removeDeploymentObject(bootstrapFileAsDeploymentFile);
            importOrder.addDeploymentObject(0, bootstrapFileAsDeploymentFile);

            writeImportOrder(importOrder);

            System.out.println("Added bootstrap file to import order " + importOrderFile + ".");
        } catch (IOException e) {
            logger.log(Level.WARNING, "While adding bootstrap file to import order file " + importOrderFile + ": " + e.getMessage(), e);
        }
    }

    private void addBootstrapToImportOrderIfItExists(File bootstrapFile) {
        File directory = bootstrapFile.getParentFile();

        File importOrderFile = new File(directory, ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME);

        if (importOrderFile.exists()) {
            addBootstrapToImportOrder(importOrderFile, bootstrapFile);
        }
    }

    private File getBootstrapFile(FilesToDeployParameters parameters) {
        File directory = parameters.getDirectory();

        File bootstrapFile = new File(directory, BOOTSTRAP_FILE_NAME);

        return bootstrapFile;
    }

    private void logBootstrapResult(Bootstrap bootstrap) {
        System.err.println(bootstrap.size() + " content object" + (bootstrap.size() != 1 ? "s" : "") + " needed bootstrapping.");
    }

    public void execute(PolopolyContext context, BootstrapParameters parameters) {
        List<DeploymentFile> deploymentFiles = parameters.discoverFiles();

        if (deploymentFiles.size() == 0) {
            System.err.println("No content XML files found in " + parameters.getFileOrDirectory() + ".");
            System.exit(1);
        }

        System.err.println(deploymentFiles.size() +  " content XML file" + plural(deploymentFiles.size()) + "s to generate bootstrap for.");

        File bootstrapFile = getBootstrapFile(parameters);

        if (parameters.isForce()) {
            deploymentFiles.remove(bootstrapFile);
        }

        Bootstrap bootstrap =
            new BootstrapGenerator(new XmlParser()).generateBootstrap(deploymentFiles);

        // eliminate present files.
        new PresentFileReader(parameters.getDirectory(), bootstrap).read();

        List<BootstrapContent> notBootstrapped = bootstrap.getNeverCreatedButReferenced();

        if (parameters.isBootstrapNonCreated()) {
            bootstrapNonCreated(bootstrap, notBootstrapped);
        }
        else {
            if (!notBootstrapped.isEmpty()) {
                logReferencedButNotCreated(notBootstrapped);
            }
        }

        Collections.sort(bootstrap);

        logBootstrapResult(bootstrap);

        if (!bootstrap.isEmpty()) {
            if (!parameters.isForce()) {
                checkFileDoesNotExist(bootstrapFile);
            }

            writeBootstrapToFile(bootstrap, bootstrapFile);

            addBootstrapToImportOrderIfItExists(bootstrapFile);
        }
    }

    private void bootstrapNonCreated(Bootstrap bootstrap, List<BootstrapContent> notBootstrapped) {
        for (BootstrapContent bootstrapContent : notBootstrapped) {
            bootstrap.add(bootstrapContent);
        }
    }

    private void logReferencedButNotCreated(List<BootstrapContent> notBootstrapped) {
        System.out.println(notBootstrapped.size() + " content object" + plural(notBootstrapped.size()) +
                " were referenced but never created within the files to deploy. Use --" + BootstrapParameters.BOOTSTRAP_NON_CREATED_PARAMETER +
                " to bootstrap these objects too.");

        if (notBootstrapped.size() < 10) {
            System.out.println("These are the objects: " + notBootstrapped);
            System.out.println("Use the validate tool to find out where they are referenced.");
        }
        else {
            System.out.println("Some of these objects are " + notBootstrapped.subList(0, 9));
            System.out.println("Use the validate tool to see the full list and find out where they are referenced.");
        }
    }

    private String plural(int number) {
        if (number == 1) {
            return "";
        }
        else {
            return "s";
        }
    }

    public String getHelp() {
        return "Generates a bootstrap.xml file with the content needed for the files in the specified directory to import properly.";
    }
}
