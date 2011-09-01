package com.polopoly.ps.hotdeploy.state;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;

class FileState {
    private long slowChecksum;
    private long quickChecksum;

    FileState(long quickChecksum, long checksum) {
        this.quickChecksum = quickChecksum;
        this.slowChecksum = checksum;
    }

    public boolean hasFileChanged(DeploymentFile file) {
        if (quickChecksum == file.getQuickChecksum()) {
            return false;
        }

        // the slow checksum is assumed to be more accurate. even if the quick
        // checksum says a file has changed, the slow checksum is the correct
        // measure of that.
        if (slowChecksum != 0) {
            return slowChecksum != file.getSlowChecksum();
        }
        else {
            return true;
        }
    }
}