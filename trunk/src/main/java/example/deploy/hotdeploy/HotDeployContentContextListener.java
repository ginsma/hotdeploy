package example.deploy.hotdeploy;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;

import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil;

/**
 * A {@link javax.servlet.ServletContextListener} that starts a thread
 * monitoring the META-INF/content directory for changes and automatically
 * imports any changed files.
 * 
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
public class HotDeployContentContextListener extends DeployContentContextListener {
    private HotDeployContentThread thread;
    
    private static final Logger logger = 
        Logger.getLogger(HotDeployContentContextListener.class.getName());

    public void contextInitialized(ServletContextEvent event) {
        try {
            boolean logout = false;
            try {
                logout = DeployContentUser.login();

                File directory = getDirectory(event.getServletContext());
                ContentDeployer contentDeployer =
                    getContentDeployer(event.getServletContext());

                doInitialDeploy(directory, contentDeployer);

                thread = new HotDeployContentThread
                    (directory, 
                     getDirectoryState(ApplicationUtil.getInitializedServer()),
                     contentDeployer);
            }
            finally {
                if (logout) {
                    DeployContentUser.logout();
                }
            }

            thread.start();
        }
        // don't ever throw an exception since it stops the deploy process.
        catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }
    
    public void contextDestroyed(ServletContextEvent event) {
        try {
            if (thread != null) {
                thread.interrupt();
                // Wait up to 5 seconds for thread to finish. We really do not
                // want thread to remain running after we left
                // contextDestroyed().
                thread.join(5000);
                if (thread.isAlive()) {
                    logger.warning
                        ("HotDeployContentThread could not be stopped");
                }
                thread = null;
            }
        }
        // don't ever throw an exception since it stops the undeploy process.
        catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }
}
