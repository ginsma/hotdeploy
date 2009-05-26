package example.deploy.xml.ordergenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.discovery.importorder.ImportOrder;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.topologicalsort.TopologicalSorter;
import example.deploy.hotdeploy.util.MapList;
import example.deploy.hotdeploy.util.Mapping;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.parser.ContentXmlParser;
import example.deploy.xml.present.PresentContentAware;

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
            logger.log(Level.FINE, "Parsing " + files + "...");

            parser.parse(file, callback);
        }

        DefinitionsAndReferences definitionsAndReferences = callback.getDefinitionsAndReferences();

        return definitionsAndReferences;
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
