package example.deploy.hotdeploy.util;

public interface Vertex<T> {
    Iterable<Vertex<T>> getDependencies();
}
