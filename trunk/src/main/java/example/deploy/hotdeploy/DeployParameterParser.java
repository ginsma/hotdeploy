package example.deploy.hotdeploy;

import example.deploy.hotdeploy.client.ArgumentParser;

public class DeployParameterParser extends DiscovererParameterParser {
	private Deploy deploy;
	private static final String IGNORE_CONTENT_LIST_ADD_FAILURES = "ignorecontentlistaddfailures";

	public DeployParameterParser(Deploy deploy) {
		super(deploy);

		this.deploy = deploy;
	}

	void parseParameters(String[] args) {
		new ArgumentParser(this, args).parse();
	}

	@Override
	public boolean argumentFound(String parameter, String value) {
		if (super.argumentFound(parameter, value)) {
			// handled by superclass.
			return true;
		} else if (parameter.equals("force")) {
			if (value != null) {
				noValueAccepted(parameter, value);
			}

			deploy.setForce(true);
		} else {
			if (value == null) {
				valueRequired(parameter);
			}

			if (parameter.equals("user")) {
				deploy.setUser(value);
			} else if (parameter.equals("password")) {
				deploy.setPassword(value);
			} else if (parameter.equals("server")) {
				deploy.setConnectionUrl(value);
			} else if (parameter.equals("considerjar")) {
				deploy.setConsiderDirectoryJar(value);
			} else if (parameter.equals(IGNORE_CONTENT_LIST_ADD_FAILURES)) {
				deploy.setIgnoreContentListAddFailures(Boolean
						.parseBoolean(value));
			} else {
				System.err.println("Unknown parameter " + parameter + ".");
				printParameterHelp();
				System.exit(1);
			}
		}

		return true;
	}

	@Override
	protected void printParameterHelp() {
		System.err.println();
		System.err.println("Accepted parameters:");
		System.err
				.println("  --dir The directory where the _import_order_ file or the content to import is located.");
		System.err
				.println("  --server The Polopoly server of connection URL (defaults to localhost).");
		System.err
				.println("  --user The user to log in to import (only required if content manipulates ACLs or users).");
		System.err
				.println("  --password If user is specified, the password of the user.");
		System.err
				.println("  --force Import all content regardless of whether it had been modified.");
		System.err
				.println("  --considerjar Consider the imported files as being in a JAR with the specified name.");
		System.err
				.println("  --discoverresources Whether to import from the class path (default to true).");
		System.err
				.println("  --onlyjarresources Whether (if discoverresources is true) to import only JAR resources and not files on the classpath. Default to true.");
		System.err
				.println("  --"
						+ IGNORE_CONTENT_LIST_ADD_FAILURES
						+ " Whether to ignore failures to add objects to content lists. This useful if there are content list wrappers acting up e.g. when dealing with bootstrapped objects.");
	}
}
