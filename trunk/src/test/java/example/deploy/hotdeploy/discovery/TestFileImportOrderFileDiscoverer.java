package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class TestFileImportOrderFileDiscoverer extends TestCase {
    public void testDiscoverer() throws NotApplicableException {
        String directory = "src" + File.separator + "test" + File.separator + "resources";

        List<DeploymentFile> files = new ImportOrderFileDiscoverer(new File(directory)).getFilesToImport();

        String subFolder = directory + File.separator + "folder" + File.separator + "subfolder";

        assertEquals(new FileDeploymentFile(new File(directory, "a.xml")), files.get(0));
        assertEquals(new FileDeploymentFile(new File(subFolder, "c.xml")), files.get(1));
        assertEquals(new FileDeploymentFile(new File(subFolder, "d.xml")), files.get(2));

        assertEquals(3, files.size());
    }
}
