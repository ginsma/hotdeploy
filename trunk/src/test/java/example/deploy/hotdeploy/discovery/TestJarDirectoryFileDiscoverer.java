package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentRoot;

public class TestJarDirectoryFileDiscoverer extends TestCase {

    @Override
    protected void setUp() throws Exception {
    }

    public void testFileDirectory() throws NotApplicableException, IOException {
        String directory = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.jar";

        JarFile jarFile = new JarFile(new File(directory));

        DirectoryFileDiscoverer discoverer = new DirectoryFileDiscoverer(new JarDeploymentRoot(jarFile));

        List<DeploymentFile> files =
            discoverer.getFilesToImport();

        assertEquals(2, files.size());
        assertTrue(files.contains(new JarDeploymentFile(jarFile, jarFile.getEntry("b/c.xml"))));
        assertTrue(files.contains(new JarDeploymentFile(jarFile, jarFile.getEntry("a.xml"))));
    }

    public void testEmptyFileDirectory()  {
        String directory = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "emptyfolder";

        DirectoryFileDiscoverer discoverer = new DirectoryFileDiscoverer(new File(directory));

        try {
            discoverer.getFilesToImport();
            fail("Did not throw NotApplicableException");
        } catch (NotApplicableException e) {
            // expected
        }
    }
}
