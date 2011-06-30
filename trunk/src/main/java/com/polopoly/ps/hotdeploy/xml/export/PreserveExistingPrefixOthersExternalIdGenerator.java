package com.polopoly.ps.hotdeploy.xml.export;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.xml.util.export.ExternalIdGenerator;

public class PreserveExistingPrefixOthersExternalIdGenerator implements
		ExternalIdGenerator {
	private static final Logger logger = Logger
			.getLogger(PreserveExistingPrefixOthersExternalIdGenerator.class
					.getName());

	private String prefix;

	public PreserveExistingPrefixOthersExternalIdGenerator(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String generateExternalId(ContentRead content) {
		ExternalContentId externalId;
		try {
			externalId = content.getExternalId();
		} catch (CMException e) {
			logger.log(Level.WARNING,
					"Could not get external ID of "
							+ content.getContentId().getContentIdString()
							+ ": " + e.getMessage(), e);

			externalId = null;
		}

		if (externalId != null) {
			return externalId.getExternalId();
		} else {
			return prefix
					+ content.getContentId().getContentId()
							.getContentIdString();
		}
	}

}
