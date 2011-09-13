package com.polopoly.ps.hotdeploy.deployer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.state.CouldNotUpdateStateException;
import com.polopoly.ps.hotdeploy.state.DefaultDirectoryState;
import com.polopoly.ps.hotdeploy.state.DummyDeploymentFile;
import com.polopoly.ps.hotdeploy.state.FileChecksums;
import com.polopoly.ps.hotdeploy.state.NoInformationStoredException;
import com.polopoly.ps.hotdeploy.state.NonPersistedFileChecksums;

public class TestMultipleFileDeployer extends TestCase {
	private DefaultDirectoryState directoryState;
	private DummySingleFileDeployer singleFileDeployer;
	private MultipleFileDeployer multipleFileDeployer;
	private NonPersistedFileChecksums fileChecksums;
	private DummyDeploymentFile aFile;
	private DummyDeploymentFile anotherFile;

	public void testDeploy(Collection<DeploymentFile> filesToDeploy)
			throws FatalDeployException {
		assertTrue(directoryState.areAllChangesPersisted());

		multipleFileDeployer.deploy(filesToDeploy);

		for (DeploymentFile fileToDeploy : filesToDeploy) {
			assertFalse(directoryState.hasFileChanged(fileToDeploy));

			if (singleFileDeployer.isFileToFail(fileToDeploy)) {
				singleFileDeployer.assertNotDeployed(fileToDeploy);
				assertFalse(fileChecksums.contains(fileToDeploy));
			} else {
				singleFileDeployer.assertDeployed(fileToDeploy);
				assertTrue(fileChecksums.contains(fileToDeploy));
			}
		}

		assertTrue(directoryState.areAllChangesPersisted());
	}

	public void testUnchangedFile() throws FatalDeployException,
			CouldNotUpdateStateException {
		directoryState.reset(aFile, false);

		multipleFileDeployer.deploy(Collections
				.singleton((DeploymentFile) aFile));

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

	public void testFastFailingDeployMultipleFiles()
			throws FatalDeployException {
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
		multipleFileDeployer.deploy(Collections
				.singleton((DeploymentFile) aFile));

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

			public void setChecksums(DeploymentFile file, long quickChecksum,
					long slowChecksum) {
			}

			public boolean areAllChangesPersisted() {
				return false;
			}

			public void persist() throws CouldNotUpdateStateException {
				throw new CouldNotUpdateStateException("Message", null);
			}

			@Override
			public Iterator<DeploymentFile> iterator() {
				List<DeploymentFile> emptyList = Collections.emptyList();

				return emptyList.iterator();
			}

			@Override
			public void deleteChecksums(DeploymentFile file) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getAdditionalInformation(DeploymentFile file)
					throws NoInformationStoredException {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setAdditionalInformation(DeploymentFile file,
					String additionalInformation) {
				throw new UnsupportedOperationException();
			}
		});

		Collection<DeploymentFile> filesToDeploy = new ArrayList<DeploymentFile>();

		filesToDeploy.add(aFile);
		filesToDeploy.add(anotherFile);

		try {
			multipleFileDeployer.deploy(filesToDeploy);

			fail("Deployer did not throw fatal exception when state could not be updated.");
		} catch (FatalDeployException e) {
			// expected
		}

		singleFileDeployer.assertDeployed(aFile);
		singleFileDeployer.assertDeployed(anotherFile);
	}

	public void testDiscoverAndDeploy() throws FatalDeployException {
		FileDiscoverer aFileDiscoverer = new FileDiscoverer() {
			public List<DeploymentFile> getFilesToImport()
					throws NotApplicableException {
				return Collections.singletonList((DeploymentFile) aFile);
			}
		};

		multipleFileDeployer.discoverAndDeploy(Collections
				.singletonList(aFileDiscoverer));

		singleFileDeployer.assertDeployed(aFile);
	}
	
	@Test
    public void testDryRun() throws Exception {
	    Collection<DeploymentFile> files = new ArrayList<DeploymentFile>();

        files.add(aFile);
        files.add(anotherFile);
        
        MultipleFileDeployer dryRunInstance = MultipleFileDeployer.getDryRunInstance(singleFileDeployer, directoryState);
        dryRunInstance.deploy(files);
       
        Assert.assertEquals(0, directoryState.getSuccessfulResets().size());
    }

	@Override
	public void setUp() {
		fileChecksums = new NonPersistedFileChecksums();
		directoryState = new DefaultDirectoryState(fileChecksums);

		singleFileDeployer = new DummySingleFileDeployer();

		aFile = new DummyDeploymentFile("afile");
		aFile.setQuickChecksum(4711);
		aFile.setSlowChecksum(4712);

		anotherFile = new DummyDeploymentFile("anotherfile");
		anotherFile.setQuickChecksum(4713);
		anotherFile.setSlowChecksum(4714);

		multipleFileDeployer = MultipleFileDeployer.getInstance(singleFileDeployer,
				directoryState);
	}
}
