package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.util.SingleObjectHolder;
import example.deploy.hotdeploy.util.Tuple;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class ContentReferenceMemento extends SingleObjectHolder<Tuple<Major, String>> implements SingleCallMemento {
    private String externalId;
    private Major major;

    public ContentReferenceMemento(Major major, String externalId) {
        super(new Tuple<Major, String>(major, externalId));

        this.major = major;
        this.externalId = externalId;
    }

    public void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback) {
         parseCallback.contentReferenceFound(context, major, externalId);
    }
}
