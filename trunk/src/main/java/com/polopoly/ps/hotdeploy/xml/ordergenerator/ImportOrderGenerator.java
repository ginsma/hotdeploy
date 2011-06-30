package com.polopoly.ps.hotdeploy.xml.ordergenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrder;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.topologicalsort.TopologicalSorter;
import com.polopoly.ps.hotdeploy.util.MapList;
import com.polopoly.ps.hotdeploy.util.Mapping;
import com.polopoly.ps.hotdeploy.xml.parser.ContentXmlParser;
import com.polopoly.ps.hotdeploy.xml.parser.DeploymentFileParser;
import com.polopoly.ps.hotdeploy.xml.present.PresentContentAware;


public class ImportOrderGenerator implements PresentContentAware {
    private static final Logger logger =
        Logger.getLogger(ImportOrderGenerator.class.getName());
    private DefinitionsAndReferencesGatherer callback;
    private DeploymentFileParser parser;

    public ImportOrderGenerator() {
        this(new ContentXmlParser());
    }

    public ImportOrderGenerator(DeploymentFileParser parser) {
        callback = new DefinitionsAndReferencesGatherer();
        this.parser = parser;
    }

    private DefinitionsAndReferences parse(Collection<DeploymentFile> files) {
        for (DeploymentFile file : files) {
            logger.log(Level.FINE, "Parsing " + file + "...");

            parser.parse(file, callback);

            if (!callback.hasDefinitionOrReference(file)) {
                logger.log(Level.WARNING, "The file " + file +
                    " does not seem to define any content. " +
                    "It will not be included in the import order.");
            }
        }

        return callback.getDefinitionsAndReferences();
    }

    private List<DeploymentFileVertex> toVertexList(
            DefinitionsAndReferences definitionsAndReferences) {
        Collection<DeploymentFileVertex> vertexCollection =
            new VertexGenerator(definitionsAndReferences).generateVertexes();

        List<DeploymentFileVertex> vertexList =
            new ArrayList<DeploymentFileVertex>(vertexCollection);

        return vertexList;
    }

    public ImportOrder generate(Collection<DeploymentFile> files) {
        DefinitionsAndReferences definitionsAndReferences = parse(files);

        List<DeploymentFileVertex> vertexList = toVertexList(definitionsAndReferences);

        List<DeploymentFileVertex> result =
            new TopologicalSorter<DeploymentFileVertex>(vertexList).sort();

        ImportOrder importOrder = new ImportOrder(new FileDeploymentDirectory(new File(".")));

        MapList.map(result, importOrder, new Mapping<DeploymentFileVertex, DeploymentFile>() {
            public DeploymentFile map(DeploymentFileVertex from) {
                return from.getDeploymentFile();
            }});

        return importOrder;
    }

    public void presentContent(String externalId) {
        callback.presentContent(externalId);
    }

    public void presentTemplate(String inputTemplate) {
        callback.presentContent(inputTemplate);
    }
}
