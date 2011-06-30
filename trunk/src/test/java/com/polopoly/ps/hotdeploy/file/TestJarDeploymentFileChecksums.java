package com.polopoly.ps.hotdeploy.file;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.JarDeploymentFile;

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
        // don't test the quick checksum. it's ok even if it is different.

        assertEquals(identicalFileInOtherJar.getSlowChecksum(),  identicalFile.getSlowChecksum());
        assertEquals(differentFileInOtherJar.getSlowChecksum(),  differentFile.getSlowChecksum());
    }

    public void testEqualityBetweenJarAndFile() throws IOException {
        TestFileDeploymentFileChecksums fileTest = new TestFileDeploymentFileChecksums();
        fileTest.setUp();

        // don't test the quick checksum. it's ok even if it is different.
        assertEquals(fileTest.differentFile.getSlowChecksum(), differentFile.getSlowChecksum());
        assertEquals(fileTest.identicalFile.getSlowChecksum(), identicalFile.getSlowChecksum());
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
