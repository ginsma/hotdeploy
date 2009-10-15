package example.deploy.hotdeploy.discovery.importorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.JarDeploymentDirectory;

public class ImportOrder extends ArrayList<DeploymentFile>{
    private static final Logger logger =
        Logger.getLogger(ImportOrder.class.getName());

    private List<String> dependencies = new ArrayList<String>();
    private DeploymentDirectory directory;

    public ImportOrder(DeploymentDirectory directory) {
        this.directory = directory;
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

    public Collection<String> getDependencies() {
        return dependencies;
    }

    public DeploymentDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(DeploymentDirectory directory) {
        this.directory = directory;
    }

    public void setDirectory(File directory) {
        this.directory = new FileDeploymentDirectory(directory);
    }

    public String calculateDependencyName() {
        if (directory instanceof JarDeploymentDirectory) {
            String jarAbsolutePath = ((JarDeploymentDirectory) directory).getJarFileName();

            return new JarFileBaseNameFinder().getBaseName(jarAbsolutePath);
        }
        else {
            return directory.getName();
        }
    }

    @Override
    public String toString() {
        return "import order file in " + directory;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImportOrder && ((ImportOrder) o).getDirectory().equals(directory);
    }

    @Override
    public int hashCode() {
        return directory.hashCode();
    }
}
