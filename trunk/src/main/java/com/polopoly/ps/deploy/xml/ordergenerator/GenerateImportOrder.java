package com.polopoly.ps.deploy.xml.ordergenerator;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.ps.deploy.hotdeploy.discovery.FileDiscoverer;


/**
 * A class with a main method for calling {@link ImportOrderGenerator} from
 * the command line.
 */
public class GenerateImportOrder {
    public static void main(String[] args) {
        ImportOrderGenerator generator = new ImportOrderGenerator();

        List<FileDiscoverer> discoverers = new ArrayList<FileDiscoverer>();

        new ImportOrderGeneratorArgumentParser(generator, discoverers, args).parse();

        DiscovereredFilesAggregator filesInDirectory =
            new DiscovereredFilesAggregator(discoverers);

        generator.generate(filesInDirectory.getFiles());
    }
}
