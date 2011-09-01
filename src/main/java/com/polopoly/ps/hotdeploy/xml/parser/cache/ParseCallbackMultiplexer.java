package com.polopoly.ps.hotdeploy.xml.parser.cache;

import static java.util.Arrays.asList;

import java.util.List;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;


public class ParseCallbackMultiplexer implements ParseCallback {
    private List<ParseCallback> callbacks;

    public ParseCallbackMultiplexer(
            ParseCallback... parseCallbacks) {
        callbacks = asList(parseCallbacks);
    }

    public void classReferenceFound(DeploymentFile file, String klass) {
        for (ParseCallback callback : callbacks) {
            callback.classReferenceFound(file, klass);
        }
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        for (ParseCallback callback : callbacks) {
            callback.contentFound(context, externalId, major, inputTemplate);
        }
    }

    public void contentReferenceFound(ParseContext context, Major major,
            String externalId) {
        for (ParseCallback callback : callbacks) {
            callback.contentReferenceFound(context, major, externalId);
        }
    }
}
