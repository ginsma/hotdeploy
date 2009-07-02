package example.deploy.xml.normalize;

import static example.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;

import java.io.File;

import example.deploy.hotdeploy.client.Major;

public class DefaultNormalizationNamingStrategy implements NormalizationNamingStrategy {
    public static final String CONTENT_DIRECTORY = "content";

    private static final String SYSTEM_TEMPLATE_PREFIX = "p.";
    public static final String XML_EXTENSION = ".xml";

    private File contentDirectory;
    private File templateDirectory;

    public DefaultNormalizationNamingStrategy(File directory) {
        templateDirectory = directory;

        contentDirectory = new File(directory, CONTENT_DIRECTORY);
        mkdir(contentDirectory);
    }

    private static void mkdir(File directory) {
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.err.println("Could not create directory " + directory.getAbsolutePath());
                System.exit(1);
            }
        }
    }

    public File getFileName(Major major, String externalId, String inputTemplate) {
        if (major == INPUT_TEMPLATE) {
            return getTemplateFileName(externalId);
        }
        else {
            return getContentFileName(externalId, inputTemplate);
        }
    }

    private File getContentFileName(String externalId, String inputTemplate) {
        File directory;

        if (inputTemplate != null && !inputTemplate.equals("")) {
            directory = new File(contentDirectory, inputTemplate);

            mkdir(directory);
        }
        else {
            directory = contentDirectory;
        }

        return new File(directory, externalId + XML_EXTENSION);
    }

    private File getTemplateFileName(String externalId) {
        File directory = templateDirectory;

        if (externalId.startsWith(SYSTEM_TEMPLATE_PREFIX)) {
            directory = new File(directory, "system");

            mkdir(directory);
        }

        return new File(directory, externalId + XML_EXTENSION);
    }

}
