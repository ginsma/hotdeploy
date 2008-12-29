package example.deploy.xmlconsistency;

import java.io.File;

interface ParseCallback {
    /**
     * A content object with the specified external ID was defined.
     */
    void contentFound(File file, String externalId, String inputTemplate);

    /**
     * A template with the specified name (external ID) was defined.
     */
    void templateFound(File file, String inputTemplate);
    
    /**
     * A template was referenced.
     */
    void templateReferenceFound(File file, String inputTemplate);

    /**
     * A content was referenced.
     */
    void contentReferenceFound(File file, String externalId);

    /**
     * A class was referenced.
     */
    void classReferenceFound(File file, String string);
}
