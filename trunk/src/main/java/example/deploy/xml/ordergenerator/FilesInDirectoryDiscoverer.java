package example.deploy.xml.ordergenerator;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.file.DeploymentFile;

public class FilesInDirectoryDiscoverer {
    private File rootDirectory;
    private Collection<FileDiscoverer> discoverers;

    private static final Logger logger =
        Logger.getLogger(FilesInDirectoryDiscoverer.class.getName());

    public FilesInDirectoryDiscoverer(Collection<FileDiscoverer> discoverers) {
        this.discoverers = discoverers;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public Set<DeploymentFile> getFiles() {
        Set<DeploymentFile> files = new HashSet<DeploymentFile>();

        for (FileDiscoverer discoverer : discoverers) {
            try {
                List<DeploymentFile> theseFiles = discoverer.getFilesToImport(rootDirectory);

                logger.log(Level.WARNING, discoverer + " identified " + theseFiles.size() + " file(s) to verify.");

                files.addAll(theseFiles);
            } catch (NotApplicableException e) {
                logger.log(Level.INFO, "Cannot apply discovery strategy " + discoverer + ": " + e.getMessage());
            }
        }

        return files;
    }
}
