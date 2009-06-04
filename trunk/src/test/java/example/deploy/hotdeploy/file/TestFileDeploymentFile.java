package example.deploy.hotdeploy.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;
import example.deploy.hotdeploy.discovery.PlatformNeutralPath;

public class TestFileDeploymentFile extends TestCase {
    private FileDeploymentFile file1;
    private FileDeploymentFile file2;
    private File javaIoFile2;

    public void testEquality() {
        assertFalse(file1.equals(file2));
        assertFalse(file1.hashCode() == file2.hashCode());

        FileDeploymentFile file1a = new FileDeploymentFile(file1.getFile());

        assertTrue(file1.equals(file1a));
        assertTrue(file1.hashCode() == file1a.hashCode());
    }

    public void testImports() {
        assertFalse(file1.imports(file2));
        assertFalse(file1.imports(file2));
        assertTrue(file1.imports(file1));
    }

    public void testStream() throws FileNotFoundException, IOException {
        file2.getInputStream().close();
    }

    public void testToString() {
        file2.toString();
    }

    public void testParentFile() {
        assertEquals(PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources"),
                file1.getParentFile().getPath());

        assertEquals(javaIoFile2.getAbsoluteFile().getParentFile().getAbsolutePath(),
                file2.getParentFile().getAbsolutePath());
    }

    public void testGetBaseURL() throws MalformedURLException {
        assertEquals("file:" + new File("foo").getAbsoluteFile().
            getParentFile().getAbsolutePath() + "/", file2.getBaseUrl().toString());
    }

    public void testRelativePath() {
        FileDeploymentDirectory grandParent = new FileDeploymentDirectory(new File(file1.getName()).getParentFile().getParentFile());
        FileDeploymentDirectory parent = new FileDeploymentDirectory(new File(file1.getName()).getParentFile());

        assertEquals("resources/a.xml", grandParent.getRelativeName(file1));
        assertEquals("resources", grandParent.getRelativeName(parent));
    }

    @Override
    public void setUp() {
        file1 = new FileDeploymentFile(
            new File(PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/a.xml")));
        javaIoFile2 = new File("pom.xml");
        file2 = new FileDeploymentFile(javaIoFile2);
    }
}
