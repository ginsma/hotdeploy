package example.deploy.hotdeploy.file;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class TestJarDeploymentFileChecksums extends ChecksumTestCase {
    private JarFile jarFile;

    @Override
    protected DeploymentFile getFile(String fileNameInJar) {
        ZipEntry entry = jarFile.getEntry(fileNameInJar);

        if (entry == null) {
            throw new RuntimeException("File " + fileNameInJar + " did not exist in " + jarFile.getName());
        }

        return new JarDeploymentFile(jarFile, entry);
    }

    @Override
    public void setUp() throws IOException {
        jarFile = new JarFile(EQUAL_FILES_DIRECTORY + "equalfiles.jar");

        super.setUp();
    }

}
