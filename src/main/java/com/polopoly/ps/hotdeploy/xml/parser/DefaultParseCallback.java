package com.polopoly.ps.hotdeploy.xml.parser;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public class DefaultParseCallback implements ParseCallback {

	@Override
	public void contentFound(ParseContext context, String externalId,
			Major major, String inputTemplate) {
	}

	@Override
	public void contentReferenceFound(ParseContext context, Major major,
			String externalId) {
	}

	@Override
	public void classReferenceFound(DeploymentFile file, String string) {
	}

}
