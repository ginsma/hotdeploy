package example.deploy.hotdeploy.state;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import example.deploy.hotdeploy.file.AbstractDeploymentObject;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.DeploymentObject;

public class DummyDeploymentFile extends AbstractDeploymentObject implements DeploymentFile {
    private long quickChecksum;
    private long slowChecksum;
    private String name;
    private InputStream inputStream;

    public DummyDeploymentFile(String name) {
        setName(name);
    }

    public URL getBaseUrl() throws MalformedURLException {
        throw new IllegalStateException("Not implemented.");
    }

    public InputStream getInputStream() throws FileNotFoundException {
        if (inputStream == null) {
            throw new IllegalStateException("No input stream set.");
        }

        return inputStream;
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

    @Override
    public String toString() {
        return name;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean imports(DeploymentObject object) {
        return false;
    }
}
