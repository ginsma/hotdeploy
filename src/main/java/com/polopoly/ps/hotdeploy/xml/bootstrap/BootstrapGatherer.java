package com.polopoly.ps.hotdeploy.xml.bootstrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;


public class BootstrapGatherer implements ParseCallback {
    private static final Logger logger = Logger.getLogger(BootstrapGatherer.class.getName());

    private Set<String> definedExternalIds = new HashSet<String>();
    private Map<String, BootstrapContent> bootstrapByExternalId = new HashMap<String, BootstrapContent>();

    public void classReferenceFound(DeploymentFile file, String string) {
    }

    private void resolve(String externalId, Major major, String inputTemplate) {
        BootstrapContent bootstrapContent = bootstrapByExternalId.get(externalId);

        if (bootstrapContent != null) {
            if (logger.isLoggable(Level.FINE) && bootstrapContent.getMajor() == Major.UNKNOWN) {
                logger.log(Level.FINE,  "We now know the major of " + externalId + ": " + major + ".");
            }

            bootstrapContent.setMajor(major);
            bootstrapContent.setInputTemplate(inputTemplate);
        }
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,  "Found content " + externalId + " of major " + major + " in " + context.getFile() + ".");
        }

        definedExternalIds.add(externalId);

        // this content might been have referenced before, so we will need to bootstrap it.
        // however, we might not have known the major before, but now we do.
        resolve(externalId, major, inputTemplate);
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        if (isNotYetDefined(externalId)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,  "Found reference to " + externalId + " of major " + major + " in " + context.getFile() + " which needs bootstrapping.");
            }

            bootstrap(context.getFile(), major, externalId);
        }
        else if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINER, "Found reference to " + externalId + " of major " + major + " in " + context.getFile() + " which has been defined and therefore needs no bootstrapping.");
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