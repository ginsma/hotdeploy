package com.polopoly.ps.hotdeploy.file;

public interface DeploymentObject {
    String getName();

    boolean imports(DeploymentObject object);
}
