package example.deploy.xml.bootstrap;

import static example.deploy.hotdeploy.client.Major.DEPARTMENT;
import static example.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;
import junit.framework.TestCase;
import example.deploy.hotdeploy.client.Major;

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
