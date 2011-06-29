package com.polopoly.ps.deploy.xml.parser;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;

public interface DeploymentFileParser {

    void parse(DeploymentFile file, ParseCallback callback);

}
