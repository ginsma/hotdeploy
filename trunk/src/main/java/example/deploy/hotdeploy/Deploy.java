package example.deploy.hotdeploy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.policy.PolicyCMServer;

import example.deploy.hotdeploy.client.ConnectException;
import example.deploy.hotdeploy.client.PolopolyClient;
import example.deploy.hotdeploy.deployer.DefaultSingleFileDeployer;
import example.deploy.hotdeploy.deployer.MultipleFileDeployer;
import example.deploy.hotdeploy.deployer.SingleFileDeployer;
import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.ImportOrderOrDirectoryFileDiscoverer;
import example.deploy.hotdeploy.discovery.ResourceFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.state.DirectoryState;
import example.deploy.hotdeploy.state.DirectoryStateFetcher;
import example.deploy.hotdeploy.state.DirectoryWillBecomeJarDirectoryState;

/**
 * A deploy client with a main method that connects to Polopoly and does a deploy of all content.
 */
public class Deploy {
    private static final Logger logger =
        Logger.getLogger(Deploy.class.getName());

    private boolean force = false;
    private String connectionUrl = "localhost";
    private String user = null;
    private String password = null;
    private String considerDirectoryJar = null;
    private boolean discoverResources = true;
    private boolean onlyJarResources = true;

    private DirectoryState directoryState;

    private File directory;

    public static void main(String[] args) {
        Deploy deploy = new Deploy();
        DeployParameterParser parameterParser = new DeployParameterParser();
        parameterParser.parseParameters(deploy, args);

        try {
            boolean success = deploy.deploy();

            if (success) {
                System.exit(0);
            }
            else {
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

    private void validateDirectory() {
        if (directory == null) {
            System.err.println("--dir not specified. Only importing resource files.");
        }
        else if (!directory.exists() || !directory.canRead() || !directory.isDirectory()) {
            System.err.println(directory.getAbsolutePath() + " is not a readable directory. Only importing resource files.");
        }
    }

    public boolean deploy() throws ConnectException {
        validateDirectory();

        System.err.println("Importing content in " +
                (directory == null ? "resource files" : directory.getAbsolutePath()) +
                " to Polopoly server " + connectionUrl + ".");

        PolopolyClient polopolyClient = getPolopolyClient();

        polopolyClient.connect();

        PolicyCMServer server = polopolyClient.getPolicyCMServer();

        boolean success;

        try {
            DirectoryState directoryState = getDirectoryState(server);

            if (considerDirectoryJar != null) {
                directoryState = new DirectoryWillBecomeJarDirectoryState(directoryState,
                        new FileDeploymentDirectory(directory), considerDirectoryJar);
            }

            SingleFileDeployer singleFileDeployer = new DefaultSingleFileDeployer(server);

            MultipleFileDeployer deployer =
                new MultipleFileDeployer(singleFileDeployer, directoryState);

            List<FileDiscoverer> discoverers = new ArrayList<FileDiscoverer>();

            if (discoverResources) {
                discoverers.add(new ResourceFileDiscoverer(onlyJarResources));
            }

            if (directory != null) {
                discoverers.add(new ImportOrderOrDirectoryFileDiscoverer(directory));
            }

            Set<DeploymentFile> failingFiles =
                deployer.discoverAndDeploy(discoverers);

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
           directoryState = new DirectoryStateFetcher(server).getDirectoryState();

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

    public void setDirectoryName(String directoryName) {
        directory = new File(directoryName);
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

    public boolean isDiscoverResources() {
        return discoverResources;
    }

    public void setDiscoverResources(boolean discoverResources) {
        this.discoverResources = discoverResources;
    }

    public boolean isOnlyJarResources() {
        return onlyJarResources;
    }

    public void setOnlyJarResources(boolean onlyJarResources) {
        this.onlyJarResources = onlyJarResources;
    }
}
