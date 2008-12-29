package example.deploy.hotdeploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.xml.hotdeploy.DirectoryState;
import com.polopoly.cm.xml.hotdeploy.FileSpec;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil.ApplicationNotInitializedException;

/**
 * Imports changed content XML files in a directory. Uses the file
 * '_import_order' (in the META-INF/content directory) to get the order of
 * import of the changed files. Files that are not in the import order will not
 * be imported.
 *
 * If the import order file does not exist, the files will be imported in the
 * sort order used by ContentDeployerBase.
 *
 * The format of the import order file: Each line in the file corresponds to the
 * import if a file, files not found will be ignored. Files not found with a #
 * as the first char will not be printed in the log file (to make it possible to
 * comment).
 *
 * Example: # Templates (not a real comment, there is just no file named this
 * way). template/bootstrap.xml template/news.xml # Content
 * content/bootstrap.xml content/news.xml
 */
@SuppressWarnings("deprecation")
public class DefaultContentDeployer
    extends ContentDeployerBase
        implements ContentDeployer
{
    private static final Logger logger =
        Logger.getLogger(DefaultContentDeployer.class.getName());
    private static final Set<FileSpec> NON_EXISTING_FILES = new HashSet<FileSpec>();

    public DefaultContentDeployer() {
        super();
    }

    public DefaultContentDeployer(boolean failFast) {
        super(failFast);
    }

    /**
     * @see ContentDeployer#deploy(File , DirectoryState)
     */
    @Override
    public Set<FileSpec> deploy(File directory, DirectoryState directoryState)
        throws PermissionDeniedException,
               CouldNotUpdateStateException,
               ApplicationNotInitializedException
    {
        logger.log(Level.FINE, "ContentDeployerBySpecifiedOrder was here.");
        return super.deploy(directory, directoryState);
    }

    /**
     * @see ContentDeployerBase#getFilesToImportOrdered(File, DirectoryState)
     */
    @Override
    protected List<FileSpec> getFilesToImportOrdered(File directory,
                                           DirectoryState directoryState)
    {
        List<FileSpec> importOrder = getImportOrder(directory);

        if (importOrder == null) {
            return super.getFilesToImportOrdered(directory, directoryState);
        }

        boolean anyFileChanged = false;

        // Remove files not changed or failed.
        Iterator<FileSpec> i = importOrder.iterator();

        while (i.hasNext()) {
            FileSpec file = i.next();

            if (!directoryState.hasFileChanged(file)) {
                i.remove();
            }
            else {
                anyFileChanged = true;
            }
        }

        if (!anyFileChanged) {
            importOrder.clear();
        }

        return importOrder;
    }

    /**
     * Parse the import order file and return as a list.
     *
     * @return a list of files (that exists and is .xml files), null if no
     *         import order exists
     */
    public static List<FileSpec> getImportOrder(File directory)
    {
        File importOrder = new File(directory, "_import_order");

        if (!importOrder.isFile()) {
            logger.log(Level.FINE, "The file " + importOrder.getAbsolutePath() +
                " did not exist or could not be read.");

            return null;
        }

        ArrayList<FileSpec> list = new ArrayList<FileSpec>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(importOrder));

            while (true) {
                String line = br.readLine();

                if (line == null) {
                    break; // End of file.
                }

                if (line.charAt(0) == '#' || line.trim().equals("")) {
                    continue;
                }

                addFile(list, line, new File(directory, line));
            }
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Could not read file '_import_order'.");
            return null;
        }

        return list;
    }

    private static void addDirectory(ArrayList<FileSpec> list, String line, File directory) {
        logger.log(Level.FINEST, "Added directory '" + line + "'.");

        File[] files = directory.listFiles();

        Arrays.sort(files);

        for (File file : files) {
            addFile(list, line + File.separator + file.getName(), file);
        }
    }

    private static void addFile(ArrayList<FileSpec> list, String line, File file) {
        FileSpec fileSpec =
            new FileSpec(file, line);

        if (!file.exists() && NON_EXISTING_FILES.add(fileSpec)) {
            logger.log(Level.WARNING, "The file " + file.getAbsolutePath() + ", specified in _import_order, does not exist.");
        }
        else if (file.isDirectory()) {
            addDirectory(list, line, file);
        }
        else if (file.isFile() &&
                XMLFileNameFilter.xmlFileFilter.accept(file.getParentFile(),
                                                       file.getName())) {
            if (!list.contains(fileSpec)) {
                list.add(fileSpec);
            }
        }
    }
}

