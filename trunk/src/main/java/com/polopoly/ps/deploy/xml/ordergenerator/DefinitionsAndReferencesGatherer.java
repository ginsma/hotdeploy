package com.polopoly.ps.deploy.xml.ordergenerator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.xml.parser.ParseCallback;
import com.polopoly.ps.deploy.xml.parser.ParseContext;
import com.polopoly.ps.deploy.xml.present.PresentContentAware;


public class DefinitionsAndReferencesGatherer implements ParseCallback, PresentContentAware {
    private DefinitionsAndReferences definitionsAndReferences = new DefinitionsAndReferences();
    private Set<String> alreadyPresentContent = new HashSet<String>();
    private Set<DeploymentFile> filesWithDefinitionsOrReferences = new HashSet<DeploymentFile>();

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    public void contentFound(ParseContext context, String externalId, Major major, String inputTemplate) {
        Map<String, Set<DeploymentFile>> definingFilesById =
            definitionsAndReferences.definingFilesByExternalId;
        Set<DeploymentFile> definingFiles = definingFilesById.get(externalId);

        if (definingFiles == null) {
            definingFiles = new HashSet<DeploymentFile>();

            definingFilesById.put(externalId, definingFiles);
        }

        definingFiles.add(context.getFile());
        filesWithDefinitionsOrReferences.add(context.getFile());
    }

    public boolean hasDefinitionOrReference(DeploymentFile file) {
        return filesWithDefinitionsOrReferences.contains(file);
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        filesWithDefinitionsOrReferences.add(context.getFile());

        DeploymentFile referringFile = context.getFile();
        Reference reference = new Reference(externalId, referringFile);

        if (alreadyPresentContent.contains(externalId)) {
            definitionsAndReferences.referencesToPresentContent.add(reference);

            return;
        }

        boolean referenceWithinFile = isAlreadyDefinedInFile(referringFile, externalId);

        // references within a file are not relevant to the order.
        if (referenceWithinFile) {
            definitionsAndReferences.referencesToPresentContent.add(reference);

            return;
        }

        definitionsAndReferences.referencesToNonPresentContent.add(reference);
    }

    private boolean isAlreadyDefinedInFile(DeploymentFile file, String externalId) {
        Set<DeploymentFile> definingFiles =
            definitionsAndReferences.definingFilesByExternalId.get(externalId);

        if (definingFiles == null) {
            return false;
        }

        return definingFiles.contains(file);
    }

    public DefinitionsAndReferences getDefinitionsAndReferences() {
        return definitionsAndReferences;
    }

    public void presentContent(String externalId) {
        alreadyPresentContent.add(externalId);
    }

    public void presentTemplate(String inputTemplate) {
        alreadyPresentContent.add(inputTemplate);
    }
}
