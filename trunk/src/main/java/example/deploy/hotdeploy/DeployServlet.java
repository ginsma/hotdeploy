package example.deploy.hotdeploy;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.AlwaysChangedDirectoryState;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil;
import com.polopoly.cm.xml.io.DispatchingDocumentImporter;

/**
 * A servlet that deploys the file(s) specified as parameters (e.g.
 * "DeploymentServlet?content.xml&/templates/basic_templates.xml"). If no files
 * are specified, all files in META-INF/content are deployed. File names without
 * paths are assumed to be located in the META-INF/content directory and
 * relative paths to be relative to the web root. If a web application is
 * running this method of importing is likely to be faster than using the
 * "polopoly xmlio" command and it will also be immediately available in the web
 * application's cache after import.
 *
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
public class DeployServlet extends HttpServlet {
    private static final Logger logger =
        Logger.getLogger(DeployServlet.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        super.service(request, response);

        Enumeration<String> parameterNames = request.getParameterNames();

        DispatchingDocumentImporter importer;

        PolicyCMServer server = ApplicationUtil.getInitializedServer();

        try {
            importer = new DispatchingDocumentImporter(server);
        }
        catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, "Failed to create importer.", e);
            return;
        }

        int paramCount = 0;

        while (parameterNames.hasMoreElements()) {
            importFile(importer, parameterNames.nextElement());
            paramCount++;
        }

        if (paramCount == 0) {
            try {
                (new DefaultContentDeployer(server)).deploy(
                    DeployContentContextListener.getDirectory(getServletContext()),
                        new AlwaysChangedDirectoryState());
            } catch (PermissionDeniedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                throw new ServletException(e);
             } catch (CouldNotUpdateStateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                throw new ServletException(e);
            }
        }
    }

    private void importFile(DispatchingDocumentImporter importer, String fileName) {
        File file;

        if (fileName.indexOf('\\') >= 0 || fileName.indexOf('/') >= 0) {
            if (fileName.charAt(0) == '\\' || fileName.charAt(0) == '/') {
                file = new File(fileName);
            }
            else {
                file = new File(getServletContext().getRealPath("/"), fileName);
            }
        }
        else {
            file = new File(DeployContentContextListener.getDirectory(getServletContext()), fileName);
        }

        try {
            importer.setBaseUrl(file.getParentFile().toURL());
        }
        catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create base url from directory.", e);
        }
    }

}
