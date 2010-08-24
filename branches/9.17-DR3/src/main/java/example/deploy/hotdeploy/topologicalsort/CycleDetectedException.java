package example.deploy.hotdeploy.topologicalsort;


public class CycleDetectedException extends Exception {
    private Vertex<?> detectedAt;

    public CycleDetectedException(Vertex<?> detectedAt) {
        this.detectedAt = detectedAt;
    }

    public Vertex<?> getDetectedAt() {
        return detectedAt;
    }

}
