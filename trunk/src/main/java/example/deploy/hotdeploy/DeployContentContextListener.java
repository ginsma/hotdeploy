package example.deploy.hotdeploy;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.polopoly.application.InternalApplicationUtil;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.UserServer;

/**
 * A {@link javax.servlet.ServletContextListener} that deploys all content in
 * META-INF/content not already present in the database on startup.
 *
 * @author AndreasE
 */
public class DeployContentContextListener implements ServletContextListener {

    private static final Logger logger =
        Logger.getLogger(DeployContentContextListener.class.getName());

    /**
     * The directories content may lie in. Scanned in this order.
     */
    private static final String[] DIRECTORIES = {
        "/META-INF/content",
        "/WEB-INF/classes/content",
        "/WEB-INF/content"
    };

    public void contextInitialized(ServletContextEvent event) {
        try {
            PolicyCMServer server = InternalApplicationUtil.getPolicyCMServer(event.getServletContext());
            UserServer userServer = InternalApplicationUtil.getUserServer(event.getServletContext());

            Deploy.doInitialDeploy(server, userServer, getDirectory(event.getServletContext()),
                            getContentDeployer(server, event.getServletContext()));
        }
        // don't ever throw an exception since it stops the deploy process.
        catch (Throwable t) {
            logger.logp(Level.WARNING, "DeployContentContextListener", "contextInitialized",  t.getMessage(), t);
        }
    }

    /**
     * Get the ContentDeployer configured.
     */
    protected ContentDeployer getContentDeployer(PolicyCMServer server, ServletContext context)
    {
        String className = context.getInitParameter("contentDeployer");

        if (className != null) {
            Object contentDeployer;

            try {
                contentDeployer = Class.forName(className).newInstance();
                if (contentDeployer instanceof ContentDeployer) {
                    logger.log(Level.INFO, "Using '" + className + "' as ContentDeployer.");
                    return (ContentDeployer) contentDeployer;
                }
                else {
                    logger.log(Level.WARNING, "Content deployer class '" + className
                               + "' not instance of ContentDeployer.");
                }
            }
            catch (ClassNotFoundException e) {
                logger.log(Level.WARNING, "Content deployer class '" + className
                           + "' not found.", e);
            }
            catch (InstantiationException  e) {
                logger.log(Level.WARNING, "Could not instantiate content deployer class '"
                           + className + "'.", e);
            }
            catch (IllegalAccessException  e) {
                logger.log(Level.WARNING, "Could not instantiate content deployer class '"
                           + className + "'.", e);
            }
        }

        logger.log(Level.INFO, "Using default ContentDeployer.");
        return new DefaultContentDeployer(server);
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

   static File getDirectory(ServletContext context) {
       File directory = null;

       if (new File(context.getRealPath("/")).getAbsolutePath().endsWith("src" + File.separator + "main" + File.separator + "webapp")) {
           // oh, look, a maven project running in Jetty!
           directory = new File(new File(context.getRealPath("/")).getParentFile().getAbsoluteFile() +
                   File.separator + "resources" + File.separator + "content");

           if (directory.exists() && directory.isDirectory()) {
               return directory;
           }
           else {
               logger.log(Level.FINE, "Could not find possible hotdeploy directory " +
                       directory.getAbsolutePath() + ".");
           }
       }

       for (int i = 0; i < DIRECTORIES.length; i++) {
           directory = new File(context.getRealPath(DIRECTORIES[i]));

           if (directory.exists() && directory.isDirectory()) {
               return directory;
           }
           else {
               logger.log(Level.FINE, "Could not find possible hotdeploy directory " +
                       directory.getAbsolutePath() + ".");
           }
       }

       logger.log(Level.WARNING, "None of the standard hotdeploy directories " +
               Arrays.toString(DIRECTORIES) + " could be found (absolute path is e.g. " + directory.getAbsolutePath() + ").");

       return directory;
   }
}
