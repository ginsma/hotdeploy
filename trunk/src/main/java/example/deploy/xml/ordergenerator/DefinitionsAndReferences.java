package example.deploy.xml.ordergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.deploy.hotdeploy.file.DeploymentFile;

public class DefinitionsAndReferences {
    Map<String, DeploymentFile> definingFileByExternalId =
        new HashMap<String, DeploymentFile>();

    List<Reference> references = new ArrayList<Reference>();
}
