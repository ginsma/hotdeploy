package example.deploy.xml.bootstrap;

import static example.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class ParseCallbackAddedContext {

    private ParseContext context;
    private ParseCallback callback;

    public ParseCallbackAddedContext(DeploymentFile file,
            ParseCallback callback) {
        this.context = new ParseContext(file);
        this.callback = callback;
    }

    public void classReferenceFound(String className) {
        callback.classReferenceFound(context.getFile(), className);
    }

    public void contentFound(String externalId,
            Major major, String inputTemplate) {
        callback.contentFound(context, externalId, major, inputTemplate);
    }

    public void contentReferenceFound(Major major,
            String externalId) {
        callback.contentReferenceFound(context, major, externalId);
    }

    public void templateFound(String inputTemplate) {
        contentFound(inputTemplate, INPUT_TEMPLATE, inputTemplate);
    }

    public void templateReferenceFound(String inputTemplate) {
        contentReferenceFound(INPUT_TEMPLATE, inputTemplate);
    }

}
