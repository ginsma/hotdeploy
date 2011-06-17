package example.deploy.hotdeploy.file;

import java.io.File;

import junit.framework.TestCase;

public class TestFileDeploymentDirectory extends TestCase {

    private FileDeploymentDirectory dir1;
    private FileDeploymentDirectory dir2;

    @Override
    public void setUp() {
        dir1 = new FileDeploymentDirectory(new File("src"));
        dir2 = new FileDeploymentDirectory(new File("src/test"));
    }

    public void testImports() {
        assertTrue(dir1.imports(dir2));
        assertFalse(dir2.imports(dir1));
    }

    public void testEquality() {
        assertFalse(dir1.equals(dir2));
        FileDeploymentDirectory dir1a = new FileDeploymentDirectory(new File("src"));
        assertTrue(dir1.equals(dir1a));
    }

    public void testListFiles() {
        DeploymentObject[] files = dir1.listFiles();

        boolean foundMain = false;

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(File.separator + ".")) {
                fail("Hidden file " + files[i] + " is returned.");
            }

            foundMain = foundMain || (files[i] instanceof DeploymentDirectory && files[i].getName().endsWith(File.separator + "main"));
        }

        if (!foundMain) {
            fail("Did not find directory when listing.");
        }
    }
}
