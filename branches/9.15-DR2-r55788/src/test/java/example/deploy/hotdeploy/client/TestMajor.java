package example.deploy.hotdeploy.client;

import junit.framework.TestCase;

public class TestMajor extends TestCase {
    public void testContent() {
        assertEquals(Major.CONTENT, Major.getMajor("Content"));
    }
}
