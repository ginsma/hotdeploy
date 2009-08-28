package example.deploy.hotdeploy.state;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.impl.exceptions.EJBFinderException;
import com.polopoly.cm.client.impl.exceptions.LockException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import example.deploy.hotdeploy.file.DeploymentFile;

public class DefaultFileChecksums implements FileChecksums {
    public static final String CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME = "hotdeploy.FileChecksumsSingleton";
    public static final String FILE_CHECKSUMS_INPUT_TEMPLATE_NAME = "p.SystemConfig";

    private static final Logger logger =
        Logger.getLogger(DefaultFileChecksums.class.getName());

    private PolicyCMServer server;
    private FileChecksumsPseudoPolicy checksumsPolicy;

    private Map<DeploymentFile, Checksums> changes = new HashMap<DeploymentFile, Checksums>();

    private class Checksums {
        private long quickChecksum;
        private long slowChecksum;
    }

    private FileChecksumsPseudoPolicy getLatestChecksumsPolicy(PolicyCMServer server)
            throws CouldNotFetchChecksumsException {
        try {
            return new FileChecksumsPseudoPolicy(
                PolicySingletonUtil.getSingleton(server, 17,
                    CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME, FILE_CHECKSUMS_INPUT_TEMPLATE_NAME,
                    Policy.class));
        } catch (CMException e) {
            throw new CouldNotFetchChecksumsException(e.getMessage(), e);
        }
    }

    public DefaultFileChecksums(PolicyCMServer server) throws CouldNotFetchChecksumsException {
        this.server = server;

        checksumsPolicy = getLatestChecksumsPolicy(server);
    }

    public void clear() {
        try {
            VersionedContentId checksumsId = getLatestChecksumVersion();
            checksumsPolicy = getLatestChecksumsPolicy(server);

            try {
                checksumsPolicy = new FileChecksumsPseudoPolicy(server.createContentVersion(checksumsId));

                changes.clear();
                checksumsPolicy.clear();

                checksumsPolicy.commit();
            }
            catch (RuntimeException e) {
                failPersisting(e);
            }
            catch (LockException e) {
                handleSingletonLocked(checksumsId);

                // retry
                persist();
            }
            catch (CMException e) {
                failPersisting(e);
            }
        } catch (CouldNotUpdateStateException e) {
            logger.log(Level.WARNING, "While deleting file checksums: " + e.getMessage(), e);
        } catch (CouldNotFetchChecksumsException e) {
            logger.log(Level.WARNING, "While deleting file checksums: " + e.getMessage(), e);
        }
    }

    public long getQuickChecksum(DeploymentFile file) {
        Checksums changedChecksum = changes.get(file);

        if (changedChecksum != null) {
            return changedChecksum.quickChecksum;
        }
        else {
            return checksumsPolicy.getQuickChecksum(file);
        }
    }

    public long getSlowChecksum(DeploymentFile file) {
        Checksums changedChecksum = changes.get(file);

        if (changedChecksum != null) {
            return changedChecksum.slowChecksum;
        }
        else {
            return checksumsPolicy.getSlowChecksum(file);
        }
    }

    public synchronized void setChecksums(DeploymentFile file, long quickChecksum,
            long slowChecksum) {
        Checksums checksums = new Checksums();
        checksums.quickChecksum = quickChecksum;
        checksums.slowChecksum = slowChecksum;

        changes.put(file, checksums);
    }

    private VersionedContentId getLatestChecksumVersion()
            throws CouldNotUpdateStateException {
        try {
            return
                server.translateSymbolicContentId(
                    new ExternalContentId(CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME));
        }
        catch (EJBFinderException finderException) {
            throw new CouldNotUpdateStateException(
                "Could not find existing checksum policy singleton: " + finderException.getMessage());
        }
        catch (CMException cmException) {
            throw new CouldNotUpdateStateException(
                "Could not fetch existing checksum policy singleton: " + cmException.getMessage(), cmException);
        }
    }

    private void handleSingletonLocked(VersionedContentId checksumsId) throws CouldNotUpdateStateException {
        try {
            logger.log(Level.WARNING,
                    "The checksum singleton " + checksumsId.getContentId().getContentIdString() + " was locked. Forcing an unlock.");

            Content checksumContent = (Content)
                server.getContent(checksumsId);

            checksumContent.forcedUnlock();
        } catch (CMException unlockException) {
            logger.log(Level.WARNING, "Unlocking failed.");

            failPersisting(unlockException);
        }
    }

    public synchronized void persist() throws CouldNotUpdateStateException {
        if (areAllChangesPersisted()) {
            return;
        }

        VersionedContentId checksumsId = getLatestChecksumVersion();

        try {
            checksumsPolicy = new FileChecksumsPseudoPolicy(server.createContentVersion(checksumsId));

            for (Map.Entry<DeploymentFile, Checksums> change : changes.entrySet()) {
                DeploymentFile changedFile = change.getKey();
                Checksums changedChecksums = change.getValue();

                checksumsPolicy.setChecksums(changedFile,
                    changedChecksums.quickChecksum, changedChecksums.slowChecksum);
            }

            changes.clear();

            checksumsPolicy.commit();
        }
        catch (RuntimeException e) {
            failPersisting(e);
        }
        catch (LockException e) {
            handleSingletonLocked(checksumsId);

            // retry
            persist();
        }
        catch (CMException e) {
            failPersisting(e);
        }
    }

    private void failPersisting(Exception e) throws CouldNotUpdateStateException {
        try {
            server.abortContent(checksumsPolicy.getDelegatePolicy(), true);
        } catch (CMException cmException) {
            logger.log(Level.WARNING, "Failed aborting new version of checksums: " + cmException.getMessage(), cmException);
        }

        throw new CouldNotUpdateStateException(e);
    }

    public boolean areAllChangesPersisted() {
        return changes.isEmpty();
    }
}
