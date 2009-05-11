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
        DeploymentFileParser parser = new DeploymentFileParser() {
            boolean called = false;

            public void parse(DeploymentFile file, ParseCallback callback) {
                if (called) {
                    fail("Parser called though parse result should have been cached.");
                }

                callback.contentFound(file, CONTENT, Major.ARTICLE, INPUT_TEMPLATE);
                callback.contentReferenceFound(file, Major.ARTICLE, CONTENT);
                callback.contentReferenceFound(file, Major.ARTICLE, CONTENT + "1");
                callback.templateFound(file, INPUT_TEMPLATE);
                callback.templateReferenceFound(file, INPUT_TEMPLATE);

                called = true;
            }
        };

        ParsedFilesCache cache = new ParsedFilesCache(parser);

        DummyDeploymentFile file = new DummyDeploymentFile("file");

        FileParseCallbackMemento mementoFirstParse = new FileParseCallbackMemento(file);
        cache.parse(file, mementoFirstParse);

        FileParseCallbackMemento mementoSecondParse = new FileParseCallbackMemento(file);
        cache.parse(file, mementoSecondParse);

        assertEquals(mementoSecondParse, mementoFirstParse);
        assertEquals(5, mementoSecondParse.getMementos().size());
    }
}
