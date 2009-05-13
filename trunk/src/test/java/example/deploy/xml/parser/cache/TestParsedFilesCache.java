package example.deploy.xml.parser.cache;

import junit.framework.TestCase;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.DummyDeploymentFile;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.parser.ParseCallback;

public class TestParsedFilesCache extends TestCase {
    protected static final String CONTENT = "content";
    protected static final String INPUT_TEMPLATE = "inputtemplate";

    public void testCache() {
        DummyDeploymentFile file = new DummyDeploymentFile("file");
        final FileParseCallbackMemento originalMemento = new FileParseCallbackMemento(file);

        originalMemento.contentFound(file, CONTENT, Major.ARTICLE, INPUT_TEMPLATE);
        originalMemento.contentReferenceFound(file, Major.ARTICLE, CONTENT);
        originalMemento.contentReferenceFound(file, Major.ARTICLE, CONTENT + "1");
        originalMemento.templateFound(file, INPUT_TEMPLATE);
        originalMemento.templateReferenceFound(file, INPUT_TEMPLATE);

        DeploymentFileParser parser = new DeploymentFileParser() {
            boolean called = false;

            public void parse(DeploymentFile file, ParseCallback callback) {
                if (called) {
                    fail("Parser called though parse result should have been cached.");
                }

                originalMemento.replay(callback);

                called = true;
            }
        };

        ParsedFilesCache cache = new ParsedFilesCache(parser);


        FileParseCallbackMemento mementoFirstParse = new FileParseCallbackMemento(file);
        cache.parse(file, mementoFirstParse);

        FileParseCallbackMemento mementoSecondParse = new FileParseCallbackMemento(file);
        cache.parse(file, mementoSecondParse);

        assertEquals(mementoSecondParse, mementoFirstParse);
        assertEquals(mementoFirstParse, originalMemento);
        assertEquals(mementoSecondParse, originalMemento);
        assertEquals(5, mementoSecondParse.getMementos().size());
    }
}
