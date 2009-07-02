package example.deploy.hotdeploy.discovery;

import java.io.File;

import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;

public class ImportOrderOrDirectoryFileDiscoverer extends FallbackDiscoverer {

    public ImportOrderOrDirectoryFileDiscoverer(File directory) {
      super(new ImportOrderFileDiscoverer(new FileDeploymentDirectory(directory)),
            new DirectoryFileDiscoverer(new FileDeploymentDirectory(directory)));
    }

}
