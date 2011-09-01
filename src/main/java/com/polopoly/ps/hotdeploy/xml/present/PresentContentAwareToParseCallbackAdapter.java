package com.polopoly.ps.hotdeploy.xml.present;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;

public class PresentContentAwareToParseCallbackAdapter implements ParseCallback {
    private PresentContentAware presentFilesAware;

    public PresentContentAwareToParseCallbackAdapter(PresentContentAware presentFilesAware) {
        this.presentFilesAware = presentFilesAware;
    }

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        if (major == Major.INPUT_TEMPLATE) {
            presentFilesAware.presentTemplate(externalId);
        }
        else {
            presentFilesAware.presentContent(externalId);
        }
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
    }
}
