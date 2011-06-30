package com.polopoly.ps.hotdeploy.xml.parser.cache;

import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;


public interface SingleCallMemento {

    void replay(ParseContext context, SingleCallMemento memento,
            ParseCallback parseCallback);

}
