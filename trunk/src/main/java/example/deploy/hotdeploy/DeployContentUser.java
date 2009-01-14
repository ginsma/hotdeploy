package example.deploy.hotdeploy;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil.ApplicationNotInitializedException;
import com.polopoly.cm.xml.hotdeploy.util.UserUtil.LoginFailedException;
import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;
import com.polopoly.user.server.UserServer;

/**
 * Manages the user name and password for the user that should be logged in
 * during deploy.
 *
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
class DeployContentUser {
    private static final Logger logger =
        Logger.getLogger(DeployContentUser.class.getName());

    private static final String PROPERTY_USER = "deploy.user";
    private static final String PROPERTY_PASSWORD = "deploy.password";

    private static String userName = "webmaster";

    private static String principalId;

    private static String password;

    /**
     * If no user is logged in, logs in the user for the current thread.
     * @return Whether no user was logged in before the call.
     */
    static boolean login(PolicyCMServer server, UserServer userServer) throws LoginFailedException {
        if (userName == null) {
            throw new LoginFailedException(
                "User name was not set.");
        }

        try {
            if (principalId == null) {
                try {
                    principalId = userServer.getUserByLoginName(
                            userName).getUserId().getPrincipalIdString();
                } catch (RemoteException e) {
                    logger.log(Level.WARNING, "Could not find user " + userName + ": " + e.getMessage(), e);
                } catch (FinderException e) {
                    logger.log(Level.WARNING, "Could not find user " + userName + ": " + e.getMessage(), e);
                }
            }

            if (password != null &&
                    !server.getCurrentCaller().isLoggedIn(userServer)) {
                try {
                    User user = userServer.getUserByLoginName(userName);

                    Caller caller = user.login(password, Caller.NOBODY_CALLER);

                    server.setCurrentCaller(caller);

                    logger.log(Level.FINE, "Successfully logged in " + userName + " for deploy.");

                    return true;
                } catch (AuthenticationFailureException e) {
                    logger.log(Level.WARNING, "The specified password for user " + userName + " was wrong. Importing without loggin in user. Content containing ACLs and user definitions will not be imported.");
                } catch (RemoteException e) {
                    logger.log(Level.WARNING, "Could not log in user " + userName + ": " + e, e);
                } catch (PermissionDeniedException e) {
                    logger.log(Level.WARNING, "Could not log in user " + userName + ": " + e, e);
                } catch (FinderException e) {
                    logger.log(Level.WARNING, "The user " + userName + " to log in did not exist. Importing without loggin in user. Content containing ACLs and user definitions will not be imported.");
                }
            }

            if (server.getCurrentCaller() == Caller.NOBODY_CALLER) {
                logger.log(Level.FINE, "Using user " + userName + " for deploy without logging in.");

                server.setCurrentCaller(new Caller(new UserId((principalId != null ? principalId : userName))));

                return true;
            }
            else {
                return false;
            }
        } catch (ApplicationNotInitializedException e) {
            throw new LoginFailedException(e);
        }
    }

    static String getUserName() {
        return userName;
    }

    /**
     * Logs out the currently logged in user.
     */
    public static void logout() {
        // Do nothing. The user was never logged in the first place (only the caller was set)
        // so logging out will fail.
    }

    /**
     * Set the CM server user name to use for deploying content.
     */
    public static void setUserName(String newUserName) {
        userName = newUserName;

        logger.log(Level.FINE, "Using user " + newUserName + " for deploy.");
    }

    public static void setPassword(String newPassword) {
        password = newPassword;

        logger.log(Level.FINE, "Using password to log in user " + userName + " for deploy.");
    }

    static {
        Properties properties = new Properties();

        try {
            InputStream resourceAsStream =
                DeployContentUser.class.getResourceAsStream("/Environment.properties");

            if (resourceAsStream != null) {
                properties.load(resourceAsStream);

                logger.log(Level.FINE, "Attempting to read hotdeploy user and password from local.properties resource.");

                String propertyUser = properties.getProperty(PROPERTY_USER);

                if (propertyUser != null) {
                    setUserName(propertyUser);
                }

                String propertyPassword = properties.getProperty(PROPERTY_PASSWORD);

                if (propertyPassword != null) {
                    setPassword(propertyPassword);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
