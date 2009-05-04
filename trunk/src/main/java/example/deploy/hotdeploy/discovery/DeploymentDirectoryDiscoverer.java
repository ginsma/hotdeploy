package example.deploy.hotdeploy.discovery;

import static example.deploy.hotdeploy.discovery.ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.JarDeploymentDirectory;
import example.deploy.hotdeploy.file.JarDeploymentRoot;

public class DeploymentDirectoryDiscoverer {
    public interface FileFinder {
        Collection<DeploymentDirectory> getDirectories(String string) throws FileNotFoundException;
    }

    private static final Logger logger =
        Logger.getLogger(DeploymentDirectoryDiscoverer.class.getName());

    /**
     * The directories content may lie in. Scanned in this order.
     */
    private Collection<String> searchDirectories = new ArrayList<String>();

    private FileFinder fileFinder;

    public DeploymentDirectoryDiscoverer(final DeploymentDirectory directory,
            Collection<String> directories) {
        this(directories);

        fileFinder = new FileFinder() {
            public Collection<DeploymentDirectory> getDirectories(String directoryName)
                    throws FileNotFoundException {
                if (".".equals(directoryName)) {
                    directory.getFile(IMPORT_ORDER_FILE_NAME);

                    return Collections.singletonList(directory);
                }
                else {
                    directory.getFile(directoryName + "/" + IMPORT_ORDER_FILE_NAME);

                    return Collections.singletonList((DeploymentDirectory) directory.getFile(directoryName));
                }
            }

            @Override
            public String toString() {
                return directory.toString();
            }
        };
    }

    public DeploymentDirectoryDiscoverer(final File projectRoot, Collection<String> directories) {
        this(new FileDeploymentDirectory(projectRoot), directories);
    }

    public DeploymentDirectoryDiscoverer(final ClassLoader classLoader, Collection<String> directories) {
        this(directories);

        fileFinder = new FileFinder() {
            public Collection<DeploymentDirectory> getDirectories(String directory) throws FileNotFoundException {
                String path = (".".equals(directory) ? "" : directory + "/");
                String resourceName = path + IMPORT_ORDER_FILE_NAME;

                Enumeration<URL> resources;
                try {
                    resources = classLoader.getResources(resourceName);
                } catch (IOException e) {
                    throw new FileNotFoundException("While fetching resource " + resourceName + ": " + e);
                }

                List<DeploymentDirectory> result = new ArrayList<DeploymentDirectory>();

                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();

                    if (resource.getProtocol().equals("file")) {
                        addFile(resource, result);
                    }
                    else if (resource.getProtocol().equals("jar")) {
                        addJar(directory, resource, result);
                    }
                    else {
                        addRemote(resource);
                    }
                }

                if (result.isEmpty()) {
                    throw new FileNotFoundException(directory + " (as a resource)");
                }

                return result;
            }

            private void addRemote(URL resource) {
                logger.log(Level.WARNING, "The file " + resource + " does not seem to be on the local file system. Cannot deploy from it.");
            }

            private void addFile(URL resource, List<DeploymentDirectory> result) {
                File file = new File(resource.getPath());

                if (!file.isDirectory()) {
                    file = file.getParentFile();
                }

                result.add(new FileDeploymentDirectory(file));
            }

            private void addJar(String directory, URL resource,
                    List<DeploymentDirectory> result) {
                String urlString = resource.getPath();

                boolean isFile = urlString.startsWith("file:");

                if (isFile) {
                    String fileUrlString = urlString.substring(5);

                    int i = fileUrlString.indexOf(".jar!");

                    if (i != -1) {
                        String jarFileName = fileUrlString.substring(0, i+4);

                        try {
                            JarFile jarFile = new JarFile(jarFileName);

                            addDirectoryInJar(jarFile, directory, result);
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "While reading " + jarFileName + ": " + e.getMessage(), e);
                        }
                    }
                    else {
                        result.add(new FileDeploymentDirectory(new File(fileUrlString).getParentFile()));
                    }
                }
                else {
                    logger.log(Level.WARNING, "The JAR file " + resource + " does not seem to be on the local file system. Cannot deploy from it.");
                }
            }

            private void addDirectoryInJar(JarFile file, String directory,
                    List<DeploymentDirectory> result) {
                if (directory.equals(".")) {
                    result.add(new JarDeploymentRoot(file));
                }
                else {
                    ZipEntry entry = file.getEntry(directory + "/");

                    if (entry != null) {
                        result.add(new JarDeploymentDirectory(file, entry));
                    }
                    else {
                        logger.log(Level.WARNING, "There seems to be a directory called \"" + directory +
                                "\" in the file " + file.getName() + ", but it could not be fetched.");
                    }
                }
            }

            @Override
            public String toString() {
                if (classLoader instanceof URLClassLoader) {
                    return Arrays.toString(((URLClassLoader) classLoader).getURLs());
                }

                return "unknown class loader";
            }
        };
    }

    public DeploymentDirectoryDiscoverer(Collection<String> searchDirectories) {
        this.searchDirectories = searchDirectories;
    }

    public Collection<DeploymentDirectory> getDiscoveredDirectories() {
        Collection<DeploymentDirectory> result = new ArrayList<DeploymentDirectory>();

        for (String directory : searchDirectories) {
            try {
                result.addAll(fileFinder.getDirectories(directory));
            } catch (FileNotFoundException e) {
                logger.log(Level.FINE, "Could not find possible hotdeploy directory: " + e.getMessage());
            }
        }

        if (result.isEmpty()) {
            logger.log(Level.WARNING, "None of the standard hotdeploy directories " +
                searchDirectories + " could be found (relative to " + fileFinder +").");

            result.add(new FileDeploymentDirectory(new File(".")));
        }

        return result;
    }
}
