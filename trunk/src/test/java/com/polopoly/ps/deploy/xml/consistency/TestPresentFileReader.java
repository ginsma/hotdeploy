package com.polopoly.ps.deploy.xml.consistency;

import java.util.HashSet;
import java.util.Set;

import com.polopoly.ps.deploy.xml.present.PresentContentAware;
import com.polopoly.ps.deploy.xml.present.PresentFileReader;


import junit.framework.TestCase;

public class TestPresentFileReader extends TestCase implements PresentContentAware {
    private Set<String> contents = new HashSet<String>();
    private Set<String> templates = new HashSet<String>();

    public void testRead() {
        new PresentFileReader(this).read();

        assertTrue("Only " + contents.size() + " present external content IDs were reported.", contents.size() > 50);
        assertTrue("Only " + templates.size() + " present templates were reported.", templates.size() > 50);
    }

    public void presentContent(String externalId) {
        contents.add(externalId);
    }

    public void presentTemplate(String inputTemplate) {
        templates.add(inputTemplate);
    }
}
