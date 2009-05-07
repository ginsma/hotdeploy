package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

public class ContentReferenceMemento extends SingleObjectHolder<String> implements SingleCallMemento {
    private String externalId;

    public ContentReferenceMemento(String externalId) {
        super(externalId);

        this.externalId = externalId;
    }

    public void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback) {
         parseCallback.contentReferenceFound(file, externalId);
    }
}
