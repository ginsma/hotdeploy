package com.polopoly.ps.deploy.xml.ordergenerator;

import java.util.HashSet;
import java.util.Set;

import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.Edge;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.MultipleEdge;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.SingleEdge;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.Vertex;


public class DeploymentFileVertex implements Vertex<DeploymentFileVertex> {
    private Set<Edge<DeploymentFileVertex>> edges = new HashSet<Edge<DeploymentFileVertex>>();
    private DeploymentFile deploymentFile;

    public DeploymentFileVertex(DeploymentFile deploymentFile) {
        this.deploymentFile = deploymentFile;
    }

    public Iterable<Edge<DeploymentFileVertex>> getEdges() {
        return edges;
    }

    public void addDependency(String externalId, Vertex<DeploymentFileVertex> vertex) {
        SingleEdge<DeploymentFileVertex> edge = new SingleEdge<DeploymentFileVertex>(vertex);
        edge.setDescription(externalId);
        edges.add(edge);
    }

    public void addDependencies(String externalId, Set<DeploymentFileVertex> vertexes) {
        if (vertexes.size() == 1) {
            addDependency(externalId, vertexes.iterator().next());
        }
        else {
            MultipleEdge<DeploymentFileVertex> edge = new MultipleEdge<DeploymentFileVertex>(vertexes);

            edge.setDescription(externalId);
            edges.add(edge);
        }
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
