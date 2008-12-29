package example.deploy.hotdeploy;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.AlwaysChangedDirectoryState;
import com.polopoly.cm.xml.hotdeploy.DirectoryState;
import com.polopoly.cm.xml.hotdeploy.DirectoryStatePolicy;
import com.polopoly.cm.xml.hotdeploy.FileSpec;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil;
import com.polopoly.cm.xml.hotdeploy.util.PolicySingletonUtil;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil.ApplicationNotInitializedException;
import com.polopoly.cm.xml.hotdeploy.util.UserUtil.LoginFailedException;

/**
 * A {@link javax.servlet.ServletContextListener} that deploys all content in
 * META-INF/content not already present in the database on startup.
 * 
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
public class DeployContentContextListener implements ServletContextListener {
    protected static final String EXTERNAL_ID = "p.HotDeployDirectoryState";

    private static final Logger logger = 
        Logger.getLogger(DeployContentContextListener.class.getName());

    private static DirectoryStatePolicy directoryState;

    public void contextInitialized(ServletContextEvent event) {
        try {
            doInitialDeploy(getDirectory(event.getServletContext()),
                            getContentDeployer(event.getServletContext()));
        }
        // don't ever throw an exception since it stops the deploy process.
        catch (Throwable t) {
            logger.logp(Level.WARNING, "DeployContentContextListener", "contextInitialized",  t.getMessage(), t);
        }
    }

    /**
     * Get the ContentDeployer configured.
     */
    protected ContentDeployer getContentDeployer(ServletContext context)
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
        return new DefaultContentDeployer();
    }
    
    /**
     * Does a first deploy after startup. Throws an exception if it is unlikely
     * that subsequent calls to deploy will fail. Will swallow other exceptions.
     */
    protected Set<FileSpec> doInitialDeploy(File directory,
                                   ContentDeployer contentDeployer, DirectoryState directoryState) 
            throws ApplicationNotInitializedException, PermissionDeniedException, 
                CouldNotUpdateStateException, LoginFailedException {
        if (!directory.exists() || !directory.canRead()) {
            return Collections.emptySet();
        }

        DeployContentUser.login();

        Set<FileSpec> result = contentDeployer.deploy(directory, directoryState);
        
        return result;
    }

    protected Set<FileSpec> doInitialDeploy(File directory, ContentDeployer contentDeployer) 
            throws ApplicationNotInitializedException, PermissionDeniedException, 
                CouldNotUpdateStateException, LoginFailedException {
        DirectoryState directoryState;
        PolicyCMServer server = ApplicationUtil.getInitializedServer();
    
        try {
            directoryState = getDirectoryState(server);
        } catch (PermissionDeniedException pde) {
            throw pde;
        } catch (CMException e) {
            logger.log(Level.WARNING, 
                "Could not get directory state: " + e.getMessage() +
                     ". Importing all content files.", e);
    
            // if something goes wrong, force an import of all files so we are 
            // sure they are up-to-date.
            directoryState = 
                new AlwaysChangedDirectoryState();
        }

        return doInitialDeploy(directory, contentDeployer, directoryState);
    }
    
    public void contextDestroyed(ServletContextEvent event) {
    }

    /**
     * Returns the import state stored in the CM server.
     */
   static DirectoryStatePolicy getDirectoryState(PolicyCMServer server) 
           throws CMException {
       if (directoryState == null) {
           try {
               directoryState = (DirectoryStatePolicy) PolicySingletonUtil.getSingleton(server, 17, 
                   EXTERNAL_ID, DirectoryStatePolicy.INPUT_TEMPLATE_NAME, 
                   DirectoryStatePolicy.class);
           }
           catch (CMException cme) {
               throw cme;
           }
       }
       
       return directoryState;
   }

   static File getDirectory(ServletContext context) {
       File directory = new File(context.getRealPath("/META-INF/content"));
       
       if (!directory.exists() || !directory.isDirectory()) {
           logger.log(Level.FINE, "Could not find /META-INF/content directory.");
       }
       
       return directory;
   }
}
