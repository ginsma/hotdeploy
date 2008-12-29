package example.deploy.hotdeploy;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A file name filter that returns all XML files.
 * 
 * @author AndreasE
 */
class XMLFileNameFilter {
    /**
     * A file name filter that returns all XML files.
     */
    static final FilenameFilter xmlFileFilter = 
        new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }       
        };
}
