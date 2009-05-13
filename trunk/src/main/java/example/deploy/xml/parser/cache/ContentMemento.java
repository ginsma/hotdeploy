package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.util.SingleObjectHolder;
import example.deploy.hotdeploy.util.Triple;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

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

    public void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.contentFound(
            context, externalId, major, inputTemplate);
    }

}
