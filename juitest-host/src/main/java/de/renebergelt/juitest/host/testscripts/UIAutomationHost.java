package de.renebergelt.juitest.host.testscripts;

import de.renebergelt.juitest.core.services.IPCTransmitter;

public interface UIAutomationHost {

    /**
     * Register an IPCTransmitter instance which can be used to send
     * messages to the Test Monitor
     */
    void setTransmitter(IPCTransmitter transmitter);

    /**
     * Launch the application under test using the given command line arguments
     */
    void launchApplicationUnderTest(String...arguments);

    /**
     * Indicates whether launchApplicationUnderTest ha sbeen called successfully before
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
