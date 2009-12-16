package example.deploy.xml.bootstrap;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;

import junit.framework.TestCase;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.state.DummyDeploymentFile;
import example.deploy.xml.parser.ContentXmlParser;
import example.deploy.xml.parser.ParseContext;
import example.deploy.xml.parser.cache.FileParseCallbackMemento;

public class TestBootstrapFileWriter extends TestCase {

    private static final String ARTICLE_EXTERNAL_ID = "id.article";
    private static final String DEPARTMENT_EXTERNAL_ID = "id.dept";
    private static final String DEPARTMENT_INPUT_TEMPLATE = "it.dept";

    private static final String EXPECTED_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<batch xmlns=\"http://www.polopoly.com/polopoly/cm/xmlio\">\n" +
        "  <content updateIfExists=\"false\">\n" +
        "    <metadata>\n" +
        "      <contentid>\n" +
        "        <major>1</major>\n" +
        "        <externalid>" + ARTICLE_EXTERNAL_ID + "</externalid>\n" +
        "      </contentid>\n" +
        "    </metadata>\n" +
        "  </content>\n" +
        "  <content updateIfExists=\"false\">\n" +
        "    <metadata>\n" +
        "      <contentid>\n" +
        "        <major>2</major>\n" +
        "        <externalid>" + DEPARTMENT_EXTERNAL_ID + "</externalid>\n" +
        "      </contentid>\n" +
        "      <input-template>\n" +
        "        <externalid>" + DEPARTMENT_INPUT_TEMPLATE + "</externalid>\n" +
        "      </input-template>\n" +
        "    </metadata>\n" +
        "  </content>\n" +
        "</batch>\n";
    private Bootstrap bootstrap;

    @Override
    public void setUp() {
        bootstrap = new Bootstrap();

        bootstrap.add(new BootstrapContent(Major.ARTICLE, ARTICLE_EXTERNAL_ID));
        BootstrapContent deptContent = new BootstrapContent(Major.DEPARTMENT, DEPARTMENT_EXTERNAL_ID);
        deptContent.setInputTemplate(DEPARTMENT_INPUT_TEMPLATE);
        bootstrap.add(deptContent);
    }

    private String generateBootstrap() {
        CharArrayWriter charArrayWriter = new CharArrayWriter(2000);

        new BootstrapFileWriter(bootstrap).write(charArrayWriter);

        return charArrayWriter.toString();
    }

    public void testExactXML() {
        assertEquals(EXPECTED_XML, generateBootstrap());
    }

    private FileParseCallbackMemento getExpectedParseResult(
            DummyDeploymentFile file) {
        FileParseCallbackMemento expectedParseResult = new FileParseCallbackMemento(file);
        ParseContext context = new ParseContext(file);
        expectedParseResult.contentFound(context, ARTICLE_EXTERNAL_ID, Major.ARTICLE, null);
        expectedParseResult.contentFound(context, DEPARTMENT_EXTERNAL_ID, Major.DEPARTMENT, DEPARTMENT_INPUT_TEMPLATE);
        expectedParseResult.contentReferenceFound(context, Major.INPUT_TEMPLATE, DEPARTMENT_INPUT_TEMPLATE);
        return expectedParseResult;
    }

    private FileParseCallbackMemento parseAndReturnMemento(DummyDeploymentFile file) {
        FileParseCallbackMemento parseResult = new FileParseCallbackMemento(file);
        new ContentXmlParser().parse(file, parseResult);
        return parseResult;
    }

    private DummyDeploymentFile getFileReading(String string) {
        DummyDeploymentFile file = new DummyDeploymentFile("any");
        file.setInputStream(new ByteArrayInputStream(string.getBytes()));
        return file;
    }

    public void testReadingXMLBack() {
        String bootStrap = generateBootstrap();

        DummyDeploymentFile file = getFileReading(bootStrap);

        FileParseCallbackMemento parseResult = parseAndReturnMemento(file);
        FileParseCallbackMemento expectedParseResult = getExpectedParseResult(file);

        assertEquals(expectedParseResult, parseResult);
    }
}


