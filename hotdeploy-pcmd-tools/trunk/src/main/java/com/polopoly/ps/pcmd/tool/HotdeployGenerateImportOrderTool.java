package com.polopoly.ps.pcmd.tool;

import static com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME;
import static com.polopoly.ps.hotdeploy.util.Plural.count;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrder;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFile;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileWriter;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.ordergenerator.AddWholeRootToImportOrderFile;
import com.polopoly.ps.hotdeploy.xml.ordergenerator.ImportOrderGenerator;
import com.polopoly.ps.hotdeploy.xml.parser.ContentXmlParser;
import com.polopoly.ps.hotdeploy.xml.parser.DeploymentFileParser;
import com.polopoly.ps.hotdeploy.xml.present.PresentFileReader;
import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.pcmd.tool.parameters.FilesToDeployParameters;
import com.polopoly.ps.pcmd.tool.parameters.ForceAndFilesToDeployParameters;
import com.polopoly.util.client.PolopolyContext;


public class HotdeployGenerateImportOrderTool implements Tool<ForceAndFilesToDeployParameters> {
    public ForceAndFilesToDeployParameters createParameters() {
        return new ForceAndFilesToDeployParameters();
    }

    public void execute(PolopolyContext context,
            ForceAndFilesToDeployParameters parameters) {
        ImportOrder importOrder = generateImportOrder(new ContentXmlParser(), parameters, parameters.isIgnorePresent());

        ImportOrderFile importOrderFile = new ImportOrderFile(importOrder);

        abortIfFileExistsAndNotForce(parameters, importOrderFile);

        writeFile(importOrderFile);
    }

    static ImportOrder generateImportOrder(DeploymentFileParser parser, FilesToDeployParameters parameters, boolean ignorePresent) {
        ImportOrderGenerator importOrderGenerator = new ImportOrderGenerator(parser);

        File directory = parameters.getDirectory();

        if (!ignorePresent) {
            new PresentFileReader(directory, importOrderGenerator).read();
        }

        List<DeploymentFile> deploymentFiles = parameters.discoverFiles();

        System.out.println("Calculating import order for " + count(deploymentFiles, "file") + "...");

        ImportOrder importOrder = importOrderGenerator.generate(deploymentFiles);

        importOrder.setDirectory(directory);

        return importOrder;
    }

    static void writeFile(ImportOrder importOrder) {
        writeFile(new ImportOrderFile(importOrder));
    }

    static void writeFile(ImportOrderFile importOrderFile) {
        new AddWholeRootToImportOrderFile(importOrderFile).addWholeRoot();

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
