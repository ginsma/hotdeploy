package example.deploy.xml.ordergenerator;

import static example.deploy.xml.consistency.ParameterConstants.DIRECTORY_ARGUMENT;

import java.io.File;
import java.util.List;

import example.deploy.hotdeploy.client.ArgumentConsumer;
import example.deploy.hotdeploy.client.ArgumentParser;
import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.ImportOrderOrDirectoryFileDiscoverer;

public class ImportOrderGeneratorArgumentParser implements ArgumentConsumer {
    private String[] args;
    private List<FileDiscoverer> discoverers;

    public ImportOrderGeneratorArgumentParser(ImportOrderGenerator generator, List<FileDiscoverer> discoverers, String[] args) {
        this.discoverers = discoverers;
        this.args = args;
    }

    public void parse() {
        new ArgumentParser(this, args).parse();
    }

    public boolean argumentFound(String argument, String value) {
        if (argument.equals(DIRECTORY_ARGUMENT)) {
            File directory = new File(value);

            if (!directory.exists() && directory.isDirectory()) {
                System.err.println("Directory " + directory.getAbsolutePath() + " could not be found or was not a directory.");
                System.exit(1);
            }

            discoverers.add(new ImportOrderOrDirectoryFileDiscoverer(directory));
        }
        else {
            System.err.println("Unknown parameter " + argument + ".");
            printParameterHelp();
            System.exit(1);
        }

        return true;
    }

    private void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err.println("  --" + DIRECTORY_ARGUMENT + " The directory where the _import_order_ file or the content to import is located.");
    }

}
