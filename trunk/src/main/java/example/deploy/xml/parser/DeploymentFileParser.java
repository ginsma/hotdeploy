package example.deploy.xml.parser;

import example.deploy.hotdeploy.file.DeploymentFile;

public interface DeploymentFileParser {

    void parse(DeploymentFile file, ParseCallback callback);

}
