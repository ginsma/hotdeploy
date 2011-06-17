package example.deploy.hotdeploy.client;

public class ConnectException extends Exception {

    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectException(String message) {
        super(message);
    }

}
