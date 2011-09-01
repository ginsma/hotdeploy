package com.polopoly.ps.hotdeploy.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrder;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderVertex;
import com.polopoly.ps.hotdeploy.file.DeploymentDirectory;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.topologicalsort.TopologicalSorter;


public class ResourceFileDiscoverer implements FileDiscoverer {
	private static final String HOTDEPLOY_DEPENDENCY_NAME = "hotdeploy";
	private static final Logger logger = Logger
			.getLogger(ResourceFileDiscoverer.class.getName());
	private boolean onlyJarResources;

	public ResourceFileDiscoverer(boolean onlyJarResources) {
		this.onlyJarResources = onlyJarResources;
	}

	public List<DeploymentFile> getFilesToImport()
			throws NotApplicableException {
		ClassLoader classLoader = getClass().getClassLoader();

		return getFilesToImport(classLoader);
	}

	private Collection<DeploymentDirectory> discoverDirectories(
			DeploymentDirectory directory) {
		return new DeploymentDirectoryDiscoverer(directory,
				DefaultDiscoveryDirectories.getDirectories())
				.getDiscoveredDirectories();
	}

	public List<DeploymentFile> getFilesToImport(ClassLoader classLoader)
			throws NotApplicableException {
		List<DeploymentFile> result = new ArrayList<DeploymentFile>();

		result.addAll(new PluginFileDiscoverer().getFilesToImport());

		if (!result.isEmpty()) {
			logger.log(Level.INFO, result.size()
					+ " file(s) to import in plugins.");
		}

		List<ImportOrder> foundImportOrderFiles = getImportOrderFiles(classLoader);

		if (foundImportOrderFiles.size() > 1) {
			logger.log(Level.INFO, "Deployment order is "
					+ foundImportOrderFiles + ".");
		}

		for (ImportOrder filesWithDependencies : foundImportOrderFiles) {
			result.addAll(filesWithDependencies);
		}

		return result;
	}

	public List<ImportOrder> getImportOrderFiles(ClassLoader classLoader) {
		Collection<DeploymentDirectory> potentialDirectories = new DeploymentDirectoryDiscoverer(
				classLoader, DefaultDiscoveryDirectories.getDirectories())
				.getDiscoveredDirectories();

		List<ImportOrder> foundImportOrderFiles = new ArrayList<ImportOrder>();

		for (DeploymentDirectory directory : potentialDirectories) {
			logger.log(Level.FINE, "Scanning " + directory
					+ " for content files.");

			if (onlyJarResources
					&& directory instanceof FileDeploymentDirectory) {
				logger.log(Level.FINE, "Skipping " + directory
						+ " since it's not in a JAR.");

				continue;
			}

			Collection<DeploymentDirectory> subDirectories = discoverDirectories(directory);

			// there's only going to be one directory here.
			for (DeploymentDirectory subDirectory : subDirectories) {
				try {
					ImportOrder thisResult = new ImportOrderFileDiscoverer(
							subDirectory).getFilesToImport();

					if (!foundImportOrderFiles.contains(thisResult)) {
						logFilesFound(subDirectory, thisResult);
						foundImportOrderFiles.add(thisResult);
					}
				} catch (NotApplicableException e) {
					handleDirectoryNotApplicable(subDirectory, e);
				}
			}
		}

		Collections.sort(foundImportOrderFiles, new Comparator<ImportOrder>() {
			public int compare(ImportOrder o1, ImportOrder o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		topologicalSort(foundImportOrderFiles);

		return foundImportOrderFiles;
	}

	private boolean isHotDeploy(ImportOrder importOrderFile) {
		return importOrderFile.calculateDependencyName().equals(
				HOTDEPLOY_DEPENDENCY_NAME);
	}

	private void topologicalSort(List<ImportOrder> foundImportOrderFiles) {
		List<ImportOrderVertex> vertexes = new ArrayList<ImportOrderVertex>(
				foundImportOrderFiles.size());

		for (int i = 0; i < foundImportOrderFiles.size(); i++) {
			vertexes.add(new ImportOrderVertex(foundImportOrderFiles.get(i)));
		}

		for (int atIndex = 0; atIndex < vertexes.size(); atIndex++) {
			ImportOrderVertex atVertex = vertexes.get(atIndex);
			ImportOrder importOrderFile = atVertex.getImportOrderFile();

			// hotdeploy comes first
			if (isHotDeploy(importOrderFile)) {
				ImportOrderVertex hotdeployVertex = atVertex;

				for (ImportOrderVertex otherVertex : vertexes) {
					if (otherVertex != hotdeployVertex) {
						otherVertex.addDependency(hotdeployVertex);
					}
				}
			}

			// JAR files come before class directories
			if (isFileDirectory(importOrderFile)) {
				ImportOrderVertex classDirectoryVertex = atVertex;

				for (ImportOrderVertex otherVertex : vertexes) {
					if (!isFileDirectory(otherVertex.getImportOrderFile())) {
						classDirectoryVertex.addDependency(otherVertex);
					}
				}
			}

			for (String dependencyName : importOrderFile.getDependencies()) {
				ImportOrderVertex dependencyVertex = getVertexWithName(
						vertexes, dependencyName);

				if (dependencyVertex == null) {
					logUnknownDependencyReferenced(foundImportOrderFiles,
							importOrderFile, dependencyName);
				} else {
					atVertex.addDependency(dependencyVertex);
				}
			}
		}

		List<ImportOrderVertex> sortedOrder = new TopologicalSorter<ImportOrderVertex>(
				vertexes).sort();

		foundImportOrderFiles.clear();

		for (ImportOrderVertex importOrderVertex : sortedOrder) {
			foundImportOrderFiles.add(importOrderVertex.getImportOrderFile());
		}
	}

	private boolean isFileDirectory(ImportOrder importOrderFile) {
		return importOrderFile.getDirectory() instanceof FileDeploymentDirectory;
	}

	private ImportOrderVertex getVertexWithName(
			List<ImportOrderVertex> vertexes, String dependencyName) {
		for (int i = 0; i < vertexes.size(); i++) {
			ImportOrderVertex vertex = vertexes.get(i);
			if (vertex.getImportOrderFile().calculateDependencyName()
					.equals(dependencyName)) {
				return vertex;
			}
		}

		return null;
	}

	private void logUnknownDependencyReferenced(
			List<ImportOrder> foundImportOrderFiles,
			ImportOrder importOrderFile, String dependencyName) {
		logger.log(Level.WARNING, "The dependency \"" + dependencyName
				+ "\" declared by " + importOrderFile
				+ " was unknown. Known dependencies are "
				+ getDependencyNameString(foundImportOrderFiles) + ".");
	}

	private String getDependencyNameString(
			List<ImportOrder> foundImportOrderFiles) {
		StringBuffer result = new StringBuffer(100);

		boolean first = true;

		for (ImportOrder importOrderFile : foundImportOrderFiles) {
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}

			result.append(importOrderFile.calculateDependencyName());
		}

		return result.toString();
	}

	private void logFilesFound(DeploymentDirectory directory,
			ImportOrder filesFound) {
		String dependencies;

		if (filesFound.getDependencies().isEmpty()) {
			dependencies = "It has no dependencies.";
		} else {
			dependencies = "It depends on " + filesFound.getDependencies()
					+ ".";
		}

		logger.log(Level.INFO, "Found " + filesFound.size()
				+ " content file(s) under " + directory + ". " + dependencies);
	}

	private void handleDirectoryNotApplicable(DeploymentDirectory directory,
			NotApplicableException e) {
		logger.log(
				Level.WARNING,
				"Thought directory " + directory
						+ " would have content, but could not find it: "
						+ e.getMessage());
	}

	@Override
	public String toString() {
		return "resource file discoverer";
	}
}
