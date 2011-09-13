package com.polopoly.ps.hotdeploy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.ps.hotdeploy.client.ConnectException;
import com.polopoly.ps.hotdeploy.client.PolopolyClient;
import com.polopoly.ps.hotdeploy.deployer.DefaultSingleFileDeployer;
import com.polopoly.ps.hotdeploy.deployer.MultipleFileDeployer;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.state.DirectoryState;
import com.polopoly.ps.hotdeploy.state.DirectoryStateFetcher;
import com.polopoly.ps.hotdeploy.state.DirectoryWillBecomeJarDirectoryState;


/**
 * A deploy client with a main method that connects to Polopoly and does a
 * deploy of all content.
 */
public class Deploy extends DiscovererMainClass {
	private static final Logger logger = Logger.getLogger(Deploy.class
			.getName());

	private boolean force = false;
	private String connectionUrl = "localhost";
	private String user = null;
	private String password = null;
	private String considerDirectoryJar = null;
	private boolean ignoreContentListAddFailures;

	private DirectoryState directoryState;

	public static void main(String[] args) {
		Deploy deploy = new Deploy();
		DeployParameterParser parameterParser = new DeployParameterParser(
				deploy);
		parameterParser.parseParameters(args);

		try {
			boolean success = deploy.deploy();

			if (success) {
				System.exit(0);
			} else {
				System.exit(1);
			}
		} catch (ConnectException e) {
			System.err.println(e.getMessage());
			parameterParser.printParameterHelp();
			System.exit(1);
		}
	}

	private PolopolyClient getPolopolyClient() {
		PolopolyClient polopolyClient = new PolopolyClient();

		polopolyClient.setConnectionUrl(connectionUrl);
		polopolyClient.setUser(user);
		polopolyClient.setPassword(password);

		return polopolyClient;
	}

	public boolean deploy() throws ConnectException {
		validateDirectories();

		Collection<File> directories = getDirectories();

		System.err.println("Importing content in "
				+ (directories.isEmpty() ? "resource files"
						: getDirectoryString()) + " to Polopoly server "
				+ connectionUrl + ".");

		PolopolyClient polopolyClient = getPolopolyClient();

		polopolyClient.connect();

		PolicyCMServer server = polopolyClient.getPolicyCMServer();

		boolean success;

		try {
			DirectoryState directoryState = getDirectoryState(server);

			if (considerDirectoryJar != null) {
				List<FileDeploymentDirectory> deploymentDirectories = new ArrayList<FileDeploymentDirectory>(
						directories.size());

				for (File directory : directories) {
					deploymentDirectories.add(new FileDeploymentDirectory(
							directory));
				}

				directoryState = new DirectoryWillBecomeJarDirectoryState(
						directoryState, deploymentDirectories,
						considerDirectoryJar);
			}

			DefaultSingleFileDeployer singleFileDeployer = new DefaultSingleFileDeployer(
					server);

			singleFileDeployer
					.setIgnoreContentListAddFailures(ignoreContentListAddFailures);

			MultipleFileDeployer deployer = MultipleFileDeployer.getInstance(
					singleFileDeployer, directoryState);

			Set<DeploymentFile> failingFiles = deployer
					.discoverAndDeploy(getDiscoverers());

			success = failingFiles.isEmpty();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			e.printStackTrace(System.err);

			success = false;
		}

		return success;
	}

	/**
	 * Returns the import state stored in the CM server.
	 */
	private DirectoryState getDirectoryState(PolicyCMServer server) {
		if (directoryState == null) {
			directoryState = new DirectoryStateFetcher(server)
					.getDirectoryState();

			if (force) {
				directoryState = new DelegatingDirectoryState(directoryState) {
					@Override
					public boolean hasFileChanged(DeploymentFile file) {
						return true;
					}
				};
			}
		}

		return directoryState;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isForce() {
		return force;
	}

	public String getConsiderDirectoryJar() {
		return considerDirectoryJar;
	}

	public void setConsiderDirectoryJar(String considerDirectoryJar) {
		this.considerDirectoryJar = considerDirectoryJar;
	}

	public void setIgnoreContentListAddFailures(boolean parseBoolean) {
		this.ignoreContentListAddFailures = parseBoolean;
	}
}
