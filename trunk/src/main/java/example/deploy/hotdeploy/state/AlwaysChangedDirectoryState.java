package example.deploy.hotdeploy.state;

import example.deploy.hotdeploy.file.DeploymentFile;


/**
 * A {@link com.polopoly.cm.xml.hotdeploy.DirectoryState} that reports that
 * files have always changed.
 *
 * @author AndreasEd
 */
public class AlwaysChangedDirectoryState implements DirectoryState {

    public boolean hasFileChanged(DeploymentFile file) {
        return true;
    }

    public boolean isFailedState(DeploymentFile file) {
        return false;
    }

    public void reset(DeploymentFile file, boolean failed) {
    }

    public void persist() throws CouldNotUpdateStateException {
    }
}
