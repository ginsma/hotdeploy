package example.deploy.xml.parser.cache;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.parser.ParseCallback;

public class CachingDeploymentFileParser implements DeploymentFileParser {
    private ParsedFilesCache cache;

    public CachingDeploymentFileParser(DeploymentFileParser delegate) {
        cache = new ParsedFilesCache(delegate);
    }

    public void parse(DeploymentFile file, ParseCallback callback) {
        cache.parse(file, callback);
    }
}
