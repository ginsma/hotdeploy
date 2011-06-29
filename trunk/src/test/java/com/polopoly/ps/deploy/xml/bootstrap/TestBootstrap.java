package com.polopoly.ps.deploy.xml.bootstrap;

import static com.polopoly.ps.deploy.hotdeploy.client.Major.DEPARTMENT;
import static com.polopoly.ps.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;

import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.deploy.xml.bootstrap.Bootstrap;
import com.polopoly.ps.deploy.xml.bootstrap.BootstrapContent;

import junit.framework.TestCase;

public class TestBootstrap extends TestCase {
    private static final String EXTERNAL_ID = "foo";
    private Bootstrap bootstrap;

    private void assertCompletelyEmpty() {
        assertTrue(bootstrap.isEmpty());
        assertTrue(bootstrap.getNeverCreatedButReferenced().isEmpty());
    }

    public void testRemoveWrongMajor() {
        bootstrap.add(new BootstrapContent(Major.DEPARTMENT, EXTERNAL_ID));
        bootstrap.presentTemplate(EXTERNAL_ID);

        assertCompletelyEmpty();
    }

    public void testRemoveWrongMajorNeverCreated() {
        bootstrap.addNeverCreatedButReferenced(new BootstrapContent(DEPARTMENT, EXTERNAL_ID));
        bootstrap.presentTemplate(EXTERNAL_ID);

        assertCompletelyEmpty();
    }

    public void testRemoveRightMajor() {
        bootstrap.add(new BootstrapContent(Major.INPUT_TEMPLATE, EXTERNAL_ID));
        bootstrap.presentTemplate(EXTERNAL_ID);

        assertCompletelyEmpty();
    }

    public void testRemoveRightMajorContent() {
        bootstrap.add(new BootstrapContent(Major.DEPARTMENT, EXTERNAL_ID));
        bootstrap.presentContent(EXTERNAL_ID);

        assertCompletelyEmpty();
    }

    public void testRemoveRightMajorContentNeverCreated() {
        bootstrap.addNeverCreatedButReferenced(new BootstrapContent(Major.DEPARTMENT, EXTERNAL_ID));
        bootstrap.presentContent(EXTERNAL_ID);

        assertCompletelyEmpty();
    }

    public void testRemoveRightMajorNeverCreated() {
        bootstrap.addNeverCreatedButReferenced(new BootstrapContent(INPUT_TEMPLATE, EXTERNAL_ID));
        bootstrap.presentTemplate(EXTERNAL_ID);

        assertCompletelyEmpty();
    }

    @Override
    public void setUp() {
        bootstrap = new Bootstrap();
    }
}
