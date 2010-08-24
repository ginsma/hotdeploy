package example.deploy.xml.normalize;

import java.io.File;

import example.deploy.hotdeploy.client.Major;

public interface NormalizationNamingStrategy {

    File getFileName(Major major, String externalId, String inputTemplate);

}
