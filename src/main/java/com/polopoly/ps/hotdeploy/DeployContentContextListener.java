package com.polopoly.ps.hotdeploy;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.polopoly.application.Application;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.ps.hotdeploy.client.DeployContentUser;
import com.polopoly.ps.hotdeploy.deployer.DefaultSingleFileDeployer;
import com.polopoly.ps.hotdeploy.deployer.DeploymentResult;
import com.polopoly.ps.hotdeploy.deployer.MultipleFileDeployer;
import com.polopoly.ps.hotdeploy.deployer.SingleFileDeployer;
import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.hotdeploy.state.DirectoryStateFetcher;
import com.polopoly.user.server.UserServer;

/**
 * A {@link javax.servlet.ServletContextListener} that deploys all content in
 * META-INF/content not already present in the database on startup.
 * 
 * @author AndreasE
 */
public class DeployContentContextListener implements ServletContextListener {

	private static final Logger logger = Logger
			.getLogger(DeployContentContextListener.class.getName());

	private DeploymentResult result = new DeploymentResult();

	public void contextInitialized(ServletContextEvent event) {
		try {
			Application application = ApplicationServletUtil
					.getApplication(event.getServletContext());

			EjbCmClient client = (EjbCmClient) application
					.getApplicationComponent(EjbCmClient.DEFAULT_COMPOUND_NAME);
			PolicyCMServer server = client.getPolicyCMServer();
			UserServer userServer = client.getUserServer();

			DeployContentUser.login(server, userServer);

			List<FileDiscoverer> discoverers = WebApplicationDiscoverers
					.getWebAppDiscoverers(event.getServletContext());

			MultipleFileDeployer deployer = getContentDeployer(server,
					event.getServletContext());
			deployer.discoverAndDeploy(discoverers, result);

			result.logResults();
		}
		// don't ever throw an exception since it stops the deploy process.
		catch (Throwable t) {
			logger.logp(Level.WARNING, "DeployContentContextListener",
					"contextInitialized", t.getMessage(), t);
		}
	}

	protected MultipleFileDeployer getContentDeployer(PolicyCMServer server,
			ServletContext servletContext) throws CMException {
		SingleFileDeployer singleFileDeployer = new DefaultSingleFileDeployer(
				server, result);

		return new MultipleFileDeployer(singleFileDeployer,
				new DirectoryStateFetcher(server).getDirectoryState());
	}

	public void contextDestroyed(ServletContextEvent event) {
	}

}
