package example.deploy.xml.ordergenerator;

import example.deploy.hotdeploy.discovery.DefaultDiscoverers;

/**
 * A class with a main method for calling {@link ImportOrderGenerator} from
 * the command line.
 */
public class GenerateImportOrder {
    public static void main(String[] args) {
        ImportOrderGenerator generator = new ImportOrderGenerator();

        FilesInDirectoryDiscoverer filesInDirectory =
            new FilesInDirectoryDiscoverer(DefaultDiscoverers.getDiscoverers());

        new ImportOrderGeneratorArgumentParser(generator, filesInDirectory, args).parse();

        generator.generate(filesInDirectory.getFiles());
    }
}
