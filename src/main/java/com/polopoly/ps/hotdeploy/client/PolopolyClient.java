package com.polopoly.ps.hotdeploy.client;

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.client.CommunityClient;
import com.polopoly.management.ManagedBeanRegistry;
import com.polopoly.management.jmx.JMXManagedBeanRegistry;
import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;
import com.polopoly.user.server.UserServer;

public class PolopolyClient {
    private static final Logger logger = Logger.getLogger(PolopolyClient.class
            .getName());

    private PolicyCMServer server;

    private UserServer userServer;

    private String connectionUrl;

    private String password;

    private String userName;

    private void loginUser() throws ConnectException {
        if ((userName != null) != (password != null)) {
            throw new ConnectException(
                    "Either specify both user name and password to log in a user or neither "
                            + "(in which case no user will be logged in).");
        }

        login();
    }

    private void loginUserWithoutPassword() throws Exception {
        User user = userServer.getUserByLoginName(userName);
        UserId userId = user.getUserId();
        server.setCurrentCaller(new NonLoggedInCaller(userId, null, null,
                userName));

        logger.info("No password provided. Set caller to user \"" + user
                + "\" but did not log in.");
    }

    private void loginUserWithPassword() throws Exception {
        Caller caller = userServer.loginAndMerge(userName, password, server
                .getCurrentCaller());

        server.setCurrentCaller(caller);

        logger.info("Logged in user \"" + userName + "\".");
    }

    private void login() throws ConnectException {
        try {
            if (password != null) {
                loginUserWithPassword();
            } else {
                loginUserWithoutPassword();
            }
        } catch (FinderException e) {
            throw new ConnectException("The user " + userName
                    + " to log in could not be found.");
        } catch (AuthenticationFailureException e) {
            throw new ConnectException("The password supplied for user "
                    + userName + " was incorrect.");
        } catch (Exception e) {
            throw new ConnectException(
                    "An error occurred while trying to log in user " + userName
                            + ": " + e.getMessage(), e);
        }
    }

    public void connect() throws ConnectException {
        if (connectionUrl.indexOf('/') == -1
                && connectionUrl.indexOf(':') == -1) {
            // if the URL does not contain a slash or colon, it's not a URL but
            // just the server name. Assume default URL on it.
            connectionUrl = "http://" + connectionUrl
                    + ":8040/connection.properties";
        }

        CmClient cmClient = null;
        StandardApplication app = null;

        try {
            // Create connection properties from an URL.
            ConnectionProperties connectionProperties = new ConnectionProperties(
                    new URL(connectionUrl));

            // Create a ManagedBeanRegistry from the standard MBeanServer.
            ManagedBeanRegistry registry = new JMXManagedBeanRegistry(
                    ManagementFactory.getPlatformMBeanServer());

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
            throw new ConnectException(
                    "Error connecting to Polopoly server with connection URL "
                            + connectionUrl + ": " + e,e);
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
        return userName;
    }

    public void setUser(String user) {
        this.userName = user;
    }

}
