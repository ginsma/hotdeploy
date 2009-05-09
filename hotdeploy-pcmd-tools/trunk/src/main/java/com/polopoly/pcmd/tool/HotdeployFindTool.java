package com.polopoly.pcmd.tool;

import java.util.List;

import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.XmlParser;

public class HotdeployFindTool implements Tool<FindParameters> {

    public class FindCallback implements ParseCallback {
        private static final String DECLARATION_PREFIX = "";
        private static final String REFERENCE_PREFIX = "<- ";

        private String searchForExternalId;
        private DeploymentFile lastFile;

        public FindCallback(String searchForExternalId) {
            this.searchForExternalId = searchForExternalId;
        }

        public void classReferenceFound(DeploymentFile file, String klass) {
        }

        public void contentFound(DeploymentFile file, String externalId,
                Major major, String inputTemplate) {
            declarationFound(file, externalId);
        }

        public void templateFound(DeploymentFile file, String inputTemplate) {
            declarationFound(file, inputTemplate);
        }

        private void declarationFound(DeploymentFile file, String externalId) {
            if (searchForExternalId.equals(externalId)) {
                printFile(DECLARATION_PREFIX, file);
            }
        }

        private void referenceFound(DeploymentFile file, String externalId) {
            if (searchForExternalId.equals(externalId)) {
                printFile(REFERENCE_PREFIX, file);
            }
        }

        private void printFile(String prefix, DeploymentFile file) {
            if (file.equals(lastFile)) {
                return;
            }

            System.out.print(prefix);
            System.out.println(file);

            lastFile = file;
        }

        public void contentReferenceFound(DeploymentFile file, Major major, String externalId) {
            referenceFound(file, externalId);
        }

        public void templateReferenceFound(DeploymentFile file,
                String inputTemplate) {
            referenceFound(file, inputTemplate);
        }
    }

    public FindParameters createParameters() {
        return new FindParameters();
    }

    public void execute(PolopolyContext context, FindParameters parameters) {
        List<DeploymentFile> files = parameters.discoverFiles();

        XmlParser parser = new XmlParser();

        ParseCallback callback = new FindCallback(parameters.getExternalId());

        for (DeploymentFile file : files) {
            parser.parse(file, callback);
        }
    }

    public String getHelp() {
        return "Finds the declaration of a content object or template in a set of content XML files.";
    }

}
