package com.polopoly.ps.deploy.hotdeploy.discovery;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import com.polopoly.ps.deploy.hotdeploy.discovery.DirectoryFileDiscoverer;
import com.polopoly.ps.deploy.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.file.JarDeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.file.JarDeploymentRoot;

import junit.framework.TestCase;

public class TestJarDirectoryFileDiscoverer extends TestCase {

    @Override
    protected void setUp() throws Exception {
    }

    public void testFileDirectory() throws NotApplicableException, IOException {
        String directory = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.jar";

        JarFile jarFile = new JarFile(new File(directory));

        DirectoryFileDiscoverer discoverer = new DirectoryFileDiscoverer(new JarDeploymentRoot(jarFile));

        List<DeploymentFile> files =
            discoverer.getFilesToImport();

        assertEquals(2, files.size());
        assertTrue(files.contains(new JarDeploymentFile(jarFile, jarFile.getEntry("b/c.xml"))));
        assertTrue(files.contains(new JarDeploymentFile(jarFile, jarFile.getEntry("a.xml"))));
    }

    public void testEmptyFileDirectory()  {
        String directory = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "emptyfolder";

        DirectoryFileDiscoverer discoverer = new DirectoryFileDiscoverer(new File(directory));

        try {
            discoverer.getFilesToImport();
            fail("Did not throw NotApplicableException");
        } catch (NotApplicableException e) {
            // expected
        }
    }
}
