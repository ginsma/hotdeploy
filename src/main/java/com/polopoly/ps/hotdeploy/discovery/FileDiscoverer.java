package com.polopoly.ps.hotdeploy.discovery;

import java.util.List;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;


public interface FileDiscoverer {
    List<DeploymentFile> getFilesToImport() throws NotApplicableException;
}
