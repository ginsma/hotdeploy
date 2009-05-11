package example.deploy.hotdeploy.discovery;

import static example.deploy.hotdeploy.discovery.PlatformNeutralPath.unixToPlatformSpecificPath;
import static example.deploy.hotdeploy.discovery.TestFileConstants.DEPENDED_TEST_JAR_PATH;
import static example.deploy.hotdeploy.discovery.TestFileConstants.DEPENDING_TEST_JAR_PATH;
import static example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.JarDeploymentRoot;

public class TestDeploymentDirectoryDiscoverer extends TestCase {
    private static final String TEST_RESOURCE_DIRECTORY = unixToPlatformSpecificPath("src/test/resources");
    private static final String FAKE_MVN_ROOT_DIRECTORY = unixToPlatformSpecificPath("src/test/resources/fakemvnproject");
    private static final String FAKE_MVN_CONTENT_DIRECTORY = unixToPlatformSpecificPath("src/test/resources/fakemvnproject/src/main/resources/content");

    private void assertEqualsSingleDirectory(
            DeploymentDirectory expectedDirectory,
            Collection<DeploymentDirectory> foundDirectories) {
        assertEquals(expectedDirectory, foundDirectories.iterator().next());
        assertEquals(1, foundDirectories.size());
    }

    private void assertEqualsSingleDirectory(String expectedPath,
            Collection<DeploymentDirectory> foundDirectories) {
        FileDeploymentDirectory expectedDirectory = new FileDeploymentDirectory(new File(expectedPath));
        assertEqualsSingleDirectory(expectedDirectory, foundDirectories);
    }


    private DeploymentDirectoryDiscoverer createDiscovererForDirectory(
            String rootDirectory, String ... searchDirectories)
            throws IOException {
        return new DeploymentDirectoryDiscoverer(
            new File(rootDirectory).getCanonicalFile(),
                Arrays.asList(searchDirectories));
    }

    private DeploymentDirectoryDiscoverer createDiscovererForDirectory(
            String rootDirectory) throws IOException {
        return new DeploymentDirectoryDiscoverer(new File(rootDirectory).getCanonicalFile(),
                DefaultDiscoveryDirectories.getDirectories());
    }

    public void testDiscoverFileSpecificDirectory() throws IOException {
        String rootDirectory = ".";

        DeploymentDirectoryDiscoverer disco =
            createDiscovererForDirectory(rootDirectory, TEST_RESOURCE_DIRECTORY);

        Collection<DeploymentDirectory> foundDirectories = disco.getDiscoveredDirectories();

        assertEqualsSingleDirectory(TEST_RESOURCE_DIRECTORY, foundDirectories);
    }

    public void testDiscoverFileDefaultDirectories() throws IOException {
        DeploymentDirectoryDiscoverer disco =
            createDiscovererForDirectory(TEST_RESOURCE_DIRECTORY);

        Collection<DeploymentDirectory> foundDirectories = disco.getDiscoveredDirectories();

        assertEqualsSingleDirectory(TEST_RESOURCE_DIRECTORY, foundDirectories);
    }

    public void testDiscoverFileMvnWebapp() throws IOException {
        DeploymentDirectoryDiscoverer disco =
            createDiscovererForDirectory(FAKE_MVN_ROOT_DIRECTORY);

        Collection<DeploymentDirectory> foundDirectories = disco.getDiscoveredDirectories();

        assertEqualsSingleDirectory(FAKE_MVN_CONTENT_DIRECTORY, foundDirectories);
    }

    private void assertSingleExistingDirectory(
            Collection<DeploymentDirectory> foundDirectories) {
        assertEquals(1, foundDirectories.size());
        assertTrue(foundDirectories.iterator().next().exists());
    }

    private void assertFindsNothing(DeploymentDirectoryDiscoverer discoverer) {
        Collection<DeploymentDirectory> foundDirectories = discoverer.getDiscoveredDirectories();
        assertSingleExistingDirectory(foundDirectories);
        assertNoImportOrder(foundDirectories);
    }

    public void testDiscoverNoResource() throws Exception {
        DeploymentDirectoryDiscoverer discoverer =
            new DeploymentDirectoryDiscoverer(getClass().getClassLoader(),
                singleton("foo"));

        assertFindsNothing(discoverer);
    }

    public void testDiscoverNoFile() throws IOException {
        DeploymentDirectoryDiscoverer disco =
            createDiscovererForDirectory("nonexistentfile");

        assertFindsNothing(disco);
    }

    public void testDiscoverResourceSpecificDirectory() throws Exception {
        assertFindsImportOrders(
            new DeploymentDirectoryDiscoverer(getClass().getClassLoader(),
                asList(new String[] { ".", "foo" })));
    }

    private void assertAllHaveImportOrders(Collection<DeploymentDirectory> discoveredDirectories) throws Exception {
        for (DeploymentDirectory foundDirectory : discoveredDirectories) {
            DeploymentFile foundImportOrderFile = (DeploymentFile)
                foundDirectory.getFile(ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME);

            assertIsReadableFile(foundImportOrderFile);
        }
    }

    private void assertContainsJarCalled(String jarFileName, Collection<DeploymentDirectory> foundDirectories) {
        for (DeploymentDirectory foundDirectory : foundDirectories) {
            if (foundDirectory instanceof JarDeploymentRoot) {
                String jarName = ((JarDeploymentRoot) foundDirectory).getName();

                if (jarName.endsWith(File.separator + jarFileName)) {
                    return;
                }
            }
        }

        fail(foundDirectories + " did not contain a JAR called " + jarFileName);
    }

    public void testDiscoverResourceDefaultDirectories() throws Exception {
        DeploymentDirectoryDiscoverer discoverer =
            new DeploymentDirectoryDiscoverer(getClass().getClassLoader(),
                    DefaultDiscoveryDirectories.getDirectories());

        Collection<DeploymentDirectory> foundDirectories =
            discoverer.getDiscoveredDirectories();

        assertAllHaveImportOrders(foundDirectories);
        assertContainsJarCalled(DEPENDED_TEST_JAR_PATH, foundDirectories);
        assertContainsJarCalled(DEPENDING_TEST_JAR_PATH, foundDirectories);
    }

    private void assertIsReadableFile(DeploymentFile file) throws IOException,
            FileNotFoundException {
        file.getInputStream().close();
    }

    private void assertFindsImportOrders(DeploymentDirectoryDiscoverer discoverer) throws Exception {
        Collection<DeploymentDirectory> discoveredDirectories = discoverer.getDiscoveredDirectories();
        assertAllHaveImportOrders(discoveredDirectories);
    }

    private void assertNoImportOrder(Collection<DeploymentDirectory> foundDirectories) {
        for (DeploymentDirectory foundDirectory : foundDirectories) {
            try {
                foundDirectory.getFile(IMPORT_ORDER_FILE_NAME);

                fail("The directory " + foundDirectory + " did contain a file called " + IMPORT_ORDER_FILE_NAME);
            } catch (FileNotFoundException e) {
                // expected
            }
        }
    }

    private void assertContainsDeploymentDirectory(
            DeploymentDirectory expectedDirectory,
            Collection<DeploymentDirectory> foundDirectories) {
        if (!foundDirectories.contains(expectedDirectory)) {
            fail("Expected " + foundDirectories + " to have contained " + expectedDirectory);
        }
    }

    private void assertContainsJar(String expectedJarFileName,
            Collection<DeploymentDirectory> foundDirectories)
            throws IOException {
        assertContainsDeploymentDirectory(new JarDeploymentRoot(new JarFile(expectedJarFileName)), foundDirectories);
    }

    public void testDiscoverInJAR() throws IOException {
        DeploymentDirectoryDiscoverer discoverer =
            new DeploymentDirectoryDiscoverer(new JarDeploymentRoot(new JarFile(DEPENDING_TEST_JAR_PATH)),
                DefaultDiscoveryDirectories.getDirectories());

        Collection<DeploymentDirectory> foundDirectories =
            discoverer.getDiscoveredDirectories();

        assertContainsJar(DEPENDING_TEST_JAR_PATH, foundDirectories);
    }
}
