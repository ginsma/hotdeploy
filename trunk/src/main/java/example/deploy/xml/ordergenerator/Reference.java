package example.deploy.xml.ordergenerator;

import example.deploy.hotdeploy.file.DeploymentFile;

public class Reference {
    String refersTo;
    DeploymentFile inFile;

    public Reference(String refersTo, DeploymentFile file) {
        this.refersTo = refersTo;
        this.inFile = file;
    }
}
