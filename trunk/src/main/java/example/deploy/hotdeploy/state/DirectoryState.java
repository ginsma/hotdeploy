package example.deploy.hotdeploy.state;


import example.deploy.hotdeploy.file.DeploymentFile;

/**
 * Describes the modified state of files in a directory.
 *
 * @author AndreasE
 */
public interface DirectoryState {
    /**
     * Whether the file has changed since the last call to
     * {@link #reset(DeploymentFile, boolean)}.
     */
    boolean hasFileChanged(DeploymentFile file);

    /**
     * The the file's current state as reference for whether it has changed.
     * Immediately after the call, {@link #hasFileChanged(DeploymentFile)} will return
     * false.
     */
    void reset(DeploymentFile file, boolean failed);

    void persist() throws CouldNotUpdateStateException;
}
