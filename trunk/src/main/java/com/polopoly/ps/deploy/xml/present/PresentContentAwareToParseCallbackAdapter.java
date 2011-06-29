package com.polopoly.ps.deploy.xml.present;

import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.xml.parser.ParseCallback;
import com.polopoly.ps.deploy.xml.parser.ParseContext;

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
