package com.polopoly.ps.deploy.xml.parser.cache;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.xml.parser.DeploymentFileParser;
import com.polopoly.ps.deploy.xml.parser.ParseCallback;

public class CachingDeploymentFileParser implements DeploymentFileParser {
    private ParsedFilesCache cache;

    public CachingDeploymentFileParser(DeploymentFileParser delegate) {
        cache = new ParsedFilesCache(delegate);
    }

    public void parse(DeploymentFile file, ParseCallback callback) {
        cache.parse(file, callback);
    }
}
