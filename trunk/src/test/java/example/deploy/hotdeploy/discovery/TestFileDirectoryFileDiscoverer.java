package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class TestFileDirectoryFileDiscoverer extends TestCase {

    private DirectoryFileDiscoverer discoverer;

    @Override
    protected void setUp() throws Exception {
        discoverer = new DirectoryFileDiscoverer();
    }

    public void testFileDirectory() throws NotApplicableException {
        String directory = PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/folder");
        List<DeploymentFile> files =
            discoverer.getFilesToImport(new FileDeploymentDirectory(
                new File(directory)));

        assertEquals(3, files.size());
        assertTrue(files.contains(new FileDeploymentFile(
                new File(directory + File.separator + "b.xml"))));
        assertTrue(files.contains(new FileDeploymentFile(
                new File(directory + File.separator + "subfolder" + File.separator + "c.xml"))));
        assertTrue(files.contains(new FileDeploymentFile(
                new File(directory + File.separator + "subfolder" + File.separator + "d.xml"))));
    }

    public void testEmptyFileDirectory() throws NotApplicableException {
        String directory = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "emptyfolder";

        try {
            discoverer.getFilesToImport(new FileDeploymentDirectory(
                new File(directory)));
            fail("Did not throw NotApplicableException");
        } catch (NotApplicableException e) {
            // expected
        }
    }
}
