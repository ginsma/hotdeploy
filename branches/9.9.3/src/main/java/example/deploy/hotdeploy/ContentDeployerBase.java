package example.deploy.hotdeploy;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import com.polopoly.cm.client.impl.exceptions.PermissionDeniedException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.hotdeploy.DirectoryState;
import com.polopoly.cm.xml.hotdeploy.FileSpec;
import com.polopoly.cm.xml.hotdeploy.DirectoryState.CouldNotUpdateStateException;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil;
import com.polopoly.cm.xml.hotdeploy.util.ApplicationUtil.ApplicationNotInitializedException;
import com.polopoly.cm.xml.io.DispatchingDocumentImporter;

/**
 * Base class for importing changed content XML files in a directory.
 * 
 * @author AndreasE
 */
@SuppressWarnings({ "deprecation" })
public class ContentDeployerBase
{
    private static final Logger logger = 
        Logger.getLogger(ContentDeployerBase.class.getName());
    private boolean failFast;

    public ContentDeployerBase() {
        this(false);
    }
    
    public ContentDeployerBase(boolean failFast) {
        this.failFast = failFast;
    }


    public Set<FileSpec> deploy(File directory, DirectoryState directoryState) 
            throws PermissionDeniedException,
                   CouldNotUpdateStateException,
                   ApplicationNotInitializedException
    {
        if (!directory.exists() || !directory.isDirectory()) {
            return Collections.emptySet();
        }

        PolicyCMServer server = ApplicationUtil.getInitializedServer();
    
        List<FileSpec> changedFiles = getFilesToImportOrdered(directory, directoryState);

        return importFiles(directory, server, changedFiles, directoryState);
    }

    
    /**
     * Return the FileSpecs for the files in the directory that should be
     * imported. The list will be returned in the correct import order.
     */
    protected List<FileSpec> getFilesToImportOrdered(File directory,
                                           DirectoryState strategy)
    {
        File[] files = directory.listFiles(XMLFileNameFilter.xmlFileFilter);
        List<FileSpec> result = new ArrayList<FileSpec>();
        boolean anyFileChanged = false;
        
        for (int i = 0; i < files.length; i++) {
            FileSpec file = new FileSpec(files[i], files[i].getName());

            if (strategy.hasFileChanged(file)) {
                result.add(file);

                anyFileChanged = true;
            }
        }

        if (!anyFileChanged) {
            result.clear();
            return result;
        }
        
        // make sure we import files with "template" in their names first,
        // since the content import is likely to be dependent on presence
        // of the templates.
        sortFiles(result);
        return result;
    }

    /**
     * Returns the files for which import failed.
     */
    protected Set<FileSpec> importFiles(File directory,
                                  PolicyCMServer server,
                                  List<FileSpec> files,
                                  DirectoryState directoryState)
        throws CouldNotUpdateStateException
    {
        Set<FileSpec> result = new HashSet<FileSpec>();
         
        Iterator<FileSpec> it = files.iterator();

        CouldNotUpdateStateException updateStateException = null;

        DispatchingDocumentImporter importer;

        try {
            importer = new DispatchingDocumentImporter(server);
        }
        catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, "Failed to create importer.", e);
            return result;
        }

        try {
            importer.setBaseUrl(directory.toURL());
        }
        catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create base url from directory.", e);
        }

        while (it.hasNext()) {
            FileSpec file = it.next();
            boolean importFailed = false;
            
            try {
                logger.log(Level.INFO, file.getName() + " had changed on disk. Importing it.");

                Thread currentThread = Thread.currentThread();
                
                ClassLoader oldClassLoader = 
                    currentThread.getContextClassLoader();
                
                currentThread.setContextClassLoader(
                    ContentDeployerBase.class.getClassLoader());

                try {
                    importer.importXML(file.getFile());
                }
                finally {
                    currentThread.setContextClassLoader(oldClassLoader);                    
                }

                logger.log(Level.INFO, "Import of " + file + " done.");
            } 
            catch (Exception e) {
                logger.log(Level.WARNING, 
                    "Import of " + file + " failed: " + e.getMessage(), e);
                importFailed = true;
                
                result.add(file);
            }

            try {
                directoryState.reset(file, importFailed);
            } catch (CouldNotUpdateStateException e) {
                // we need to notify the caller on exception to avoid continually 
                // importing the same file, but we should first import all files 
                // we know need to be imported.
                updateStateException  = e;
            }

            if (importFailed && failFast) {
                break;
            }
        }
        
        if (updateStateException != null) {
            throw updateStateException;
        }
        
        return result;
    }

    /**
     * Sort the list of files. Minor sorting is by name. Major sorting is
     * 'bootstrap' in the name < 'template' in the name < other files.
     */ 
    protected void sortFiles(List<FileSpec> changedFiles) {
        Collections.sort(changedFiles, new Comparator<FileSpec>() {
            public int compare(FileSpec o1, FileSpec o2) {
                int result = compare(o1, o2, "bootstrap");

                if (result == 0) {
                    result = compare(o1, o2, "template");
                }
                
                if (result == 0) {
                    result = ((FileSpec) o1).getName().compareTo(((FileSpec) o2).getName());
                }
                
                return result;
            }
            
            private int compare(Object o1, Object o2, String keyword) {
                boolean keyword1 = ((FileSpec) o1).getName().indexOf(keyword) != -1;
                boolean keyword2 = ((FileSpec) o2).getName().indexOf(keyword) != -1;

                if (keyword1 && !keyword2) {
                    return -1;
                } else if (keyword2 && !keyword1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }
}

