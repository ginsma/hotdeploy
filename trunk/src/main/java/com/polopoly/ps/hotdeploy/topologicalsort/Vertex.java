package com.polopoly.ps.hotdeploy.topologicalsort;

public interface Vertex<T> {
    Iterable<Edge<T>> getEdges();
}
