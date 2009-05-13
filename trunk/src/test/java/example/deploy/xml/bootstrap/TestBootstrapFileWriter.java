package example.deploy.xml.bootstrap;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;

import junit.framework.TestCase;

import org.apache.tools.ant.util.ReaderInputStream;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.state.DummyDeploymentFile;
import example.deploy.xml.parser.ParseContext;
import example.deploy.xml.parser.XmlParser;
import example.deploy.xml.parser.cache.FileParseCallbackMemento;

public class TestBootstrapFileWriter extends TestCase {

    private static final String ARTICLE_EXTERNAL_ID = "id.article";
    private static final String DEPARTMENT_EXTERNAL_ID = "id.dept";

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
        "    </metadata>\n" +
        "  </content>\n" +
        "</batch>\n";
    private Bootstrap bootstrap;

    @Override
    public void setUp() {
        bootstrap = new Bootstrap();

        bootstrap.add(new BootstrapContent(Major.ARTICLE, ARTICLE_EXTERNAL_ID));
        bootstrap.add(new BootstrapContent(Major.DEPARTMENT, DEPARTMENT_EXTERNAL_ID));
    }

    private char[] generateBootstrap() {
        final CharArrayWriter writer = new CharArrayWriter(1000);
        new BootstrapFileWriter(bootstrap).write(writer);
        char[] charArray = writer.toCharArray();
        return charArray;
    }

    public void testExactXML() {
        char[] charArray = generateBootstrap();

        String bootstrapXml =
            new StringBuffer().append(charArray).toString();

        assertEquals(EXPECTED_XML, bootstrapXml);
    }

    private FileParseCallbackMemento getExpectedParseResult(
            DummyDeploymentFile file) {
        FileParseCallbackMemento expectedParseResult = new FileParseCallbackMemento(file);
        ParseContext context = new ParseContext(file);
        expectedParseResult.contentFound(context, ARTICLE_EXTERNAL_ID, Major.ARTICLE, null);
        expectedParseResult.contentFound(context, DEPARTMENT_EXTERNAL_ID, Major.DEPARTMENT, null);
        return expectedParseResult;
    }

    private FileParseCallbackMemento parseAndReturnMemento(DummyDeploymentFile file) {
        FileParseCallbackMemento parseResult = new FileParseCallbackMemento(file);
        new XmlParser().parse(file, parseResult);
        return parseResult;
    }

    private DummyDeploymentFile getFileReading(char[] charArray) {
        DummyDeploymentFile file = new DummyDeploymentFile("any");
        file.setInputStream(new ReaderInputStream(new CharArrayReader(charArray)));
        return file;
    }

    public void testReadingXMLBack() {
        char[] charArray = generateBootstrap();

        DummyDeploymentFile file = getFileReading(charArray);

        FileParseCallbackMemento parseResult = parseAndReturnMemento(file);
        FileParseCallbackMemento expectedParseResult = getExpectedParseResult(file);

        assertEquals(expectedParseResult, parseResult);
    }
}


