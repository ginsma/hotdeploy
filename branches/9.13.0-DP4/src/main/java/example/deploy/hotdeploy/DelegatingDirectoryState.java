package example.deploy.hotdeploy;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.CouldNotUpdateStateException;
import example.deploy.hotdeploy.state.DirectoryState;

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
