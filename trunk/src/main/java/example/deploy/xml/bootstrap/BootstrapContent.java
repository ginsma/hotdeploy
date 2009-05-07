package example.deploy.xml.bootstrap;

import example.deploy.hotdeploy.client.Major;
import example.deploy.xml.parser.cache.SingleObjectHolder;
import example.deploy.xml.parser.cache.Tuple;

public class BootstrapContent extends SingleObjectHolder<Tuple<Major, String>>{
    private String externalId;
    private Major major;

    public BootstrapContent(Major major, String externalId) {
        super(new Tuple<Major, String>(major, externalId));

        this.externalId = externalId;
        this.major = major;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setMajor(Major major) {
        setHeldObject(new Tuple<Major, String>(major, externalId));
        this.major = major;
    }

    public Major getMajor() {
        return major;
    }

    @Override
    public String toString() {
        return getExternalId() + " (major " + major + ")";
    }
}
