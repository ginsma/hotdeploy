package com.polopoly.pcmd.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.discovery.importorder.ImportOrderFile;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileParser;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileWriter;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class BootstrapToImportOrderAdder {
    private File bootstrapFile;
    private static final Logger logger =
        Logger.getLogger(BootstrapToImportOrderAdder.class.getName());

    public BootstrapToImportOrderAdder(File bootstrapFile) {
        this.bootstrapFile = bootstrapFile;
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

    public void addBootstrapToImportOrderIfItExists() {
        File directory = bootstrapFile.getParentFile();

        File importOrderFile = new File(directory, ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME);

        if (importOrderFile.exists()) {
            addBootstrapToImportOrder(importOrderFile, bootstrapFile);
        }
    }
}
