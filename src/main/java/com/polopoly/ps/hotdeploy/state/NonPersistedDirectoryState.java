package com.polopoly.ps.hotdeploy.state;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public class NonPersistedDirectoryState implements DirectoryState {
    
    private final DirectoryState directoryState;

    public NonPersistedDirectoryState(DirectoryState directoryState) {
        this.directoryState = directoryState;
    }

    @Override
    public boolean hasFileChanged(DeploymentFile file) {
        return directoryState.hasFileChanged(file);
    }

    @Override
    public void reset(DeploymentFile file, boolean failed) {
        //do nothing
    }

    @Override
    public void forget(DeploymentFile file) {
        directoryState.forget(file);
    }

    @Override
    public void persist() throws CouldNotUpdateStateException {
       directoryState.persist();
    }

}
