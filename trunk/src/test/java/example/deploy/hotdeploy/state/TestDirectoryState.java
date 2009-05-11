package example.deploy.hotdeploy.state;

import junit.framework.TestCase;

public class TestDirectoryState extends TestCase {
    private DefaultDirectoryState directoryState;
    private DummyDeploymentFile file;

    public void testHasChangedInitially() {
        assertTrue(directoryState.hasFileChanged(file));
    }

    public void testResetOnFailure() throws CouldNotUpdateStateException {
        directoryState.reset(file, false);
        assertFalse(directoryState.hasFileChanged(file));
    }

    public void testResetOnSuccess() throws CouldNotUpdateStateException {
        directoryState.reset(file, true);
        assertFalse(directoryState.hasFileChanged(file));
    }

    public void testChangeBothChecksumsAfterReset() throws CouldNotUpdateStateException {
        directoryState.reset(file, false);

        file.setQuickChecksum(4710);
        file.setSlowChecksum(4711);

        assertTrue(directoryState.hasFileChanged(file));
    }

    public void testChangeQuickChecksumsAfterReset() throws CouldNotUpdateStateException {
        directoryState.reset(file, false);

        file.setQuickChecksum(4710);

        assertFalse(directoryState.hasFileChanged(file));
    }

    public void testChangeSlowChecksumsAfterReset() throws CouldNotUpdateStateException {
        directoryState.reset(file, false);

        file.setSlowChecksum(4710);

        assertFalse(directoryState.hasFileChanged(file));
    }

    @Override
    public void setUp() {
        directoryState = new DefaultDirectoryState(new NonPersistedFileChecksums());

        file = new DummyDeploymentFile("any");
        file.setQuickChecksum(4711);
        file.setSlowChecksum(4712);
    }
}
