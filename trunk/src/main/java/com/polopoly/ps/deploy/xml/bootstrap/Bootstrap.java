package com.polopoly.ps.deploy.xml.bootstrap;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.deploy.xml.present.PresentContentAware;


public class Bootstrap extends ArrayList<BootstrapContent> implements PresentContentAware {
    private List<BootstrapContent> neverCreatedButReferenced = new ArrayList<BootstrapContent>();

    public void addNeverCreatedButReferenced(BootstrapContent bootstrapContent) {
        neverCreatedButReferenced.add(bootstrapContent);
    }

    public List<BootstrapContent> getNeverCreatedButReferenced() {
        return neverCreatedButReferenced;
    }

    public void presentContent(String externalId) {
        BootstrapContent bootstrap = new BootstrapContent(Major.UNKNOWN, externalId);

        remove(bootstrap);
        neverCreatedButReferenced.remove(bootstrap);
    }

    public void presentTemplate(String inputTemplate) {
        BootstrapContent bootstrap = new BootstrapContent(Major.INPUT_TEMPLATE, inputTemplate);

        remove(bootstrap);
        neverCreatedButReferenced.remove(bootstrap);
    }
}
