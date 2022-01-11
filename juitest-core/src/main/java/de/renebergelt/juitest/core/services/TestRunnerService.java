package de.renebergelt.juitest.core.services;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.exceptions.UITestException;

import java.util.List;
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
     * Return a list of all available tests
     */
    List<TestDescriptor> discoverTests();

    /**
     * Run the given test in the attached application's UI process
     * @param testDescriptor The descriptor for the test to run
     * @throws TimeoutException Thrown if the test timed out before completing
     * @throws CancellationException Thrown if the test was cancelled before completing
     * @throws UITestException Thrown if a test assertion fails
     */
    void runTest(TestDescriptor testDescriptor) throws TimeoutException, CancellationException, UITestException;

    /**
     * Resume test execution if it has been paused
     */
    void resumeTest();

    void cancelRunningTest();

    /**
     * Add a listener to receive additional test related status changes
     * @param testStatusListener The listener instance to add
     */
    void addTestStatusListener(TestStatusListener testStatusListener);

    /**
     * Remove the given TestStatusListener
     * @param testStatusListener The listener instance to remove
     */
    void removeTestStatusListener(TestStatusListener testStatusListener);
}
