package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.JarDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentRoot;

public class TestJarDirectoryFileDiscoverer extends TestCase {

    private DirectoryFileDiscoverer discoverer;

    @Override
    protected void setUp() throws Exception {
        discoverer = new DirectoryFileDiscoverer();
    }

    public void testFileDirectory() throws NotApplicableException, IOException {
        String directory = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.jar";

        JarFile jarFile = new JarFile(new File(directory));

        List<DeploymentFile> files =
            discoverer.getFilesToImport(new JarDeploymentRoot(jarFile));

        assertEquals(2, files.size());
        assertTrue(files.contains(new JarDeploymentFile(jarFile, jarFile.getEntry("b/c.xml"))));
        assertTrue(files.contains(new JarDeploymentFile(jarFile, jarFile.getEntry("a.xml"))));
    }

    public void testEmptyFileDirectory()  {
        String directory = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "emptyfolder";

        try {
            discoverer.getFilesToImport(new FileDeploymentDirectory(
                new File(directory)));
            fail("Did not throw NotApplicableException");
        } catch (NotApplicableException e) {
            // expected
        }
    }
}
