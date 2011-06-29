package com.polopoly.ps.deploy.xml.ordergenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;


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
        Map<String, Set<DeploymentFile>> definingFileByExternalId =
            definitionsAndReferences.definingFilesByExternalId;

        List<Reference> references = definitionsAndReferences.referencesToNonPresentContent;

        addDependencies(references, definingFileByExternalId);

        // add files that only modify existing objects
        for (Reference referenceToPresentContent : definitionsAndReferences.referencesToPresentContent) {
            getVertex(referenceToPresentContent.inFile);
        }

        // add files that don't have any references.
        for (Set<DeploymentFile> fileSet : definitionsAndReferences.definingFilesByExternalId.values()) {
            for (DeploymentFile file : fileSet) {
                getVertex(file);
            }
        }

        return vertexByFile.values();
    }

    private void addDependencies(List<Reference> references,
            Map<String, Set<DeploymentFile>> definingFilesByExternalId) {
        for (final Reference reference : references) {
            Set<DeploymentFile> definingFiles =
                definingFilesByExternalId.get(reference.refersTo);

            if (definingFiles != null) {
                addDependencies(reference, definingFiles);

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, definingFiles + " define \"" + reference.refersTo + "\".");
                }
            }
            else {
                // create the vertex
                getVertex(reference.inFile);

                if (missingDeclarations.add(reference.refersTo)) {
                    logger.log(Level.FINE, "Cannot find any file defining \"" + reference.refersTo + "\".");
                }
            }
        }
    }

    private void addDependencies(Reference reference, Set<DeploymentFile> definingFiles) {
        DeploymentFile file = reference.inFile;
        String externalId = reference.refersTo;

        // do this first so the vertex is added to the list
        DeploymentFileVertex referringFileVertex = getVertex(file);

        Set<DeploymentFileVertex> referredVertexes =
            toVertexSetExcludingFile(definingFiles, file);

        if (!referredVertexes.isEmpty()) {
            referringFileVertex.addDependencies(externalId, referredVertexes);
        }
    }

    private Set<DeploymentFileVertex> toVertexSetExcludingFile(
            Set<DeploymentFile> files, final DeploymentFile excludeFile) {
        Set<DeploymentFileVertex> result = new HashSet<DeploymentFileVertex>();

        for (DeploymentFile file : files) {
            if (file.equals(excludeFile)) {
                continue;
            }

            result.add(getVertex(file));
        }

        return result;
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
