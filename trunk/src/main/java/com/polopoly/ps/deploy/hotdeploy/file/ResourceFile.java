package com.polopoly.ps.deploy.hotdeploy.file;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ResourceFile extends AbstractDeploymentObject implements DeploymentFile {
    private String resourceName;

    public ResourceFile(String resourceName) {
        this.resourceName = resourceName;
    }

    public URL getBaseUrl() throws MalformedURLException {
        throw new IllegalStateException("Not implemented.");
    }

    public InputStream getInputStream() throws FileNotFoundException {
        InputStream result = getClass().getResourceAsStream(resourceName);

        if (result == null) {
            throw new FileNotFoundException("No resource with name \"" + resourceName + "\" existed.");
        }

        return result;
    }

    public long getQuickChecksum() {
        throw new IllegalStateException("Not implemented.");
    }

    public long getSlowChecksum() {
        throw new IllegalStateException("Not implemented.");
    }

    public String getName() {
        return resourceName;
    }

    public boolean imports(DeploymentObject object) {
        return object.equals(this);
    }

}
