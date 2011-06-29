package com.polopoly.ps.deploy.hotdeploy.topologicalsort;

public interface Vertex<T> {
    Iterable<Edge<T>> getEdges();
}
