package example.deploy.xml.bootstrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.parser.ParseCallback;

public class BootstrapGenerator {
    private static final Logger logger = Logger.getLogger(Bootstrap.class.getName());

    private Set<String> definedExternalIds = new HashSet<String>();
    private Map<String, BootstrapContent> bootstrapByExternalId = new HashMap<String, BootstrapContent>();
    private DeploymentFileParser parser;

    public class BootstrapGeneratorParserCallback implements ParseCallback {
        public void classReferenceFound(DeploymentFile file, String string) {
        }

        private void resolveMajor(String externalId, Major major) {
            BootstrapContent bootstrapContent = bootstrapByExternalId.get(externalId);

            if (bootstrapContent != null) {
                bootstrapContent.setMajor(major);
            }
        }

        public void contentFound(DeploymentFile file, String externalId,
                Major major, String inputTemplate) {
            definedExternalIds.add(externalId);

            // this content might have referenced before, so we will need to bootstrap it.
            // however, we might not have known the major before, but now we do.
            resolveMajor(externalId, major);
        }

        public void contentReferenceFound(DeploymentFile file, Major major, String externalId) {
            if (isNotYetDefined(externalId)) {
                bootstrap(file, major, externalId);
            }
        }

        public void templateFound(DeploymentFile file, String inputTemplate) {
            definedExternalIds.add(inputTemplate);
            resolveMajor(inputTemplate, Major.INPUT_TEMPLATE);
        }

        public void templateReferenceFound(DeploymentFile file, String inputTemplate) {
            if (isNotYetDefined(inputTemplate)) {
                bootstrap(file, Major.INPUT_TEMPLATE, inputTemplate);
            }
        }

        private void bootstrap(DeploymentFile file, Major major, String externalId) {
            if (externalId.equals("")) {
                logger.log(Level.WARNING, "Attempt to bootstrap an empty external ID (major " + major + ") in file " + file + ".");
                return;
            }

            BootstrapContent existingBootstrap = bootstrapByExternalId.get(externalId);

            if (existingBootstrap != null) {
                if (isDisagreeing(major, existingBootstrap)) {
                    logger.log(Level.WARNING, "The major of " + externalId + " is unclear: it might be " + existingBootstrap.getMajor() + " or " + major + ".");
                }

                if (existingBootstrap.getMajor() == Major.UNKNOWN) {
                    existingBootstrap.setMajor(major);
                }
            }
            else {
                bootstrapByExternalId.put(externalId, new BootstrapContent(major, externalId));
            }
        }

        private boolean isDisagreeing(Major aMajor,
                BootstrapContent anotherMajor) {
            return anotherMajor.getMajor() != aMajor &&
                    aMajor != Major.UNKNOWN &&
                    anotherMajor.getMajor() != Major.UNKNOWN;
        }

        private boolean isNotYetDefined(String externalId) {
            return !definedExternalIds.contains(externalId);
        }
    }

    public BootstrapGenerator(DeploymentFileParser parser) {
        this.parser = parser;
    }

    public Bootstrap generateBootstrap(List<DeploymentFile> files) {
        for (DeploymentFile deploymentFile : files) {
            parser.parse(deploymentFile, new BootstrapGeneratorParserCallback());
        }

        Bootstrap result = new Bootstrap();

        Iterator<BootstrapContent> bootstrapContentIterator = bootstrapByExternalId.values().iterator();

        while (bootstrapContentIterator.hasNext()) {
            BootstrapContent bootstrapContent = bootstrapContentIterator.next();

            // we don't bootstrap content that is never defined (these are either system templates
            // or errors), so remove those from the bootstrap.
            if (definedExternalIds.contains(bootstrapContent.getExternalId())) {
                result.add(bootstrapContent);
            }
            else {
                result.addNeverCreatedButReferenced(bootstrapContent);
            }
        }

        return result;
    }
}
