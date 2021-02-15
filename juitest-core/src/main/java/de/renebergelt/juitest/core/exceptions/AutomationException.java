package de.renebergelt.juitest.core.exceptions;

public class AutomationException extends RuntimeException {

    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }

}
