package com.polopoly.ps.hotdeploy.discovery;

import static com.polopoly.ps.hotdeploy.discovery.FileConstants.DEPENDED_TEST_JAR_FILE_NAME;
import static com.polopoly.ps.hotdeploy.discovery.FileConstants.DEPENDING_TEST_JAR_PATH;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrder;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import com.polopoly.ps.hotdeploy.file.DeploymentDirectory;
import com.polopoly.ps.hotdeploy.file.JarDeploymentFile;
import com.polopoly.ps.hotdeploy.file.JarDeploymentRoot;

import junit.framework.TestCase;

public class TestJarImportOrderFileDiscoverer extends TestCase {
    public void testDiscoverer() throws NotApplicableException, IOException {
        String fileName = PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/test.jar");

        JarFile file = new JarFile(new File(fileName));
        DeploymentDirectory dir = new JarDeploymentRoot(file);

        ImportOrder files = new ImportOrderFileDiscoverer(dir).getFilesToImport();

        assertEquals(new JarDeploymentFile(file, file.getEntry("b/c.xml")), files.get(0));

        assertEquals("test", files.calculateDependencyName());
        assertEquals(1, files.size());
        assertEquals(0, files.getDependencies().size());
    }

    public void testDiscoverResources() throws Exception {
        String fileName = DEPENDING_TEST_JAR_PATH;
        String dependedFileNameWithVersion = DEPENDED_TEST_JAR_FILE_NAME;

        int i = dependedFileNameWithVersion.indexOf('-');

        String dependedFileName = dependedFileNameWithVersion.substring(0, i);

        JarFile file = new JarFile(new File(fileName));
        DeploymentDirectory dir = new JarDeploymentRoot(file);

        ImportOrder files = new ImportOrderFileDiscoverer(dir).getFilesToImport();

        String firstDependency = files.getDependencies().iterator().next();

        assertEquals(1, files.getDependencies().size());
        assertEquals(dependedFileName, firstDependency);
    }
}
