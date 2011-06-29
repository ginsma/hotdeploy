package com.polopoly.ps.deploy.hotdeploy.state;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;



/**
 * A {@link com.polopoly.cm.xml.hotdeploy.DirectoryState} that reports that
 * all files have been changed (until they are reset).
 */
public class NoFilesImportedDirectoryState implements DirectoryState {
    private Set<DeploymentFile> successfulResets = new HashSet<DeploymentFile>();
    private Set<DeploymentFile> failedResets = new HashSet<DeploymentFile>();

    public boolean hasFileChanged(DeploymentFile file) {
        if (successfulResets.contains(file) || failedResets.contains(file)) {
            return false;
        }

        return true;
    }

    public boolean isFailedState(DeploymentFile file) {
        return false;
    }

    public void reset(DeploymentFile file, boolean failed) {
        if (failed) {
            failedResets.add(file);
        }
        else {
            successfulResets.add(file);
        }
    }

    public void persist() throws CouldNotUpdateStateException {
    }

    public Collection<DeploymentFile> getSuccessfulResets() {
        return successfulResets;
    }
}
