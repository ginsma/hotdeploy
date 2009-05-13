package example.deploy.xml.parser.cache;

import static java.util.Arrays.asList;

import java.util.List;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

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

    public void contentFound(DeploymentFile file, String externalId,
            Major major, String inputTemplate) {
        for (ParseCallback callback : callbacks) {
            callback.contentFound(file, externalId, major, inputTemplate);
        }
    }

    public void contentReferenceFound(DeploymentFile file, Major major, String externalId) {
        for (ParseCallback callback : callbacks) {
            callback.contentReferenceFound(file, major, externalId);
        }
    }

    public void templateFound(DeploymentFile file, String inputTemplate) {
        for (ParseCallback callback : callbacks) {
            callback.templateFound(file, inputTemplate);
        }
    }

    public void templateReferenceFound(DeploymentFile file, String inputTemplate) {
        for (ParseCallback callback : callbacks) {
            callback.templateReferenceFound(file, inputTemplate);
        }
    }

}
