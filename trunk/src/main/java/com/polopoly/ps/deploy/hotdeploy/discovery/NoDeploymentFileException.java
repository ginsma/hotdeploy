package com.polopoly.ps.deploy.hotdeploy.discovery;

public class NoDeploymentFileException extends Exception {

	public NoDeploymentFileException() {
		super();
	}

	public NoDeploymentFileException(String message) {
		super(message);
	}

	public NoDeploymentFileException(String message, Throwable cause) {
		super(message, cause);
	}

}
