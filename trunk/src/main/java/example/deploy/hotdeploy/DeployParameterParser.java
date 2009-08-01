package example.deploy.hotdeploy;

import example.deploy.hotdeploy.client.ArgumentConsumer;
import example.deploy.hotdeploy.client.ArgumentParser;

public class DeployParameterParser implements ArgumentConsumer {
    private Deploy deploy;

    void parseParameters(Deploy deploy, String[] args) {
        this.deploy = deploy;
        new ArgumentParser(this, args).parse();
    }

    public void argumentFound(String parameter, String value) {
        if (parameter.equals("discoverresources")) {
            deploy.setDiscoverResources((value == null ? true : Boolean.parseBoolean(value)));
        }
        else if (parameter.equals("onlyjarresources")) {
            deploy.setOnlyJarResources((value == null ? true : Boolean.parseBoolean(value)));
        }
        else if (parameter.equals("force")) {
            deploy.setForce(true);

            if (value != null) {
                System.err.println("Force parameter does not take value (value \"" + value + "\" was provided).");
                printParameterHelp();
                System.exit(1);
            }
        }
        else {
            if (value == null) {
                System.err.println("Parameter " + parameter + " required a value. Provide it using --" + parameter + "=<value>.");
                printParameterHelp();
                System.exit(1);
            }

            if (parameter.equals("user")) {
                deploy.setUser(value);
            }
            else if (parameter.equals("password")) {
                deploy.setPassword(value);
            }
            else if (parameter.equals("server")) {
                deploy.setConnectionUrl(value);
            }
            else if (parameter.equals("dir")) {
                deploy.setDirectoryName(value);
            }
            else if (parameter.equals("considerjar")) {
                deploy.setConsiderDirectoryJar(value);
            }
            else {
                System.err.println("Unknown parameter " + parameter + ".");
                printParameterHelp();
                System.exit(1);
            }
        }
    }

    void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err.println("  --dir The directory where the _import_order_ file or the content to import is located.");
        System.err.println("  --server The Polopoly server of connection URL (defaults to localhost).");
        System.err.println("  --user The user to log in to import (only required if content manipulates ACLs or users).");
        System.err.println("  --password If user is specified, the password of the user.");
        System.err.println("  --force Import all content regardless of whether it had been modified.");
        System.err.println("  --considerjar Consider the imported files as being in a JAR with the specified name.");
        System.err.println("  --discoverresources Whether to import from the class path (default to true).");
        System.err.println("  --onlyjarresources Whether (if discoverresources is true) to import only JAR resources and not files on the classpath. Default to true.");
    }
}
