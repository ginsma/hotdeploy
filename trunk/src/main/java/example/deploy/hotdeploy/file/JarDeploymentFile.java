package example.deploy.hotdeploy.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarDeploymentFile extends AbstractDeploymentObject implements DeploymentFile {
    protected JarFile file;
    protected ZipEntry entry;

    public JarDeploymentFile(JarFile file, ZipEntry entry) {
        this.file = file;
        this.entry = entry;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        if (entry == null) {
            throw new FileNotFoundException("While reading " + this + ": file not found");
        }

        try {
            return file.getInputStream(entry);
        } catch (IOException e) {
            throw new FileNotFoundException("While reading " + this + ": " + e.getMessage());
        }
    }

    public String getName() {
        String name = null;

        if (entry != null) {
            name = entry.getName();

            if (name.endsWith("/")) {
                name = name.substring(0, name.length()-1);
            }
        }

        return file.getName() + "!" + (name != null ? name : "n/a");
    }

    public URL getBaseUrl() throws MalformedURLException {
        String name = entry.getName();

        int i = name.lastIndexOf("/");

        if (i != -1) {
            name = name.substring(0, i+1);
        }
        else {
            name = "/";
        }

        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        return new URL("jar:file:" + (new File(file.getName())).getAbsolutePath() + "!" + name);
    }

    public JarFile getJarFile() {
        return file;
    }

    public String getEntryName() {
        if (entry != null) {
            return entry.getName();
        }
        else {
            return "";
        }
    }

    public long getQuickChecksum() {
        return entry.getTime();
    }

    public long getSlowChecksum() {
        return entry.getCrc();
    }
}
