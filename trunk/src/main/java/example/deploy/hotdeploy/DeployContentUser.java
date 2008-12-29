package example.deploy.hotdeploy;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.FinderException;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil.ApplicationNotInitializedException;
import com.polopoly.cm.xml.hotdeploy.util.UserUtil.LoginFailedException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserId;

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

    private static String userName = "webmaster";

    private static String principalId;
    
    /**
     * If no user is logged in, logs in the user for the current thread.
     * @return Whether no user was logged in before the call.
     */ 
    static boolean login() throws LoginFailedException {
        if (userName == null) {
            throw new LoginFailedException(
                "User name was not set. " +
                "They are usually set through HotDeployContent.properties. " +
                "Make sure hot deploy context listener is not placed before the pear " +
                "listener in the application web.xml.");
        }
        
        try {
            PolicyCMServer server = ApplicationUtil.getInitializedServer();

            if (principalId == null) {
                try {
                    principalId = ApplicationUtil.getInitializedApplication().getUserServer().getUserByLoginName(
                            userName).getUserId().getPrincipalIdString();
                } catch (RemoteException e) {
                    logger.log(Level.WARNING, "Could not find user " + userName + ": " + e.getMessage(), e);
                } catch (FinderException e) {
                    logger.log(Level.WARNING, "Could not find user " + userName + ": " + e.getMessage(), e);
                }
            }
            
            if (server.getCurrentCaller() == Caller.NOBODY_CALLER) {
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
    }
}
