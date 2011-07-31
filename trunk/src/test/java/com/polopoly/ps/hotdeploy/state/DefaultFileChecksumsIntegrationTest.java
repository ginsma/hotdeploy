package com.polopoly.ps.hotdeploy.state;

import org.junit.After;

import com.polopoly.util.exception.NoSuchExternalIdException;

public class DefaultFileChecksumsIntegrationTest extends
		AbstractFileChecksumsTest {
	private String externalId;

	@Override
	FileChecksums createChecksums() throws Exception {
		externalId = randomString();

		return new DefaultFileChecksums(context.getPolicyCMServer(), externalId);
	}

	@After
	public void tearDown() throws Exception {
		try {
			context.getPolicyCMServer().removeContent(
					context.resolveExternalId(externalId));
		} catch (NoSuchExternalIdException e) {
			// a test that never persisted the checksums
		}
	}

}
