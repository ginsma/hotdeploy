package com.polopoly.ps.deploy.hotdeploy.file;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.polopoly.ps.deploy.hotdeploy.discovery.NoDeploymentFileException;


public class DeploymentObjectFromUrlCalculator {
	private URL resource;

	public DeploymentObjectFromUrlCalculator(URL resource) {
		this.resource = resource;
	}

	public DeploymentObject toDeploymentObject()
			throws NoDeploymentFileException {
		if (isNormalFile(resource)) {
			return normalFileUrltoDeploymentFile(resource);
		} else if (isFileInJar(resource)) {
			return fileInJarToDeploymentFile(resource);
		} else {
			throw new NoDeploymentFileException(
					"The file "
							+ resource
							+ " does not seem to be on the local file system. Cannot deploy from it.");
		}
	}

	private boolean isNormalFile(URL resource) {
		return resource.getProtocol().equals("file");
	}

	private boolean isFileInJar(URL resource) {
		return resource.getProtocol().equals("jar");
	}

	private FileDeploymentFile normalFileUrltoDeploymentFile(URL resource) {
		String path = resource.getPath().replace("%20", " ");

		File file = new File(path);

		return new FileDeploymentFile(file);
	}

	private DeploymentObject fileInJarToDeploymentFile(URL resource)
			throws NoDeploymentFileException {
		String urlString = resource.getPath().replace("%20", " ");
		boolean isFile = urlString.startsWith("file:");

		if (isFile) {
			String fileUrlString = urlString.substring(5);

			int i = fileUrlString.indexOf(".jar!");

			if (i != -1) {
				String jarFileName = fileUrlString.substring(0, i + 4);

				try {
					JarFile jarFile = new JarFile(jarFileName);

					String fileName = fileUrlString.substring(i + 5);

					return getFileInJar(jarFile, fileName);
				} catch (IOException e) {
					throw new NoDeploymentFileException("While reading "
							+ jarFileName + ": " + e.getMessage(), e);
				}
			} else {
				return new FileDeploymentDirectory(
						new File(fileUrlString).getParentFile());
			}
		} else {
			throw new NoDeploymentFileException(
					"The JAR file "
							+ resource
							+ " does not seem to be on the local file system. Cannot deploy from it.");
		}
	}

	private DeploymentObject getFileInJar(JarFile file, String fileName)
			throws NoDeploymentFileException {
		if (fileName.equals("/")) {
			return new JarDeploymentRoot(file);
		} else {
			ZipEntry entry = file.getEntry(fileName.substring(1));

			if (entry == null) {
				throw new NoDeploymentFileException(
						"There seems to be a file called \"" + fileName
								+ "\" in the JAR " + file.getName()
								+ ", but it could not be fetched.");
			} else if (entry.isDirectory()) {
				return new JarDeploymentDirectory(file, entry);
			} else {
				return new JarDeploymentFile(file, entry);
			}
		}
	}

}

