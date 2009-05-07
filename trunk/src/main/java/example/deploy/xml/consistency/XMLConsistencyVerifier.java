package example.deploy.xml.consistency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.XmlParser;

/**
 * Verifies that content XML is consistent and warns in
 * non-existing fields are referenced.
 * @author AndreasE
 */
public class XMLConsistencyVerifier implements ParseCallback {
    private static final String PRESENT_CONTENT_FILE = "presentContent.txt";

    private static final String PRESENT_TEMPLATES_FILE = "presentTemplates.txt";

    private static final Logger logger =
        Logger.getLogger(XMLConsistencyVerifier.class.getName());

    private Set<String> inputTemplates = new HashSet<String>(100);
    private Map<String, String> contentTemplateByExternalId = new HashMap<String,String>(100);

    private Set<String> nonFoundContent = new HashSet<String>(100);
    private Set<String> nonFoundTemplates = new HashSet<String>(100);
    private Set<String> nonFoundClasses = new HashSet<String>(100);

    private Set<String> unusedTemplates = new HashSet<String>(100);

    private Collection<File> classDirectories = new ArrayList<File>();

    private File rootDirectory;

    private Collection<FileDiscoverer> discoverers;

    XMLConsistencyVerifier(XMLConsistencyVerifier verifier, Collection<FileDiscoverer> discoverers) {
        this(discoverers);

        contentTemplateByExternalId.putAll(verifier.contentTemplateByExternalId);
        inputTemplates.addAll(verifier.inputTemplates);
        this.classDirectories.addAll(verifier.classDirectories);
    }

    public XMLConsistencyVerifier(Collection<FileDiscoverer> discoverers) {
        this.discoverers = discoverers;
    }

    /**
     * Verify all XML files specified in the _import_order file in the specified directory.
     * @return true if the XML is consistent, false if it is not.
     */
    public boolean verify() {
        readPresent();

        logger.log(Level.INFO, "Starting verification of content XML in " + rootDirectory + ".");

        List<DeploymentFile> files = new ArrayList<DeploymentFile>();

        for (FileDiscoverer discoverer : discoverers) {
            try {
                List<DeploymentFile> theseFiles = discoverer.getFilesToImport(rootDirectory);

                logger.log(Level.WARNING, discoverer + " identified " + theseFiles.size() + " file(s) to verify.");

                files.addAll(theseFiles);
            } catch (NotApplicableException e) {
                logger.log(Level.INFO, "Cannot apply discovery strategy " + discoverer + ": " + e.getMessage(), e);
            }
        }

        if (files == null) {
            logger.log(Level.WARNING, "Found no files to verify in " + rootDirectory);

            return false;
        }

        for (DeploymentFile file : files) {
            logger.log(Level.FINE, "Parsing " + files + "...");

            new XmlParser().parse(file, this);
        }

        if (!nonFoundContent.isEmpty()) {
            logger.log(Level.WARNING, "The following content objects " +
                "were referenced before they were declared: " + nonFoundContent + ".");
        }

        if (!nonFoundTemplates.isEmpty()) {
            logger.log(Level.WARNING, "The following templates " +
                "were referenced before they were declared: " + nonFoundTemplates + ".");
        }

        if (!nonFoundClasses.isEmpty()) {
            logger.log(Level.WARNING, "The following policy classes " +
                "did not exist: " + nonFoundClasses + ".");
        }

        if (!unusedTemplates.isEmpty()) {
            logger.log(Level.FINE, "The following templates are defined but never used: " + unusedTemplates);
        }

        logger.log(Level.INFO, "Verification of " + files.size() + " content XML files finished.");

        return nonFoundContent.isEmpty() && nonFoundTemplates.isEmpty();
    }

    private void readPresent() {
        FileDeploymentDirectory directory = new FileDeploymentDirectory(rootDirectory);

        try {
            DeploymentFile presentContent =
                CheckedCast.cast(directory.getFile(PRESENT_CONTENT_FILE), DeploymentFile.class);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(presentContent.getInputStream()));

            String line = reader.readLine();

            while (line != null) {
                contentTemplateByExternalId.put(line.trim(), null);

                line = reader.readLine();
            }

            reader.close();
        } catch (FileNotFoundException e) {
            // ignore
        } catch (CheckedClassCastException e) {
            logger.log(Level.WARNING, PRESENT_CONTENT_FILE + " in " + directory + " does not seem to be an ordinary file.");
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        try {
            DeploymentFile presentTemplates =
                CheckedCast.cast(directory.getFile(PRESENT_TEMPLATES_FILE), DeploymentFile.class);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(presentTemplates.getInputStream()));

            String line = reader.readLine();

            while (line != null) {
                inputTemplates.add(line.trim());

                line = reader.readLine();
            }

            reader.close();
        } catch (FileNotFoundException e) {
            // ignore
        } catch (CheckedClassCastException e) {
            logger.log(Level.WARNING, PRESENT_TEMPLATES_FILE + " in " + directory + " does not seem to be an ordinary file.");
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void contentFound(DeploymentFile file, String externalId, Major major, String inputTemplate) {
        if (inputTemplate != null) {
            templateReferenceFound(file, inputTemplate);
        }

        logger.log(Level.FINE, "Found content " + externalId + " with input template " + inputTemplate + ".");

        contentTemplateByExternalId.put(externalId, inputTemplate);
    }

    public void contentReferenceFound(DeploymentFile file, String externalId) {
        if (!contentTemplateByExternalId.containsKey(externalId)) {
            if (inputTemplates.contains(externalId)) {
                unusedTemplates.remove(externalId);
            }
            else {
                nonFoundContent.add(externalId);
                logger.log(Level.WARNING, "Undefined content " + externalId + " was referenced in " + file);
            }
        }
    }

    public void templateFound(DeploymentFile file, String inputTemplate) {
        if (inputTemplates.add(inputTemplate)) {
            unusedTemplates.add(inputTemplate);
        }
    }

    public void templateReferenceFound(DeploymentFile file, String inputTemplate) {
        if (!inputTemplates.contains(inputTemplate)) {
            nonFoundTemplates.add(inputTemplate);
            logger.log(Level.WARNING, "Undefined template " + inputTemplate + " was referenced in " + file);
        }

        unusedTemplates.remove(inputTemplate);
    }

    public void classReferenceFound(DeploymentFile file, String className) {
        if (className.startsWith("com.polopoly")) {
            return;
        }

        StringBuffer fileName = new StringBuffer(className.length() + 10);

        for (int i = 0; i < className.length(); i++) {
            char ch = className.charAt(i);

            if (ch == '.') {
                fileName.append(File.separatorChar);
            }
            else {
                fileName.append(ch);
            }
        }

        fileName.append(".class");

        if (classDirectories != null) {
            boolean found = false;

            for (File classDirectory : classDirectories) {
                if ((new File(classDirectory, fileName.toString())).exists()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                logger.log(Level.WARNING, "Unknown class " + className + " was referenced in file " + file + ".");
                nonFoundClasses.add(className);
            }
        }
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void addClassDirectory(File classDirectory) {
        classDirectories.add(classDirectory);
    }

    public boolean areErrorsFound() {
        return !nonFoundContent.isEmpty() ||
            !nonFoundTemplates.isEmpty() ||
            !nonFoundClasses.isEmpty();
    }
}
