package example.deploy.hotdeploy.deployer;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

import example.deploy.hotdeploy.discovery.PlatformNeutralPath;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.hotdeploy.manualtest.ManualTestCase;

public class TestDefaultSingleFileDeployer extends ManualTestCase {
    private static final String CONTENT_FILE =
        PlatformNeutralPath.unixToPlatformSpecificPath("src/main/resources/content/templates.xml");

    public void testDeploy() throws FatalDeployException, ParserConfigurationException {
        DefaultSingleFileDeployer deployer =
            new DefaultSingleFileDeployer(context.getPolicyCMServer());

        deployer.prepare();

        DeploymentFile fileToImport =
            new FileDeploymentFile(new File(CONTENT_FILE));

        boolean success = deployer.importAndHandleException(fileToImport);

        assertTrue(success);
    }
}
