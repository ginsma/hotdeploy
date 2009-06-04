package example.deploy.hotdeploy.file;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarDeploymentDirectory extends AbstractDeploymentObject implements DeploymentDirectory {
    protected JarFile jarFile;
    private ZipEntry entry;

    public JarDeploymentDirectory(JarFile file, ZipEntry entry) {
        this.jarFile = file;
        this.entry = entry;
    }

    public boolean exists() {
        return entry != null;
    }

    public DeploymentObject getFile(String fileName) throws FileNotFoundException {
        if (fileName.endsWith("/")) {
            fileName = fileName.substring(0, fileName.length()-1);
        }

        if (entry != null) {
            if (!entry.isDirectory()) {
                throw new RuntimeException("Attempt to get subdirectory " + fileName + " from file " + this + ".");
            }
            fileName = getNameWithinJar() + fileName;
        }

        ZipEntry newEntry = jarFile.getEntry(fileName + '/');

        if (newEntry != null) {
            return new JarDeploymentDirectory(jarFile, newEntry);
        }

        newEntry = jarFile.getEntry(fileName);

        if (newEntry == null) {
            throw new FileNotFoundException(fileName + " could not be fetched from JAR " + jarFile.getName());
        }

        return new JarDeploymentFile(jarFile, newEntry);
    }

    public DeploymentObject[] listFiles() {
        List<DeploymentObject> result = new ArrayList<DeploymentObject>();

        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry resultEntry = entries.nextElement();

            if (isInThisDir(resultEntry)) {
                if (resultEntry.isDirectory()) {
                    result.add(new JarDeploymentDirectory(jarFile, resultEntry));
                }
                else {
                    result.add(new JarDeploymentFile(jarFile, resultEntry));
                }
            }
        }

        return result.toArray(new DeploymentObject[result.size()]);
    }

    protected boolean isInThisDir(JarEntry resultEntry) {
        return resultEntry.getName().startsWith(getNameWithinJar()) &&
            !resultEntry.getName().equals(getNameWithinJar());
    }

    public String getName() {
        String nameWithinJar = getNameWithinJar();

        if (nameWithinJar.endsWith("/")) {
            nameWithinJar = nameWithinJar.substring(0, nameWithinJar.length()-1);
        }

        return jarFile.getName() + "!" + nameWithinJar;
    }

    protected String getNameWithinJar() {
        return entry.getName();
    }

    public String getJarFileName() {
        return jarFile.getName();
    }

    public String getRelativeName(DeploymentObject deploymentObject) {
        if (deploymentObject instanceof JarDeploymentFile &&
                ((JarDeploymentFile) deploymentObject).getJarFile().equals(getJarFile())) {
            String fileName = ((JarDeploymentFile) deploymentObject).getNameWithinJar();
            String directoryName = getNameWithinJar();

            if (fileName.startsWith(directoryName)) {
                return fileName.substring(directoryName.length());
            }
        }

        return deploymentObject.getName();
    }

    protected JarFile getJarFile() {
        return jarFile;
    }

    public boolean imports(DeploymentObject object) {
        if (object instanceof JarDeploymentDirectory) {
            return ((JarDeploymentDirectory) object).getJarFile().equals(jarFile) &&
                ((JarDeploymentDirectory) object).getNameWithinJar().startsWith(getNameWithinJar());
        }

        if (object instanceof JarDeploymentFile) {
            return ((JarDeploymentFile) object).getJarFile().equals(jarFile) &&
                ((JarDeploymentFile) object).getNameWithinJar().startsWith(getNameWithinJar());
        }

        return false;
    }
}
