package com.polopoly.ps.hotdeploy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.ImportOrderOrDirectoryFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.PluginFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.ResourceFileDiscoverer;


public class DiscovererMainClass {
	private boolean discoverResources = true;
	private boolean onlyJarResources = true;

	private List<File> directories = new ArrayList<File>();

	protected Iterable<FileDiscoverer> getDiscoverers() {
		List<FileDiscoverer> discoverers = new ArrayList<FileDiscoverer>();

		if (discoverResources) {
			discoverers.add(new ResourceFileDiscoverer(onlyJarResources));
			discoverers.add(new PluginFileDiscoverer());
		}

		for (File directory : directories) {
			discoverers
					.add(new ImportOrderOrDirectoryFileDiscoverer(directory));
		}

		return discoverers;
	}

	public Collection<File> getDirectories() {
		return directories;
	}

	public void addDirectoryName(String directoryName) {
		File directory = new File(directoryName);
		addDirectory(directory);
	}

	protected void addDirectory(File directory) {
		directories.add(directory);
	}

	protected void validateDirectories() {
		for (File directory : directories) {
			if (!directory.exists() || !directory.canRead()
					|| !directory.isDirectory()) {
				System.err.println(directory.getAbsolutePath()
						+ " is not a readable directory. Cannot import it.");
			}
		}
	}

	protected String getDirectoryString() {
		StringBuffer result = new StringBuffer(100);

		boolean first = true;

		for (File directory : directories) {
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}

			result.append(directory.getAbsolutePath());
		}

		return result.toString();
	}

	public boolean isDiscoverResources() {
		return discoverResources;
	}

	public void setDiscoverResources(boolean discoverResources) {
		this.discoverResources = discoverResources;
	}

	public boolean isOnlyJarResources() {
		return onlyJarResources;
	}

	public void setOnlyJarResources(boolean onlyJarResources) {
		this.onlyJarResources = onlyJarResources;
	}
}
