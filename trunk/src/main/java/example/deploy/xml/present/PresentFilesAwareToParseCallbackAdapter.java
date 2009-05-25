package example.deploy.xml.present;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class PresentFilesAwareToParseCallbackAdapter implements ParseCallback {
    private PresentFilesAware presentFilesAware;

    public PresentFilesAwareToParseCallbackAdapter(PresentFilesAware presentFilesAware) {
        this.presentFilesAware = presentFilesAware;
    }

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        if (major == Major.INPUT_TEMPLATE) {
            presentFilesAware.presentTemplate(inputTemplate);
        }
        else {
            presentFilesAware.presentContent(externalId);
        }
    }

    public void contentReferenceFound(ParseContext context, Major major,
            String externalId) {
    }
}
