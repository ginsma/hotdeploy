package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.List;

import example.deploy.hotdeploy.file.DeploymentFile;

public class FallbackDiscoverer implements FileDiscoverer {

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
        List<DeploymentFile> result = primaryDiscoverer.getFilesToImport(dir);

        if (result.isEmpty()) {
            result = secondaryDiscoverer.getFilesToImport(dir);
        }

        return result;
    }

}
