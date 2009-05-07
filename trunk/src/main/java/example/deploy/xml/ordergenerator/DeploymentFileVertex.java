package example.deploy.xml.ordergenerator;

import java.util.HashSet;
import java.util.Set;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.Vertex;

public class DeploymentFileVertex implements Vertex<DeploymentFileVertex> {
    private Set<Vertex<DeploymentFileVertex>> dependencies = new HashSet<Vertex<DeploymentFileVertex>>();
    private DeploymentFile deploymentFile;

    public DeploymentFileVertex(DeploymentFile deploymentFile) {
        this.deploymentFile = deploymentFile;
    }

    public Iterable<Vertex<DeploymentFileVertex>> getDependencies() {
        return dependencies;
    }

    public void addDependency(Vertex<DeploymentFileVertex> vertex) {
        dependencies.add(vertex);
    }

    public DeploymentFile getDeploymentFile() {
        return deploymentFile;
    }

    @Override
    public String toString() {
        return deploymentFile.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeploymentFileVertex && ((DeploymentFileVertex) obj).deploymentFile.equals(deploymentFile);
    }

    @Override
    public int hashCode() {
        return deploymentFile.hashCode();
    }


}
