package com.polopoly.ps.hotdeploy.deployer;



/**
 * An error while deploying that is fatal, i.e. there is no point in skipping the current file and moving on to the next file.
 * @author andreasehrencrona
 */
public class FatalDeployException extends Exception {

    public FatalDeployException(String message) {
        super(message);
    }

    public FatalDeployException(String message, Exception cause) {
        super(message, cause);
    }

    public FatalDeployException(Exception cause) {
        super(cause);
    }

}
