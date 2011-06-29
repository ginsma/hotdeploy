package com.polopoly.ps.deploy.hotdeploy.client;

import com.polopoly.ps.deploy.hotdeploy.client.Major;

import junit.framework.TestCase;

public class TestMajor extends TestCase {
    public void testContent() {
        assertEquals(Major.CONTENT, Major.getMajor("Content"));
    }
}
