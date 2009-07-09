package example.deploy.xml.bootstrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class BootstrapGatherer implements ParseCallback {
    private Set<String> definedExternalIds = new HashSet<String>();
    private Map<String, BootstrapContent> bootstrapByExternalId = new HashMap<String, BootstrapContent>();

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    private void resolve(String externalId, Major major, String inputTemplate) {
        BootstrapContent bootstrapContent = bootstrapByExternalId.get(externalId);

        if (bootstrapContent != null) {
            bootstrapContent.setMajor(major);
            bootstrapContent.setInputTemplate(inputTemplate);
        }
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        definedExternalIds.add(externalId);

        // this content might been have referenced before, so we will need to bootstrap it.
        // however, we might not have known the major before, but now we do.
        resolve(externalId, major, inputTemplate);
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        if (isNotYetDefined(externalId)) {
            bootstrap(context.getFile(), major, externalId);
        }
    }

    private void bootstrap(DeploymentFile file, Major major, String externalId) {
        if (externalId.equals("")) {
            BootstrapGenerator.logger.log(Level.WARNING, "Attempt to bootstrap an empty external ID (major " + major + ") in file " + file + ".");
            return;
        }

        BootstrapContent existingBootstrap = bootstrapByExternalId.get(externalId);

        if (existingBootstrap != null) {
            if (isDisagreeing(major, existingBootstrap)) {
                BootstrapGenerator.logger.log(Level.WARNING, "The major of " + externalId + " is unclear: it might be " + existingBootstrap.getMajor() + " or " + major + ".");
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

    public Iterable<BootstrapContent> getBootstrapContent() {
        return bootstrapByExternalId.values();
    }

    public boolean isDefined(String externalId) {
        return definedExternalIds.contains(externalId);
    }
}