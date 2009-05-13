package com.polopoly.pcmd.tool;

import java.util.List;

import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.consistency.PresentFileReader;
import example.deploy.xml.consistency.VerifyResult;
import example.deploy.xml.consistency.XMLConsistencyVerifier;

public class HotdeployValidateTool implements Tool<ValidateParameters> {

    public ValidateParameters createParameters() {
        return new ValidateParameters();
    }

    public void execute(PolopolyContext context, ValidateParameters parameters) {
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
