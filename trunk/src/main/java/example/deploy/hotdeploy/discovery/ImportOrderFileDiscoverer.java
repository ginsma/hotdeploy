package example.deploy.hotdeploy.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.DeploymentObject;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;

public class ImportOrderFileDiscoverer implements FileDiscoverer {
    private static final Logger logger =
        Logger.getLogger(ImportOrderFileDiscoverer.class.getName());

    private static final Set<String> NON_EXISTING_FILES = new HashSet<String>();

    static final String IMPORT_ORDER_FILE_NAME = "_import_order";

    private static final String XML_EXTENSION = ".xml";

    private static final String DEPENDENCY_PREFIX = "depends:";

    public List<DeploymentFile> getFilesToImport(File directory) throws NotApplicableException {
        if (directory == null) {
            throw new NotApplicableException("No root directory available.");
        }

        List<DeploymentFile> result = getFilesToImport(new FileDeploymentDirectory(directory));

        logger.log(Level.INFO, "Found " + result.size() + " content file(s) in " + directory + ".");

        return result;

    }

    private void parseDependency(String line, ImportOrderFile list) {
        list.addDependency(line.substring(DEPENDENCY_PREFIX.length()).trim());
    }

    private boolean isDependencyDeclaration(String line) {
        return line.startsWith(DEPENDENCY_PREFIX);
    }

    private boolean isLineThatShouldBeSkipped(String line) {
        return line.charAt(0) == '#' || line.trim().equals("");
    }

    private boolean isNotAFile(String line) {
        return line.indexOf(':') != -1;
    }

    public ImportOrderFile getFilesToImport(DeploymentDirectory directory) throws NotApplicableException {
        ImportOrderFile list = new ImportOrderFile(directory);

        try {
            DeploymentFile importOrderFile = CheckedCast.cast(
                directory.getFile(IMPORT_ORDER_FILE_NAME), DeploymentFile.class);

            InputStream is = importOrderFile.getInputStream();

            logger.log(Level.FINE, "Reading import order from " + importOrderFile + ".");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while (true) {
                String line = br.readLine();

                if (line == null) {
                    break; // End of file.
                }

                if (isDependencyDeclaration(line)) {
                    parseDependency(line, list);
                    continue;
                }

                if (isLineThatShouldBeSkipped(line)) {
                    continue;
                }

                if (isNotAFile(line)) {
                    logger.log(Level.WARNING, "The line \"" + line + "\" in " + importOrderFile + " does not seem to denote a file name.");
                    continue;
                }

                try {
                    DeploymentObject file = directory.getFile(line);

                    if (file instanceof DeploymentDirectory) {
                        addDirectory(list, (DeploymentDirectory) file);
                    }
                    else if (file instanceof DeploymentFile) {
                        addFile(list, (DeploymentFile) file);
                    }
                } catch (FileNotFoundException e) {
                    if (NON_EXISTING_FILES.add(line)) {
                        logger.log(Level.WARNING, "A directory or file specified in " +
                                importOrderFile + " does not exist: " + e.getMessage());
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            throw new NotApplicableException("The import order file could not be read: " + e.getMessage());
        } catch (CheckedClassCastException e) {
            throw new NotApplicableException("The import order file " + IMPORT_ORDER_FILE_NAME + " in " + directory + " is not an ordinary file.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not read file " + IMPORT_ORDER_FILE_NAME + " in " + directory + ".");
        }

        return list;
    }

    private static int addDirectory(ArrayList<DeploymentFile> list, DeploymentDirectory directory) {
    	if (logger.isLoggable(Level.FINEST)) {
    		logger.log(Level.FINEST, "Adding whole directory " + directory + ".");
    	}

        DeploymentObject[] files = directory.listFiles();

        Arrays.sort(files);

        int fileCount = 0;

        for (DeploymentObject file : files) {
            if (file instanceof DeploymentDirectory) {
            	fileCount += addDirectory(list, (DeploymentDirectory) file);
            }
            else if (file instanceof DeploymentFile) {
            	fileCount += addFile(list, (DeploymentFile) file);
            }
        }

        if (fileCount == 0) {
            logger.log(Level.FINE, "The directory " + directory + " did not contain any importable files.");
        }
        else {
            logger.log(Level.FINE, "Added directory '" + directory + "' with " + fileCount + " file(s).");
        }

        return fileCount;
    }

    private static int addFile(ArrayList<DeploymentFile> list, DeploymentFile file) {
        int result = 0;

        if (file.getName().endsWith(XML_EXTENSION) && !list.contains(file)) {
        	if (logger.isLoggable(Level.FINEST)) {
        		logger.log(Level.FINEST, "Adding file " + file + ".");
        	}

        	list.add(file);
            result++;
        }

        return result;
    }

    @Override
    public String toString() {
        return "files specified in " + IMPORT_ORDER_FILE_NAME;
    }
}
