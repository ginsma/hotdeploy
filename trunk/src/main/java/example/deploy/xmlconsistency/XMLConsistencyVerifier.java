package example.deploy.xmlconsistency;

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

import example.deploy.hotdeploy.discovery.DefaultDiscoverers;
import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;

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

    private static String directoryName;

    private static String classDirectoryNames;

    private Set<String> inputTemplates = new HashSet<String>(100);
    private Map<String, String> contentTemplateByExternalId = new HashMap<String,String>(100);

    private Set<String> nonFoundContent = new HashSet<String>(100);
    private Set<String> nonFoundTemplates = new HashSet<String>(100);
    private Set<String> nonFoundClasses = new HashSet<String>(100);

    private Set<String> unusedTemplates = new HashSet<String>(100);

    private Collection<File> classDirectories;

    private File rootDirectory;

    private Collection<FileDiscoverer> discoverers;

    /**
     * Constructor.
     * @param verifier A verified from which to load present templates and
     *        content.
     * @param xmlDirectory The directory where the _import_order file is
     *        located.
     * @param classDirectories2 The directories of java class files (note that jars
     *        are not supported).
     */
    XMLConsistencyVerifier(XMLConsistencyVerifier verifier, File rootDirectory, Collection<FileDiscoverer> discoverers, File[] classDirectories) {
        this.classDirectories = new ArrayList<File>();

        if (verifier != null) {
            contentTemplateByExternalId.putAll(verifier.contentTemplateByExternalId);
            inputTemplates.addAll(verifier.inputTemplates);
            this.classDirectories.addAll(verifier.classDirectories);
        }

        this.discoverers = discoverers;
        this.rootDirectory = rootDirectory;

        if (classDirectories != null) {
            for (File classDirectory : classDirectories) {
                this.classDirectories.add(classDirectory);
            }
        }
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
            logger.log(Level.WARNING, "Found no files to deploy in " + rootDirectory);

            return false;
        }

        for (DeploymentFile file : files) {
            logger.log(Level.FINE, "Parsing " + files + "...");

            new XmlParser(file, this);
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

    public void contentFound(DeploymentFile file, String externalId, String inputTemplate) {
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

    /**
     * Command-line interface. Specify the directory as the first parameter.
     */
    public static void main(String[] args) {
        parseParameters(args);

        if (directoryName == null) {
            System.err.println("The parameter --dir is required.");
            System.exit(1);
        }

        File xmlDirectory = new File(directoryName);

        if (!xmlDirectory.canRead()) {
            logger.log(Level.WARNING,
                "The directory " + xmlDirectory.getAbsolutePath() + " does not exist.");

            System.exit(1);
        }

        File[] classDirectories = null;

        if (classDirectoryNames != null) {
            String[] classDirectoryNamesArray = classDirectoryNames.split(File.pathSeparator);

            classDirectories = new File[classDirectoryNamesArray.length];

            for (int i = 0; i < classDirectoryNamesArray.length; i++) {
                classDirectories[i] = new File(classDirectoryNamesArray[i]);

                if (!classDirectories[i].canRead()) {
                    logger.log(Level.WARNING,
                            "The class directory " + classDirectories[i].getAbsolutePath() +
                            " (specified as \"" + classDirectoryNamesArray[i] + "\") could not be read.");

                    System.exit(1);
                }
            }
        }

        XMLConsistencyVerifier verifier;

        verifier = new XMLConsistencyVerifier(null, xmlDirectory, DefaultDiscoverers.getDiscoverers(), classDirectories);

        verifier.verify();

        if (!verifier.nonFoundContent.isEmpty() ||
                !verifier.nonFoundTemplates.isEmpty() ||
                !verifier.nonFoundClasses.isEmpty()) {
            System.exit(1);
        }
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

    private static void parseParameters(String[] args) {
        String parameter = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                if (parameter != null) {
                    parameterFound(parameter, null);
                }

                parameter = arg.substring(2);

                int eq;

                if ((eq = parameter.indexOf('=')) != -1) {
                    String value = parameter.substring(eq+1);
                    parameter = parameter.substring(0, eq);
                    parameterFound(parameter, value);

                    parameter = null;
                }
            }
            else if (parameter != null) {
                parameterFound(parameter, arg);
            }
        }

        if (parameter != null) {
            parameterFound(parameter, null);
        }
    }

    private static void parameterFound(String parameter, String value) {
        if (value == null) {
            System.err.println("Parmater " + parameter + " required a value. Provide it using --" + parameter + "=<value>.");
            printParameterHelp();
            System.exit(1);
        }

        if (parameter.equals("dir")) {
            directoryName = value;
        }
        else if (parameter.equals("classdir")) {
            classDirectoryNames = value;
        }
        else {
            System.err.println("Unknown parameter " + parameter + ".");
            printParameterHelp();
            System.exit(1);
        }
    }

    private static void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err.println("  --dir The directory where the _import_order_ file or the content to import is located.");
        System.err.println("  --classdir The directory where the project classes are built to (optional).");
    }
}
