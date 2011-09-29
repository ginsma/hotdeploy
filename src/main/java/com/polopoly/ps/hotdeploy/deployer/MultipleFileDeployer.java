package com.polopoly.ps.hotdeploy.deployer;

import static com.polopoly.ps.hotdeploy.util.Plural.count;
import static com.polopoly.ps.hotdeploy.util.Plural.plural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.state.CouldNotUpdateStateException;
import com.polopoly.ps.hotdeploy.state.DirectoryState;

public class MultipleFileDeployer {
	private static final Logger logger = Logger
			.getLogger(MultipleFileDeployer.class.getName());
	private boolean failFast;
	private DirectoryState directoryState;
	private ClassLoader oldClassLoader;

	private Set<DeploymentFile> failedFiles = new HashSet<DeploymentFile>();
	private Set<DeploymentFile> successfulFiles = new HashSet<DeploymentFile>();
	private SingleFileDeployer deployer;

	public MultipleFileDeployer(SingleFileDeployer deployer,
			DirectoryState directoryState) {
		this(deployer, directoryState, false);
	}

	public MultipleFileDeployer(SingleFileDeployer deployer,
			DirectoryState directoryState, boolean failFast) {
		this.failFast = failFast;
		this.deployer = deployer;
		this.directoryState = directoryState;
	}

	private void logFileChanged(DeploymentFile fileToImport) {
		logger.log(Level.INFO, fileToImport
				+ " had changed on disk. Importing it.");
	}

	private void logFiledUnchanged(DeploymentFile fileToImport) {
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, fileToImport
					+ " had not changed. Skipping it.");
		}
	}

	private void swapClassLoader() {
		Thread currentThread = Thread.currentThread();

		oldClassLoader = currentThread.getContextClassLoader();

		currentThread.setContextClassLoader(MultipleFileDeployer.class
				.getClassLoader());
	}

	private void restoreClassLoader() {
		if (oldClassLoader != null) {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
	}

	public Set<DeploymentFile> deploy(Collection<DeploymentFile> filesToImport,
			DeploymentResult result) throws FatalDeployException {
		logger.log(
				Level.INFO,
				"Found "
						+ filesToImport.size()
						+ " content file(s) in total. Importing those that have been modified...");

		try {
			deployer.prepare();

			swapClassLoader();

			for (DeploymentFile fileToImport : filesToImport) {
				if (!directoryState.hasFileChanged(fileToImport)) {
					logFiledUnchanged(fileToImport);
					continue;
				}

				logFileChanged(fileToImport);

				boolean success = deployer
						.importAndHandleException(fileToImport);

				if (success) {
					successfulFiles.add(fileToImport);
				} else {
					failedFiles.add(fileToImport);
				}

				directoryState.reset(fileToImport, !success);

				if (!success && failFast) {
					break;
				}
			}
		} catch (ParserConfigurationException e) {
			throw new FatalDeployException("Failed to create importer: "
					+ e.getMessage(), e);
		} finally {
			restoreClassLoader();
		}

		try {
			directoryState.persist();
		} catch (CouldNotUpdateStateException e) {
			throw new FatalDeployException(
					"Could not record deployment state after deploy: "
							+ e.getMessage(), e);
		}

		logResult(filesToImport);

		return failedFiles;
	}

	private void logResult(Collection<DeploymentFile> filesToImport) {
		logger.log(Level.INFO, getResultMessage(filesToImport));
	}

	public Set<DeploymentFile> getFailedFiles() {
		return failedFiles;
	}

	public String getResultMessage(Collection<DeploymentFile> filesToImport) {
		int unmodifiedFiles = filesToImport.size() - successfulFiles.size()
				- failedFiles.size();

		StringBuffer result = new StringBuffer(100);

		if (!successfulFiles.isEmpty()) {
			result.append(count(successfulFiles, "file")
					+ " imported successfully. ");
		}

		if (unmodifiedFiles > 0) {
			result.append(count(unmodifiedFiles, "file")
					+ " had not been modified and "
					+ (unmodifiedFiles == 1 ? "was" : "were")
					+ " not imported. ");
		}

		if (!failedFiles.isEmpty()) {
			result.append(count(failedFiles, "file") + " failed during import.");
		}

		return result.toString();
	}

	public Set<DeploymentFile> discoverAndDeploy(
			Iterable<FileDiscoverer> discoverers, DeploymentResult result)
			throws FatalDeployException {
		List<DeploymentFile> files = new ArrayList<DeploymentFile>();

		for (FileDiscoverer discoverer : discoverers) {
			try {
				files.addAll(discoverer.getFilesToImport());
			} catch (NotApplicableException e) {
				logger.log(Level.INFO, "Cannot apply discovery strategy "
						+ discoverer + ": " + e.getMessage());
			}
		}

		if (files.isEmpty()) {
			logger.log(Level.INFO,
					"Could not find any content files to import.");

			return Collections.emptySet();
		}

		return deploy(files, result);
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	public boolean isAllFilesUnchanged() {
		return successfulFiles.isEmpty() && failedFiles.isEmpty();
	}

	public void printStartMessage(List<DeploymentFile> filesToDeploy) {
		System.out.println(filesToDeploy.size() + " content file"
				+ plural(filesToDeploy.size()) + " found in the classpath.");
	}

	public void printResultMessage(String string, List<DeploymentFile> files) {
		System.out.println(string + getResultMessage(files));
	}
}
