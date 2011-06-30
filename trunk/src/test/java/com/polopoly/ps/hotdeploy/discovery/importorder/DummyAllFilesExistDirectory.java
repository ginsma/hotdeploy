package com.polopoly.ps.hotdeploy.discovery.importorder;

import java.io.FileNotFoundException;

import com.polopoly.ps.hotdeploy.file.DeploymentDirectory;
import com.polopoly.ps.hotdeploy.file.DeploymentObject;
import com.polopoly.ps.hotdeploy.state.DummyDeploymentFile;


public class DummyAllFilesExistDirectory implements DeploymentDirectory {

    public boolean exists() {
        return true;
    }

    public DeploymentObject getFile(String fileName)
            throws FileNotFoundException {
        return new DummyDeploymentFile(fileName);
    }

    public DeploymentObject[] listFiles() {
        return new DeploymentObject[0];
    }

    public String getName() {
        return "dummyDirectory";
    }

    public String getRelativeName(DeploymentObject deploymentObject) {
        return deploymentObject.getName();
    }

    public boolean imports(DeploymentObject object) {
        return false;
    }

}
