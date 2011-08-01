package com.polopoly.ps.hotdeploy.state;

import org.junit.After;
import org.junit.Ignore;

import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.exception.NoSuchExternalIdException;

@Ignore
public class DefaultFileChecksumsIntegrationTest extends
		AbstractFileChecksumsTest {
	private String externalId;

	@Override
	FileChecksums createChecksums() throws Exception {
		externalId = randomString();

		return new DefaultFileChecksums(getContext().getPolicyCMServer(),
				externalId);
	}

	@After
	public void tearDown() throws Exception {
		try {
			getContext().getPolicyCMServer().removeContent(
					getContext().resolveExternalId(externalId));
		} catch (NoSuchExternalIdException e) {
			// a test that never persisted the checksums
		}
	}

	protected PolopolyContext getContext() {
		throw new UnsupportedOperationException(
				"Re-include testfirst dependency to run this test and extend AbstractIntegrationTest");
	}

}
