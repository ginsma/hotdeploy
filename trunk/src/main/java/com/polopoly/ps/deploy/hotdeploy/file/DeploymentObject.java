package com.polopoly.ps.deploy.hotdeploy.file;

public interface DeploymentObject {
    String getName();

    boolean imports(DeploymentObject object);
}
