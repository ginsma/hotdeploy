package com.polopoly.pcmd.tool;

import java.util.List;

import com.polopoly.pcmd.tool.parameters.HotdeployValidateParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.consistency.VerifyResult;
import example.deploy.xml.consistency.XMLConsistencyVerifier;
import example.deploy.xml.present.PresentFileReader;

public class HotdeployValidateTool implements Tool<HotdeployValidateParameters> {

    public HotdeployValidateParameters createParameters() {
        return new HotdeployValidateParameters();
    }

    public void execute(PolopolyContext context, HotdeployValidateParameters parameters) {
        List<DeploymentFile> files = parameters.discoverFiles();

        System.out.println("Validating " + Plural.count(files, "file") + "...");

        XMLConsistencyVerifier verifier =
            new XMLConsistencyVerifier(files);

        if (!parameters.isIgnorePresent()) {
            new PresentFileReader(verifier.getRootDirectory(), verifier).read();
        }

        verifier.setValidateClassReferences(parameters.isValidateClasses());

        if (parameters.getClassDirectory() != null) {
            verifier.addClassDirectory(parameters.getClassDirectory());
        }

        VerifyResult verifyResult = verifier.verify();

        if (verifyResult.isEverythingOk()) {
            System.out.println("The files are in consistent order and do not reference non-existing content.");
        }
        else {
            verifyResult.reportUsingLogging();
        }
    }

    public String getHelp() {
        return "Parses the content and template XML in the specified directory and " +
    		"reports on whether there is any content being imported in the wrong order " +
    		"or references to content that does exist.";
    }

}
