package com.polopoly.ps.hotdeploy.state;

public class NonPersistedFileChecksumsIntegrationTest extends AbstractFileChecksumsTest {

	@Override
	FileChecksums createChecksums() {
		return new NonPersistedFileChecksums();
	}
}
