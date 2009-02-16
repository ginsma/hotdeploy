package example.deploy.hotdeploy;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.AlwaysChangedDirectoryState;
import com.polopoly.cm.xml.hotdeploy.DirectoryState;
import com.polopoly.cm.xml.hotdeploy.DirectoryStatePolicy;
import com.polopoly.cm.xml.hotdeploy.FileSpec;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil.ApplicationNotInitializedException;
import com.polopoly.cm.xml.hotdeploy.util.UserUtil.LoginFailedException;
import com.polopoly.management.ManagedBeanRegistry;
import com.polopoly.management.jmx.JMXManagedBeanRegistry;
import com.polopoly.user.server.UserServer;

/**
 * Does a first deploy of all XML files after an install. Does not log in a user.
 */
@SuppressWarnings("deprecation")
public class Deploy {
    private static final Logger logger =
        Logger.getLogger(Deploy.class.getName());
    private static boolean force = false;
    private static String connectionUrl = "localhost";
    private static String directoryName = ".";
    private static String user = null;
    private static String password = null;

    protected static final String DIRECTORY_STATE_EXTERNAL_ID = "p.HotDeployDirectoryState";

    private static DirectoryStatePolicy directoryState;

    public static void main(String[] args) {
        parseParameters(args);

        File directory = new File(directoryName);

        System.err.println("Importing content in " + directory.getAbsolutePath() + " to Polopoly server " + connectionUrl + ".");

        if (!directory.exists() || !directory.canRead() || !directory.isDirectory()) {
            System.err.println(directory.getAbsolutePath() + " is not a readable directory.");
            System.exit(1);
        }

        if (connectionUrl.indexOf('/') == -1 && connectionUrl.indexOf(':') == -1) {
            // if the URL does not contain a slash or colon, it's not a URL but just the server name. Assume default URL on it.
            connectionUrl = "http://" + connectionUrl + ":8040/connection.properties";
        }

        CmClient cmClient = null;
        StandardApplication app = null;

        try {
            // Create connection properties from an URL.
            ConnectionProperties connectionProperties =
                new ConnectionProperties(new URL(connectionUrl));

            // Create a ManagedBeanRegistry from the standard MBeanServer.
            ManagedBeanRegistry registry =
                new JMXManagedBeanRegistry(ManagementFactory.getPlatformMBeanServer());

            // Create a CM client ApplicationComponent.
            cmClient = new EjbCmClient();

            // Create the Application.
            app = new StandardApplication("deploy");

            // Set the registry.
            app.setManagedBeanRegistry(registry);

            // Add the CM client.
            app.addApplicationComponent(cmClient);

            // Read connection properties.
            app.readConnectionProperties(connectionProperties);

            // Init.
            app.init();
        } catch (Exception e) {
            System.err.println("Error connecting to Polopoly server with connection URL " + connectionUrl + ": " + e);
            printParameterHelp();
            System.exit(1);
        }

        PolicyCMServer server = cmClient.getPolicyCMServer();
        UserServer userServer = cmClient.getUserServer();

        if ((user != null) != (password != null)) {
            System.err.println("Either specify both user name and password to log in a user or neither (in which case no user will be logged in).");
            printParameterHelp();
            System.exit(1);
        }

        if (password != null) {
            DeployContentUser.setPassword(password);
        }

        if (user != null) {
            DeployContentUser.setUserName(user);
        }

        try {
            DeployContentUser.login(server, userServer);
        } catch (LoginFailedException e) {
            System.err.println("While logging in " + DeployContentUser.getUserName() + ": " + e.toString());

            e.printStackTrace();


            System.exit(1);
        }

        try {
            Set<FileSpec> result;

            if (force) {
                result = doInitialDeploy(
                        server, userServer, directory,
                        new DefaultContentDeployer(server, true), new AlwaysChangedDirectoryState());
            }
            else {
                result = doInitialDeploy(
                        server, userServer, directory,
                        new DefaultContentDeployer(server, true));
            }

            if (!result.isEmpty()) {
                System.exit(1);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.exit(0);
    }

    private static void parseParameters(String[] args) {
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

    private static void parameterFound(String parameter, String value) {
        if (parameter.equals("force")) {
            force = true;

            if (value != null) {
                System.err.println("Force parameter does not take value (value \"" + value + "\" was provided).");
                printParameterHelp();
                System.exit(1);
            }
        }
        else {
            if (value == null) {
                System.err.println("Parmater " + parameter + " required a value. Provide it using --" + parameter + "=<value>.");
                printParameterHelp();
                System.exit(1);
            }

            if (parameter.equals("user")) {
                user = value;
            }
            else if (parameter.equals("password")) {
                password = value;
            }
            else if (parameter.equals("server")) {
                connectionUrl = value;
            }
            else if (parameter.equals("dir")) {
                directoryName = value;
            }
            else {
                System.err.println("Unknown parameter " + parameter + ".");
                printParameterHelp();
                System.exit(1);
            }
        }
    }

    private static void printParameterHelp() {
        System.err.println();
        System.err.println("Accepted parameters:");
        System.err.println("  --dir The directory where the _import_order_ file or the content to import is located.");
        System.err.println("  --server The Polopoly server of connection URL (defaults to localhost).");
        System.err.println("  --user The user to log in to import (only required if content manipulates ACLs or users).");
        System.err.println("  --password If user is specified, the password of the user.");
        System.err.println("  --force Import all content regardless of whether it had been modified.");
    }

    /**
     * Does a first deploy after startup. Throws an exception if it is unlikely
     * that subsequent calls to deploy will fail. Will swallow other exceptions.
     */
    protected static Set<FileSpec> doInitialDeploy(PolicyCMServer server, UserServer userServer, File directory,
                                   ContentDeployer contentDeployer, DirectoryState directoryState)
            throws PermissionDeniedException,
                CouldNotUpdateStateException, LoginFailedException {
        if (!directory.exists() || !directory.canRead()) {
            logger.log(Level.WARNING, "The directory " + directory + " where content was expected did not exist. Cannot perform hotdeploy.");

            return Collections.emptySet();
        }

        DeployContentUser.login(server, userServer);

        logger.log(Level.INFO, "Starting content import after webapp deployment...");

        Set<FileSpec> result = contentDeployer.deploy(directory, directoryState);

        if (result.size() > 0) {
            logger.log(Level.WARNING, "Import failed for at least " + result.size() + " file(s).");
        }

        logger.log(Level.INFO, "Content import done.");

        return result;
    }

    protected static Set<FileSpec> doInitialDeploy(PolicyCMServer server, UserServer userServer, File directory, ContentDeployer contentDeployer)
            throws ApplicationNotInitializedException, PermissionDeniedException,
                CouldNotUpdateStateException, LoginFailedException {
        DeployContentUser.login(server, userServer);

        DirectoryState directoryState;

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

        return doInitialDeploy(server, userServer, directory, contentDeployer, directoryState);
    }

    /**
     * Returns the import state stored in the CM server.
     */
   static DirectoryStatePolicy getDirectoryState(PolicyCMServer server)
           throws CMException {
       if (directoryState == null) {
           try {
               directoryState = (DirectoryStatePolicy) PolicySingletonUtil.getSingleton(server, 17,
                   DIRECTORY_STATE_EXTERNAL_ID, DirectoryStatePolicy.INPUT_TEMPLATE_NAME,
                   DirectoryStatePolicy.class);
           }
           catch (CMException cme) {
               throw cme;
           }
       }

       return directoryState;
   }
}
