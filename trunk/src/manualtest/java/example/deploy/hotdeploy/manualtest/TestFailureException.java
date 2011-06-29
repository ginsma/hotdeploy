package com.polopoly.ps.deploy.hotdeploy.manualtest;

import com.polopoly.cm.client.CMException;

public class TestFailureException extends CMException {

    public TestFailureException(Exception cause) {
        super(cause);
    }

}
