package example.deploy.hotdeploy.topologicalsort;

import java.util.HashSet;
import java.util.Set;

public class MultipleEdge<T> implements Edge<T> {
    private Set<Vertex<T>> fromVertexes = new HashSet<Vertex<T>>();
    private String description;

    public MultipleEdge(Vertex<T> vertex, Vertex<T> vertex2) {
        fromVertexes.add(vertex);
        fromVertexes.add(vertex2);
    }

    @SuppressWarnings("unchecked")
    public MultipleEdge(Set<T> vertexes) {
        fromVertexes.addAll((Set<Vertex<T>>) vertexes);
    }

    public Iterable<Vertex<T>> getFromVertexes() {
        return fromVertexes;
    }

    public boolean isFromAny(Set<Vertex<T>> vertexes) {
        for (Vertex<T> fromVertex : fromVertexes) {
            if (vertexes.contains(fromVertex)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        return o instanceof MultipleEdge &&
            ((MultipleEdge) o).fromVertexes.equals(fromVertexes);
    }

    @Override
    public int hashCode() {
        return fromVertexes.hashCode();
    }

    @Override
    public String toString() {
        return fromVertexes.toString() + (description != null ? " (" + description + ")" : "");
    }

    public void setDescription(String externalId) {
        this.description = externalId;
    }

    public String getDescription() {
        return description;
    }
}
