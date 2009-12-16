package example.deploy.hotdeploy.discovery;

import java.util.List;

import example.deploy.hotdeploy.file.DeploymentFile;

public interface FileCollector {

    void collect(List<DeploymentFile> result);

}
