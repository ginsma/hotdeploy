package com.polopoly.ps.deploy.xml.ordergenerator;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;

public class Reference {
    String refersTo;
    DeploymentFile inFile;

    public Reference(String refersTo, DeploymentFile file) {
        this.refersTo = refersTo;
        this.inFile = file;
    }

    @Override
    public String toString() {
        return refersTo + "<-" + inFile;
    }
}
