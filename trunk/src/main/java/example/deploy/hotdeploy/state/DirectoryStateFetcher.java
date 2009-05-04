package example.deploy.hotdeploy.state;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.policy.PolicyCMServer;

public class DirectoryStateFetcher {
    private static final Logger logger =
        Logger.getLogger(DirectoryStateFetcher.class.getName());

    private PolicyCMServer server;
    private DirectoryState directoryState;

    public DirectoryStateFetcher(PolicyCMServer server) {
        this.server = server;
    }

    public DirectoryState getDirectoryState() {
        if (directoryState == null) {
            FileChecksums checksums;

            try {
                checksums = new DefaultFileChecksums(server);
            } catch (CouldNotFetchChecksumsException e) {
                logger.log(Level.SEVERE, "Could not fetch checksums policy. " +
        		"Will assume all content has been modified: " + e.getMessage(), e);

                checksums = new NonPersistedFileChecksums();
            }

            directoryState = new DefaultDirectoryState(checksums);
        }

        return directoryState;
    }
}
