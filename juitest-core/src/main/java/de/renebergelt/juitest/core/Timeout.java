package de.renebergelt.juitest.core;

/**
 * Represents a timeout for UI actions
 */
public class Timeout {

    public static Timeout NONE = new Timeout(-1);

    private long milliseconds;

    private Timeout(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public static Timeout milliseconds(long milliseconds) {
        return new Timeout(milliseconds);
    }

    public static Timeout seconds(double seconds) {
        return new Timeout((int)(seconds * 1000));
    }

    public static Timeout minutes(double minutes) {
        return new Timeout((int)(minutes * 60 * 1000));
    }

}
