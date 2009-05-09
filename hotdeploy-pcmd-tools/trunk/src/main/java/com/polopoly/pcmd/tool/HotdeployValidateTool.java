package com.polopoly.pcmd.tool;

import java.util.List;

import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.consistency.PresentFileReader;
import example.deploy.xml.consistency.XMLConsistencyVerifier;

public class HotdeployValidateTool implements Tool<FilesToDeployParameters> {

    public FilesToDeployParameters createParameters() {
        return new FilesToDeployParameters();
    }

    public void execute(PolopolyContext context, FilesToDeployParameters parameters) {
        List<DeploymentFile> files = parameters.discoverFiles();

        XMLConsistencyVerifier verifier =
            new XMLConsistencyVerifier(files);

        new PresentFileReader(verifier.getRootDirectory(), verifier).read();

        verifier.setValidateClassReferences(parameters.isValidateClasses());

        if (parameters.getClassDirectory() != null) {
            verifier.addClassDirectory(parameters.getClassDirectory());
        }

        verifier.verify().reportUsingLogging();
    }

    public String getHelp() {
        return "Parses the content and template XML in the specified directory and " +
    		"reports on whether there is any content being imported in the wrong order " +
    		"or references to content that does exist.";
    }

}
