package com.polopoly.ps.hotdeploy.xml.ordergenerator;

import java.io.File;

import com.polopoly.ps.hotdeploy.file.DeploymentObject;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.file.FileDeploymentFile;


public class RootDirectoryFinder {
    public class NoRootDirectoryException extends Exception {
    }

    private Iterable<DeploymentObject> files;

    RootDirectoryFinder(Iterable<DeploymentObject> files) {
        this.files = files;
    }

    public File findRootDirectory() throws NoRootDirectoryException {
        File root = null;

        for (DeploymentObject deploymentFile : files) {
            if (deploymentFile instanceof FileDeploymentFile ||
                deploymentFile instanceof FileDeploymentDirectory) {

                File thisDirectory;

                if (deploymentFile instanceof FileDeploymentDirectory) {
                    thisDirectory = ((FileDeploymentDirectory) deploymentFile).getFile();
                }
                else {
                    thisDirectory = ((FileDeploymentFile) deploymentFile).getFile().getParentFile();
                }

                if (thisDirectory == null) {
                    thisDirectory = new File("");
                }

                if (root == null) {
                    root = thisDirectory;
                }
                else {
                    root = greatestCommonDenominator(thisDirectory, root);

                    if (root == null) {
                        throw new NoRootDirectoryException();
                    }
                }
            }
        }

        if (root == null) {
            throw new NoRootDirectoryException();
        }

        return root;
    }

    private File greatestCommonDenominator(File file1, File file2) {
        String path1 = file1.getAbsolutePath();
        String path2 = file2.getAbsolutePath();

        int shortestLength = Math.min(path1.length(), path2.length());
        int lastDelimiter = -1;

        boolean fullyIdentical = true;

        for (int i = 0; i < shortestLength; i++) {
            if (path1.charAt(i) != path2.charAt(i)) {
                fullyIdentical = false;
                break;
            }

            if (path1.charAt(i) == File.separatorChar) {
                lastDelimiter = i;
            }
        }

        if (fullyIdentical) {
            lastDelimiter = shortestLength;
        }

        if (lastDelimiter != -1) {
            if (lastDelimiter == 0) {
                return new File(File.separator);
            }
            else {
                return new File(path1.substring(0, lastDelimiter));
            }
        }
        else {
            return null;
        }
    }
}
