package example.deploy.hotdeploy.file;

import java.io.FileNotFoundException;

public interface DeploymentDirectory extends DeploymentObject {
    boolean exists();

    DeploymentObject getFile(String fileName) throws FileNotFoundException;

    DeploymentObject[] listFiles();
}
