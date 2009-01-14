package example.deploy.hotdeploy;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.DirectoryState;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;
import com.polopoly.cm.xml.hotdeploy.util.UserUtil.LoginFailedException;
import com.polopoly.common.lang.PolopolyThread;
import com.polopoly.user.server.UserServer;

/**
 * A thread monitoring the specified directory for changes and automatically
 * imports any changed files.
 *
 * @author AndreasE
 */
@SuppressWarnings("deprecation")
public class HotDeployContentThread extends PolopolyThread {
    private File directory;
    private DirectoryState directoryState;
    private static final int SLEEP_INTERVAL = 1000;
    private ContentDeployer contentDeployer = null;
    private PolicyCMServer server;
    private UserServer userServer;

    private static final Logger logger =
        Logger.getLogger(HotDeployContentThread.class.getName());

    /**
     * Construct a new thread. The threads needs to be explicitly started after
     * construction.
     *
     * @param directory
     *                The directory containing content files.
     */
    public HotDeployContentThread(PolicyCMServer server,
                                  UserServer userServer,
                                  File directory,
                                  DirectoryState directoryState,
                                  ContentDeployer contentDeployer) {
        super("Content HotDeploy Thread");

        this.server = server;
        this.userServer = userServer;
        this.directory = directory;
        this.directoryState = directoryState;
        this.contentDeployer = contentDeployer;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Content hot deployment thread started.");

        try {
            DeployContentUser.login(server, userServer);

            while (true) {
                try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    return;
                }

                try {
                    contentDeployer.deploy(directory, directoryState);
                }
                catch (PermissionDeniedException e) {
                    // in case the session gets invalidated for any reason, retry once.
                    DeployContentUser.login(server, userServer);

                    contentDeployer.deploy(directory, directoryState);
                }

                if (interrupted()) {
                    return;
                }
            }
        } catch (CouldNotUpdateStateException e) {
            // if this happens we must abort the whole thread since we risk
            // perpetually keep re-importing the same content.
            logger.log(Level.WARNING,
                "Could not record imported state. Aborting import thread. " +
                "No more content will be automatically reloaded.", e);
        } catch (PermissionDeniedException pde) {
            logger.log(Level.WARNING,
                "User  " + DeployContentUser.getUserName() +
                " did not have enough permissions: " + pde.getMessage(),
                    pde);
        } catch (LoginFailedException e) {
            logger.log(Level.WARNING,
                "Could not log in the user " +
                    DeployContentUser.getUserName() +
                ". Aborting import thread. " +
                "No more content will be automatically reloaded.", e);
        }
        catch (RuntimeException e) {
            logger.log(Level.SEVERE,
                       "Unexpected exception in HotDeployContentThread", e);
        }
        catch (Error e) {
            logger.log(Level.SEVERE,
                       "Unexpected error in HotDeployContentThread", e);
            throw e;
        }
        finally {
            try {
                DeployContentUser.logout();
            }
            catch (Throwable e) {
                logger.log(Level.FINE, "Logout failed", e);
            }
            logger.log(Level.INFO, "Content hot deployment thread stopped.");
        }
    }
}
