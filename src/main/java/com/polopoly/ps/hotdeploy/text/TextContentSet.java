package com.polopoly.ps.hotdeploy.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class TextContentSet implements Iterable<TextContent> {
    private Map<String, TextContent> contents = new HashMap<String, TextContent>();

    public void validate(ValidationContext context) throws ValidationException {
        for (TextContent content : contents()) {
            context.add(content);
        }

        for (TextContent content : contents()) {
            content.validate(context);
        }
    }

    private Collection<TextContent> contents() {
        return contents.values();
    }

    public TextContent get(String id) {
        return contents.get(id);
    }

    public void add(TextContent currentContent) {
        contents.put(currentContent.getId(), currentContent);
    }

    public Iterator<TextContent> iterator() {
        return contents.values().iterator();
    }

}
