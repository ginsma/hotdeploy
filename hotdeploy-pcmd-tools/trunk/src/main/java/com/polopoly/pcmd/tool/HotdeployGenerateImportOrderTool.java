package com.polopoly.pcmd.tool;

import static example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME;

import java.io.File;
import java.io.IOException;

import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.discovery.importorder.ImportOrder;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFile;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileWriter;
import example.deploy.xml.consistency.PresentFileReader;
import example.deploy.xml.ordergenerator.ImportOrderGenerator;

public class HotdeployGenerateImportOrderTool implements Tool<ForceAndFilesToDeployParameters> {
    public ForceAndFilesToDeployParameters createParameters() {
        return new ForceAndFilesToDeployParameters();
    }

    public void execute(PolopolyContext context,
            ForceAndFilesToDeployParameters parameters) {
        ImportOrder importOrder = generateImportOrder(parameters);

        ImportOrderFile importOrderFile = new ImportOrderFile(importOrder);

        abortIfFileExistsAndNotForce(parameters, importOrderFile);

        writeFile(importOrderFile);
    }

    static ImportOrder generateImportOrder(
            FilesToDeployParameters parameters) {
        ImportOrderGenerator importOrderGenerator = new
                    ImportOrderGenerator();

        File directory = parameters.getDirectory();

        new PresentFileReader(directory, importOrderGenerator).read();

        ImportOrder importOrder = importOrderGenerator.generate(parameters.discoverFiles());

        importOrder.setDirectory(directory);

        return importOrder;
    }

    static void writeFile(ImportOrderFile importOrderFile) {
        try {
            new ImportOrderFileWriter(importOrderFile).write();

            System.out.println("Wrote import order file " + importOrderFile + ".");
        } catch (IOException e) {
            System.err.println("Could not write import file " + importOrderFile + ": " + e.getMessage());
        }
    }

    private void abortIfFileExistsAndNotForce(ForceAndFilesToDeployParameters parameters, ImportOrderFile importOrderFile) {
        if (parameters.isForce()) {
            return;
        }

        try {
            if (importOrderFile.getFile().exists()) {
                System.err.println("The import order file "+ importOrderFile + " already exists. " +
            		"Use --"+ ForceAndFilesToDeployParameters.FORCE_PARAMETER + " to overwrite.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("The import order file "+ importOrderFile + " cannot be found: " + e.getMessage());
            System.exit(1);
        }
    }

    public String getHelp() {
        return "Analyzes content XML to find out the order in which it should be be imported. Generates an " + IMPORT_ORDER_FILE_NAME + " file.";
    }
}
