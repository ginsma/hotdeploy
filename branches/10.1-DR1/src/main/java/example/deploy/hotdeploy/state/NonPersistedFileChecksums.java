package example.deploy.hotdeploy.state;

import java.util.HashMap;
import java.util.Map;

import example.deploy.hotdeploy.file.DeploymentFile;

public class NonPersistedFileChecksums implements FileChecksums {
    private Map<DeploymentFile, Long> quickChecksums =
        new HashMap<DeploymentFile, Long>();
    private Map<DeploymentFile, Long> slowChecksums =
        new HashMap<DeploymentFile, Long>();
    private boolean persisted = true;

    public long getQuickChecksum(DeploymentFile file) {
        Long result = quickChecksums.get(file);

        if (result == null) {
            return -1;
        }
        else {
            return result;
        }
    }

    public long getSlowChecksum(DeploymentFile file) {
        Long result = slowChecksums.get(file);

        if (result == null) {
            return -1;
        }
        else {
            return result;
        }
    }

    public void setChecksums(DeploymentFile file, long quickChecksum, long slowChecksum) {
        slowChecksums.put(file, slowChecksum);
        quickChecksums.put(file, quickChecksum);
        persisted = false;
    }

    public boolean contains(DeploymentFile file) {
        return slowChecksums.containsKey(file) || quickChecksums.containsKey(file);
    }

    public boolean areAllChangesPersisted() {
        return persisted;
    }

    public void persist() throws CouldNotUpdateStateException {
        persisted = true;
    }

}
