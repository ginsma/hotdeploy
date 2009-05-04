package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentFile;

public class TestResourceFileDiscoverer extends TestCase {
    private List<DeploymentFile> foundFiles;
    private ResourceFileDiscoverer discoverer;

    @Override
    protected void setUp() throws Exception {
        discoverer = new ResourceFileDiscoverer();
        foundFiles =
            discoverer.getFilesToImport(
                new File("."));
    }

    private void assertContainsFile(String fileName) {
        FileDeploymentFile file = new FileDeploymentFile(new File(fileName));

        if (!foundFiles.contains(file)) {
            fail("Found files " + foundFiles + " did not include " + file + ".");
        }
    }

    public void testFoundTestClassPathFiles() throws NotApplicableException {
        assertContainsFile("target/test-classes/a.xml");
        assertContainsFile("target/test-classes/folder/subfolder/c.xml");
        assertContainsFile("target/test-classes/folder/subfolder/d.xml");
    }

    public void testFoundClassPathFiles() {
        assertContainsFile("target/classes/templates.xml");
    }

    private void assertContainsFileInJar(String fileNameInJar) {
        for (DeploymentFile foundFile : foundFiles) {
            if (foundFile instanceof JarDeploymentFile &&
                    ((JarDeploymentFile) foundFile).getEntryName().equals(fileNameInJar)) {
                return;
            }
        }

        fail("Found files " + foundFiles + " did not include a JAR containing the file " + fileNameInJar + ".");
    }

    public void testFoundTestJarFiles() {
        assertContainsFileInJar("b/c.xml");
    }

    public void testOrder() {
        List<ImportOrderFile> importOrderFiles =
            discoverer.getImportOrderFiles(getClass().getClassLoader());

        Iterator<ImportOrderFile> importOrderFileIterator = importOrderFiles.iterator();

        ImportOrderFile firstImportOrderFile = importOrderFileIterator.next();
        ImportOrderFile secondImportOrderFile = importOrderFileIterator.next();

        assertEquals(TestFileConstants.DEPENDED_TEST_JAR_DEPENDENCY_NAME, firstImportOrderFile.calculateDependencyName());
        assertEquals(TestFileConstants.DEPENDING_TEST_JAR_DEPENDENCY_NAME, secondImportOrderFile.calculateDependencyName());
    }
}
