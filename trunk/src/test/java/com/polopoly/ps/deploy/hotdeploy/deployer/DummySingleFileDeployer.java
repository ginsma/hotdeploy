package com.polopoly.ps.deploy.hotdeploy.deployer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import com.polopoly.ps.deploy.hotdeploy.deployer.FatalDeployException;
import com.polopoly.ps.deploy.hotdeploy.deployer.SingleFileDeployer;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;

import junit.framework.Assert;

public class DummySingleFileDeployer implements SingleFileDeployer {
    private Set<DeploymentFile> importedFiles = new HashSet<DeploymentFile>();
    private Set<DeploymentFile> failTheseFiles = new HashSet<DeploymentFile>();

    public boolean importAndHandleException(DeploymentFile fileToImport)
            throws FatalDeployException {
        if (failTheseFiles.contains(fileToImport)) {
            return false;
        }

        importedFiles.add(fileToImport);

        return true;
    }

    public void prepare() throws ParserConfigurationException {
    }

    public void failThisFile(DeploymentFile file) {
        failTheseFiles.add(file);
    }

    public void assertDeployed(DeploymentFile fileExpectDeployed) {
        Assert.assertTrue(fileExpectDeployed + " was not deployed. Only " + importedFiles + " were",
                importedFiles.contains(fileExpectDeployed));
    }

    public void assertNotDeployed(DeploymentFile fileToDeploy) {
        Assert.assertTrue(fileToDeploy + " had been deployed.",
                !importedFiles.contains(fileToDeploy));
    }

    public boolean isFileToFail(DeploymentFile fileToDeploy) {
        return failTheseFiles.contains(fileToDeploy);
    }
}
