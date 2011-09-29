package com.polopoly.ps.hotdeploy.deployer;

import static com.polopoly.ps.hotdeploy.util.Plural.count;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public class DeploymentResult {
	private static final Logger logger = Logger
			.getLogger(DeploymentResult.class.getName());

	private static class DeployResult {
		private final DeploymentFile file;
		private final List<String> messages = new ArrayList<String>();

		public DeployResult(DeploymentFile file) {
			this.file = file;
		}

		public void addMessage(String message) {
			messages.add(message);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(file.getName()).append(":");
			for (String message : messages) {
				sb.append("\n    ").append(message);
			}

			return sb.toString();
		}
	}

	private int filesToDeployCount = -1;

	private final Set<DeploymentFile> successfulResults = new HashSet<DeploymentFile>();
	private final Map<DeploymentFile, DeployResult> failedResults = new HashMap<DeploymentFile, DeployResult>();
	private final Map<DeploymentFile, DeployResult> partialResults = new HashMap<DeploymentFile, DeployResult>();

	public DeploymentResult() {
	}

	public void setFilesToDeploy(Collection<DeploymentFile> filesToDeploy) {
		this.filesToDeployCount = filesToDeploy.size();
	}

	public void reportSuccessful(DeploymentFile file) {
		successfulResults.add(file);
	}

	public void reportFailed(DeploymentFile file, String message) {
		addMessage(failedResults, file, message);
	}

	void reportPartial(DeploymentFile file, String message) {
		addMessage(partialResults, file, message);
	}

	private void addMessage(Map<DeploymentFile, DeployResult> map,
			DeploymentFile file, String message) {
		DeployResult result = map.get(file);

		if (result == null) {
			result = new DeployResult(file);
			map.put(file, result);
		}

		result.addMessage(message);
	}

	public int getSuccessfulFilesCount() {
		return successfulResults.size();
	}

	public int getFailedFilesCount() {
		return failedResults.size();
	}

	public int getPartialFilesCount() {
		return partialResults.size();
	}

	public int getTotalReportedCount() {
		return getSuccessfulFilesCount() + getFailedFilesCount()
				+ getPartialFilesCount();
	}

	public boolean hasIssues() {
		return !failedResults.isEmpty() || !partialResults.isEmpty();
	}

	public boolean hasFailingFiles() {
		return !failedResults.isEmpty();
	}

	public String getSuccessfulResultString() {
		int cnt = getSuccessfulFilesCount();
		return count(cnt, "file") + " " + (cnt == 1 ? "was" : "were")
				+ " successfully imported.";
	}

	public String getFailedResultString() {
		StringBuilder sb = new StringBuilder();
		sb.append(count(getFailedFilesCount(), "file") + " failed to import:");
		for (DeployResult result : failedResults.values()) {
			sb.append("\n  ").append(result);
		}
		return sb.toString();
	}

	public String getPartialResultString() {
		StringBuilder sb = new StringBuilder();
		sb.append(count(getPartialFilesCount(), "file") + " "
				+ (getPartialFilesCount() == 1 ? "was" : "were")
				+ " only partially imported:");
		for (DeployResult result : partialResults.values()) {
			sb.append("\n  ").append(result);
		}
		return sb.toString();
	}

	public void logResults() {
		if (filesToDeployCount > 0) {
			int unmodifiedFiles = filesToDeployCount - getTotalReportedCount();

			if (unmodifiedFiles > 0) {
				logger.log(Level.INFO, count(unmodifiedFiles, "file")
						+ " had not been modified and "
						+ (unmodifiedFiles == 1 ? "was" : "were")
						+ " not imported. ");
			}
		}

		logger.log(Level.INFO, getSuccessfulResultString());

		if (!failedResults.isEmpty()) {
			logger.log(Level.WARNING, getFailedResultString());
		}
		if (!partialResults.isEmpty()) {
			logger.log(Level.WARNING, getPartialResultString());
		}
	}
}
