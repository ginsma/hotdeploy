package com.polopoly.ps.hotdeploy.xml.parser;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public interface DeploymentFileParser {

    void parse(DeploymentFile file, ParseCallback callback);

}
