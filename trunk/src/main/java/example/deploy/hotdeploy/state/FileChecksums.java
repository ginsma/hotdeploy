package example.deploy.hotdeploy.state;

import example.deploy.hotdeploy.file.DeploymentFile;

public interface FileChecksums {

    void setChecksums(DeploymentFile file, long quickChecksum, long slowChecksum);

    void persist() throws CouldNotUpdateStateException;

    long getQuickChecksum(DeploymentFile file);

    long getSlowChecksum(DeploymentFile file);

    boolean areAllChangesPersisted();

}
