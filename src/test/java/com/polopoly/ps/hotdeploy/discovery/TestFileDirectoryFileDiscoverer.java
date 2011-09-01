package com.polopoly.ps.hotdeploy.discovery;

import java.io.File;
import java.util.List;

import com.polopoly.ps.hotdeploy.discovery.DirectoryFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.file.FileDeploymentFile;

import junit.framework.TestCase;

public class TestFileDirectoryFileDiscoverer extends TestCase {

    @Override
    protected void setUp() throws Exception {
    }

    public void testFileDirectory() throws NotApplicableException {
        String directory = PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/folder");

        DirectoryFileDiscoverer discoverer =
            new DirectoryFileDiscoverer(new FileDeploymentDirectory(new File(directory)));

        List<DeploymentFile> files = discoverer.getFilesToImport();

        assertEquals(3, files.size());
        assertTrue(files.contains(new FileDeploymentFile(
                new File(directory + File.separator + "b.xml"))));
        assertTrue(files.contains(new FileDeploymentFile(
                new File(directory + File.separator + "subfolder" + File.separator + "c.xml"))));
        assertTrue(files.contains(new FileDeploymentFile(
                new File(directory + File.separator + "subfolder" + File.separator + "d.xml"))));
    }

    public void testEmptyFileDirectory() throws NotApplicableException {
        String directory = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "emptyfolder";

        DirectoryFileDiscoverer discoverer =
            new DirectoryFileDiscoverer(new FileDeploymentDirectory(new File(directory)));

        try {
            discoverer.getFilesToImport();
            fail("Did not throw NotApplicableException");
        } catch (NotApplicableException e) {
            // expected
        }
    }
}
