package com.polopoly.pcmd.tool;

import static com.polopoly.ps.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;

import java.util.List;

import com.polopoly.pcmd.tool.parameters.HotdeployValidateParameters;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.util.Plural;
import com.polopoly.ps.deploy.xml.consistency.VerifyResult;
import com.polopoly.ps.deploy.xml.consistency.XMLConsistencyVerifier;
import com.polopoly.ps.deploy.xml.present.PresentFileReader;
import com.polopoly.ps.pcmd.tool.Tool;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.exception.NoSuchExternalIdException;


public class HotdeployValidateTool implements Tool<HotdeployValidateParameters> {

	public HotdeployValidateParameters createParameters() {
		return new HotdeployValidateParameters();
	}

	public void execute(final PolopolyContext context,
			final HotdeployValidateParameters parameters) {
		List<DeploymentFile> files = parameters.discoverFiles();

		System.out.println("Validating " + Plural.count(files, "file") + "...");

		XMLConsistencyVerifier verifier = new XMLConsistencyVerifier(files) {

			@Override
			protected boolean isPresentContent(String externalId) {
				if (super.isPresentContent(externalId)) {
					return true;
				}

				if (parameters.isDatabaseIsPresent()
						&& isPresentContentInDatabase(externalId)) {
					return true;
				}

				return false;
			}

			private boolean isPresentContentInDatabase(String externalId) {
				try {
					context.resolveExternalId(externalId);

					return true;
				} catch (NoSuchExternalIdException e) {
					return false;
				}
			}

			@Override
			protected boolean isPresentInputTemplate(String externalId) {
				if (super.isPresentInputTemplate(externalId)) {
					return true;
				}

				if (parameters.isDatabaseIsPresent()
						&& isPresentInputTemplateInDatabase(externalId)) {
					return true;
				}

				return false;
			}

			private boolean isPresentInputTemplateInDatabase(String externalId) {
				try {
					if (context.resolveExternalId(externalId).getMajor() == INPUT_TEMPLATE
							.getIntegerMajor()) {
						return true;
					} else {
						System.err
								.println("Content \""
										+ externalId
										+ "\" was present but was not an input template as expected.");
					}
				} catch (NoSuchExternalIdException e) {
					// nope.
				}

				return false;
			}

		};

		if (!parameters.isIgnorePresent()) {
			new PresentFileReader(parameters.getDirectory(), verifier).read();
		}

		verifier.setDiscoverResources(parameters.isSearchResources());
		verifier.setOnlyJarResources(parameters.isOnlyJarResources());

		verifier.setValidateClassReferences(parameters.isValidateClasses());

		if (parameters.getClassDirectory() != null) {
			verifier.addClassDirectory(parameters.getClassDirectory());
		}

		VerifyResult verifyResult = verifier.verify();

		if (verifyResult.isEverythingOk()) {
			System.out
					.println("The files are in consistent order and do not reference non-existing content.");
		} else {
			verifyResult.reportUsingLogging();
		}
	}

	public String getHelp() {
		return "Parses the content and template XML in the specified directory and "
				+ "reports on whether there is any content being imported in the wrong order "
				+ "or references to content that does exist.";
	}
}
