package example.deploy.hotdeploy.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.jar.JarFile;

import junit.framework.TestCase;

public class TestJarDeploymentFile extends TestCase {
    private JarDeploymentFile file;
    private JarDeploymentFile file2;
    private JarFile jarFile;

    public void testRead() throws FileNotFoundException, IOException {
        file.getInputStream().close();
    }

    public void testReadFailure() throws IOException {
        try {
            new JarDeploymentFile(jarFile, jarFile.getEntry("fail.xml")).getInputStream();
            fail("No FileNotFoundException");
        } catch (FileNotFoundException e) {
            // success
        }
    }

    public void testImports() {
        assertFalse(file.imports(file2));
        assertTrue(file.imports(file));
        assertTrue(new JarDeploymentRoot(jarFile).imports(file));
    }

    public void testEquality() {
        JarDeploymentFile fileb = new JarDeploymentFile(jarFile, jarFile.getEntry("a.xml"));

        assertTrue(file.equals(fileb));
        assertTrue(file.hashCode() == fileb.hashCode());
        assertFalse(file2.equals(file.hashCode()));
        assertFalse(file2.hashCode() == file.hashCode());
    }

    public void testGetBaseURL() throws MalformedURLException {
        String jarAbsoluteName = new File(jarFile.getName()).getAbsolutePath();
        assertEquals("jar:file:" + jarAbsoluteName + "!/", file.getBaseUrl().toString());
        assertEquals("jar:file:" + jarAbsoluteName + "!/b/", file2.getBaseUrl().toString());
    }

    @Override
    public void setUp() throws IOException {
        jarFile = new JarFile("src/test/resources/test.jar");

        file = new JarDeploymentFile(jarFile, jarFile.getEntry("a.xml"));
        file2 = new JarDeploymentFile(jarFile, jarFile.getEntry("b/c.xml"));
    }
}
