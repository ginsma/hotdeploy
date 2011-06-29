package com.polopoly.ps.deploy.hotdeploy.state;

import com.polopoly.ps.deploy.hotdeploy.manualtest.ManualTestCase;

public class TestDirectoryStateFetcher extends ManualTestCase {
    public void testFetching() {
        DirectoryState directoryState =
            new DirectoryStateFetcher(context.getPolicyCMServer()).getDirectoryState();

        assertTrue(directoryState instanceof DefaultDirectoryState);
        assertTrue(((DefaultDirectoryState) directoryState).getFileChecksums() instanceof DefaultFileChecksums);
    }
}
