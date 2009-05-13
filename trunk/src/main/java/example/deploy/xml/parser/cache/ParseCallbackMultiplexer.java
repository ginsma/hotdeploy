package example.deploy.xml.parser.cache;

import static java.util.Arrays.asList;

import java.util.List;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class ParseCallbackMultiplexer implements ParseCallback {
    private List<ParseCallback> callbacks;

    public ParseCallbackMultiplexer(
            ParseCallback... parseCallbacks) {
        callbacks = asList(parseCallbacks);
    }

    public void classReferenceFound(DeploymentFile file, String klass) {
        for (ParseCallback callback : callbacks) {
            callback.classReferenceFound(file, klass);
        }
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        for (ParseCallback callback : callbacks) {
            callback.contentFound(context, externalId, major, inputTemplate);
        }
    }

    public void contentReferenceFound(ParseContext context, Major major,
            String externalId) {
        for (ParseCallback callback : callbacks) {
            callback.contentReferenceFound(context, major, externalId);
        }
    }
}
