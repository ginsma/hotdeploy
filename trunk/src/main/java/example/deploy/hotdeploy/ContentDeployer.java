package example.deploy.hotdeploy;

import java.io.File;
import java.util.Set;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.xml.hotdeploy.DirectoryState;
import com.polopoly.cm.xml.hotdeploy.FileSpec;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;

/**
 * Interface for class that Imports changed content XML files in a directory.
 *
 * Application Framework. Use import servlet instead.
 */
@SuppressWarnings("deprecation")
public interface ContentDeployer
{
    /**
     * Import the XML files in the directory that directoryState indicates have
     * been modified.
     *
     * @throws PermissionDeniedException
     *                 If the current user in the CM server did not have enough
     *                 rights to import content.
     * @throws CouldNotUpdateStateException
     *                 If the directory state could not be updated after
     *                 importing a file.
     * @throws ApplicationNotInitializedException
     *                 If the CM server could not be located.
     * Polopoly Application Framework. Use import servlet instead.
     * @return The files for which import failed.
     */
    public Set<FileSpec> deploy(File directory, DirectoryState directoryState)
            throws PermissionDeniedException,
                   CouldNotUpdateStateException;

}
