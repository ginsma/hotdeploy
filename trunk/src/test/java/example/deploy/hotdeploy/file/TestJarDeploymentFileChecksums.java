package example.deploy.hotdeploy.file;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class TestJarDeploymentFileChecksums extends ChecksumTestCase {
    private JarFile jarFile;
    private JarFile identicalJarFile;

    private DeploymentFile identicalFileInOtherJar;
    private DeploymentFile differentFileInOtherJar;

    @Override
    protected DeploymentFile getFile(String fileNameInJar) {
        return getFile(jarFile, fileNameInJar);
    }

    protected DeploymentFile getFileInOtherJar(String fileNameInJar) {
        return getFile(identicalJarFile, fileNameInJar);
    }

    protected DeploymentFile getFile(JarFile jarFile, String fileNameInJar) {
        ZipEntry entry = jarFile.getEntry(fileNameInJar);

        if (entry == null) {
            throw new RuntimeException("File " + fileNameInJar + " did not exist in " + jarFile.getName());
        }

        return new JarDeploymentFile(jarFile, entry);
    }

    public void testEqualityBetweenJars() {
        assertEquals(identicalFileInOtherJar.getQuickChecksum(), identicalFile.getQuickChecksum());
        assertEquals(identicalFileInOtherJar.getSlowChecksum(),  identicalFile.getSlowChecksum());
        assertEquals(differentFileInOtherJar.getQuickChecksum(), differentFile.getQuickChecksum());
        assertEquals(differentFileInOtherJar.getSlowChecksum(),  differentFile.getSlowChecksum());
    }

    @Override
    public void setUp() throws IOException {
        jarFile = new JarFile(EQUAL_FILES_DIRECTORY + "equalfiles.jar");
        identicalJarFile = new JarFile(EQUAL_FILES_DIRECTORY + "equalfilescopy.jar");

        super.setUp();

        identicalFileInOtherJar = getFileInOtherJar(COPY_OF_A_FILE_NAME);

        differentFileInOtherJar = getFileInOtherJar(DIFFERENT_FILE_NAME);
    }
}
