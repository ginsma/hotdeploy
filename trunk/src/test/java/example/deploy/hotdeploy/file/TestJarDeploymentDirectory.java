package example.deploy.hotdeploy.file;

import static example.deploy.hotdeploy.discovery.TestFileConstants.DEPENDING_TEST_JAR_PATH;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;

import junit.framework.TestCase;

public class TestJarDeploymentDirectory extends TestCase {
    private static final String DIR_NAME_NO_SLASH = "b";
    private static final String FILE_IN_DIR_NAME = "c.xml";
    private static final String FILE_IN_DIR_PATH = DIR_NAME_NO_SLASH + "/" + FILE_IN_DIR_NAME;
    private JarFile jarFile;
    private JarDeploymentRoot root;
    private JarDeploymentDirectory dir;

    @Override
    public void setUp() throws IOException {
        jarFile = new JarFile(DEPENDING_TEST_JAR_PATH);

        root = new JarDeploymentRoot(jarFile);
        dir = new JarDeploymentDirectory(jarFile, jarFile.getEntry(DIR_NAME_NO_SLASH + "/"));
    }

    public void testImports() {
        assertTrue(root.imports(dir));
        assertFalse(dir.imports(root));
    }

    public void testGetDir() throws FileNotFoundException {
        DeploymentObject file = root.getFile(DIR_NAME_NO_SLASH);

        assertEquals(dir, file);
        assertTrue(file instanceof DeploymentDirectory);
    }

    private JarDeploymentFile getFileInJar(String fileName) {
        return new JarDeploymentFile(jarFile, jarFile.getEntry(fileName));
    }

    public void testReadFile() throws IOException {
        getFileInJar(FILE_IN_DIR_PATH).getInputStream().close();
    }

    public void testListFiles() {
        DeploymentObject[] files = root.listFiles();

        JarDeploymentFile file = getFileInJar("a.xml");

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

        String fileName = FILE_IN_DIR_PATH;
        assertTrue(dir.listFiles()[0].equals(getFileInJar(fileName)));
    }

    public void testRelativeName(){
        JarDeploymentFile file = new JarDeploymentFile(jarFile, jarFile.getEntry(FILE_IN_DIR_PATH));

        assertEquals(FILE_IN_DIR_NAME, dir.getRelativeName(file));
        assertEquals(FILE_IN_DIR_PATH, root.getRelativeName(file));
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
