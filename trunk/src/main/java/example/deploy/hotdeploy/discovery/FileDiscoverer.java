package example.deploy.hotdeploy.discovery;

import java.util.List;

import example.deploy.hotdeploy.file.DeploymentFile;

public interface FileDiscoverer {
    List<DeploymentFile> getFilesToImport() throws NotApplicableException;
}
