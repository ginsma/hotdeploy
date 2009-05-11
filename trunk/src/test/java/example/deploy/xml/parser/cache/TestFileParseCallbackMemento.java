package example.deploy.xml.parser.cache;

import junit.framework.TestCase;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.state.DummyDeploymentFile;

public class TestFileParseCallbackMemento extends TestCase {
    private static final String CONTENT_ID = "content1";
    private static final String TEMPLATE_ID = "template1";
    private static final String CONTENT_ID2 = "content2";
    private static final String TEMPLATE_ID2 = "template2";
    private static final String INPUT_TEMPLATE = "inputtemplate";

    public void testReplay() {
        DummyDeploymentFile file = new DummyDeploymentFile("file");
        FileParseCallbackMemento memento = new FileParseCallbackMemento(file);

        memento.contentFound(file, CONTENT_ID, Major.ARTICLE, INPUT_TEMPLATE);
        memento.templateFound(file, TEMPLATE_ID);
        memento.contentReferenceFound(file, Major.ARTICLE, CONTENT_ID2);
        memento.templateReferenceFound(file, TEMPLATE_ID2);

        FileParseCallbackMemento newMemento = new FileParseCallbackMemento(file);
        memento.replay(newMemento);

        assertEquals(memento, newMemento);
    }

    public void testAddTwoFiles() {
        DummyDeploymentFile file1 = new DummyDeploymentFile("file1");
        DummyDeploymentFile file2 = new DummyDeploymentFile("file2");

        FileParseCallbackMemento memento = new FileParseCallbackMemento(file1);

        memento.contentFound(file1, CONTENT_ID, Major.ARTICLE, INPUT_TEMPLATE);
        memento.contentFound(file2, CONTENT_ID, Major.ARTICLE, INPUT_TEMPLATE);

        assertEquals(1, memento.getMementos().size());
    }
}
