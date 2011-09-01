package com.polopoly.ps.hotdeploy.xml.bootstrap;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.parser.DeploymentFileParser;


public class BootstrapGenerator {
    static final Logger logger = Logger.getLogger(Bootstrap.class.getName());

    private DeploymentFileParser parser;

    public BootstrapGenerator(DeploymentFileParser parser) {
        this.parser = parser;
    }

    public Bootstrap generateBootstrap(List<DeploymentFile> files) {
        BootstrapGatherer gatherer = new BootstrapGatherer();

        for (DeploymentFile deploymentFile : files) {
            parser.parse(deploymentFile, gatherer);
        }

        Bootstrap result = new Bootstrap();

        Iterator<BootstrapContent> bootstrapContentIterator = gatherer.getBootstrapContent().iterator();

        while (bootstrapContentIterator.hasNext()) {
            BootstrapContent bootstrapContent = bootstrapContentIterator.next();

            // we don't bootstrap content that is never defined (these are either system templates
            // or errors), so remove those from the bootstrap.
            if (gatherer.isDefined(bootstrapContent.getExternalId())) {
                result.add(bootstrapContent);
            }
            else {
                result.addNeverCreatedButReferenced(bootstrapContent);
            }
        }

        return result;
    }
}
