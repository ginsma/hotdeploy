package example.deploy.xml.ordergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import example.deploy.hotdeploy.file.DeploymentFile;

public class DefinitionsAndReferences {
    public Map<String, Set<DeploymentFile>> definingFilesByExternalId =
        new HashMap<String, Set<DeploymentFile>>();

    public List<Reference> references = new ArrayList<Reference>();
}
