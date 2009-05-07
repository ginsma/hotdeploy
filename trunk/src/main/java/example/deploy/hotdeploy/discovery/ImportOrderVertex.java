package example.deploy.hotdeploy.discovery;

import java.util.ArrayList;
import java.util.List;

import example.deploy.hotdeploy.util.Vertex;

public class ImportOrderVertex implements Vertex<ImportOrderVertex> {
    private List<Vertex<ImportOrderVertex>> dependencies = new ArrayList<Vertex<ImportOrderVertex>>();
    private ImportOrderFile importOrderFile;

    public ImportOrderVertex(ImportOrderFile importOrderFile) {
        this.importOrderFile = importOrderFile;
    }

    public Iterable<Vertex<ImportOrderVertex>> getDependencies() {
        return dependencies;
    }

    public void addDependency(Vertex<ImportOrderVertex> vertex) {
        dependencies.add(vertex);
    }

    public ImportOrderFile getImportOrderFile() {
        return importOrderFile;
    }
}
