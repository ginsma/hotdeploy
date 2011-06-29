package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import example.deploy.hotdeploy.discovery.importorder.ImportOrder;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentFile;

public class TestResourceFileDiscoverer extends TestCase {
    private List<DeploymentFile> foundFiles;
    private ResourceFileDiscoverer discoverer;

    @Override
    protected void setUp() throws Exception {
        discoverer = new ResourceFileDiscoverer(false);
        foundFiles = discoverer.getFilesToImport();
    }

    public void testFoundTestClassPathFiles() throws NotApplicableException {
        assertContainsFile("target/test-classes/a.xml");
        assertContainsFile("target/test-classes/folder/subfolder/c.xml");
        assertContainsFile("target/test-classes/folder/subfolder/d.xml");
    }

    public void testFoundClassPathFiles() {
        assertContainsFile("target/classes/content/templates.xml");
    }

    public void testDiscoveringOnlyJars() throws Exception {
        discoverer = new ResourceFileDiscoverer(true);
        foundFiles = discoverer.getFilesToImport();

        assertDoesNotContainFile("target/test-classes/a.xml");
        assertDoesNotContainFile("target/classes/content/templates.xml");
        assertContainsFileInJar("b/c.xml");
    }

    private void assertContainsFile(String fileName) {
        FileDeploymentFile file = new FileDeploymentFile(new File(fileName));

        if (!foundFiles.contains(file)) {
            fail("Found files " + foundFiles + " did not include " + file + ".");
        }
    }

    private void assertDoesNotContainFile(String fileName) {
        FileDeploymentFile file = new FileDeploymentFile(new File(fileName));

        if (foundFiles.contains(file)) {
            fail("Found files " + foundFiles + " did include " + file + ".");
        }
    }

    private void assertContainsFileInJar(String fileNameInJar) {
        Assert.assertTrue(
            "Found files " + foundFiles + " did not include a JAR containing the file " + fileNameInJar + ".",
            contains(fileNameInJar));
    }

    private boolean contains(String fileNameInJar) {
        for (DeploymentFile foundFile : foundFiles) {
            if (foundFile instanceof JarDeploymentFile &&
                    ((JarDeploymentFile) foundFile).getNameWithinJar().equals(fileNameInJar)) {
                return true;
            }
        }

        return false;
    }

    public void testFoundTestJarFiles() {
        assertContainsFileInJar("b/c.xml");
    }

    public void testOrder() {
        List<ImportOrder> importOrderFiles =
            discoverer.getImportOrderFiles(getClass().getClassLoader());

        Iterator<ImportOrder> importOrderFileIterator = importOrderFiles.iterator();

        ImportOrder firstImportOrderFile = importOrderFileIterator.next();
        ImportOrder secondImportOrderFile = importOrderFileIterator.next();

        assertEquals(FileConstants.DEPENDED_TEST_JAR_DEPENDENCY_NAME, firstImportOrderFile.calculateDependencyName());
        assertEquals(FileConstants.DEPENDING_TEST_JAR_DEPENDENCY_NAME, secondImportOrderFile.calculateDependencyName());
    }
}
