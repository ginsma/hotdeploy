package example.deploy.text;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.impl.exceptions.EJBFinderException;
import com.polopoly.cm.policy.PolicyCMServer;

public class CMServerValidationContext extends StandAloneValidationContext {
    private PolicyCMServer server;

    public CMServerValidationContext(PolicyCMServer server) {
        this.server = server;
    }


    @Override
    public void validateTemplateExistence(String externalId)
            throws ValidationException {
        validateNonAddedContent(externalId);
    }

    @Override
    public void validateContentExistence(String externalId) throws ValidationException {
        try {
            super.validateContentExistence(externalId);
        }
        catch (ValidationException v) {
            validateNonAddedContent(externalId);
        }
    }


    private void validateNonAddedContent(String externalId) throws ValidationException {
        boolean exists;

        try {
            exists = server.findContentIdByExternalId(new ExternalContentId(externalId)) != null;
        } catch (EJBFinderException e) {
            exists = false;
        } catch (CMException e) {
            throw new ValidationException("While looking up " + externalId + ": " + e, e);
        }

        if (!exists) {
            throw new ValidationException("The external ID \"" + externalId + "\" was unknown.");
        }
    }
}
