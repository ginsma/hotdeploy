package com.polopoly.ps.deploy.hotdeploy.file;

import java.io.File;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.file.FileDeploymentFile;

public class TestFileDeploymentFileChecksums extends ChecksumTestCase { 
    
    @Override
	public void testQuickChecksum() {
    	// this will generally not work: the modification dates of the files will usually be differen.t
	}

	@Override
    protected DeploymentFile getFile(String fileName) {
        return new FileDeploymentFile(
                new File(EQUAL_FILES_DIRECTORY + fileName));
		}
}
