package com.polopoly.pcmd.bootstrap;

import static example.deploy.hotdeploy.util.Plural.plural;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import com.polopoly.pcmd.tool.parameters.HotdeployBootstrapParameters;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.xml.bootstrap.Bootstrap;
import example.deploy.xml.bootstrap.BootstrapContent;
import example.deploy.xml.bootstrap.BootstrapFileWriter;
import example.deploy.xml.bootstrap.BootstrapGenerator;
import example.deploy.xml.parser.ContentXmlParser;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.present.PresentFileReader;

public class BootstrapFileGenerator {
	public static final String BOOTSTRAP_FILE_NAME = "bootstrap.xml";

	private boolean force = false;
	private boolean bootstrapNonCreated = false;
	private boolean ignorePresent = false;
	private DeploymentFileParser parser = new ContentXmlParser();

	static void bootstrapNonCreated(Bootstrap bootstrap,
			List<BootstrapContent> notBootstrapped) {
		for (BootstrapContent bootstrapContent : notBootstrapped) {
			bootstrap.add(bootstrapContent);
		}
	}

	private void writeBootstrapToFile(Bootstrap bootstrap, File bootstrapFile) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(
					bootstrapFile, false), "UTF-8");
			new BootstrapFileWriter(bootstrap).write(writer);
			writer.close();
		} catch (IOException e) {
			System.err.println("Could not write to the new bootstrap file "
					+ bootstrapFile.getAbsolutePath() + ": " + e.getMessage());
			System.exit(1);
		}

		System.out.println("Wrote bootstrap content to "
				+ bootstrapFile.getAbsolutePath() + ".");
	}

	static File getBootstrapFile(File directory) {
		File bootstrapFile = new File(directory, BOOTSTRAP_FILE_NAME);

		return bootstrapFile;
	}

	void logReferencedButNotCreated(List<BootstrapContent> notBootstrapped) {
		System.out
				.println(notBootstrapped.size()
						+ " content object"
						+ plural(notBootstrapped.size())
						+ " were referenced but never created within the files to deploy. Use --"
						+ HotdeployBootstrapParameters.BOOTSTRAP_NON_CREATED_PARAMETER
						+ " to bootstrap these objects too.");

		if (notBootstrapped.size() < 10) {
			System.out.println("These are the objects: " + notBootstrapped);
			System.out
					.println("Use the hotdeploy-find tool to find out where they are referenced.");
		} else {
			System.out.println("Some of these objects are "
					+ notBootstrapped.subList(0, 9));
			System.out
					.println("Use the hotdeploy-validate tool to see the full list and find out where they are referenced.");
		}
	}

	private static void logBootstrapResult(Bootstrap bootstrap) {
		System.err
				.println(bootstrap.size() + " content object"
						+ (bootstrap.size() != 1 ? "s" : "")
						+ " needed bootstrapping.");
	}

	private void checkFileDoesNotExist(File bootstrapFile) {
		if (bootstrapFile.exists()) {
			System.err.println("The bootstrap file "
					+ bootstrapFile.getAbsolutePath() + " already exists. "
					+ "Use --" + HotdeployBootstrapParameters.FORCE_PARAMETER
					+ " to overwrite.");
			System.exit(1);
		}
	}

	public void generateBootstrap(File directory,
			List<DeploymentFile> deploymentFiles) {
		System.err.println(deploymentFiles.size() + " content XML file"
				+ plural(deploymentFiles) + " to generate bootstrap for.");

		File bootstrapFile = getBootstrapFile(directory);
		DeploymentFile bootstrapFileAsDeploymentFile = new FileDeploymentFile(
				bootstrapFile);

		if (force) {
			deploymentFiles.remove(bootstrapFileAsDeploymentFile);
		}

		Bootstrap bootstrap = new BootstrapGenerator(parser)
				.generateBootstrap(deploymentFiles);

		if (!isIgnorePresent()) {
			// eliminate present files.
			new PresentFileReader(directory, bootstrap).read();
		}

		List<BootstrapContent> notBootstrapped = bootstrap
				.getNeverCreatedButReferenced();

		if (!notBootstrapped.isEmpty()) {
			if (bootstrapNonCreated) {
				bootstrapNonCreated(bootstrap, notBootstrapped);
			} else {
				logReferencedButNotCreated(notBootstrapped);
			}
		}

		Collections.sort(bootstrap);

		logBootstrapResult(bootstrap);

		if (!bootstrap.isEmpty()) {
			if (!force) {
				checkFileDoesNotExist(bootstrapFile);
			}

			writeBootstrapToFile(bootstrap, bootstrapFile);

			deploymentFiles.add(0, bootstrapFileAsDeploymentFile);
			new BootstrapToImportOrderAdder(bootstrapFile)
					.addBootstrapToImportOrderIfItExists();
		}
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public boolean isBootstrapNonCreated() {
		return bootstrapNonCreated;
	}

	public void setBootstrapNonCreated(boolean bootstrapNonCreated) {
		this.bootstrapNonCreated = bootstrapNonCreated;
	}

	public DeploymentFileParser getParser() {
		return parser;
	}

	public void setParser(DeploymentFileParser parser) {
		this.parser = parser;
	}

	public void setIgnorePresent(boolean ignorePresent) {
		this.ignorePresent = ignorePresent;
	}

	public boolean isIgnorePresent() {
		return ignorePresent;
	}

}
