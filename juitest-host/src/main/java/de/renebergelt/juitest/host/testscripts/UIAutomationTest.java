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
 */
public abstract class UIAutomationTest<THost extends UIAutomationHost> {

    public static final Timeout DEFAULT_TIMEOUT = Timeout.minutes(1);
    protected boolean cancellationRequested;
    TestExecutionListener executionListener;

    protected AtomicReference<AutoResetEvent> currentEvent = new AtomicReference<AutoResetEvent>();

    public abstract String getName();

    public void setExecutionListener(TestExecutionListener executionListener) {
        this.executionListener = executionListener;
    }

    public UIAutomationTest(Object... namedParameters) {
        if (namedParameters.length % 2 != 0)
            throw new IllegalArgumentException("namedParameters");

        for(int i = 0; i < namedParameters.length; i += 2) {
            setParameter(String.valueOf(namedParameters[i]), namedParameters[i + 1]);
        }
    }

    /**
     * Run this script
     * (Should be called from a thread separate from the EDT)
     */
    public void run(THost context) throws CancellationException, TimeoutException, UITestException {
        cancellationRequested = false;
        doRun(context);
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
     */
    public void scriptAssert(Supplier<Boolean> condition) throws AutomationException {
        if (!condition.get())
            throw new AutomationException("Condition not satisfied");
    }

    /**
     * Throws an AutomationException with the given failureMessage when the condition is not satisfied and thus aborts script execution
     */
    public void scriptAssert(Supplier<Boolean> condition, String failureMessage) throws AutomationException {
        if (!condition.get())
            throw new AutomationException("Condition not satisfied: " + failureMessage);
    }

    public abstract void setParameter(String parameterName, Object parameterValue);

    protected abstract void doRun(THost context) throws CancellationException, TimeoutException, UITestException;

}
