package de.renebergelt.juitest.core.services;

/**
 * Listener interface through which an automation test can indicate events
 */
public interface TestExecutionListener {

    void testLog(String message);

    void testPaused(String message);

}
