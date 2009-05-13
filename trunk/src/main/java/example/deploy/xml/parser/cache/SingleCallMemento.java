package example.deploy.xml.parser.cache;

import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;


public interface SingleCallMemento {

    void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback);

}
