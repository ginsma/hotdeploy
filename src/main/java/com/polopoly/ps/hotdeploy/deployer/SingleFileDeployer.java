package com.polopoly.ps.hotdeploy.deployer;

import javax.xml.parsers.ParserConfigurationException;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public interface SingleFileDeployer {

	public abstract void prepare() throws ParserConfigurationException;

	public abstract boolean importAndHandleException(DeploymentFile fileToImport)
			throws FatalDeployException;

}