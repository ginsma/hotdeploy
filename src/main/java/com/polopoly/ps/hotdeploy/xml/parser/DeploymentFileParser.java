package com.polopoly.ps.hotdeploy.xml.parser;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.text.TextContentSet;

public interface DeploymentFileParser {

    TextContentSet parse(DeploymentFile file, ParseCallback callback);

}
