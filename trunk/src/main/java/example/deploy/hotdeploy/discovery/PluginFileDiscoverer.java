package example.deploy.hotdeploy.discovery;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.DeploymentObject;
import example.deploy.hotdeploy.file.DeploymentObjectFromUrlCalculator;
import example.deploy.hotdeploy.file.JarDeploymentFile;

/**
 * Returns all XML files in Polopoly plugins. These are found by looking for all
 * JARs in the classpath whose name ends with "contentdata".
 * 
 * There are no dependencies between plugins.
 */
public class PluginFileDiscoverer implements FileDiscoverer {
	private static final Logger logger = Logger
			.getLogger(PluginFileDiscoverer.class.getName());

	public class NotAPluginException extends Exception {

		public NotAPluginException(NoDeploymentFileException e) {
			// TODO Auto-generated constructor stub
		}

		public NotAPluginException() {
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * We assume all plugins contain a manifest.
	 */
	private static final String FILE_THAT_IS_ALWAYS_IN_PLUGINS = "META-INF/MANIFEST.MF";

	private static final String PLUGIN_JAR_SUFFIX = "-contentdata";

	private static final String CONTENT_FILE_EXTENSION = ".xml";

	public List<DeploymentFile> getFilesToImport()
			throws NotApplicableException {
		List<DeploymentFile> result = new ArrayList<DeploymentFile>();

		ClassLoader classLoader = PluginFileDiscoverer.class.getClassLoader();

		try {
			Enumeration<URL> potentialPlugins = classLoader
					.getResources(FILE_THAT_IS_ALWAYS_IN_PLUGINS);

			while (potentialPlugins.hasMoreElements()) {
				try {
					URL potentialFileInPlugin = potentialPlugins.nextElement();

					result.addAll(getFilesToImport(getPluginRoot(potentialFileInPlugin)));
				} catch (NotAPluginException e) {
					// ok. try next potential plugin.
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return result;
	}

	private List<DeploymentFile> getFilesToImport(JarFile pluginRoot)
			throws NotAPluginException {
		List<DeploymentFile> result = new ArrayList<DeploymentFile>();

		Enumeration<JarEntry> entries = pluginRoot.entries();

		while (entries.hasMoreElements()) {
			try {
				JarEntry entry = entries.nextElement();

				result.add(toDeploymentFile(pluginRoot, entry));
			} catch (NoDeploymentFileException e) {
				// try next entry.
			}
		}

		Collections.sort(result, new Comparator<DeploymentFile>() {
			public int compare(DeploymentFile f1, DeploymentFile f2) {
				return f1.getName().compareTo(f1.getName());
			}
		});

		return result;
	}

	private DeploymentFile toDeploymentFile(JarFile jarFile, JarEntry entry)
			throws NoDeploymentFileException {
		if (entry.isDirectory()) {
			throw new NoDeploymentFileException();
		}

		if (!entry.getName().endsWith(CONTENT_FILE_EXTENSION)) {
			throw new NoDeploymentFileException();
		}

		return new JarDeploymentFile(jarFile, entry);
	}

	private JarFile getPluginRoot(URL potentialFileInPlugin)
			throws NotAPluginException {
		try {
			DeploymentObject deploymentObject = toDeploymentObject(potentialFileInPlugin);

			if (deploymentObject instanceof JarDeploymentFile) {
				JarFile jarFile = ((JarDeploymentFile) deploymentObject)
						.getJarFile();

				if (!jarFile.getName().contains(PLUGIN_JAR_SUFFIX)) {
					throw new NotAPluginException();
				}

				return jarFile;
			}

			throw new NotAPluginException();
		} catch (NoDeploymentFileException e) {
			throw new NotAPluginException(e);
		}
	}

	private DeploymentObject toDeploymentObject(URL url)
			throws NoDeploymentFileException {
		return new DeploymentObjectFromUrlCalculator(url).toDeploymentObject();
	}

}
