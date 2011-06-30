package com.polopoly.ps.hotdeploy.discovery;

import java.io.File;

import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;


public class ImportOrderOrDirectoryFileDiscoverer extends FallbackDiscoverer {

    public ImportOrderOrDirectoryFileDiscoverer(File directory) {
      super(new ImportOrderFileDiscoverer(new FileDeploymentDirectory(directory)),
            new DirectoryFileDiscoverer(new FileDeploymentDirectory(directory)));
    }

}
