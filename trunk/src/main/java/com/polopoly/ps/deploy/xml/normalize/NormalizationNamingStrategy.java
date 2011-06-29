package com.polopoly.ps.deploy.xml.normalize;

import java.io.File;

import com.polopoly.cm.client.ContentRead;
import com.polopoly.ps.deploy.hotdeploy.client.Major;


public interface NormalizationNamingStrategy {

	/**
	 * The external ID is the one we will have after exporting; it may be
	 * different from the existing.
	 */
	File getFileName(ContentRead content);

	File getFileName(Major major, String externalId, String inputTemplate);

}
