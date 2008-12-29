package example.deploy.xmlconsistency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.xml.hotdeploy.FileSpec;

import example.deploy.hotdeploy.DefaultContentDeployer;

/**
 * Verifies that content XML is consistent and warns in
 * non-existing fields are referenced.
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
public class XMLConsistencyVerifier implements ParseCallback {
    private static final Logger logger = 
        Logger.getLogger(XMLConsistencyVerifier.class.getName());
       
    private Set<String> inputTemplates = new HashSet<String>(100);
    private Map<String, String> contentTemplateByExternalId = new HashMap<String,String>(100);

    private Set<String> nonFoundContent = new HashSet<String>(100);
    private Set<String> nonFoundTemplates = new HashSet<String>(100);
    private Set<String> nonFoundClasses = new HashSet<String>(100);

    private Set<String> unusedTemplates = new HashSet<String>(100);

    private File xmlDirectory;
    private Collection<File> classDirectories;

    /**
     * Constructor.
     * @param xmlDirectory The directory where the _import_order file is located.
     */
    XMLConsistencyVerifier(File xmlDirectory) {
        this.xmlDirectory = xmlDirectory;
    }
    
    /**
     * Constructor.
     * @param verifier A verified from which to load present templates and
     *        content.
     * @param xmlDirectory The directory where the _import_order file is
     *        located.
     * @param classDirectory The directory of java class files (note that jars
     *        are not supported).
     */
    XMLConsistencyVerifier(XMLConsistencyVerifier verifier, File xmlDirectory, File classDirectory) {
        classDirectories = new ArrayList<File>();
        
        if (verifier != null) {
            contentTemplateByExternalId.putAll(verifier.contentTemplateByExternalId);
            inputTemplates.addAll(verifier.inputTemplates);
            classDirectories.addAll(verifier.classDirectories);
        }
        
        this.xmlDirectory = xmlDirectory;
        
        classDirectories.add(classDirectory);
    }

    /**
     * Verify all XML files specified in the _import_order file in the specified directory.
     * @return true if the XML is consistent, false if it is not. 
     */
    public boolean verify() {
        readPresent();

        File file = new File(xmlDirectory, "_import_order");        
        
        if (!file.exists()) {
            logger.log(Level.WARNING, "The XML import order file " + file + " does not exist. Skipping XML verification.");
            return false;
        }
        
        logger.log(Level.INFO, "Starting verification of content XML in " + xmlDirectory + ".");

        List<FileSpec> importOrder = DefaultContentDeployer.getImportOrder(xmlDirectory);
        
        for (FileSpec fileSpec : importOrder) {
            logger.log(Level.FINE, "Parsing " + fileSpec + "...");
                
            new XmlParser(fileSpec.getFile(), this);
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
        
        logger.log(Level.INFO, "Verification of " + importOrder.size() + " content XML files finished.");
        
        return nonFoundContent.isEmpty() && nonFoundTemplates.isEmpty();
    }

    private void readPresent() {
        try {
            File presentContent = new File(xmlDirectory, "presentContent.txt");
            
            if (presentContent.exists()) {
                BufferedReader reader = new BufferedReader(
                    new FileReader(presentContent));
                
                String line = reader.readLine();
    
                while (line != null) {
                    contentTemplateByExternalId.put(line.trim(), null);
                    
                    line = reader.readLine();
                }
    
                reader.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        try {
            File presentTemplates = new File(xmlDirectory, "presentTemplates.txt");
            
            if (presentTemplates.exists()) {
                BufferedReader reader = new BufferedReader(
                    new FileReader(presentTemplates));
                
                String line = reader.readLine();
    
                while (line != null) {
                    inputTemplates.add(line.trim());
    
                    line = reader.readLine();
                }
    
                reader.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void contentFound(File file, String externalId, String inputTemplate) {
        if (inputTemplate != null) {
            templateReferenceFound(file, inputTemplate);
        }
        
        logger.log(Level.FINE, "Found content " + externalId + " with input template " + inputTemplate + ".");
        
        contentTemplateByExternalId.put(externalId, inputTemplate);
    }

    public void contentReferenceFound(File file, String externalId) {
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

    public void templateFound(File file, String inputTemplate) {
        if (inputTemplates.add(inputTemplate)) {
            unusedTemplates.add(inputTemplate);
        }
    }

    public void templateReferenceFound(File file, String inputTemplate) {
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
        if (args.length == 0) {
            System.err.println("Specify XML directory as first parameter.");
            
            System.exit(1);
        }
        
        File xmlDirectory = new File(args[0]);

        if (!xmlDirectory.canRead()) {
            logger.log(Level.WARNING, 
                "The directory " + xmlDirectory.getAbsolutePath() + " does not exist.");
            
            System.exit(1);
        }

        File classDirectory = null;
        
        if (args.length > 1) {
            classDirectory = new File(args[1]);

            if (!classDirectory.canRead()) {
                logger.log(Level.WARNING, 
                    "The class directory " + xmlDirectory.getAbsolutePath() + " does not exist.");
                
                System.exit(1);
            }
        }
        
        XMLConsistencyVerifier verifier;
        
        if (classDirectory != null) {
            verifier = new XMLConsistencyVerifier(null, xmlDirectory, classDirectory);
        }
        else {
            verifier = new XMLConsistencyVerifier(xmlDirectory);
        }
        
        verifier.verify();
        
        if (!verifier.nonFoundContent.isEmpty() ||
                !verifier.nonFoundTemplates.isEmpty() ||
                !verifier.nonFoundClasses.isEmpty()) {
            System.exit(1);
        }
    }

    public void classReferenceFound(File file, String className) {
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
}
