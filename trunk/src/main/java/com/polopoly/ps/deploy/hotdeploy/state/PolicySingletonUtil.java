package com.polopoly.ps.deploy.hotdeploy.state;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.ps.deploy.hotdeploy.util.CheckedCast;
import com.polopoly.ps.deploy.hotdeploy.util.CheckedClassCastException;


/**
 * Class for retrieving an (unversioned) singleton, i.e. a policy of which only
 * one instance is supposed to exist.
 *
 * @author AndreasE
 */
public class PolicySingletonUtil {
    private static final Logger logger =
        Logger.getLogger(PolicySingletonUtil.class.getName());

    /**
     * Returns the singleton policy.
     */
    public static Policy getSingleton(PolicyCMServer server, int major,
            String externalId, String inputTemplateName, Class<?> klass) throws CMException {
        try {
            VersionedContentId id =
                server.findContentIdByExternalId(new ExternalContentId(
                        new ExternalContentId(externalId), VersionedContentId.LATEST_VERSION));

            if (id == null) {
                logger.log(Level.INFO, "Singleton " + externalId + " did not exist. Creating it.");

                return createSingleton(server, major, externalId, inputTemplateName, klass);
            }

            try {
                Policy policy = server.getPolicy(id);

                return (Policy) CheckedCast.cast(policy, klass, "Policy of " + externalId);
            }
            catch (ContentOperationFailedException e) {
                logger.log(Level.INFO, "Singleton " + externalId +
                    " could not be retrieved. Trying to recreate it: " + e.getMessage());

                try {
                    return createSingleton(server, major, externalId, inputTemplateName, klass);
                } catch (CMException recreatingException) {
                    logger.log(Level.WARNING, "Recreating singleton " + externalId + " failed: " +
                            recreatingException.getMessage());

                    // intentionally throwing original exception. this is the real error, not the exception
                    // from trying to recover.
                    throw e;
                }
            }
        } catch (CheckedClassCastException e) {
            throw new CMException(e);
        }
    }

    private static Policy createSingleton(PolicyCMServer server, int major,
            String externalId, String inputTemplateName, Class<?> klass)
            throws CheckedClassCastException, CMException {
        ContentId inputTemplate =
            server.findContentIdByExternalId(new ExternalContentId(inputTemplateName));

        if (inputTemplate == null) {
            throw new CMException("The template " + inputTemplateName + " did not exist.");
        }

        Policy result = (Policy) CheckedCast.cast(
            server.createContent(major,
                inputTemplate),
                    klass);

        result.getContent().setExternalId(externalId);

        result.getContent().commit();

        return result;
    }
}
