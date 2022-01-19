package de.renebergelt.juitest.host.testscripts;

import de.renebergelt.juitest.core.services.IPCTransmitter;

/**
 * Interface for automation hosts
 */
public interface UIAutomationHost {

    /**
     * Register an IPCTransmitter instance which can be used to send
     * messages to the Test Monitor
     * @param transmitter The transmitter to use for sending messages
     */
    void setTransmitter(IPCTransmitter transmitter);

    /**
     * Launch the application under test using the given command line arguments
     * @param arguments The arguments to pass to the application-under-test
     */
    void launchApplicationUnderTest(String...arguments);

    /**
     * Indicates whether launchApplicationUnderTest has been called successfully before
     * @return True, if the application-under-test has been launched
     */
    boolean hasLaunched();

    /**
     * Revert the application state after a test has been run
     */
    void cleanup_after_test();

    /**
     * Shutdown the application under test
     */
    void teardown();

}
