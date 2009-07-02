package example.deploy.hotdeploy;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.polopoly.application.InternalApplicationUtil;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.UserServer;

import example.deploy.hotdeploy.client.DeployContentUser;
import example.deploy.hotdeploy.deployer.MultipleFileDeployer;
import example.deploy.hotdeploy.discovery.FileDiscoverer;

/**
 * A {@link javax.servlet.ServletContextListener} that starts a thread
 * monitoring the META-INF/content directory for changes and automatically
 * imports any changed files.
 *
 * @author AndreasE
 */
public class HotDeployContentContextListener extends DeployContentContextListener {
    private HotDeployContentThread thread;

    private static final Logger logger =
        Logger.getLogger(HotDeployContentContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            boolean logout = false;
            try {
                PolicyCMServer server = InternalApplicationUtil.getPolicyCMServer(event.getServletContext());
                UserServer userServer = InternalApplicationUtil.getUserServer(event.getServletContext());

                logout = DeployContentUser.login(server, userServer);

                MultipleFileDeployer contentDeployer =
                    getContentDeployer(server, event.getServletContext());

                ServletContext servletContext = event.getServletContext();

                List<FileDiscoverer> discoverers = WebApplicationDiscoverers.getWebAppDiscoverers(servletContext);
                contentDeployer.discoverAndDeploy(discoverers);

                thread = new HotDeployContentThread(server, userServer, contentDeployer, discoverers);
            }
            finally {
                if (logout) {
                    DeployContentUser.logout();
                }
            }

            thread.start();
        }
        // don't ever throw an exception since it stops the web application deployment.
        catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            if (thread != null) {
                thread.interrupt();
                // Wait up to 5 seconds for thread to finish. We really do not
                // want thread to remain running after we left
                // contextDestroyed().
                thread.join(5000);
                if (thread.isAlive()) {
                    logger.warning("HotDeployContentThread could not be stopped");
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
