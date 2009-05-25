package example.deploy.xml.present;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.DeploymentObject;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.ResourceFile;
import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;

public class PresentFileReader {
    private static final Logger logger =
        Logger.getLogger(PresentFileReader.class.getName());

    private static final String PRESENT_FILES_RESOURCE_DIRECTORY = "/content/";
    private static final String PRESENT_CONTENT_FILE = "presentContent.txt";
    private static final String PRESENT_TEMPLATES_FILE = "presentTemplates.txt";

    private File rootDirectory;
    private PresentFilesAware presentFilesAware;

    public PresentFileReader(File rootDirectory, PresentFilesAware presentFilesAware) {
        this.rootDirectory = rootDirectory;
        this.presentFilesAware = presentFilesAware;
    }

    public PresentFileReader(PresentFilesAware presentFilesAware) {
        this.presentFilesAware = presentFilesAware;
    }

    public void read() {
        if (rootDirectory != null) {
            readFromRootDirectory();
        }

        readFromResource();
    }

    private void readFromResource() {
        DeploymentFile presentContentResourceFile = new ResourceFile(PRESENT_FILES_RESOURCE_DIRECTORY + PRESENT_CONTENT_FILE);
        readPresentContent(presentContentResourceFile);

        DeploymentFile presentTemplatesResourceFile = new ResourceFile(PRESENT_FILES_RESOURCE_DIRECTORY + PRESENT_TEMPLATES_FILE);
        readPresentTemplates(presentTemplatesResourceFile);
    }

    private void readFromRootDirectory() {
        FileDeploymentDirectory directory = new FileDeploymentDirectory(rootDirectory);

        try {
            DeploymentObject presentContentFile = directory.getFile(PRESENT_CONTENT_FILE);
            readPresentContent(presentContentFile);
        } catch (FileNotFoundException e) {
            // fine.
        }

        try {
            DeploymentObject presentTemplatesFile = directory.getFile(PRESENT_TEMPLATES_FILE);
            readPresentTemplates(presentTemplatesFile);
        } catch (FileNotFoundException e) {
            // fine.
        }
    }

    private void readPresentContent(DeploymentObject presentContentFile) {
        readListOfExternalIds(presentContentFile, Major.UNKNOWN);
    }

    private void readPresentTemplates(DeploymentObject presentTemplatesFile) {
        readListOfExternalIds(presentTemplatesFile, Major.INPUT_TEMPLATE);
    }

    private void readListOfExternalIds(DeploymentObject file, Major major) {
        try {
            DeploymentFile presentContent =
                CheckedCast.cast(file, DeploymentFile.class);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(presentContent.getInputStream()));

            String line = reader.readLine();

            while (line != null) {
                line = line.trim();

                if (ignoreLine(line)) {
                    continue;
                }

                if (major == Major.INPUT_TEMPLATE) {
                    presentFilesAware.presentTemplate(line);
                }
                else {
                    presentFilesAware.presentContent(line);
                }

                line = reader.readLine();
            }

            reader.close();
        } catch (FileNotFoundException e) {
            // ignore
        } catch (CheckedClassCastException e) {
            logger.log(Level.WARNING, file + " does not seem to be an ordinary file.");
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private boolean ignoreLine(String line) {
        return line.equals("") || line.startsWith("#");
    }

}

