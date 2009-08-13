package example.deploy.hotdeploy.text;

public class DeployException extends Exception {

    public DeployException(String message) {
        super(message);
    }

    public DeployException(String message, Throwable cause) {
        super(message, cause);
    }

}
