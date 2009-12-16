package example.deploy.xml.parser;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.util.SingleObjectHolder;
import example.deploy.hotdeploy.util.Tuple;

public class ParsedContentId extends SingleObjectHolder<Tuple<Major, String>> {
    private Major major;
    private String externalId;

    public ParsedContentId(Major major, String externalId) {
        super(new Tuple<Major, String>(major, externalId));
        this.major = (major == null ? Major.UNKNOWN : major);
        this.externalId = externalId;
    }

    public Major getMajor() {
        return major;
    }

    public String getExternalId() {
        return externalId;
    }

}
