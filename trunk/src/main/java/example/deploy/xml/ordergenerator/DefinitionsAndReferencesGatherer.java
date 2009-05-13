package example.deploy.xml.ordergenerator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.consistency.PresentFilesAware;
import example.deploy.xml.parser.ParseCallback;

public class DefinitionsAndReferencesGatherer implements ParseCallback, PresentFilesAware {
    private DefinitionsAndReferences definitionsAndReferences = new DefinitionsAndReferences();
    private Set<String> alreadyPresentContent = new HashSet<String>();

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    public void contentFound(DeploymentFile file, String externalId, Major major, String inputTemplate) {
        Map<String, Set<DeploymentFile>> definingFilesById = definitionsAndReferences.definingFilesByExternalId;
        Set<DeploymentFile> definingFiles = definingFilesById.get(externalId);

        if (definingFiles == null) {
            definingFiles = new HashSet<DeploymentFile>();

            definingFilesById.put(externalId, definingFiles);
        }

        definingFiles.add(file);
    }

    public void templateFound(DeploymentFile file, String inputTemplate) {
        contentFound(file, inputTemplate, Major.INPUT_TEMPLATE, null);
    }

    public void contentReferenceFound(DeploymentFile file, Major major, String externalId) {
        if (alreadyPresentContent.contains(externalId)) {
            return;
        }

        boolean referenceWithinFile = isAlreadyDefinedInFile(file, externalId);

        // references within a file are not relevant to the order.
        if (referenceWithinFile) {
            return;
        }

        definitionsAndReferences.references.add(new Reference(externalId, file));
    }

    private boolean isAlreadyDefinedInFile(DeploymentFile file, String externalId) {
        Set<DeploymentFile> definingFiles =
            definitionsAndReferences.definingFilesByExternalId.get(externalId);

        if (definingFiles == null) {
            return false;
        }

        return definingFiles.contains(file);
    }

    public void templateReferenceFound(DeploymentFile file, String inputTemplate) {
        contentReferenceFound(file, Major.INPUT_TEMPLATE, inputTemplate);
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
