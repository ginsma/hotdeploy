package example.deploy.hotdeploy.state;

import static example.deploy.hotdeploy.state.DefaultFileChecksums.CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.Policy;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserId;
import com.polopoly.util.exception.PolicyCreateException;
import com.polopoly.util.policy.PolicyModification;

import example.deploy.hotdeploy.manualtest.ManualTestCase;

public class TestFileChecksums extends ManualTestCase {
    private static final ExternalContentId CHECKSUMS_SINGLETON_EXTERNAL_ID = new ExternalContentId(CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME);
    private static final int SLOW_CHECKSUM = 4711;
    private static final int QUICK_CHECKSUM = 4712;
    private DummyDeploymentFile aFile;

    private void createChecksums() throws CouldNotFetchChecksumsException {
        new DefaultFileChecksums(context.getPolicyCMServer());
    }

    public void testCreating() throws CouldNotFetchChecksumsException {
        createChecksums();
    }

    private void lockChecksums() throws CMException {
        Caller oldCaller = context.getPolicyCMServer().getCurrentCaller();

        context.getPolicyCMServer().setCurrentCaller(new Caller(new UserId("99")));

        try {
            ((Content) context.getPolicyCMServer().getContent(CHECKSUMS_SINGLETON_EXTERNAL_ID)).lock();
        }
        finally {
            context.getPolicyCMServer().setCurrentCaller(oldCaller);
        }
    }

    private void persistChecksums() throws CouldNotFetchChecksumsException,
            CouldNotUpdateStateException {
        DefaultFileChecksums checksums = new DefaultFileChecksums(context.getPolicyCMServer());
        checksums.setChecksums(aFile, 10, 11);
        checksums.persist();
    }

    public void testFetchingWhenLocked() throws CouldNotFetchChecksumsException, CMException, CouldNotUpdateStateException {
        createChecksums();

        lockChecksums();

        persistChecksums();
    }

    private void createOtherContentWithSameExternalId()
            throws PolicyCreateException {
        context.createPolicy(2, "p.DepartmentSystemTemplate", new PolicyModification<Policy>() {
            public void modify(Policy newVersion) throws CMException {
                newVersion.getContent().setExternalId(CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME);
            }});
    }

    public void testFetchingWhenBlocked() throws CouldNotFetchChecksumsException, CMException, CouldNotUpdateStateException {
        createOtherContentWithSameExternalId();

        persistChecksums();
    }

    public void testOnlySingleVersion() throws Exception {
        createChecksums();

        persistChecksums();

        persistChecksums();

        assertEquals(1,
            context.getPolicyCMServer().getContentHistory(CHECKSUMS_SINGLETON_EXTERNAL_ID).getVersionInfos().length);
    }

    private void assertChecksumsEqual(int expectedQuick, int expectedSlow,
            DefaultFileChecksums checksums) {
        assertEquals(expectedQuick, checksums.getQuickChecksum(aFile));
        assertEquals(expectedSlow, checksums.getSlowChecksum(aFile));
    }

    public void testPersisting() throws Exception {
        DefaultFileChecksums originalChecksums =
            new DefaultFileChecksums(context.getPolicyCMServer());

        originalChecksums.setChecksums(aFile, SLOW_CHECKSUM, QUICK_CHECKSUM);

        assertChecksumsEqual(SLOW_CHECKSUM, QUICK_CHECKSUM, originalChecksums);

        originalChecksums.persist();
        assertTrue(originalChecksums.areAllChangesPersisted());

        DefaultFileChecksums newChecksums =
            new DefaultFileChecksums(context.getPolicyCMServer());

        assertChecksumsEqual(SLOW_CHECKSUM, QUICK_CHECKSUM, newChecksums);
    }

    private void deleteChecksumContent() {
        try {
            context.getPolicyCMServer().removeContent(CHECKSUMS_SINGLETON_EXTERNAL_ID);
        } catch (CMException e) {
            // fine. probably didn't exist.
        }
    }

    private static DummyDeploymentFile createAFile() {
        DummyDeploymentFile aFile = new DummyDeploymentFile("a-test-file");
        aFile.setQuickChecksum(4711);
        aFile.setSlowChecksum(4712);

        return aFile;
    }

    @Override
    public void setUp() {
        aFile = createAFile();

        deleteChecksumContent();
    }
}
