package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentFile;

public class FallbackDiscoverer implements FileDiscoverer {
    private static final Logger logger =
        Logger.getLogger(FallbackDiscoverer.class.getName());

    private FileDiscoverer primaryDiscoverer;
    private FileDiscoverer secondaryDiscoverer;

    public FallbackDiscoverer(
            FileDiscoverer primaryDiscoverer,
            FileDiscoverer secondaryDiscoverer) {
        this.primaryDiscoverer = primaryDiscoverer;
        this.secondaryDiscoverer = secondaryDiscoverer;
    }

    public List<DeploymentFile> getFilesToImport(File dir)
            throws NotApplicableException {
        List<DeploymentFile> result = null;
        try {
            result = primaryDiscoverer.getFilesToImport(dir);
        } catch (NotApplicableException e) {
            logger.log(Level.INFO, "Cannot apply discovery strategy " + primaryDiscoverer + ": " + e.getMessage());
        }

        if (result == null) {
            result = secondaryDiscoverer.getFilesToImport(dir);
        }

        return result;
    }

}
