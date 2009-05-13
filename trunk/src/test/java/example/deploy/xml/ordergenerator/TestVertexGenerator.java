package example.deploy.xml.ordergenerator;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.state.DummyDeploymentFile;
import example.deploy.hotdeploy.topologicalsort.Edge;

public class TestVertexGenerator extends TestCase {
    private DefinitionsAndReferences definitionsAndReferences;
    private DefinitionsAndReferencesGatherer parseCallback;

    public void testEmpty() {
        Collection<DeploymentFileVertex> result =
            generateVertexes();

        assertTrue(result.isEmpty());
    }

    public void testSingleDefinition() {
        DummyDeploymentFile file1 = createFile("file1");

        defines(file1, "example.Field");

        Collection<DeploymentFileVertex> result = generateVertexes();

        assertEquals(1, result.size());
    }

    public void testSingleReference() {
        DummyDeploymentFile file1 = createFile("file1");

        refers(file1, "example.Field");

        Collection<DeploymentFileVertex> result = generateVertexes();

        assertEquals(1, result.size());
    }

    public void testTwoFiles() {
        DummyDeploymentFile file1 = createFile("file1");
        DummyDeploymentFile file2 = createFile("file2");

        defines(file1, "example.Field");
        defines(file2, "example.Article");

        refers(file2, "example.Field");

        Collection<DeploymentFileVertex> result = generateVertexes();

        assertEquals(2, result.size());

        assertContains(result, new DeploymentFileVertex(file1));
        assertContains(result, new DeploymentFileVertex(file2));

        for (DeploymentFileVertex deploymentFileVertex : result) {
            Iterable<Edge<DeploymentFileVertex>> iterable = deploymentFileVertex.getEdges();

            if (deploymentFileVertex.getDeploymentFile() == file1) {
                assertEmpty(iterable);
            }
            else {
                assertEquals(new DeploymentFileVertex(file1), getFirst(iterable).getFromVertexes().iterator().next());
                assertEquals(1, size(iterable));
            }
        }
    }

    public void testRecursiveDependency() {
        DummyDeploymentFile file1 = createFile("file1");

        defines(file1, "example.Field");
        refers(file1, "example.Field");

        assertSingleVertexNoDependencies(definitionsAndReferences);
    }

    public void testMissingDeclaration() {
        DummyDeploymentFile file1 = createFile("file1");

        defines(file1, "example.Article");
        refers(file1, "example.Field");

        assertSingleVertexNoDependencies(definitionsAndReferences);
    }

    public void testTwoFilesSameDeclaration() {
        DummyDeploymentFile file1 = createFile("file1");
        DummyDeploymentFile file2 = createFile("file2");

        defines(file1, "example.Field");
        defines(file2, "example.Field");
        refers(file1, "example.Field");
        refers(file2, "example.Field");

        Collection<DeploymentFileVertex> result = generateVertexes();

        assertEquals(2, result.size());

        for (DeploymentFileVertex deploymentFileVertex : result) {
            assertEmpty(deploymentFileVertex.getEdges());
        }
    }

    private <T> void assertContains(Collection<T> collection, T object) {
        assertTrue(collection.contains(object));
    }

    private Collection<DeploymentFileVertex> generateVertexes() {
        return new VertexGenerator(definitionsAndReferences).generateVertexes();
    }

    private void defines(DummyDeploymentFile file, String externalId) {
        parseCallback.contentFound(file, externalId, Major.ARTICLE, "anytemplate");
    }

    private void refers(DeploymentFile file, String externalId) {
        parseCallback.contentReferenceFound(file, Major.ARTICLE, externalId);
    }

    @Override
    public void setUp() {
        parseCallback = new DefinitionsAndReferencesGatherer();
        definitionsAndReferences = parseCallback.getDefinitionsAndReferences();
    }

    private DummyDeploymentFile createFile(String name) {
        return new DummyDeploymentFile(name);
    }

    private void assertSingleVertexNoDependencies(
            DefinitionsAndReferences definitionsAndReferences) {
        Collection<DeploymentFileVertex> result = generateVertexes();

        assertEquals(1, result.size());

        DeploymentFileVertex vertex = getFirst(result);

        assertEquals(0, size(vertex.getEdges()));
    }

    private int size(Iterable<?> iterable) {
        return size(iterable.iterator());
    }

    private int size(Iterator<?> iterator) {
        if (!iterator.hasNext()) {
            return 0;
        }

        iterator.next();

        return size(iterator)+1;
    }

    private <T> T getFirst(
            Iterable<T> iterable) {
        return iterable.iterator().next();
    }

    private void assertEmpty(Iterable<?> iterable) {
        assertFalse(iterable.iterator().hasNext());
    }

}
