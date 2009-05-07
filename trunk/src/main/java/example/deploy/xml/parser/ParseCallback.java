package example.deploy.xml.parser;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;

public interface ParseCallback {
    /**
     * A content object with the specified external ID was defined.
     */
    void contentFound(DeploymentFile file, String externalId, Major major, String inputTemplate);

    /**
     * A template with the specified name (external ID) was defined.
     */
    void templateFound(DeploymentFile file, String inputTemplate);

    /**
     * A template was referenced.
     */
    void templateReferenceFound(DeploymentFile file, String inputTemplate);

    /**
     * A content was referenced.
     */
    void contentReferenceFound(DeploymentFile file, String externalId);

    /**
     * A class was referenced.
     */
    void classReferenceFound(DeploymentFile file, String string);
}
