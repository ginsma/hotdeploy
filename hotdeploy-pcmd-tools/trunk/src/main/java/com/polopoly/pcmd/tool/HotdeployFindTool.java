package com.polopoly.pcmd.tool;

import java.util.List;

import com.polopoly.pcmd.tool.parameters.HotdeployFindParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;
import example.deploy.xml.parser.ContentXmlParser;

public class HotdeployFindTool implements Tool<HotdeployFindParameters> {

    public class ReferenceGatherer implements ParseCallback {
        private static final String DECLARATION_PREFIX = "";
        private static final String REFERENCE_PREFIX = "<- ";

        private String searchForExternalId;
        private DeploymentFile lastFile;

        public ReferenceGatherer(String searchForExternalId) {
            this.searchForExternalId = searchForExternalId;
        }

        public void classReferenceFound(DeploymentFile file, String klass) {
        }

        private void printFile(String prefix, DeploymentFile file) {
            if (file.equals(lastFile)) {
                return;
            }

            System.out.print(prefix);
            System.out.println(file);

            lastFile = file;
        }

        public void contentFound(ParseContext context, String externalId,
                Major major, String inputTemplate) {
            if (searchForExternalId.equals(externalId)) {
                printFile(DECLARATION_PREFIX, context.getFile());
            }
        }

        public void contentReferenceFound(ParseContext context, Major major,
                String externalId) {
            if (searchForExternalId.equals(externalId)) {
                printFile(REFERENCE_PREFIX, context.getFile());
            }
        }
    }

    public HotdeployFindParameters createParameters() {
        return new HotdeployFindParameters();
    }

    public void execute(PolopolyContext context, HotdeployFindParameters parameters) {
        List<DeploymentFile> files = parameters.discoverFiles();

        ContentXmlParser parser = new ContentXmlParser();

        ParseCallback callback = new ReferenceGatherer(parameters.getExternalId());

        for (DeploymentFile file : files) {
            parser.parse(file, callback);
        }
    }

    public String getHelp() {
        return "Finds the declaration of a content object or template in a set of content XML files.";
    }

}
