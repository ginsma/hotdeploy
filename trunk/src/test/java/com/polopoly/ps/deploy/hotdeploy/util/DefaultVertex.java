package com.polopoly.ps.deploy.hotdeploy.util;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.ps.deploy.hotdeploy.topologicalsort.Edge;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.MultipleEdge;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.SingleEdge;
import com.polopoly.ps.deploy.hotdeploy.topologicalsort.Vertex;


public class DefaultVertex implements Vertex<DefaultVertex> {
    private List<Edge<DefaultVertex>> edges = new ArrayList<Edge<DefaultVertex>>();
    private String name;

    public DefaultVertex(String name) {
        this.name = name;
    }

    public Iterable<Edge<DefaultVertex>> getEdges() {
        return edges;
    }

    public void addEdge(Vertex<DefaultVertex> vertex) {
        edges.add(new SingleEdge<DefaultVertex>(vertex));
    }

    @Override
    public String toString() {
        return name;
    }

    public void addEdge(DefaultVertex vertex, DefaultVertex vertex2) {
        edges.add(new MultipleEdge<DefaultVertex>(vertex, vertex2));
    }
}