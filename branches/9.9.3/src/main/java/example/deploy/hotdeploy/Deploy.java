package example.deploy.hotdeploy;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.xml.hotdeploy.AlwaysChangedDirectoryState;
import com.polopoly.cm.xml.hotdeploy.FileSpec;
import com.polopoly.pear.ApplicationFactory;

/**
 * Does a first deploy of all XML files after an install. Does not log in a user.
 */
@SuppressWarnings("deprecation")
public class Deploy {
    private static final Logger logger =
        Logger.getLogger(Deploy.class.getName());

    /**
     * Call with the polopoly config file to use as first parameter and the directory of the
     * _import_order file as second parameter.
     */
    public static void main(String[] args) {
        boolean forceRedeploy = args.length == 3 && args[2].equals("force");

        if (!((args.length == 2) || forceRedeploy)) {
            System.err.println("Usage: java " + Deploy.class.getName() +
            " <polopoly path>/pear/config/polopolyclient.properties <_import_order_directory> [force]");
            System.exit(1);
        }

        File directory = new File(args[1]);

        if (!directory.exists() || !directory.canRead() || !directory.isDirectory()) {
            System.err.println(directory.getAbsolutePath() + " is not a readable directory.");
            System.exit(1);
        }

        ApplicationFactory.createApplication(args);

        try {
            DeployContentUser.login();

            Set<FileSpec> result;

            if (!forceRedeploy) {
                result = new DeployContentContextListener().doInitialDeploy(directory,
                        new DefaultContentDeployer(true));
            }
            else {
                result = new DeployContentContextListener().doInitialDeploy(directory,
                    new DefaultContentDeployer(true), new AlwaysChangedDirectoryState());
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
}
