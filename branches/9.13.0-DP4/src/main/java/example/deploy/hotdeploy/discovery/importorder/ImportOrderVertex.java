package example.deploy.hotdeploy.discovery.importorder;

import java.util.ArrayList;
import java.util.List;

import example.deploy.hotdeploy.topologicalsort.Edge;
import example.deploy.hotdeploy.topologicalsort.SingleEdge;
import example.deploy.hotdeploy.topologicalsort.Vertex;

public class ImportOrderVertex implements Vertex<ImportOrderVertex> {
    private List<Edge<ImportOrderVertex>> dependencies = new ArrayList<Edge<ImportOrderVertex>>();
    private ImportOrder importOrderFile;

    public ImportOrderVertex(ImportOrder importOrderFile) {
        this.importOrderFile = importOrderFile;
    }

    public Iterable<Edge<ImportOrderVertex>> getEdges() {
        return dependencies;
    }

    public void addEdge(Edge<ImportOrderVertex> vertex) {
        dependencies.add(vertex);
    }

    public ImportOrder getImportOrderFile() {
        return importOrderFile;
    }

    public void addDependency(ImportOrderVertex hotdeployVertex) {
        addEdge(new SingleEdge<ImportOrderVertex>(hotdeployVertex));
    }

    @Override
    public String toString() {
        return importOrderFile.toString();
    }
}
