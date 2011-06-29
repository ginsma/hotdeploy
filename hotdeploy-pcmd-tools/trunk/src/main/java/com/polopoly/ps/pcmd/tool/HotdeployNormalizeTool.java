package com.polopoly.ps.pcmd.tool;

import java.io.File;
import java.util.List;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.xml.normalize.NormalizeElementGatherer;
import com.polopoly.ps.deploy.xml.parser.ContentXmlParser;
import com.polopoly.ps.pcmd.tool.Tool;
import com.polopoly.ps.pcmd.tool.parameters.HotdeployNormalizeParameters;
import com.polopoly.util.client.PolopolyContext;


public class HotdeployNormalizeTool implements
		Tool<HotdeployNormalizeParameters> {

	private static final String BOOTSTRAP_FILE_NAME = File.separator
			+ "bootstrap.xml";

	public HotdeployNormalizeParameters createParameters() {
		return new HotdeployNormalizeParameters();
	}

	public void execute(PolopolyContext context,
			HotdeployNormalizeParameters parameters) {

		List<DeploymentFile> files = parameters.discoverFiles();

		for (DeploymentFile file : files) {
			if (isBootstrap(file)) {
				System.out.println("Skipping bootstrap file " + file + ".");
			} else {
				new ContentXmlParser()
						.parse(file,
								new NormalizeElementGatherer(parameters
										.getToDirectory(), context
										.getPolicyCMServer()));
			}
		}
	}

	private boolean isBootstrap(DeploymentFile file) {
		return file.getName().endsWith(BOOTSTRAP_FILE_NAME);
	}

	public String getHelp() {
		return "Restructures the content XML into a separate files for each content or template defined.";
	}
}
