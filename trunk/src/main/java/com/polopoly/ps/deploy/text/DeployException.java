package com.polopoly.ps.deploy.text;

public class DeployException extends Exception {

    public DeployException(String message) {
        super(message);
    }

    public DeployException(String message, Throwable cause) {
        super(message, cause);
    }

}
