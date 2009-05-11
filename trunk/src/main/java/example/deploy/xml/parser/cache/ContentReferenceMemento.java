package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.SingleObjectHolder;
import example.deploy.hotdeploy.util.Tuple;
import example.deploy.xml.parser.ParseCallback;

public class ContentReferenceMemento extends SingleObjectHolder<Tuple<Major, String>> implements SingleCallMemento {
    private String externalId;
    private Major major;

    public ContentReferenceMemento(Major major, String externalId) {
        super(new Tuple<Major, String>(major, externalId));

        this.major = major;
        this.externalId = externalId;
    }

    public void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback) {
         parseCallback.contentReferenceFound(file, major, externalId);
    }
}
