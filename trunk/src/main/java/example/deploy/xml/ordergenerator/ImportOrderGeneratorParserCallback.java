package example.deploy.xml.ordergenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

public class ImportOrderGeneratorParserCallback implements ParseCallback {
    private static final Logger logger =
        Logger.getLogger(ImportOrderGeneratorParserCallback.class.getName());

    private DefinitionsAndReferences definitionsAndReferences = new DefinitionsAndReferences();

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    public void contentFound(DeploymentFile file, String externalId, Major major, String inputTemplate) {
        DeploymentFile oldFile = definitionsAndReferences.definingFileByExternalId.put(externalId, file);

        if (oldFile != null) {
            logger.log(Level.FINE, "The content " + externalId + " was defined twice: in " + file + " and in " + oldFile + ".");
        }
    }

    public void templateFound(DeploymentFile file, String inputTemplate) {
        contentFound(file, inputTemplate, null, null);
    }

    public void contentReferenceFound(DeploymentFile file, String externalId) {
        definitionsAndReferences.references.add(new Reference(externalId, file));
    }

    public void templateReferenceFound(DeploymentFile file, String inputTemplate) {
        contentReferenceFound(file, inputTemplate);
    }

    public DefinitionsAndReferences getDefinitionsAndReferences() {
        return definitionsAndReferences;
    }
}
