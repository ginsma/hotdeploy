package com.polopoly.ps.hotdeploy.state;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentFile;

public abstract class AbstractFileChecksumsTest {
	private FileChecksums checksums;

	abstract FileChecksums createChecksums() throws Exception;

	@Before
	public void setUp() throws Exception {
		checksums = createChecksums();
	}

	@Test
	public void testNeverSet() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		Assert.assertEquals(-1, checksums.getQuickChecksum(deploymentFile));
		Assert.assertEquals(-1, checksums.getSlowChecksum(deploymentFile));
	}

	@Test
	public void testDeleteBeforePersist() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		checksums.setChecksums(deploymentFile, 1234, 5678);

		checksums.deleteChecksums(deploymentFile);

		Assert.assertFalse(gatherContents(checksums).contains(deploymentFile));
		Assert.assertEquals(-1, checksums.getQuickChecksum(deploymentFile));
		Assert.assertEquals(-1, checksums.getSlowChecksum(deploymentFile));
	}

	@Test
	public void testDeleteAfterPersist() throws Exception {
		DeploymentFile deploymentFile1 = createFileDeploymentFile();
		DeploymentFile deploymentFile2 = createFileDeploymentFile("bar");

		checksums.setChecksums(deploymentFile1, 1234, 5678);
		checksums.setChecksums(deploymentFile2, 1234, 5678);

		checksums.deleteChecksums(deploymentFile1);

		checksums.persist();

		checksums.deleteChecksums(deploymentFile2);

		Assert.assertTrue(gatherContents(checksums).isEmpty());
		Assert.assertEquals(-1, checksums.getQuickChecksum(deploymentFile2));
		Assert.assertEquals(-1, checksums.getSlowChecksum(deploymentFile2));
		Assert.assertEquals(-1, checksums.getQuickChecksum(deploymentFile1));
		Assert.assertEquals(-1, checksums.getSlowChecksum(deploymentFile1));
	}

	@Test
	public void testSetGetBeforePersist() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		checksums.setChecksums(deploymentFile, 1234, 5678);

		assertChecksums(deploymentFile, 1234, 5678);
	}

	@Test
	public void testSetGetAfterPersist() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		checksums.setChecksums(deploymentFile, 1234, 5678);

		checksums.persist();

		assertChecksums(deploymentFile, 1234, 5678);
	}

	@Test
	public void testAreAllChangesPersisted() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		checksums.setChecksums(deploymentFile, 1234, 5678);

		Assert.assertFalse(checksums.areAllChangesPersisted());

		checksums.persist();

		Assert.assertTrue(checksums.areAllChangesPersisted());
	}

	@Test
	public void testAdditionalInformationBeforePersist() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		try {
			checksums.getAdditionalInformation(deploymentFile);

			Assert.fail("Could get additional information that had never been stored.");
		} catch (NoInformationStoredException e) {
			// expected
		}

		checksums.setAdditionalInformation(deploymentFile, "foobar");

		Assert.assertEquals("foobar",
				checksums.getAdditionalInformation(deploymentFile));
	}

	@Test
	public void testAdditionalInformationAfterPersist() throws Exception {
		DeploymentFile deploymentFile = createFileDeploymentFile();

		checksums.persist();

		checksums.setAdditionalInformation(deploymentFile, "foobar");

		Assert.assertEquals("foobar",
				checksums.getAdditionalInformation(deploymentFile));
	}

	@Test
	public void testIterator() throws Exception {
		Set<DeploymentFile> fileSet = new HashSet<DeploymentFile>();

		createAndAddRandomFile(fileSet);

		createAndAddRandomFile(fileSet);

		Assert.assertEquals(fileSet, gatherContents(checksums));

		checksums.persist();

		Assert.assertEquals(fileSet, gatherContents(checksums));

		createAndAddRandomFile(fileSet);

		Assert.assertEquals(fileSet, gatherContents(checksums));
	}

	private <O> Set<O> gatherContents(Iterable<O> iterable) {
		Set<O> result = new HashSet<O>();

		for (O o : iterable) {
			result.add(o);
		}

		return result;
	}

	private void createAndAddRandomFile(Set<DeploymentFile> fileSet)
			throws Exception {
		String name = randomString();

		DeploymentFile deploymentFile = createFileDeploymentFile(name);

		checksums.setChecksums(deploymentFile, 1234, 5678);
		fileSet.add(deploymentFile);
	}

	protected String randomString() {
		return Integer.toString(new Random().nextInt());
	}

	private void assertChecksums(DeploymentFile deploymentFile,
			long quickchecksum, long slowchecksum) {
		Assert.assertEquals(quickchecksum,
				checksums.getQuickChecksum(deploymentFile));
		Assert.assertEquals(slowchecksum,
				checksums.getSlowChecksum(deploymentFile));
	}

	private DeploymentFile createFileDeploymentFile() {
		return createFileDeploymentFile("foobar");
	}

	private DeploymentFile createFileDeploymentFile(String name) {
		return new FileDeploymentFile(new File(name));
	}
}
