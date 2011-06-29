package com.polopoly.ps.deploy.hotdeploy.state;


public class CouldNotFetchChecksumsException extends Exception {

    public CouldNotFetchChecksumsException(String message, Exception cause) {
        super(message, cause);
    }

    public CouldNotFetchChecksumsException(Exception cause) {
        super(cause);
    }

}
