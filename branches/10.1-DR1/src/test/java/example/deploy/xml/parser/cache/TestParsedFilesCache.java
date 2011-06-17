package example.deploy.xml.parser.cache;

import static example.deploy.hotdeploy.client.Major.ARTICLE;
import static example.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;
import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.DummyDeploymentFile;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class TestParsedFilesCache extends TestCase {
    protected static final String CONTENT = "content";
    protected static final String AN_INPUT_TEMPLATE = "inputtemplate";

    public void testCache() {
        DummyDeploymentFile file = new DummyDeploymentFile("file");
        final FileParseCallbackMemento originalMemento = new FileParseCallbackMemento(file);

        ParseContext context = new ParseContext(file);

        originalMemento.contentFound(context, CONTENT, ARTICLE, AN_INPUT_TEMPLATE);
        originalMemento.contentReferenceFound(context, ARTICLE, CONTENT);
        originalMemento.contentReferenceFound(context, ARTICLE, CONTENT + "1");
        originalMemento.contentFound(context, AN_INPUT_TEMPLATE, INPUT_TEMPLATE, null);
        originalMemento.contentReferenceFound(context, INPUT_TEMPLATE, AN_INPUT_TEMPLATE);

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
