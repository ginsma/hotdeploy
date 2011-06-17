package example.deploy.hotdeploy.deployer;

import javax.xml.parsers.ParserConfigurationException;

import example.deploy.hotdeploy.file.DeploymentFile;

public interface SingleFileDeployer {

    public abstract void prepare() throws ParserConfigurationException;

    public abstract boolean importAndHandleException(DeploymentFile fileToImport)
            throws FatalDeployException;

}