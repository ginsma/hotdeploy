package example.deploy.hotdeploy.text;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

public class ExternalIdReference implements Reference {
    private String externalId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ExternalIdReference(String externalId) {
        this.externalId = externalId;
    }

    public void validate(ValidationContext context) throws ValidationException {
        context.validateContentExistence(externalId);
    }

    public void validateTemplate(ValidationContext context) throws ValidationException {
        context.validateTemplateExistence(externalId);
    }

    @Override
    public String toString() {
        return externalId;
    }

    public ContentId resolve(PolicyCMServer server) throws CMException {
        VersionedContentId result = server.findContentIdByExternalId(new ExternalContentId(externalId));

        if (result != null) {
            return result.getContentId();
        }
        else {
            throw new CMException("Could not find content with external ID \"" + externalId + "\".");
        }
    }
}
