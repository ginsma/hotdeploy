package com.polopoly.ps.deploy.xml.parser.cache;

import com.polopoly.ps.deploy.xml.parser.ParseCallback;
import com.polopoly.ps.deploy.xml.parser.ParseContext;


public interface SingleCallMemento {

    void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback);

}
