package com.polopoly.ps.hotdeploy.xml.export;

public class NotExportableException extends Exception {

    public NotExportableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExportableException(String message) {
        super(message);
    }

    public NotExportableException(Throwable cause) {
        super(cause);
    }

}
