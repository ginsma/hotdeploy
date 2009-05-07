package example.deploy.xml.ordergenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentFile;

public class VertexGenerator {
    private static final Logger logger =
        Logger.getLogger(VertexGenerator.class.getName());

    private DefinitionsAndReferences definitionsAndReferences;
    private Map<DeploymentFile, DeploymentFileVertex> vertexByFile =
        new HashMap<DeploymentFile, DeploymentFileVertex>();

    private Collection<String> missingDeclarations = new HashSet<String>();

    VertexGenerator(DefinitionsAndReferences definitionsAndReferences) {
        this.definitionsAndReferences = definitionsAndReferences;
    }

    public Collection<DeploymentFileVertex> generateVertexes() {
        Map<String, DeploymentFile> definingFileByExternalId = definitionsAndReferences.definingFileByExternalId;

        List<Reference> references = definitionsAndReferences.references;

        addDependencies(references, definingFileByExternalId);

        return vertexByFile.values();
    }

    private void addDependencies(List<Reference> references,
            Map<String, DeploymentFile> definingFileByExternalId) {
        for (Reference reference : references) {
            DeploymentFile definingFile =
                definingFileByExternalId.get(reference.refersTo);

            // do this first so the vertex is added to the list
            DeploymentFileVertex referringFileVertex = getVertex(reference.inFile);

            if (definingFile == null) {
                if (missingDeclarations.add(reference.refersTo)) {
//                    logger.log(Level.WARNING, "Cannot find any file defining \"" + reference.refersTo + "\".");
                }
                continue;
            }

            DeploymentFileVertex definitionFileVertex = getVertex(definingFile);

            if (!referringFileVertex.equals(definitionFileVertex)) {
if (reference.refersTo.indexOf("mailte") != -1)
                logger.log(Level.INFO, referringFileVertex + " depends on " + definitionFileVertex + " because it defines " + reference.refersTo + ".");
                referringFileVertex.addDependency(definitionFileVertex);
            }
        }
    }

    private DeploymentFileVertex getVertex(DeploymentFile file) {
        DeploymentFileVertex result = vertexByFile.get(file);

        if (result == null) {
            result = new DeploymentFileVertex(file);
            vertexByFile.put(file, result);
        }

        return result;
    }
}
