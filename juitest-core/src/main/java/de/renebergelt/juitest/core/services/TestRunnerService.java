package de.renebergelt.juitest.core.services;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.exceptions.UITestException;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

/**
 * Interface to control executed test scripts
 * in the UIAutomationHost
 */
public interface TestRunnerService {

    /**
     * Indicates whether the runner service has been attached to an instance of the application under test
     */
    boolean isAttached();

    /**
     * Attach this test runner to a (new) instance of the application under test which is launched
     * using the given arguments
     */
    void attach(String...programArguments);

    /**
     * Disattach from the automation instance
     */
    void disattach();

    /**
     * Run the given test in the attached applications's UI process
     */
    void runTest(TestDescriptor testDescriptor) throws TimeoutException, CancellationException, UITestException;

    /**
     * Resume test execution if it has been paused
     */
    void resumeTest();

    void cancelRunningTest();

    /**
     * Add a listener to receive additional test related status changes
     */
    void addTestStatusListener(TestStatusListener testStatusListener);

    /**
     * Remove the given TestStatusListener
     */
    void removeTestStatusListener(TestStatusListener testStatusListener);
}
