package com.polopoly.ps.hotdeploy.text;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

public class ExternalIdReference implements Reference {
	private static final Logger LOGGER = Logger
			.getLogger(ExternalIdReference.class.getName());

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

		this.metadataExternalId = metadataExternalId;
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

	public void validateTemplate(ValidationContext context)
			throws ValidationException {
		context.validateTemplateExistence(externalId);
	}

	@Override
	public String toString() {
		return externalId;
	}

	public ContentReference resolveReference(PolicyCMServer server)
			throws CMException {
		VersionedContentId referredId = resolveId(server);

		VersionedContentId metadata = null;

		if (metadataExternalId != null) {
			metadata = server.findContentIdByExternalId(new ExternalContentId(
					metadataExternalId));
		}

		if (metadata != null && referredId.getMajor() == 13
				&& metadata.getMajor() != 13) {
			LOGGER.log(Level.WARNING,
					"The referred ID and the major seem to be swapped in a reference to "
							+ metadata.getContentIdString() + " with metadata "
							+ this + ". Swapping them back.");

			VersionedContentId temp = metadata;
			metadata = referredId;
			referredId = temp;
		}

		return new ContentReference(referredId.getContentId(), metadata);
	}

	public VersionedContentId resolveId(PolicyCMServer server)
			throws CMException {
		VersionedContentId referredId = server
				.findContentIdByExternalId(new ExternalContentId(externalId));

		if (referredId == null) {
			throw new CMException("Could not find content with external ID \""
					+ externalId + "\".");
		}

		return referredId;
	}
}
