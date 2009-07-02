package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.util.SingleObjectHolder;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;


public class ClassReferenceMemento extends SingleObjectHolder<String> implements SingleCallMemento {
    private String klass;

    public ClassReferenceMemento(String klass) {
        super(klass);
        this.klass = klass;
    }

    public void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback) {
        parseCallback.classReferenceFound(context.getFile(), klass);
    }
}
