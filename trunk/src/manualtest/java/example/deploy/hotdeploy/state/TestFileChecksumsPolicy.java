package com.polopoly.ps.hotdeploy.state;

import static com.polopoly.ps.hotdeploy.state.DefaultFileChecksums.FILE_CHECKSUMS_INPUT_TEMPLATE_NAME;
import com.polopoly.ps.hotdeploy.manualtest.CreatePolicyTestAndAbort;
import com.polopoly.ps.hotdeploy.manualtest.ManualTestCase;

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

                assertEquals(3, checksums.getQuickChecksum(anotherFile));
                assertEquals(4, checksums.getSlowChecksum(anotherFile));
            }};
    }

    @Override
    public void setUp() {
        aFile = new DummyDeploymentFile("a-test-file");
        aFile.setQuickChecksum(4711);
        aFile.setSlowChecksum(4712);

        anotherFile = new DummyDeploymentFile("another-test-file");
        anotherFile.setQuickChecksum(4711);
        anotherFile.setSlowChecksum(4712);
    }
}
