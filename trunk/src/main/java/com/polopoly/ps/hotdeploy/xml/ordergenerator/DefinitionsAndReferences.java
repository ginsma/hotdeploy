package com.polopoly.ps.hotdeploy.xml.ordergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;


public class DefinitionsAndReferences {
    public Map<String, Set<DeploymentFile>> definingFilesByExternalId =
        new HashMap<String, Set<DeploymentFile>>();

    public List<Reference> referencesToNonPresentContent = new ArrayList<Reference>();
    public List<Reference> referencesToPresentContent = new ArrayList<Reference>();
}
