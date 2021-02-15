package de.renebergelt.juitest.host.services;

import de.renebergelt.juitest.host.testscripts.UIAutomationTest;
import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.comm.IPCMessages;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.host.testscripts.UIAutomationHost;
import de.renebergelt.juitest.core.services.IPCTransmitter;
import de.renebergelt.juitest.core.services.TestExecutionListener;
import de.renebergelt.juitest.core.services.TestRunnerService;
import de.renebergelt.juitest.core.services.TestStatusListener;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of TestRunnerService which executes tests in the same process
 */
public class SameProcessTestRunnerService implements TestRunnerService {

    IPCTransmitter transmitter;

    AtomicReference<AutomationTestThread> currentRunningTest = new AtomicReference<>();

    UIAutomationHost automationHost;

    public void setTransmitter(IPCTransmitter transmitter) {
        this.transmitter = transmitter;

        if (automationHost != null) {
            automationHost.setTransmitter(transmitter);
        }
    }

    public SameProcessTestRunnerService(UIAutomationHost host) {
        this.automationHost = host;
    }

    @Override
    public boolean isAttached() {
        return automationHost != null && automationHost.hasLaunched();
    }

    @Override
    public void attach(String...programArguments) {
        if (automationHost.hasLaunched())
            throw new IllegalStateException("TestRunner has already been attached");

        AtomicReference<UIAutomationHost> result = new AtomicReference<>();

        automationHost.launchApplicationUnderTest(programArguments);

        if (!automationHost.hasLaunched()) {
            throw new RuntimeException("Could not launch Application under test");
        }
    }

    @Override
    public void disattach() {
        if (isAttached()) {
            automationHost.teardown();
        }
    }

    public void resumeTest() {
        AutomationTestThread curTest = currentRunningTest.get();
        if (curTest != null) {
            curTest.resume();
        }
    }

    @Override
    public void runTest(TestDescriptor testDescriptor) throws TimeoutException, CancellationException, UITestException {
        if (automationHost == null)
            throw new IllegalStateException("TestRunner has not been attached");

        // instantiate the test class
        UIAutomationTest test = instantiateTest(testDescriptor);

        AutomationTestThread aThread = currentRunningTest.updateAndGet((current) -> {
            if (current != null)
                throw new IllegalStateException("Cannot start a new test when a different test is still running.");

            AutomationTestThread at = AutomationTestThread.createThreadFor(automationHost, test);
            return at;
        });

        // run in new Thread
        aThread.setExecutionListener(new TestExecutionListener() {
            @Override
            public void testLog(String message) {
                if (transmitter != null) {
                    transmitter.sendMessage(IPCMessages.createTestLogMessage("", message));
                }
            }

            @Override
            public void testPaused(String message) {
                if (transmitter != null) {
                    transmitter.sendMessage(IPCMessages.createTestPausedMessage("", message));
                }
            }
        });

        try {
            aThread.runAndWait();
        } finally {
            currentRunningTest.set(null);

            automationHost.cleanup_after_test();
        }
    }

    @Override
    public void cancelRunningTest() {
        currentRunningTest.getAndUpdate( (s) -> {
            try {
                if (s != null) {
                    s.cancel();
                }
            } finally {
                return null;
            }
        });
    }

    private UIAutomationTest instantiateTest(TestDescriptor descriptor) throws UITestException {
        try {
            Class<?> clazz = Class.forName(descriptor.getTestClassName());
            Constructor<?> ctor = clazz.getConstructor();
            UIAutomationTest instance = (UIAutomationTest)ctor.newInstance();

            // set parameters
            Object[] params = descriptor.getParameters();

            for(int pidx = 0; pidx < descriptor.getParameters().length; pidx += 2) {
                instance.setParameter(String.valueOf(params[pidx]), params[pidx + 1]);
            }

            return instance;
        } catch (ClassNotFoundException e) {
            throw new UITestException("Invalid test class: " + descriptor.getTestClassName(), e);
        } catch (NoSuchMethodException e) {
            throw new UITestException("Missing parameter-less constructor: " + descriptor.getTestClassName(), e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new UITestException("Could not instantiate test class: " + descriptor.getTestClassName(), e);
        }
    }

    public void addTestStatusListener(TestStatusListener testStatusListener) {
        // not supported
    }

    public void removeTestStatusListener(TestStatusListener testStatusListener) {
        // not supported
    }

    static class AutomationTestThread {
        private Thread thread;
        private UIAutomationTest test;
        private Runnable threadBody;
        private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
        private AtomicReference<Throwable> failException = new AtomicReference<>(null);

        public void setExecutionListener(TestExecutionListener executionListener) {
            test.setExecutionListener(executionListener);
        }

        private AutomationTestThread(UIAutomationHost context, UIAutomationTest test) {
            this.test = test;

            threadBody = () -> {
                failException.set(null);

                try {
                    test.run(context);
                } catch (Exception e) {
                    failException.getAndUpdate( (v) -> {
                        if (v == null)
                            return e;

                        return v;
                    });
                }
                catch (Error e) { // JUnit assertions are Errors not Exceptions
                    failException.getAndUpdate( (v) -> {
                        if (v == null)
                            return e;

                        return v;
                    });
                }
            };

            uncaughtExceptionHandler = (t, e) -> {
                // uncaught exceptions fail the automation test
                failException.getAndUpdate( (v) -> {

                    if (v == null)
                        return e;

                    return v;
                });
                thread.interrupt();
            };
            thread = new Thread(threadBody);
        }

        private void runAndWait() throws TimeoutException, UITestException, CancellationException {

            // register the UncaughtExceptionHandler so that we can fail the test
            // if an uncaught exception occurs

            AtomicReference<Thread.UncaughtExceptionHandler> oldUncaughtExceptionHandler = new AtomicReference<>();
            try {
                SwingUtilities.invokeAndWait( () -> {
                    oldUncaughtExceptionHandler.set(Thread.getDefaultUncaughtExceptionHandler());
                    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
                });
            } catch (Exception e) {
                // TODO: log error
                System.out.println("Could not set UncaughtExceptionHandler");
            }

            try {
                thread.start();
                thread.join(); // TODO: pass test timeout in milliseconds
            } catch (InterruptedException e) {
                throw new CancellationException();
            } finally {
                try {
                    SwingUtilities.invokeAndWait( () -> Thread.setDefaultUncaughtExceptionHandler(oldUncaughtExceptionHandler.get()));
                } catch (Exception e) {
                    // TODO: log error
                    System.out.println("Could not reset UncaughtExceptionHandler");
                }
            }

            // unpack the exception
            Throwable t = failException.get();

            if (t != null) {
                // pass-through knows exceptions as is
                if (t instanceof  TimeoutException) throw (TimeoutException)t;
                if (t instanceof UITestException) throw (UITestException)t;

                if (t instanceof Error) throw (Error)t;

                // do not wrap RuntimeExceptions in another RuntimeException
                if (t instanceof RuntimeException) throw (RuntimeException)t;

                // throw unchecked
                throw new RuntimeException(t);
            }
        }

        /**
         * Resume execution of this test if it has been paused before
         */
        public void resume() {
            test.resume();
        }

        /**
         * Cancel the thread (as requested by the user)
         */
        public void cancel() {
            failException.set(new CancellationException());

            thread.interrupt();
        }

        public static AutomationTestThread createThreadFor(UIAutomationHost context, UIAutomationTest test) {
            return new AutomationTestThread(context, test);
        }
    }
}
