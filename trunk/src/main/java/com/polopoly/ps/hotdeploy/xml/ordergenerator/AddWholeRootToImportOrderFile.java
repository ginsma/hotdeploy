package com.polopoly.ps.hotdeploy.xml.ordergenerator;

import java.io.File;
import java.util.List;

import com.polopoly.ps.hotdeploy.discovery.DirectoryFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.FileCollector;
import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFile;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.xml.ordergenerator.RootDirectoryFinder.NoRootDirectoryException;


/**
 * Finds the most general directory in an import order file and, if all of the files
 * are in the import order, adds the directory as the last line. The point is that if
 * files are added in the future, they will be automatically imported.
 */
public class AddWholeRootToImportOrderFile {
    public class WholeRootNotCoveredException extends RuntimeException {
    }

    private ImportOrderFile importOrderFile;

    public AddWholeRootToImportOrderFile(ImportOrderFile importOrderFile) {
        this.importOrderFile = importOrderFile;
    }

    public void addWholeRoot() {
        File root;

        try {
            root = new RootDirectoryFinder(importOrderFile).findRootDirectory();
        } catch (NoRootDirectoryException e) {
            return;
        }

        if (importOrderFile.imports(new FileDeploymentDirectory(root))) {
            // whole root already in import order
            return;
        }

        try {
            new DirectoryFileDiscoverer(new FileDeploymentDirectory(root)).getFilesToImport(
                    new FileCollector() {
                public void collect(List<DeploymentFile> filesInDirectory) {
                    for (DeploymentFile fileInDirectory : filesInDirectory) {
                        if (!importOrderFile.imports(fileInDirectory)) {
                            throw new WholeRootNotCoveredException();
                        }
                }}});

            importOrderFile.addDeploymentObject(new FileDeploymentDirectory(root));
        } catch (NotApplicableException e) {
            // fine. skip.
        } catch (WholeRootNotCoveredException e) {
            // ok, we cannot add the whole directory since that would cover files
            // that are not currently in the import order.
        }
    }
}
