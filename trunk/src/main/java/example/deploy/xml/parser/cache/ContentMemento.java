package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;

public class ContentMemento extends SingleObjectHolder<Triple<String, Major, String>> implements SingleCallMemento {
    private String externalId;
    private String inputTemplate;
    private Major major;

    public ContentMemento(String externalId, Major major, String inputTemplate) {
        super(new Triple<String, Major, String>(externalId, major, inputTemplate));

        this.externalId = externalId;
        this.major = major;
        this.inputTemplate = inputTemplate;
    }

    public void replay(DeploymentFile file, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.contentFound(file, externalId, major, inputTemplate);
    }

}
