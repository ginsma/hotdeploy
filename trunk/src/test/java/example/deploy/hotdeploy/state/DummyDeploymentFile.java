package example.deploy.hotdeploy.state;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import example.deploy.hotdeploy.file.DeploymentFile;

public class DummyDeploymentFile implements DeploymentFile {
    private long quickChecksum;
    private long slowChecksum;
    private String name;

    public URL getBaseUrl() throws MalformedURLException {
        throw new IllegalStateException("Not implemented.");
    }

    public InputStream getInputStream() throws FileNotFoundException {
        throw new IllegalStateException("Not implemented.");
    }

    public void setQuickChecksum(long quickChecksum) {
        this.quickChecksum = quickChecksum;
    }

    public long getQuickChecksum() {
        return quickChecksum;
    }

    public void setSlowChecksum(long slowChecksum) {
        this.slowChecksum = slowChecksum;
    }

    public long getSlowChecksum() {
        return slowChecksum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
