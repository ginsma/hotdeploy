package example.deploy.hotdeploy.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopologicalSorter<T extends Vertex<T>> {
    private static final Logger logger =
        Logger.getLogger(TopologicalSorter.class.getName());

    private List<Integer>[] vertexes;

    private List<Integer> sortedOrder = new ArrayList<Integer>();

    private List<T> vertexesAsObject;

    @SuppressWarnings("unchecked")
    public TopologicalSorter(List<T> vertexesAsObjects) {
        this.vertexesAsObject = vertexesAsObjects;
        vertexes = new List[vertexesAsObjects.size()];

        for (int vertexIndex = 0; vertexIndex < vertexesAsObjects.size(); vertexIndex++) {
            vertexes[vertexIndex] = new ArrayList<Integer>();

            Vertex<T> vertexObject = vertexesAsObjects.get(vertexIndex);

            for (Vertex<T> dependency : vertexObject.getDependencies()) {
                int dependencyIndex = vertexesAsObjects.indexOf(dependency);

                if (dependencyIndex == -1) {
                    logger.log(Level.WARNING, "The dependency " + dependency + " was not present in the list of vertexes.");
                }
                else {
                    vertexes[vertexIndex].add(dependencyIndex);
                }
            }
        }
    }

    public TopologicalSorter(List<Integer>[] vertexes) {
        this.vertexes = vertexes;
    }

    public List<T> sort() {
        while (notDone()) {
            boolean found = false;

            for (int vertexIndex = 0; vertexIndex < vertexes.length; vertexIndex++) {
                if (canBePicked(vertexIndex)) {
                    found = true;
                    pick(vertexIndex);
                }
            }

            if (!found) {
                logger.log(Level.SEVERE, "There is a cyclical dependency among the vertexes: " + getUnpickedCycleAsString());

                pick(getFirstUnpicked());
            }
        }

        return resultAsList();
    }

    public String getUnpickedCycleAsString() {
        return getUnpickedCycleAsString(getFirstUnpicked());
    }

    public String getUnpickedCycleAsString(int startingIndex) {
        StringBuffer result = new StringBuffer(100);

        Set<Vertex<T>> cycleDependencies = getAllDependenciesUntilCycle(vertexesAsObject.get(startingIndex));

        for (Vertex<T> cycleDependency : cycleDependencies) {
            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(cycleDependency.toString());
        }

        return result.toString();
    }

    private Set<Vertex<T>> getAllDependenciesUntilCycle(T t) {
        Set<Vertex<T>> result = new LinkedHashSet<Vertex<T>>();

        result.add(t);
        try {
            getAllDependenciesUntilCycle(t, result);
        } catch (CycleDetectedException e) {
            // done.
        }

        return result;
    }

    private void getAllDependenciesUntilCycle(Vertex<T> t, Set<Vertex<T>> result) throws CycleDetectedException {
        for (Vertex<T> dependency : t.getDependencies()) {
            boolean alreadyAdded = !result.add(dependency);

            if (alreadyAdded) {
                throw new CycleDetectedException();
            }

            getAllDependenciesUntilCycle(dependency, result);
        }
    }

    private int getFirstUnpicked() {
        for (int vertexIndex = 0; vertexIndex < vertexes.length; vertexIndex++) {
            if (isNotYetPicked(vertexIndex)) {
                return vertexIndex;
            }
        }

        throw new IllegalStateException("There is no unpicked vertex.");
    }

    private List<T> resultAsList() {
        List<T> result = new ArrayList<T>(sortedOrder.size());

        for (Integer index : sortedOrder) {
            result.add(vertexesAsObject.get(index));
        }

        return result;
    }

    private void pick(int vertexIndex) {
        sortedOrder.add(vertexIndex);
        vertexes[vertexIndex] = null;

        Integer iAsInt = vertexIndex;

        for (int j = 0; j < vertexes.length; j++) {
            if (isNotYetPicked(j)) {
                vertexes[j].remove(iAsInt);
            }
        }
    }

    private boolean isNotYetPicked(int j) {
        return vertexes[j] != null;
    }

    private boolean canBePicked(int vertexIndex) {
        return isNotYetPicked(vertexIndex) && vertexes[vertexIndex].isEmpty();
    }

    private boolean notDone() {
        return sortedOrder.size() < vertexes.length;
    }
}
