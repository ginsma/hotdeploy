package com.polopoly.ps.hotdeploy.xml.bootstrap;

import static com.polopoly.ps.hotdeploy.client.Major.INPUT_TEMPLATE;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;

public class ParseCallbackAddedContext {

    private ParseContext context;
    private ParseCallback callback;

    public ParseCallbackAddedContext(DeploymentFile file,
            ParseCallback callback) {
        this.context = new ParseContext(file);
        this.callback = callback;
    }

    public void classReferenceFound(String className) {
        callback.classReferenceFound(context.getFile(), className);
    }

    public void contentFound(String externalId,
            Major major, String inputTemplate) {
        callback.contentFound(context, externalId, major, inputTemplate);
    }

    public void contentReferenceFound(Major major,
            String externalId) {
        callback.contentReferenceFound(context, major, externalId);
    }

    public void templateFound(String inputTemplate) {
        contentFound(inputTemplate, INPUT_TEMPLATE, inputTemplate);
    }

    public void templateReferenceFound(String inputTemplate) {
        contentReferenceFound(INPUT_TEMPLATE, inputTemplate);
    }

}
