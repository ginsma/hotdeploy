package com.polopoly.ps.hotdeploy.xml.parser.cache;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.text.TextContentSet;
import com.polopoly.ps.hotdeploy.xml.parser.DeploymentFileParser;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;

public class CachingDeploymentFileParser implements DeploymentFileParser {
    private ParsedFilesCache cache;

    public CachingDeploymentFileParser(DeploymentFileParser delegate) {
        cache = new ParsedFilesCache(delegate);
    }

    public TextContentSet parse(DeploymentFile file, ParseCallback callback) {
        return cache.parse(file, callback);
    }
}
