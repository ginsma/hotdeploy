package example.deploy.hotdeploy.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.CouldNotUpdateStateException;
import example.deploy.hotdeploy.state.DefaultDirectoryState;
import example.deploy.hotdeploy.state.DummyDeploymentFile;
import example.deploy.hotdeploy.state.FileChecksums;
import example.deploy.hotdeploy.state.NonPersistedFileChecksums;

public class TestMultipleFileDeployer extends TestCase {
    private DefaultDirectoryState directoryState;
    private DummySingleFileDeployer singleFileDeployer;
    private MultipleFileDeployer multipleFileDeployer;
    private NonPersistedFileChecksums fileChecksums;
    private DummyDeploymentFile aFile;
    private DummyDeploymentFile anotherFile;

    public void testDeploy(Collection<DeploymentFile> filesToDeploy) throws FatalDeployException {
        assertTrue(directoryState.areAllChangesPersisted());

        multipleFileDeployer.deploy(filesToDeploy);

        for (DeploymentFile fileToDeploy : filesToDeploy) {
            assertFalse(directoryState.hasFileChanged(fileToDeploy));

            if (singleFileDeployer.isFileToFail(fileToDeploy)) {
                singleFileDeployer.assertNotDeployed(fileToDeploy);
                assertFalse(fileChecksums.contains(fileToDeploy));
            }
            else {
                singleFileDeployer.assertDeployed(fileToDeploy);
                assertTrue(fileChecksums.contains(fileToDeploy));
            }
        }

        assertTrue(directoryState.areAllChangesPersisted());
    }

    public void testUnchangedFile() throws FatalDeployException, CouldNotUpdateStateException {
        directoryState.reset(aFile, false);

        multipleFileDeployer.deploy(Collections.singleton((DeploymentFile) aFile));

        singleFileDeployer.assertNotDeployed(aFile);
    }

    public void testWorkingDeploySingleFile() throws FatalDeployException {
        testDeploy(Collections.singleton((DeploymentFile) aFile));
    }

    public void testWorkingDeployMultipleFiles() throws FatalDeployException {
        Collection<DeploymentFile> files = new ArrayList<DeploymentFile>();

        files.add(aFile);
        files.add(anotherFile);

        testDeploy(files);
    }

    public void testFailingDeploySingleFile() throws FatalDeployException {
        singleFileDeployer.failThisFile(aFile);
        testDeploy(Collections.singleton((DeploymentFile) aFile));
    }

    public void testFailingDeployMultipleFiles() throws FatalDeployException {
        Collection<DeploymentFile> files = new ArrayList<DeploymentFile>();

        files.add(aFile);
        files.add(anotherFile);

        singleFileDeployer.failThisFile(aFile);

        testDeploy(Collections.singleton((DeploymentFile) aFile));
    }

    public void testFastFailingDeployMultipleFiles() throws FatalDeployException {
        Collection<DeploymentFile> filesToDeploy = new ArrayList<DeploymentFile>();

        filesToDeploy.add(aFile);
        filesToDeploy.add(anotherFile);

        singleFileDeployer.failThisFile(aFile);
        multipleFileDeployer.setFailFast(true);

        multipleFileDeployer.deploy(filesToDeploy);

        singleFileDeployer.assertNotDeployed(anotherFile);
    }

    public void testFailedDeploy() throws FatalDeployException {
        singleFileDeployer.failThisFile(aFile);
        multipleFileDeployer.deploy(Collections.singleton((DeploymentFile) aFile));

        assertFalse(directoryState.hasFileChanged(aFile));

        singleFileDeployer.assertNotDeployed(aFile);
    }

    public void testFailUpdatingState() {
        directoryState.setFileChecksums(new FileChecksums() {
            public long getQuickChecksum(DeploymentFile file) {
                return 0;
            }

            public long getSlowChecksum(DeploymentFile file) {
                return 0;
            }

            public void setChecksums(DeploymentFile file, long quickChecksum, long slowChecksum)  {
            }

            public boolean areAllChangesPersisted() {
                return false;
            }

            public void persist() throws CouldNotUpdateStateException {
                throw new CouldNotUpdateStateException("Message", null);
            }});

        Collection<DeploymentFile> filesToDeploy = new ArrayList<DeploymentFile>();

        filesToDeploy.add(aFile);
        filesToDeploy.add(anotherFile);

        try {
            multipleFileDeployer.deploy(filesToDeploy);

            fail("Deployer did not throw fatal exception when state could not be updated.");
        }
        catch (FatalDeployException e) {
            // expected
        }

        singleFileDeployer.assertDeployed(aFile);
        singleFileDeployer.assertDeployed(anotherFile);
    }

    public void testDiscoverAndDeploy() throws FatalDeployException {
        FileDiscoverer aFileDiscoverer = new FileDiscoverer() {
            public List<DeploymentFile> getFilesToImport(File rootDirectory)
            throws NotApplicableException {
                return Collections.singletonList((DeploymentFile) aFile);
            }};

        multipleFileDeployer.discoverAndDeploy(Collections.singletonList(aFileDiscoverer));

        singleFileDeployer.assertDeployed(aFile);
    }

    @Override
    public void setUp() {
        fileChecksums = new NonPersistedFileChecksums();
        directoryState =
            new DefaultDirectoryState(fileChecksums);

        singleFileDeployer = new DummySingleFileDeployer();

        aFile = new DummyDeploymentFile();
        aFile.setQuickChecksum(4711);
        aFile.setSlowChecksum(4712);

        anotherFile = new DummyDeploymentFile();
        anotherFile.setQuickChecksum(4713);
        anotherFile.setSlowChecksum(4714);

        multipleFileDeployer = new MultipleFileDeployer(
            singleFileDeployer,
            new File("."),
            directoryState);
    }
}
