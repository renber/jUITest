package de.renebergelt.juitest.core.services;

public interface TestStatusListener {

    void onTestExecutionPaused(String message);

    /**
     * Called when a log message has been received (which is an out-of-turn message as it does not need
     * to be requested)
     */
    void onLogMessageReceived(String message);

    /**
     * Called when the established connection is closed due to a protocol error
     * or after the remote-host terminated
     */
    void connectionTerminated(Throwable cause);

}
