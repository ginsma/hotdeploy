package example.deploy.xml.ordergenerator;

import static example.deploy.xml.consistency.ParameterConstants.DIRECTORY_ARGUMENT;

import java.io.File;

import example.deploy.hotdeploy.client.ArgumentConsumer;
import example.deploy.hotdeploy.client.ArgumentParser;

public class ImportOrderGeneratorArgumentParser implements ArgumentConsumer {
    private String[] args;
    private FilesInDirectoryDiscoverer filesInDirectory;

    public ImportOrderGeneratorArgumentParser(ImportOrderGenerator generator, FilesInDirectoryDiscoverer filesInDirectory, String[] args) {
        this.filesInDirectory = filesInDirectory;
        this.args = args;
    }

    public void parse() {
        new ArgumentParser(this, args).parse();
    }

    public void argumentFound(String argument, String value) {
        if (argument.equals(DIRECTORY_ARGUMENT)) {
            File directory = new File(value);

            if (!directory.exists() && directory.isDirectory()) {
                System.err.println("Directory " + directory.getAbsolutePath() + " could not be found or was not a directory.");
                System.exit(1);
            }

            filesInDirectory.setRootDirectory(directory);
        }
        else {
            System.err.println("Unknown parameter " + argument + ".");
            printParameterHelp();
            System.exit(1);
        }
    }

    private void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err.println("  --" + DIRECTORY_ARGUMENT + " The directory where the _import_order_ file or the content to import is located.");
    }

}
