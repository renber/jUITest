package de.renebergelt.juitest.core;

/**
 * Represents a timeout for UI actions
 */
public class Timeout {

    /**
     * Can be passed to signal that no timeout is desired
     */
    public static Timeout NONE = new Timeout(-1);

    private long milliseconds;

    private Timeout(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Return the length of the timeout in milliseconds
     * @return Length of the timeout in milliseconds
     */
    public long getMilliseconds() {
        return milliseconds;
    }

    /**
     * Create a new timeout object
     * @param milliseconds Length in milliseconds
     * @return The timeout object
     */
    public static Timeout milliseconds(long milliseconds) {
        return new Timeout(milliseconds);
    }

    /**
     * Create a new timeout object
     * @param seconds Length in seconds
     * @return The timeout object
     */
    public static Timeout seconds(double seconds) {
        return new Timeout((int)(seconds * 1000));
    }

    /**
     * Create a new timeout object
     * @param minutes Length in minutes
     * @return The timeout object
     */
    public static Timeout minutes(double minutes) {
        return new Timeout((int)(minutes * 60 * 1000));
    }

}
