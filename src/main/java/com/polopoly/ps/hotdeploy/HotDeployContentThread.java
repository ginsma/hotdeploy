package com.polopoly.ps.hotdeploy;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.common.lang.PolopolyThread;
import com.polopoly.community.util.content.AdminUserUtil.LoginFailedException;
import com.polopoly.ps.hotdeploy.client.DeployContentUser;
import com.polopoly.ps.hotdeploy.deployer.DeploymentResult;
import com.polopoly.ps.hotdeploy.deployer.FatalDeployException;
import com.polopoly.ps.hotdeploy.deployer.MultipleFileDeployer;
import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.user.server.UserServer;

/**
 * A thread monitoring the specified directory for changes and automatically
 * imports any changed files.
 * 
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
public class HotDeployContentThread extends PolopolyThread {
	private static final int SLEEP_INTERVAL = 1000;
	private MultipleFileDeployer contentDeployer = null;
	private PolicyCMServer server;
	private UserServer userServer;
	private Collection<FileDiscoverer> discoverers;

	private static final Logger logger = Logger
			.getLogger(HotDeployContentThread.class.getName());

	/**
	 * Construct a new thread. The threads needs to be explicitly started after
	 * construction.
	 * 
	 * @param directory
	 *            The directory containing content files.
	 */
	public HotDeployContentThread(PolicyCMServer server, UserServer userServer,
			MultipleFileDeployer contentDeployer,
			Collection<FileDiscoverer> discoverers) {
		super("Content HotDeploy Thread");

		this.server = server;
		this.userServer = userServer;
		this.contentDeployer = contentDeployer;
		this.discoverers = discoverers;
	}

	@Override
	public void run() {
		logger.log(Level.INFO, "Content hot deployment thread started.");

		DeploymentResult result = new DeploymentResult();

		try {
			DeployContentUser.login(server, userServer);

			while (true) {
				try {
					Thread.sleep(SLEEP_INTERVAL);
				} catch (InterruptedException e) {
					return;
				}

				try {
					contentDeployer.discoverAndDeploy(discoverers, result);
				} catch (FatalDeployException e) {
					if (e.getCause() instanceof PermissionDeniedException) {
						// in case the session gets invalidated for any reason,
						// retry once.
						DeployContentUser.login(server, userServer);

						contentDeployer.discoverAndDeploy(discoverers, result);
					} else {
						throw e;
					}
				}

				if (interrupted()) {
					return;
				}
			}
		} catch (FatalDeployException e) {
			// if this happens we must abort the whole thread since by
			// definition
			// there is no point in retrying on a fatal exception.
			logger.log(Level.WARNING,
					"While trying to deploy content: " + e.getMessage(), e);
		} catch (LoginFailedException e) {
			logger.log(Level.WARNING, "Could not log in the user "
					+ DeployContentUser.getUserName()
					+ ". Aborting import thread. "
					+ "No more content will be automatically reloaded.", e);
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE,
					"Unexpected exception in HotDeployContentThread", e);
		} catch (Error e) {
			logger.log(Level.SEVERE,
					"Unexpected error in HotDeployContentThread", e);
			throw e;
		} finally {
			try {
				DeployContentUser.logout();
			} catch (Throwable e) {
				logger.log(Level.FINE, "Logout failed", e);
			}
			logger.log(Level.INFO, "Content hot deployment thread stopped.");
		}
	}
}
