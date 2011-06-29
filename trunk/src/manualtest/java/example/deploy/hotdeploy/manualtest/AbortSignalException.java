package com.polopoly.ps.deploy.hotdeploy.manualtest;

import com.polopoly.cm.client.CMException;

public class AbortSignalException extends CMException {

    public AbortSignalException() {
        super("Test completed successfully. Roll back version");
    }

}
