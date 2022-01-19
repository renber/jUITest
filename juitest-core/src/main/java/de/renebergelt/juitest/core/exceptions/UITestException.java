package de.renebergelt.juitest.core.exceptions;

/**
 * Signals a failing test
 */
public class UITestException extends Exception {

    /**
     * Constructs a new UITestException with the specified detail message.
     * @param message The error message
     */
    public UITestException(String message) {
        super(message);
    }

    /**
     * Constructs a new UITestException with the specified detail message and cause
     * @param message The error message
     * @param cause Throwable which caused this UITestException (if any)
     */
    public UITestException(String message, Throwable cause) {
        super(message, cause);
    }

}
