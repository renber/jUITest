package de.renebergelt.juitest.core.exceptions;

/**
 * Signals a failing test
 */
public class UITestException extends Exception {

    public UITestException(String message) {
        super(message);
    }

    public UITestException(String message, Throwable cause) {
        super(message, cause);
    }

}
