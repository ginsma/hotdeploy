package example.deploy.hotdeploy.state;

import static example.deploy.hotdeploy.state.DefaultFileChecksums.FILE_CHECKSUMS_INPUT_TEMPLATE_NAME;
import example.deploy.hotdeploy.manualtest.CreatePolicyTestAndAbort;
import example.deploy.hotdeploy.manualtest.ManualTestCase;

public class TestFileChecksumsPolicy extends ManualTestCase {
    private DummyDeploymentFile aFile;
    private DummyDeploymentFile anotherFile;

    public void testEmptyFileChecksums() throws Throwable {
        new CreatePolicyTestAndAbort<FileChecksumsPolicy>(
                context, FILE_CHECKSUMS_INPUT_TEMPLATE_NAME, FileChecksumsPolicy.class) {
            @Override
            protected void test(FileChecksumsPolicy checksums) throws Exception {
                assertEquals(-1, checksums.getQuickChecksum(aFile));

                checksums.setChecksums(aFile, 1, 2);

                assertEquals(1, checksums.getQuickChecksum(aFile));
                assertEquals(2, checksums.getSlowChecksum(aFile));

                checksums.setChecksums(anotherFile, 3, 4);

                assertEquals(3, checksums.getQuickChecksum(aFile));
                assertEquals(4, checksums.getSlowChecksum(aFile));
            }};
    }

    @Override
    public void setUp() {
        aFile = new DummyDeploymentFile();
        aFile.setName("a-test-file");
        aFile.setQuickChecksum(4711);
        aFile.setSlowChecksum(4712);

        anotherFile = new DummyDeploymentFile();
        anotherFile.setName("another-test-file");
        anotherFile.setQuickChecksum(4711);
        anotherFile.setSlowChecksum(4712);
    }
}
