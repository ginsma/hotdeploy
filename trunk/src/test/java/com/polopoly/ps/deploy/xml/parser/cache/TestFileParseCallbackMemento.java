package com.polopoly.ps.deploy.xml.parser.cache;

import static com.polopoly.ps.deploy.hotdeploy.client.Major.ARTICLE;
import static com.polopoly.ps.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;

import com.polopoly.ps.deploy.hotdeploy.state.DummyDeploymentFile;
import com.polopoly.ps.deploy.xml.parser.ParseContext;
import com.polopoly.ps.deploy.xml.parser.cache.FileParseCallbackMemento;

import junit.framework.TestCase;

public class TestFileParseCallbackMemento extends TestCase {
    private static final String CONTENT_ID = "content1";
    private static final String TEMPLATE_ID = "template1";
    private static final String CONTENT_ID2 = "content2";
    private static final String TEMPLATE_ID2 = "template2";
    private static final String AN_INPUT_TEMPLATE = "inputtemplate";

    public void testReplay() {
        DummyDeploymentFile file = new DummyDeploymentFile("file");
        FileParseCallbackMemento memento = new FileParseCallbackMemento(file);

        ParseContext context = new ParseContext(file);
        memento.contentFound(context, CONTENT_ID, ARTICLE, AN_INPUT_TEMPLATE);
        memento.contentFound(context, TEMPLATE_ID, INPUT_TEMPLATE, null);
        memento.contentReferenceFound(context, ARTICLE, CONTENT_ID2);
        memento.contentReferenceFound(context, INPUT_TEMPLATE, TEMPLATE_ID2);

        FileParseCallbackMemento newMemento = new FileParseCallbackMemento(file);
        memento.replay(newMemento);

        assertEquals(memento, newMemento);
    }

    public void testAddTwoFiles() {
        DummyDeploymentFile file1 = new DummyDeploymentFile("file1");
        DummyDeploymentFile file2 = new DummyDeploymentFile("file2");

        FileParseCallbackMemento memento = new FileParseCallbackMemento(file1);
        ParseContext context1 = new ParseContext(file1);
        ParseContext context2 = new ParseContext(file2);

        memento.contentFound(context1, CONTENT_ID, ARTICLE, AN_INPUT_TEMPLATE);
        memento.contentFound(context2, CONTENT_ID, ARTICLE, AN_INPUT_TEMPLATE);

        assertEquals(1, memento.getMementos().size());
    }
}
