package de.renebergelt.juitest.core.services;

/**
 * Interface for observing the status of a running test
 */
public interface TestStatusListener {

    /**
     * Called when test execution has paused
     * @param message A message to display to the user
     */
    void onTestExecutionPaused(String message);

    /**
     * Called when a test succeeded
     * @param testId Id of the test which succeeded
     */
    void onTestSucceeded(String testId);

    /**
     * Called when a test failed
     * @param testId Id of the test which failed
     * @param reason A message indicating the cause for the failure
     */
    void onTestFailed(String testId, String reason);

    /**
     * Called when a test timed-out (and thus failed)
     * @param testId Id of the test which timed-out
     */
    void onTestTimedout(String testId);

    /**
     * Called when a test has been cancelled by the user
     * @param testId Id of the test which was canelled
     */
    void onTestCancelledByUser(String testId);

    /**
     * Called when a log message has been received
     * @param message The message which has been received
     */
    void onLogMessageReceived(String message);

    /**
     * Called when the established connection is closed due to a protocol error
     * or after the remote-host terminated
     * @param cause The underlying cause, if any
     */
    void connectionTerminated(Throwable cause);

}
