package example.deploy.hotdeploy.discovery.importorder;

import junit.framework.TestCase;

public class TestJarFileBaseNameFinder extends TestCase {

    private JarFileBaseNameFinder finder;

    @Override
    public void setUp() {
        finder = new JarFileBaseNameFinder();
    }

    public void testAll() {
        assertBaseName("basename", "/foo/hej/basename.jar");
        assertBaseName("base-name", "/foo/hej/base-name.jar");
        assertBaseName("base-name", "\\foo\\hej\\base-name.jar");
        assertBaseName("basename", "basename.jar");

        assertBaseName("basename", "/foo/hej/basename-1.0.jar");
        assertBaseName("basename", "basename-1.0.jar");
        assertBaseName("base-name", "/foo/hej/base-name-1.jar");
        assertBaseName("base-name", "/foo/hej/base-name-1.0.0.jar");
        assertBaseName("base-name", "/foo/hej/base-name-1.0-SNAPSHOT.jar");
        assertBaseName("base-name", "/foo/hej/base-name-1.0-SNAPSHOT-tests.jar");
        assertBaseName("basename", "/foo/hej/basename-1.0-SNAPSHOT-tests.jar");
        assertBaseName("basename", "basename-1.0-SNAPSHOT-tests.jar");
    }

    private void assertBaseName(String baseName, String path) {
        assertEquals(baseName, finder.getBaseName(path));
    }
}
