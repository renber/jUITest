package de.renebergelt.juitest.host.testscripts;

import de.renber.quiterables.QuIterables;
import de.renebergelt.juitest.core.AutoResetEvent;
import de.renebergelt.juitest.core.Timeout;
import de.renebergelt.juitest.core.exceptions.AutomationException;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class SwingAutomationTest<THost extends UIAutomationHost> extends UIAutomationTest<THost> {

    /**
     * Execute the given method in the UI thread (i.e. Swing's Event Dispatch Thread)
     * and block until the action has finished
     */
    public void uiActionWait(Runnable step) throws CancellationException {
        try {
            uiActionWait(Timeout.NONE, step);
        } catch (TimeoutException e) {
            // not possible
        }
    }

    /**
     * Execute the given method in the UI thread (i.e. Swing's Event Dispatch Thread)
     * and block until the action has finished
     */
    public void uiActionWait(Timeout timeout, Runnable step) throws CancellationException, TimeoutException {
        uiActionWait(timeout, () -> {step.run(); return true;});
    }

    /**
     * Execute the given method in the UI thread (i.e. Swing's Event Dispatch Thread)
     * and block until the action has finished
     */
    public <T> T uiActionWait(Supplier<T> step) throws CancellationException
    {
        try {
            return uiActionWait(Timeout.NONE, step);
        } catch (TimeoutException e) {
            // not possible
            return null;
        }
    }

    /**
     * Execute the given method, which produces a return value, in the UI thread (i.e. Swing's Event Dispatch Thread)
     * and block until the action has finished. Returns the value produced by step
     */
    public <T> T uiActionWait(Timeout timeout, Supplier<T> step) throws CancellationException, TimeoutException {

        if (cancellationRequested)
            throw new CancellationException();

        AutoResetEvent evt = new AutoResetEvent(false);

        try {
            AtomicReference<T> rval = new AtomicReference<>();
            SwingUtilities.invokeLater( () -> {rval.set(step.get()); evt.set(); } );

            currentEvent.set(evt);

            if (timeout == Timeout.NONE)
                timeout = DEFAULT_TIMEOUT;

            if (!evt.waitOne(timeout.getMilliseconds())) {
                throw new TimeoutException();
            }

            if (cancellationRequested) {
                throw new CancellationException();
            }

            return rval.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("UI action failed: " + e.getMessage(), e);
        }
    }

    /**
     * Schedules the given step in the EDT and returns immediately
     * @param step
     * @throws CancellationException
     */
    public void uiActionAsync(Runnable step) throws CancellationException {
        if (cancellationRequested)
            throw new CancellationException();

        SwingUtilities.invokeLater( step );
    }

    /**
     * Wait until the given condition is satisfied
     * @param timeout
     * @param condition The condition check. It is not executed in the EDT!
     * @throws TimeoutException
     */
    public void waitForCondition(Timeout timeout, Supplier<Boolean> condition) throws TimeoutException {
        log("WAIT FOR CONDITION");

        long t = System.currentTimeMillis();

        // Todo: refactor tight polling
        while (!condition.get()) {
            if (cancellationRequested) {
                throw new CancellationException();
            }

            if (timeout != Timeout.NONE) {
                if (System.currentTimeMillis() - t >= timeout.getMilliseconds()) {
                    throw new TimeoutException();
                }
            }

            wait(Timeout.milliseconds(500));
        }
    }

    /**
     * Execute the given action which should open a window of the given class
     *
     * @param timeout
     * @param winOpenAction Action, which should open a window of the given class
     * @param windowClass The class of the window
     * @throws TimeoutException
     */
    public <T extends java.awt.Window> T expectWindow(Timeout timeout, Runnable winOpenAction, Class<T> windowClass) throws TimeoutException {
        uiActionAsync(winOpenAction);
        return waitForWindow(timeout, windowClass);
    }

    /**
     * Blocks until at least one window of the given class is opened
     * and returns an open instance
     */
    public <T extends java.awt.Window> T waitForWindow(Timeout timeout, Class<T> windowClass) throws TimeoutException {
        log("WAIT FOR WINDOW " + windowClass.toString());
        return waitForWindow_internal(timeout, (w) -> windowClass.equals(w.getClass()));
    }

    /**
     * Wait until the given window has been closed
     */
    public void waitUntilWindowIsClosed(Timeout timeout, Window window) throws TimeoutException {
        long t = System.currentTimeMillis();

        while(window.isShowing()) {
            if (timeout != Timeout.NONE) {
                if (System.currentTimeMillis() - t >= timeout.getMilliseconds()) {
                    throw new TimeoutException();
                }
            }

            wait(Timeout.milliseconds(500));
        }

        wait(Timeout.milliseconds(900));
    }

    /**
     * Wait for a window which fulfills the given condition
     */
    public <T extends java.awt.Window> T waitForWindow(Timeout timeout, Predicate<Window> condition) throws TimeoutException {
        log("WAIT FOR WINDOW BY CONDITION");
        return waitForWindow_internal(timeout, condition);
    }

    /**
     * Wait for a window which fulfills the given condition
     */
    public <T extends java.awt.Window> T waitForWindow_internal(Timeout timeout, Predicate<Window> condition) throws TimeoutException {
        if (timeout == Timeout.NONE)
            timeout = DEFAULT_TIMEOUT;

        long t = System.currentTimeMillis();
        // Todo: refactor tight polling
        while (true) {
            if (cancellationRequested) {
                throw new CancellationException();
            }

            if (System.currentTimeMillis() - t >= timeout.getMilliseconds()) {
                throw new TimeoutException();
            }

            Window[] windows = Window.getWindows();
            Window w = QuIterables.query(windows).firstOrDefault(x -> x.isVisible() && condition.test(x));

            if (w != null) {

                while (!w.isShowing()) {
                    wait(Timeout.seconds(2));
                }

                return (T) w;
            }

            wait(Timeout.milliseconds(500));
        }
    }

    /**
     * Waits for an JOptionPane based Dialog and closes it using the given result
     */
    public void waitForAndDimissOptionPane(Timeout timeout, Object result) throws TimeoutException {
        log("WAIT FOR OPTIONPANE");
        Window dialog = waitForWindow(timeout, (w) ->
                (w instanceof JDialog) && ((JDialog) w).getContentPane().getComponentCount() > 0 && ((JDialog) w).getContentPane().getComponent(0) instanceof JOptionPane);

        JOptionPane p = (JOptionPane) ((JDialog) dialog).getContentPane().getComponent(0);
        uiActionWait(() -> {
            if (p.getWantsInput()) {
                p.setInputValue(result);
            } else {
                p.setValue(result);
            }
            dialog.setVisible(false);
        });
    }

    /**
     * Wait for the given amount of time
     */
    public void wait(Timeout timeout) {
        if (timeout.getMilliseconds() >= 1000) {
            // do not log when waiting during polling
            log("WAIT FOR " + (timeout.getMilliseconds() / 1000.0) + "s");
        }

        AutoResetEvent evt = new AutoResetEvent(false);
        try {
            currentEvent.set(evt);

            evt.waitOne(timeout.getMilliseconds());
        } catch (InterruptedException e) {
            throw new AutomationException(e.getMessage(), e);
        }
    }

    /**
     * Return the first component of the given class which is a child of parent or contained in
     * any of parent's children
     * @param parent The container to start searching for the class
     * @param componentClass The class of the component to find
     * @param <T> The class of the component to find
     * @return Component instance if found or null otherwise
     */
    public static <T extends Component> T findComponent(Container parent, Class<T> componentClass) {
        return findComponent(parent, componentClass, (c) -> true);
    }

    /**
     * Return the first component of the given class which is a child of parent or contained in
     * any of parent's children and which fulfills the given condition
     * @param parent The container to start searching for the class
     * @param componentClass The class of the component to find
     * @param <T> The class of the component to find
     * @return Component instance if found or null otherwise
     */
    public static <T extends Component> T findComponent(Container parent, Class<T> componentClass, Predicate<T> condition) {
        for(int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            if (componentClass.isInstance(c) && condition.test((T)c)) {
                return (T)c;
            }
            if (c instanceof  Container) {
                T cc = findComponent((Container)c, componentClass, condition);
                if (cc != null) {
                    return cc;
                }
            }
        }

        return null;
    }

}
