package example.deploy.hotdeploy.topologicalsort;

public interface Vertex<T> {
    Iterable<Edge<T>> getEdges();
}
