package com.polopoly.ps.hotdeploy.topologicalsort;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.ps.hotdeploy.topologicalsort.TopologicalSorter;
import com.polopoly.ps.hotdeploy.util.DefaultVertex;

import junit.framework.TestCase;

public class TestTopologicalSorter extends TestCase {
    List<DefaultVertex> vertexes = new ArrayList<DefaultVertex>();
    private DefaultVertex vertex1;
    private DefaultVertex vertex2;
    private DefaultVertex vertex3;

    public void testSimpleDependency() {
        vertexes.add(vertex2);
        vertexes.add(vertex1);

        vertex2.addEdge(vertex1);

        List<DefaultVertex> result =
            new TopologicalSorter<DefaultVertex>(vertexes).sort();

        assertSame(vertex1, result.get(0));
        assertSame(vertex2, result.get(1));
    }

    public void testSimpleDependencyReverseOrder() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);

        vertex2.addEdge(vertex1);

        List<DefaultVertex> result =
            new TopologicalSorter<DefaultVertex>(vertexes).sort();

        assertSame(vertex1, result.get(0));
        assertSame(vertex2, result.get(1));
    }

    public void testThreeVertexes() {
        vertexes.add(vertex3);
        vertexes.add(vertex2);
        vertexes.add(vertex1);

        vertex2.addEdge(vertex1);
        vertex3.addEdge(vertex1);
        vertex3.addEdge(vertex2);

        List<DefaultVertex> result =
            new TopologicalSorter<DefaultVertex>(vertexes).sort();

        assertSame(vertex1, result.get(0));
        assertSame(vertex2, result.get(1));
        assertSame(vertex3, result.get(2));
    }

    public void testTwoCycle() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);

        vertex1.addEdge(vertex2);
        vertex2.addEdge(vertex1);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        assertEquals("1, 2", sorter.findCycle().toString());

        List<DefaultVertex> result =
            sorter.sort();

        assertEquals(2, result.size());
    }

    public void testThreeCycle() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);

        vertex3.addEdge(vertex2);
        vertex2.addEdge(vertex3);
        vertex1.addEdge(vertex1);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        assertEquals("1", sorter.findCycle().toString());
        assertEquals("2, 3", sorter.findCycle(vertex2).toString());

        List<DefaultVertex> result =
            sorter.sort();

        assertEquals(3, result.size());
    }

    public void testPickThenCycle() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);

        vertex3.addEdge(vertex2);
        vertex2.addEdge(vertex3);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        sorter.setBreakOnCycle(true);
        sorter.sort();

        assertEquals("2, 3", sorter.findCycle().toString());
    }

    public void testMinimalCycleReturned() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);

        vertex1.addEdge(vertex2);
        vertex3.addEdge(vertex2);
        vertex2.addEdge(vertex3);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        assertEquals("2, 3", sorter.findCycle().toString());
    }

    public void testCycleDoesntIncludePicked() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);

        vertex3.addEdge(vertex1);
        vertex3.addEdge(vertex2);
        vertex2.addEdge(vertex1);
        vertex2.addEdge(vertex3);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        sorter.setBreakOnCycle(true);
        sorter.sort();

        assertEquals("2, 3", sorter.findCycle().toString());
    }


    public void testMultipleEdgeSimplestCase() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);

        vertex1.addEdge(vertex2, vertex1);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        List<DefaultVertex> result = sorter.sort();

        assertSame(vertex2, result.get(0));
        assertSame(vertex1, result.get(1));
    }

    public void testMultipleEdgeHarderCase() {
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);

        vertex1.addEdge(vertex3, vertex2);
        vertex2.addEdge(vertex3);
        vertex2.addEdge(vertex1);

        TopologicalSorter<DefaultVertex> sorter =
            new TopologicalSorter<DefaultVertex>(vertexes);

        List<DefaultVertex> result = sorter.sort();

        assertSame(vertex3, result.get(0));
        assertSame(vertex1, result.get(1));
        assertSame(vertex2, result.get(2));
    }

    @Override
    public void setUp() {
        vertex1 = new DefaultVertex("1");
        vertex2 = new DefaultVertex("2");
        vertex3 = new DefaultVertex("3");
    }
}
