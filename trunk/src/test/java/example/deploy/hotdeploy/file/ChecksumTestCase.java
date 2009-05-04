package example.deploy.hotdeploy.file;

import java.io.IOException;

import junit.framework.TestCase;
import example.deploy.hotdeploy.discovery.PlatformNeutralPath;

public abstract class ChecksumTestCase extends TestCase {
    protected static final String EQUAL_FILES_DIRECTORY =
        PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/equalfiles/");

    protected static final String DIFFERENT_FILE_NAME = "a_different_file";
    protected static final String COPY_OF_A_FILE_NAME = "copy_of_a_file";
    protected static final String A_FILE_NAME = "a_file";

    private DeploymentFile file;
    private DeploymentFile identicalFile;
    private DeploymentFile differentFile;

    public void testQuickChecksum() {
        assertFalse(file.getQuickChecksum() == identicalFile.getQuickChecksum());
        assertFalse(file.getQuickChecksum() == differentFile.getQuickChecksum());
    }

    public void testSlowChecksum() {
        assertTrue(file.getSlowChecksum() == identicalFile.getSlowChecksum());
        assertFalse(
                file + "'s checksum " + file.getSlowChecksum() + " was equal to " +
                differentFile + "'s checksum "+ differentFile.getSlowChecksum(),
                file.getSlowChecksum() == differentFile.getSlowChecksum());
    }

    public void testStability() {
        assertTrue(file.getQuickChecksum() == file.getQuickChecksum());
        assertTrue(identicalFile.getQuickChecksum() == identicalFile.getQuickChecksum());
        assertTrue(differentFile.getQuickChecksum() == differentFile.getQuickChecksum());

        assertTrue(file.getSlowChecksum() == file.getSlowChecksum());
        assertTrue(identicalFile.getSlowChecksum() == identicalFile.getSlowChecksum());
        assertTrue(differentFile.getSlowChecksum() == differentFile.getSlowChecksum());
    }

    @Override
    public void setUp() throws IOException {
        file = getFile(A_FILE_NAME);

        identicalFile = getFile(COPY_OF_A_FILE_NAME);

        differentFile = getFile(DIFFERENT_FILE_NAME);
    }

    protected abstract DeploymentFile getFile(String fileName);
}
