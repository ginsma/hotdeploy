package com.polopoly.ps.hotdeploy.discovery.importorder;

import com.polopoly.ps.hotdeploy.discovery.importorder.JarFileBaseNameFinder;

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
        assertBaseName("base-name", "/foo/hej/base-name-1.10.0.jar");
        assertBaseName("base-name", "/foo/hej/base-name-1.0-SNAPSHOT.jar");
        assertBaseName("base-name-tests",
                "/foo/hej/base-name-1.0.12-SNAPSHOT-tests.jar");
        assertBaseName("basename-tests",
                "/foo/hej/basename-1.0-SNAPSHOT-tests.jar");
        assertBaseName("basename-tests", "basename-1.0-SNAPSHOT-tests.jar");

        assertBaseName("basename", "/foo/hej/basename-9.15-DR5.jar");
        assertBaseName("basename", "/foo/hej/basename-9.15-DR5.jar");
        assertBaseName("basename",
                "/foo/hej/basename-9.13.0-depends-9.15-DR5.jar");

    }

    private void assertBaseName(String baseName, String path) {
        assertEquals(baseName, finder.getBaseName(path));
    }
}
