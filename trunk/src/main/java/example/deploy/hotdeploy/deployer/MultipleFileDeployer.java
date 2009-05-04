package example.deploy.hotdeploy.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.CouldNotUpdateStateException;
import example.deploy.hotdeploy.state.DirectoryState;

public class MultipleFileDeployer
{
    private static final Logger logger =
        Logger.getLogger(MultipleFileDeployer.class.getName());
    private boolean failFast;
    private File rootDirectory;
    private DirectoryState directoryState;
    private ClassLoader oldClassLoader;

    private Set<DeploymentFile> failedFiles = new HashSet<DeploymentFile>();
    private Set<DeploymentFile> successfulFiles = new HashSet<DeploymentFile>();
    private SingleFileDeployer deployer;

    public MultipleFileDeployer(SingleFileDeployer deployer, File rootDirectory, DirectoryState directoryState) {
        this(deployer, rootDirectory, directoryState, false);
    }

    public MultipleFileDeployer(SingleFileDeployer deployer, File rootDirectory, DirectoryState directoryState, boolean failFast) {
        this.failFast = failFast;
        this.deployer = deployer;
        this.rootDirectory = rootDirectory;
        this.directoryState = directoryState;
    }

    private void logFileChanged(DeploymentFile fileToImport) {
        logger.log(Level.INFO, fileToImport + " had changed on disk. Importing it.");
    }

    private void logFiledUnchanged(DeploymentFile fileToImport) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, fileToImport + " had not changed. Skipping it.");
        }
    }

    private void swapClassLoader() {
        Thread currentThread = Thread.currentThread();

        oldClassLoader =
            currentThread.getContextClassLoader();

        currentThread.setContextClassLoader(
            MultipleFileDeployer.class.getClassLoader());
    }

    private void restoreClassLoader() {
        if (oldClassLoader != null) {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public Set<DeploymentFile> deploy(Collection<DeploymentFile> filesToImport)
            throws FatalDeployException {
        logger.log(Level.INFO, "Found " + filesToImport.size() + " content file(s) in total. Importing those that have been modified...");

        try {
            deployer.prepare();

            swapClassLoader();

            for (DeploymentFile fileToImport : filesToImport) {
                if (!directoryState.hasFileChanged(fileToImport)) {
                    logFiledUnchanged(fileToImport);
                    continue;
                }

                logFileChanged(fileToImport);

                boolean success = deployer.importAndHandleException(fileToImport);

                if (success) {
                    successfulFiles.add(fileToImport);
                }
                else {
                    failedFiles.add(fileToImport);
                }

                directoryState.reset(fileToImport, !success);

                if (!success && failFast) {
                    break;
                }
            }
        }
        catch (ParserConfigurationException e) {
            throw new FatalDeployException("Failed to create importer: " + e.getMessage(), e);
        }
        finally {
            restoreClassLoader();
        }

        try {
            directoryState.persist();
        } catch (CouldNotUpdateStateException e) {
            throw new FatalDeployException("Could not record deployment state after deploy: " + e.getMessage(), e);
        }

        int unmodifiedFiles = filesToImport.size() - successfulFiles.size() - failedFiles.size();

        logger.log(Level.INFO, "Imported " + successfulFiles.size() + " file(s) successfully. " +
                unmodifiedFiles + " file(s) had not been modified. " +
    		"Import failed for " + failedFiles.size() + " file(s)");

        return failedFiles;
    }

    public Set<DeploymentFile> discoverAndDeploy(Collection<FileDiscoverer> discoverers)
            throws FatalDeployException {
        List<DeploymentFile> files = new ArrayList<DeploymentFile>();

        for (FileDiscoverer discoverer : discoverers) {
            try {
                files.addAll(discoverer.getFilesToImport(rootDirectory));
            } catch (NotApplicableException e) {
                logger.log(Level.INFO, "Cannot apply discovery strategy " + discoverer + ": " + e.getMessage(), e);
            }
        }

        if (files.isEmpty()) {
            logger.log(Level.INFO, "Could not find any content files to import.");

            return Collections.emptySet();
        }

        return deploy(files);
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }
}

