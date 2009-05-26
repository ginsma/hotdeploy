package example.deploy.xml.ordergenerator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;
import example.deploy.xml.present.PresentContentAware;

public class DefinitionsAndReferencesGatherer implements ParseCallback, PresentContentAware {
    private DefinitionsAndReferences definitionsAndReferences = new DefinitionsAndReferences();
    private Set<String> alreadyPresentContent = new HashSet<String>();

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
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        if (alreadyPresentContent.contains(externalId)) {
            return;
        }

        DeploymentFile referringFile = context.getFile();
        boolean referenceWithinFile = isAlreadyDefinedInFile(referringFile, externalId);

        // references within a file are not relevant to the order.
        if (referenceWithinFile) {
            return;
        }

        definitionsAndReferences.references.add(new Reference(externalId, referringFile));
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
