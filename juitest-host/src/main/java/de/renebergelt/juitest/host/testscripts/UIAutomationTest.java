package de.renebergelt.juitest.host.testscripts;

import de.renebergelt.juitest.core.AutoResetEvent;
import de.renebergelt.juitest.core.Timeout;
import de.renebergelt.juitest.core.exceptions.AutomationException;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.core.services.TestExecutionListener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Interface for automation tests
 * @param <THost> Type of the UIAutomationHost which runs this test
 */
public abstract class UIAutomationTest<THost extends UIAutomationHost> {

    /**
     * The default timeout (1 minute)
     */
    public static final Timeout DEFAULT_TIMEOUT = Timeout.minutes(1);

    /**
     * Indicates if the cancellation of this test has been requested
     */
    protected boolean cancellationRequested;

    TestExecutionListener executionListener;

    /**
     * The Host of this UIAutomationTest
     */
    protected THost context;

    /**
     * The current wait event
     */
    protected AtomicReference<AutoResetEvent> currentEvent = new AtomicReference<AutoResetEvent>();

    /**
     * Sets the automation context
     * @param context The automation context
     */
    public void setContext(THost context) {
        this.context = context;
    }

    /**
     * Set the execution listener
     * @param executionListener The execution listener instance to inform of the test execution state
     */
    public void setExecutionListener(TestExecutionListener executionListener) {
        this.executionListener = executionListener;
    }

    /**
     * Continue execution if this test has been paused
     */
    public void resume() {
        currentEvent.getAndUpdate((evt) -> {
            if (evt != null) evt.set();

            return null;
        });
    }

    /**
     * Pause test execution and signal to the test monitor that the current script has been paused.
     * test execution will continue once the test monitor sends the continue execution command
     *
     * Test will only be paused, if a TestExecutionListener has been set
     * @param message A message to display to the user
     */
    public void pause(String message) {

        if (executionListener == null) return;

        AutoResetEvent evt = new AutoResetEvent(false);
        currentEvent.set(evt);

        try {
            executionListener.testPaused(message);
            evt.waitOne();
        } catch (InterruptedException e) {
            throw new CancellationException();
        }

        if (cancellationRequested) {
            throw new CancellationException();
        }
    }

    /**
     * Cancel the execution of this script
     */
    public void cancel() {
        cancellationRequested = true;

        Thread cancelThread = new Thread( () -> {
            // signal the waiting event, so that we can cancel
            AutoResetEvent evt = currentEvent.get();
            if (evt != null) {
                evt.cancel();
            }
        });
        cancelThread.start();
    }

    /**
     * Send teh given log message to the registered TestExucitionListener (if any)
     * @param message The log message
     */
    public void log(String message) {
        if (executionListener == null) return;

        executionListener.testLog(message);
    }

    /**
     * Throws an AutomationException when the condition is not satisfied and thus aborts script execution
     * @param condition The condition to check
     * @throws AutomationException Thrown, if the condition supplier returns false
     */
    public void scriptAssert(Supplier<Boolean> condition) throws AutomationException {
        if (!condition.get())
            throw new AutomationException("Condition not satisfied");
    }

    /**
     * Throws an AutomationException with the given failureMessage when the condition is not satisfied and thus aborts script execution
     * @param condition The condition to check
     * @param failureMessage The message to use for the AutomationException if the condition check failed
     * @throws AutomationException Thrown, if the condition supplier returns false
     */
    public void scriptAssert(Supplier<Boolean> condition, String failureMessage) throws AutomationException {
        if (!condition.get())
            throw new AutomationException("Condition not satisfied: " + failureMessage);
    }

    /**
     * Executed before every tests
     */
    public void beforeTest() {
        // override in derived class
    }

    /**
     * Executed after a test has run
     */
    public void afterTest() {
        // override in derived class
    }
}
