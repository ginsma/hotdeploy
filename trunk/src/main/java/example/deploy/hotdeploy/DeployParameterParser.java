package example.deploy.hotdeploy;

public class DeployParameterParser {
    private Deploy deploy;

    void parseParameters(Deploy deploy, String[] args) {
        this.deploy = deploy;
        String parameter = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                if (parameter != null) {
                    parameterFound(parameter, null);
                }

                parameter = arg.substring(2);

                int eq;

                if ((eq = parameter.indexOf('=')) != -1) {
                    String value = parameter.substring(eq+1);
                    parameter = parameter.substring(0, eq);
                    parameterFound(parameter, value);

                    parameter = null;
                }
            }
            else if (parameter != null) {
                parameterFound(parameter, arg);
            }
        }

        if (parameter != null) {
            parameterFound(parameter, null);
        }
    }

    void parameterFound(String parameter, String value) {
        if (parameter.equals("force")) {
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
    }
}
