package example.deploy.xml.parser;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;

public interface ParseCallback {
    /**
     * A content object with the specified external ID was defined.
     */
    void contentFound(ParseContext context, String externalId, Major major, String inputTemplate);

    /**
     * A content was referenced.
     */
    void contentReferenceFound(ParseContext context, Major major, String externalId);

    /**
     * A class was referenced.
     */
    void classReferenceFound(DeploymentFile file, String string);
}
