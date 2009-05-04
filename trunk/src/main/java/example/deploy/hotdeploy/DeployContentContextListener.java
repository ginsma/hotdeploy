package example.deploy.hotdeploy;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.polopoly.application.InternalApplicationUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.UserServer;

import example.deploy.hotdeploy.client.DeployContentUser;
import example.deploy.hotdeploy.deployer.DefaultSingleFileDeployer;
import example.deploy.hotdeploy.deployer.MultipleFileDeployer;
import example.deploy.hotdeploy.deployer.SingleFileDeployer;
import example.deploy.hotdeploy.discovery.DefaultDiscoverers;
import example.deploy.hotdeploy.state.DirectoryStateFetcher;

/**
 * A {@link javax.servlet.ServletContextListener} that deploys all content in
 * META-INF/content not already present in the database on startup.
 *
 * @author AndreasE
 */
public class DeployContentContextListener implements ServletContextListener {

    private static final Logger logger =
        Logger.getLogger(DeployContentContextListener.class.getName());


    public void contextInitialized(ServletContextEvent event) {
        try {
            PolicyCMServer server = InternalApplicationUtil.getPolicyCMServer(event.getServletContext());
            UserServer userServer = InternalApplicationUtil.getUserServer(event.getServletContext());

            File rootDirectory = new File(event.getServletContext().getRealPath("/"));

            DeployContentUser.login(server, userServer);

            MultipleFileDeployer deployer = getContentDeployer(server, event.getServletContext(), rootDirectory);
            deployer.discoverAndDeploy(DefaultDiscoverers.getDiscoverers());
        }
        // don't ever throw an exception since it stops the deploy process.
        catch (Throwable t) {
            logger.logp(Level.WARNING, "DeployContentContextListener", "contextInitialized",  t.getMessage(), t);
        }
    }

    protected MultipleFileDeployer getContentDeployer(PolicyCMServer server, ServletContext servletContext, File rootDirectory) throws CMException {
        SingleFileDeployer singleFileDeployer = new DefaultSingleFileDeployer(server);

        return new MultipleFileDeployer(singleFileDeployer, rootDirectory,
            new DirectoryStateFetcher(server).getDirectoryState());
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

}
