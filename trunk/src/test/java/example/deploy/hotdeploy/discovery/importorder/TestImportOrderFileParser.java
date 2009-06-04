package example.deploy.hotdeploy.discovery.importorder;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentObject;
import example.deploy.hotdeploy.state.DummyDeploymentFile;

public class TestImportOrderFileParser extends TestCase {
    private static final String FILE_CONTENT = "depends: othermodule\ncontent\ntemplates";
    private static final String CANONICAL_FILE_CONTENT = "depends:othermodule\ncontent\ntemplates\n";
    private static final String ADDED_FILE = "content/addedFile.xml";
    private static final String CANONICAL_FILE_CONTENT_WITH_ADDED_FILE = "depends:othermodule\n" + ADDED_FILE + "\ncontent\ntemplates\n";

    private ImportOrderFile importOrderFile;
    private DummyAllFilesExistDirectory directory;

    public void testParse() throws IOException {
        Iterator<DeploymentObject> deploymentObjects = importOrderFile.iterator();

        assertEquals(new DummyDeploymentFile("content"), deploymentObjects.next());
        assertEquals(new DummyDeploymentFile("templates"), deploymentObjects.next());

        assertEquals(1, importOrderFile.getDependencies().size());
        assertEquals("othermodule", importOrderFile.getDependencies().get(0));
    }

    public void testParseAndWriteBack() {
        ImportOrderFileWriter writer = new ImportOrderFileWriter(importOrderFile);

        StringWriter stringWriter = new StringWriter();

        writer.write(stringWriter);

        assertEquals(CANONICAL_FILE_CONTENT, stringWriter.getBuffer().toString());
    }

    public void testParseModifyAndWriteBack() {
        importOrderFile.addDeploymentObject(0, new DummyDeploymentFile(ADDED_FILE));

        ImportOrderFileWriter writer = new ImportOrderFileWriter(importOrderFile);

        StringWriter stringWriter = new StringWriter();

        writer.write(stringWriter);

        assertEquals(CANONICAL_FILE_CONTENT_WITH_ADDED_FILE, stringWriter.getBuffer().toString());
    }

    @Override
    public void setUp() throws IOException {
        directory = new DummyAllFilesExistDirectory();

        DummyDeploymentFile importOrderFileAsDeploymentFile = new DummyDeploymentFile("file") {
            @Override
            public InputStream getInputStream() throws FileNotFoundException {
                return new ByteArrayInputStream(FILE_CONTENT.getBytes());
            }
        };

        importOrderFile = new ImportOrderFileParser(directory, importOrderFileAsDeploymentFile).parse();
    }
}
