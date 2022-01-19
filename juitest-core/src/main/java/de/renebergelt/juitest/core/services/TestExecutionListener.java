package de.renebergelt.juitest.core.services;

/**
 * Listener interface through which an automation test can indicate events
 */
public interface TestExecutionListener {

    /**
     * Called when the test wants to log a message
     * @param message The message to log
     */
    void testLog(String message);

    /**
     * Called when the test pauses execution
     * @param message A message to display to the user
     */
    void testPaused(String message);

    /**
     * Called when the test fails
     * @param error The cause of the failure
     */
    void testFailed(Throwable error);

    /**
     * Called when the test succeeded
     */
    void testSucceeded();

}
