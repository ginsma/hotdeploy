package example.deploy.hotdeploy.state;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.policy.PolicyCMServer;

import example.deploy.hotdeploy.file.DeploymentFile;

public class DirectoryStateFetcher {
    private static final Logger logger =
        Logger.getLogger(DirectoryStateFetcher.class.getName());

    private PolicyCMServer server;
    private DefaultDirectoryState directoryState;

    public DirectoryStateFetcher(PolicyCMServer server) {
        this.server = server;
    }

    public DirectoryState getDirectoryState() {
        if (directoryState == null) {
            FileChecksums checksums;

            try {
                checksums = new DefaultFileChecksums(server);
            } catch (CouldNotFetchChecksumsException e) {
                logger.log(Level.WARNING, "Could not fetch checksums policy. " +
        		"Will assume all content has been modified: " + e.getMessage());

                checksums = new NonPersistedFileChecksums();
            }

            directoryState = new DefaultDirectoryState(checksums);
        }

        return directoryState;
    }

    public DirectoryState refreshAfterFailingToFetch() {
        if (!isFailedState()) {
            return directoryState;
        }

        DefaultDirectoryState oldState = directoryState;

        invalidateCache();

        getDirectoryState();

        for (DeploymentFile deploymentFile : oldState.getSuccessfulResets()) {
            directoryState.reset(deploymentFile, false);
        }

        for (DeploymentFile deploymentFile : oldState.getFailedResets()) {
            directoryState.reset(deploymentFile, false);
        }

        return directoryState;
    }

    private boolean isFailedState() {
        return directoryState.getFileChecksums() instanceof NonPersistedFileChecksums;
    }

    private void invalidateCache() {
        directoryState = null;
    }
}
