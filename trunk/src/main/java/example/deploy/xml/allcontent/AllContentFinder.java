package example.deploy.xml.allcontent;

import java.util.List;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.DeploymentFileParser;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;
import example.deploy.xml.parser.XmlParser;

public class AllContentFinder {
    public class AllContentFinderParserCallback implements ParseCallback {
        public void classReferenceFound(DeploymentFile file, String string) {
        }

        public void contentFound(ParseContext context, String externalId,
                Major major, String inputTemplate) {
            result.add(major, externalId);
        }

        public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        }
    }

    private List<DeploymentFile> files;
    private DeploymentFileParser parser;

    private AllContent result = new AllContent();

    public AllContentFinder(List<DeploymentFile> files) {
        this(new XmlParser(), files);
    }

    public AllContentFinder(DeploymentFileParser parser, List<DeploymentFile> files) {
        this.parser = parser;
        this.files = files;
    }

    public AllContent find() {
        AllContentFinderParserCallback callback =
            new AllContentFinderParserCallback();

        for (DeploymentFile deploymentFile : files) {
            parser.parse(deploymentFile, callback);
        }

        return result;
    }
}
