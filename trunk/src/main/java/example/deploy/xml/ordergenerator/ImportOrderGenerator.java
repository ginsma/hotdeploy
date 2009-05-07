package example.deploy.xml.ordergenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.MapList;
import example.deploy.hotdeploy.util.Mapping;
import example.deploy.hotdeploy.util.TopologicalSorter;
import example.deploy.xml.parser.XmlParser;

public class ImportOrderGenerator {
    private static final Logger logger =
        Logger.getLogger(ImportOrderGenerator.class.getName());

    public List<DeploymentFile> generate(Collection<DeploymentFile> files) {
        ImportOrderGeneratorParserCallback callback = new ImportOrderGeneratorParserCallback();

        for (DeploymentFile file : files) {
            logger.log(Level.FINE, "Parsing " + files + "...");

            new XmlParser().parse(file, callback);
        }

        DefinitionsAndReferences definitionsAndReferences = callback.getDefinitionsAndReferences();

        Collection<DeploymentFileVertex> vertexCollection =
            new VertexGenerator(definitionsAndReferences).generateVertexes();

        List<DeploymentFileVertex> vertexList =
            new ArrayList<DeploymentFileVertex>(vertexCollection);

        List<DeploymentFileVertex> result =
            new TopologicalSorter<DeploymentFileVertex>(vertexList).sort();

        return MapList.map(result, new Mapping<DeploymentFileVertex, DeploymentFile>() {
            public DeploymentFile map(DeploymentFileVertex from) {
                return from.getDeploymentFile();
            }});
    }
}
