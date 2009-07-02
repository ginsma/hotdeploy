package example.deploy.hotdeploy.discovery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.DeploymentObject;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;

public class DirectoryFileDiscoverer implements FileDiscoverer {
    private static final Logger logger =
        Logger.getLogger(DirectoryFileDiscoverer.class.getName());
    private static final String XML_SUFFIX = ".xml";
    private DeploymentDirectory directory;

    public DirectoryFileDiscoverer(DeploymentDirectory directory) {
        this.directory = directory;
    }

    public DirectoryFileDiscoverer(File directory) {
        this.directory = new FileDeploymentDirectory(directory);
    }

    public List<DeploymentFile> getFilesToImport() throws NotApplicableException {
        return getFilesToImport(directory);
    }

    public void getFilesToImport(FileCollector collector) throws NotApplicableException {
        getFilesToImport(directory, collector);
    }

    private List<DeploymentFile> getFilesToImport(DeploymentDirectory directory) throws NotApplicableException {
        if (!directory.exists()) {
            throw new NotApplicableException(directory + " did not exist.");
        }

        final List<DeploymentFile> result = new ArrayList<DeploymentFile>();

        getFilesToImport(directory, new FileCollector() {
            public void collect(List<DeploymentFile> files) {
                result.addAll(files);
            }});

        return result;
    }

    private void getFilesToImport(DeploymentDirectory directory, FileCollector collector) throws NotApplicableException {
        DeploymentObject[] files = directory.listFiles();

        List<DeploymentFile> result = new ArrayList<DeploymentFile>();

        if (files.length == 0) {
            throw new NotApplicableException("There were no XML files in " + directory + ".");
        }

        int fileCount = 0;

        for (DeploymentObject file : files) {
            if (file instanceof DeploymentFile && file.getName().endsWith(XML_SUFFIX)) {
                result.add((DeploymentFile) file);
                fileCount++;
            }
        }

        logger.log(Level.INFO, "Found " + fileCount + " XML files in " + directory + ".");

        // make sure we import files with "template" in their names first,
        // since the content import is likely to be dependent on presence
        // of the templates.
        sortFiles(result);

        collector.collect(result);

        for (DeploymentObject file : files) {
            if (file instanceof DeploymentDirectory) {
                try {
                    logger.log(Level.INFO, "Recursing into " + file);

                    getFilesToImport((DeploymentDirectory) file, collector);
                } catch (NotApplicableException e) {
                }
            }
        }
    }

    /**
     * Sort the list of files. Minor sorting is by name. Major sorting is
     * 'bootstrap' in the name < 'template' in the name < other files.
     */
    protected void sortFiles(List<DeploymentFile> changedFiles) {
        Collections.sort(changedFiles, new Comparator<DeploymentFile>() {
            public int compare(DeploymentFile o1, DeploymentFile o2) {
                int result = compare(o1, o2, "bootstrap");

                if (result == 0) {
                    result = compare(o1, o2, "template");
                }

                if (result == 0) {
                    result = (o1).getName().compareTo((o2).getName());
                }

                return result;
            }

            private int compare(Object o1, Object o2, String keyword) {
                boolean keyword1 = ((DeploymentFile) o1).getName().indexOf(keyword) != -1;
                boolean keyword2 = ((DeploymentFile) o2).getName().indexOf(keyword) != -1;

                if (keyword1 && !keyword2) {
                    return -1;
                } else if (keyword2 && !keyword1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    @Override
    public String toString() {
        return "all XML files in " + directory;
    }
}
