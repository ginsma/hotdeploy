package example.deploy.hotdeploy.file;

import static example.deploy.hotdeploy.discovery.TestFileConstants.DEPENDING_TEST_JAR_PATH;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;

import junit.framework.TestCase;

public class TestJarDeploymentDirectory extends TestCase {
    private JarFile jarFile;
    private JarDeploymentRoot root;
    private JarDeploymentDirectory dir;

    @Override
    public void setUp() throws IOException {
        jarFile = new JarFile(DEPENDING_TEST_JAR_PATH);

        root = new JarDeploymentRoot(jarFile);
        dir = new JarDeploymentDirectory(jarFile, jarFile.getEntry("b/"));
    }

    public void testGetDir() throws FileNotFoundException {
        DeploymentObject file = root.getFile("b");

        assertEquals(dir, file);
        assertTrue(file instanceof DeploymentDirectory);
    }

    public void testReadFile() throws IOException {
        ((DeploymentFile) root.getFile("b/c.xml")).getInputStream().close();
    }

    public void testListFiles() {
        DeploymentObject[] files = root.listFiles();

        JarDeploymentFile file = new JarDeploymentFile(jarFile, jarFile.getEntry("a.xml"));

        if (files.length != 4) {
            fail("Did not find three files listing. Found: " + Arrays.toString(files));
        }

        if (!contains(files, dir)) {
            fail(dir + " not found listing. found: " + Arrays.toString(files));
        }

        if (!contains(files, file)) {
            fail(file + " not found listing. found: " + Arrays.toString(files));
        }

        assertTrue(dir.listFiles().length == 1);

        assertTrue(dir.listFiles()[0].equals(new JarDeploymentFile(jarFile, jarFile.getEntry("b/c.xml"))));
    }

    private boolean contains(DeploymentObject[] files, DeploymentObject file) {
        for (DeploymentObject deploymentFile : files) {
            if (deploymentFile.equals(file)) {
                return true;
            }
        }

        return false;
    }
}
