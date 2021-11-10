package de.renebergelt.juitest.core.services;

public interface TestStatusListener {

    void onTestExecutionPaused(String message);

    void onTestSucceeded(String testId);

    void onTestFailed(String testId, String reason);

    void onTestTimedout(String testId);

    void onTestCancelledByUser(String testId);

    /**
     * Called when a log message has been received
     */
    void onLogMessageReceived(String message);

    /**
     * Called when the established connection is closed due to a protocol error
     * or after the remote-host terminated
     */
    void connectionTerminated(Throwable cause);

}
