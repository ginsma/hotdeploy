package example.deploy.hotdeploy.deployer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.io.DispatchingDocumentImporter;
import com.polopoly.common.xml.DOMUtil;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentFile;

public class DefaultSingleFileDeployer implements SingleFileDeployer {
    private static final Logger logger =
        Logger.getLogger(DefaultSingleFileDeployer.class.getName());
    private PolicyCMServer server;
    private DispatchingDocumentImporter importer;

    public DefaultSingleFileDeployer(PolicyCMServer server) {
        this.server = server;
    }

    /* (non-Javadoc)
     * @see example.deploy.hotdeploy.deployer.ISingleFileDeployer#prepare()
     */
    public void prepare() throws ParserConfigurationException {
        importer = new DispatchingDocumentImporter(server);
    }

    private void setImporterBaseUrl(DispatchingDocumentImporter importer,
            DeploymentFile fileToImport) {
        try {
            URL baseUrl = fileToImport.getBaseUrl();

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Using base URL " + baseUrl);
            }

            importer.setBaseUrl(baseUrl);
        }
        catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create base URL for file " + fileToImport, e);
        }
    }

    private void importFile(DeploymentFile fileToImport) throws Exception {
        setImporterBaseUrl(importer, fileToImport);

        DocumentBuilder documentBuilder = DOMUtil.newDocumentBuilder();
        Document xmlDocument =
            documentBuilder.parse(fileToImport.getInputStream());

        if (fileToImport instanceof JarDeploymentFile) {
            importer.importXML(
                xmlDocument,
                ((JarDeploymentFile) fileToImport).getJarFile(),
                ((JarDeploymentFile) fileToImport).getNameWithinJar());
        }
        else {
            importer.importXML(
                xmlDocument,
                null,
                ((FileDeploymentFile) fileToImport).getDirectory());
        }
    }

    /* (non-Javadoc)
     * @see example.deploy.hotdeploy.deployer.ISingleFileDeployer#importAndHandleException(example.deploy.hotdeploy.file.DeploymentFile)
     */
    public boolean importAndHandleException(DeploymentFile fileToImport) throws FatalDeployException {
        if (importer == null) {
            throw new FatalDeployException("prepare() must be called before import.");
        }

        try {
            importFile(fileToImport);

            logger.log(Level.INFO, "Import of " + fileToImport + " done.");

            return true;
        }
        catch (PermissionDeniedException e) {
            throw new FatalDeployException(e);
        }
        catch (ParserConfigurationException e) {
            throw new FatalDeployException(e);
        }
        catch (Exception e) {
            logger.log(Level.WARNING,
                "Import of " + fileToImport + " failed: " + e.getMessage(), e);

            return false;
        }
    }
}
