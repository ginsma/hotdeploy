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

    public void testEquality() {
        assertFalse(file1.equals(file2));
        assertFalse(file1.hashCode() == file2.hashCode());

        FileDeploymentFile file1a = new FileDeploymentFile(file1.getFile());

        assertTrue(file1.equals(file1a));
        assertTrue(file1.hashCode() == file1a.hashCode());
    }

    public void testStream() throws FileNotFoundException, IOException {
        file2.getInputStream().close();
    }

    public void testToString() {
        file2.toString();
    }

    public void testGetBaseURL() throws MalformedURLException {
        assertEquals("file:" + new File(".").getAbsolutePath() + "/", file2.getBaseUrl().toString());
    }

    @Override
    public void setUp() {
        file1 = new FileDeploymentFile(
            new File(PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/a.xml")));
        file2 = new FileDeploymentFile(new File("pom.xml"));
    }
}
