package example.deploy.hotdeploy.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopologicalSorter {
    private static final Logger logger =
        Logger.getLogger(TopologicalSorter.class.getName());

    private List<Integer>[] vertexes;

    private List<Integer> result = new ArrayList<Integer>();

    public TopologicalSorter(List<Integer>[] vertexes) {
        this.vertexes = vertexes;
    }

    public int[] sort() {
        while (notDone()) {
            boolean found = false;

            for (int vertexIndex = 0; vertexIndex < vertexes.length; vertexIndex++) {
                if (canBePicked(vertexIndex)) {
                    found = true;
                    pick(vertexIndex);
                }
            }

            if (!found) {
                logger.log(Level.SEVERE, "There is a cyclical dependency among the content to deploy.");

                pickAny();
            }
        }

        return resultAsArray();
    }

    private void pickAny() {
        for (int vertexIndex = 0; vertexIndex < vertexes.length; vertexIndex++) {
            if (isNotYetPicked(vertexIndex)) {
                pick(vertexIndex);
            }
        }
    }

    private int[] resultAsArray() {
        int[] resultArray = new int[result.size()];

        for (int i = 0; i < result.size(); i++) {
            resultArray[i] = result.get(i);
        }

        return resultArray;
    }

    private void pick(int vertexIndex) {
        result.add(vertexIndex);
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
        return result.size() < vertexes.length;
    }
}
