package de.renebergelt.juitest.core.exceptions;

/**
 * Exception which is thrown when an automation error occurs
 */
public class AutomationException extends RuntimeException {

    /**
     * Constructs a new AutomationException with the specified detail message.
     * @param message The error message
     */
    public AutomationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AutomationException with the specified detail message and cause
     * @param message The error message
     * @param cause Throwable which caused this AutomationException (if any)
     */
    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }

}
