package com.polopoly.ps.deploy.hotdeploy.state;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;

public interface FileChecksums {

    void setChecksums(DeploymentFile file, long quickChecksum, long slowChecksum);

    void persist() throws CouldNotUpdateStateException;

    long getQuickChecksum(DeploymentFile file);

    long getSlowChecksum(DeploymentFile file);

    boolean areAllChangesPersisted();

}
