package example.deploy.hotdeploy.deployer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.impl.exceptions.LockException;
import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.io.DispatchingDocumentImporter;
import com.polopoly.common.xml.DOMUtil;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.hotdeploy.file.JarDeploymentFile;
import example.deploy.text.CMServerValidationContext;
import example.deploy.text.TextContentDeployer;
import example.deploy.text.TextContentParser;
import example.deploy.text.TextContentSet;

public class DefaultSingleFileDeployer implements SingleFileDeployer {
    private static final Logger logger = Logger
            .getLogger(DefaultSingleFileDeployer.class.getName());

    private PolicyCMServer server;

    private DispatchingDocumentImporter importer;

    public DefaultSingleFileDeployer(PolicyCMServer server) {
        this.server = server;
    }

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
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create base URL for file "
                    + fileToImport, e);
        }
    }

    private void importFile(DeploymentFile fileToImport) throws Exception {
        if (fileToImport.getName().endsWith(
                '.' + TextContentParser.TEXT_CONTENT_FILE_EXTENSION)) {
            TextContentSet textContent = new TextContentParser(fileToImport
                    .getInputStream(), fileToImport.getBaseUrl(), fileToImport
                    .getName()).parse();

            textContent.validate(new CMServerValidationContext(server));

            for (Policy createdPolicy : new TextContentDeployer(textContent,
                    server).deploy()) {
                contentCommitted(createdPolicy.getContentId());
            }

            return;
        }

        setImporterBaseUrl(importer, fileToImport);

        DocumentBuilder documentBuilder = DOMUtil.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(fileToImport
                .getInputStream());

        if (fileToImport instanceof JarDeploymentFile) {
            importer.importXML(xmlDocument, ((JarDeploymentFile) fileToImport)
                    .getJarFile(), ((JarDeploymentFile) fileToImport)
                    .getNameWithinJar());
        } else {
            importer.importXML(xmlDocument, null,
                    ((FileDeploymentFile) fileToImport).getDirectory());
        }
    }

    protected void contentCommitted(ContentId createdId) {
    }

    private static Throwable contains(Throwable t, Class<?> klass) {
        if (klass.isAssignableFrom(t.getClass())) {
            return t;
        }

        if (t.getCause() != null) {
            return contains(t.getCause(), klass);
        }

        return null;
    }

    public boolean importAndHandleException(DeploymentFile fileToImport)
            throws FatalDeployException {
        if (importer == null) {
            throw new FatalDeployException(
                    "prepare() must be called before import.");
        }

        try {
            importFile(fileToImport);

            logger.log(Level.INFO, "Import of " + fileToImport + " done.");

            return true;
        } catch (PermissionDeniedException e) {
            throw new FatalDeployException(e);
        } catch (ParserConfigurationException e) {
            throw new FatalDeployException(e);
        } catch (Exception e) {
            LockException lockException = (LockException) contains(e,
                    LockException.class);

            if (lockException != null) {
                ContentId lockedId = null;

                if (lockException.getLockInfo() != null) {
                    lockedId = lockException.getLockInfo().getLocked();
                } else {
                    // there are two locked exceptions: something being locked
                    // by someone else
                    // and something not being locked while trying to modify.
                    if (e.getMessage().indexOf("is not locked") != -1) {
                        throw new FatalDeployException(e);
                    }

                    int i = e.getMessage().indexOf("ContentId(");

                    if (i != -1) {
                        int j = e.getMessage().indexOf(")", i + 1);

                        String contentIdString = e.getMessage().substring(
                                i + 10, j);

                        try {
                            lockedId = ContentIdFactory
                                    .createContentId(contentIdString);
                        } catch (IllegalArgumentException iae) {
                            logger.log(Level.WARNING,
                                    "Could not parse content ID \""
                                            + contentIdString
                                            + "\"in error message.");
                        }
                    }
                }

                if (lockedId == null) {
                    logger.log(Level.WARNING, "Import of " + fileToImport
                            + " failed: " + e.getMessage());

                    return false;
                }

                try {
                    Content content = (Content) server
                            .getContent(new VersionedContentId(lockedId,
                                    VersionedContentId.LATEST_VERSION));

                    if (content.getLockInfo() == null) {
                        throw new FatalDeployException(e);
                    }

                    logger.log(Level.WARNING, lockedId.getContentIdString()
                            + " was locked. Trying to unlock it.");

                    content.forcedUnlock();
                } catch (CMException cmException) {
                    logger.log(Level.WARNING,
                            "While unlocking: " + cmException, cmException);
                    logger.log(Level.WARNING, "Import of " + fileToImport
                            + " failed: " + e.getMessage());

                    return false;
                }

                logger.log(Level.INFO, "Retrying import...");

                return importAndHandleException(fileToImport);
            }

            String message = e.getMessage();

            if (message == null) {
                message = e.toString();
            }

            logger.log(Level.WARNING, "Import of " + fileToImport + " failed: "
                    + message, (e instanceof RuntimeException ? e : null));

            return false;
        }
    }
}
