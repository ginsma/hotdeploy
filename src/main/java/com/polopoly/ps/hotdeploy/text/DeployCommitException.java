package com.polopoly.ps.hotdeploy.text;

/**
 * Represents an internal error where we need to print the stacktrace.
 */
public class DeployCommitException extends DeployException {

	public DeployCommitException(String message, Throwable cause) {
		super(message, cause);
	}

}
