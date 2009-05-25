package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.List;

import com.polopoly.pcmd.tool.parameters.HotdeployNormalizeParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.normalize.NormalizeElementGatherer;
import example.deploy.xml.parser.ContentXmlParser;

public class HotdeployNormalizeTool implements Tool<HotdeployNormalizeParameters> {

    private static final String BOOTSTRAP_FILE_NAME = File.separator + "bootstrap.xml";

    public HotdeployNormalizeParameters createParameters() {
        return new HotdeployNormalizeParameters();
    }

    public void execute(PolopolyContext context,
            HotdeployNormalizeParameters parameters) {

        List<DeploymentFile> files = parameters.discoverFiles();

        for (DeploymentFile file : files) {
            if (isBootstrap(file)) {
                System.out.println("Skipping bootstrap file " + file + ".");
            }
            else {
                new ContentXmlParser().parse(file,
                    new NormalizeElementGatherer(parameters.getToDirectory()));
            }
        }
    }

    private boolean isBootstrap(DeploymentFile file) {
        return file.getName().endsWith(BOOTSTRAP_FILE_NAME);
    }

    public String getHelp() {
        return "Restructures the content XML into a separate files for each content or template defined.";
    }
}
