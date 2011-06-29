package com.polopoly.ps.deploy.xml.ordergenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.ps.deploy.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.deploy.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;


public class DiscovereredFilesAggregator {
    private Collection<FileDiscoverer> discoverers;

    private static final Logger logger =
        Logger.getLogger(DiscovereredFilesAggregator.class.getName());

    public DiscovereredFilesAggregator(Collection<FileDiscoverer> discoverers) {
        this.discoverers = discoverers;
    }

    public Set<DeploymentFile> getFiles() {
        Set<DeploymentFile> files = new HashSet<DeploymentFile>();

        for (FileDiscoverer discoverer : discoverers) {
            try {
                List<DeploymentFile> theseFiles = discoverer.getFilesToImport();

                logger.log(Level.WARNING, discoverer + " identified " + theseFiles.size() + " file(s) to verify.");

                files.addAll(theseFiles);
            } catch (NotApplicableException e) {
                logger.log(Level.INFO, "Cannot apply discovery strategy " + discoverer + ": " + e.getMessage());
            }
        }

        return files;
    }
}
