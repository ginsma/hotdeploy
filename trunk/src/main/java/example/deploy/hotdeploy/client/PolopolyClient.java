package example.deploy.hotdeploy.client;

import java.lang.management.ManagementFactory;
import java.net.URL;

import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.util.UserUtil.LoginFailedException;
import com.polopoly.community.client.CommunityClient;
import com.polopoly.management.ManagedBeanRegistry;
import com.polopoly.management.jmx.JMXManagedBeanRegistry;
import com.polopoly.user.server.UserServer;


@SuppressWarnings("deprecation")
public class PolopolyClient {

    private PolicyCMServer server;
    private UserServer userServer;
    private String connectionUrl;
    private String password;
    private String user;

    private void loginUser() throws ConnectException {
        if ((user != null) != (password != null)) {
            throw new ConnectException("Either specify both user name and password to log in a user or neither " +
                        "(in which case no user will be logged in).");
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
            throw new ConnectException("While logging in " + DeployContentUser.getUserName() + ": " + e.toString());
        }
    }

    public void connect() throws ConnectException {
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

            app.addApplicationComponent(new CommunityClient(cmClient));

            // Read connection properties.
            app.readConnectionProperties(connectionProperties);

            // Init.
            app.init();
        } catch (Exception e) {
            throw new ConnectException("Error connecting to Polopoly server with connection URL " + connectionUrl + ": " + e);
        }

        server = cmClient.getPolicyCMServer();
        userServer = cmClient.getUserServer();

        loginUser();
    }

    public UserServer getUserServer() {
        return userServer;
    }

    public PolicyCMServer getPolicyCMServer() {
        return server;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
