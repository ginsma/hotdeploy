package example.deploy.hotdeploy.file;

public interface DeploymentObject {
    String getName();

    boolean imports(DeploymentObject object);
}
