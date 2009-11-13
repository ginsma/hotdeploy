package example.deploy.text;

import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

public class ExternalIdReference implements Reference {
    private String externalId;
    private String metadataExternalId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ExternalIdReference(String externalId, String metadataExternalId) {
        this(externalId);
    }

    public ExternalIdReference(String externalId) {
        this.externalId = externalId;
    }

    public void validate(ValidationContext context) throws ValidationException {
        context.validateContentExistence(externalId);

        if (metadataExternalId != null) {
            context.validateContentExistence(metadataExternalId);
        }
    }

    public void validateTemplate(ValidationContext context) throws ValidationException {
        context.validateTemplateExistence(externalId);
    }

    @Override
    public String toString() {
        return externalId;
    }

    public ContentReference resolveReference(PolicyCMServer server) throws CMException {
        VersionedContentId referredId = resolveId(server);

        VersionedContentId metadata = null;

        if (metadataExternalId != null) {
            metadata = server.findContentIdByExternalId(new ExternalContentId(metadataExternalId));
        }

        return new ContentReference(referredId.getContentId(), metadata);
    }

    public VersionedContentId resolveId(PolicyCMServer server) throws CMException {
        VersionedContentId referredId = server.findContentIdByExternalId(new ExternalContentId(externalId));

        if (referredId == null) {
            throw new CMException("Could not find content with external ID \"" + externalId + "\".");
        }

        return referredId;
    }
}
