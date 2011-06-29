package com.polopoly.ps.deploy.hotdeploy.deployer;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

import com.polopoly.ps.deploy.hotdeploy.discovery.PlatformNeutralPath;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.file.FileDeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.manualtest.ManualTestCase;

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
