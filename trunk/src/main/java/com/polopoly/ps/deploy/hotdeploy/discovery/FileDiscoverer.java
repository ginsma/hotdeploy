package com.polopoly.ps.deploy.hotdeploy.discovery;

import java.util.List;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;


public interface FileDiscoverer {
    List<DeploymentFile> getFilesToImport() throws NotApplicableException;
}
