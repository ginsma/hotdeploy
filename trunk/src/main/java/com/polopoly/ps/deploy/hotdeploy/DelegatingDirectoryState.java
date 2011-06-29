package com.polopoly.ps.deploy.hotdeploy;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.state.CouldNotUpdateStateException;
import com.polopoly.ps.deploy.hotdeploy.state.DirectoryState;

public class DelegatingDirectoryState implements DirectoryState {
    private DirectoryState delegate;

    public DelegatingDirectoryState(DirectoryState delegate) {
        this.delegate = delegate;
    }

    public boolean hasFileChanged(DeploymentFile file) {
        return delegate.hasFileChanged(file);
    }

    public void reset(DeploymentFile file, boolean failed) {
        delegate.reset(file, failed);
    }

    public void persist() throws CouldNotUpdateStateException {
        delegate.persist();
    }

}
