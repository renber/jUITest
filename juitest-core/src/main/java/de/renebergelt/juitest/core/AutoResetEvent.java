package de.renebergelt.juitest.core;

import java.util.concurrent.CancellationException;

/**
 * A thread synchronization object which automatically
 * resets itself (basically a Semaphore with 1 slot)
 */
public class AutoResetEvent {
    private final Object _monitor = new Object();
    private volatile boolean _isOpen = false;

    private boolean _cancelled = false;

    /**
     * Create an instance of AutoResetEvent
     *
     * @param open True, if the lock can be aquired
     */
    public AutoResetEvent(boolean open) {
        this._isOpen = open;
    }

    /**
     * Wait until the lock can be aquired
     *
     * @throws InterruptedException  Thrown if the waiting thread was interrupted
     * @throws CancellationException Thrown, if cancel() was called while waiting
     */
    public void waitOne() throws InterruptedException, CancellationException {
        synchronized (this._monitor) {
            while (!this._isOpen) {

                if (this._cancelled) {
                    throw new CancellationException();
                }

                this._monitor.wait();
            }

            this._isOpen = false;
        }
    }

    /**
     * Wait until the lock can be aquired but not longer than timeout
     *
     * @param timeout The maximum wait time in milliseconds
     * @return True, if the lock was aquired otherwise false (i.e. timeout occured)
     * @throws InterruptedException  Thrown if the waiting thread was interrupted
     * @throws CancellationException Thrown, if cancel() was called while waiting
     */
    public boolean waitOne(long timeout) throws InterruptedException, CancellationException {
        synchronized (this._monitor) {
            long t = System.currentTimeMillis();

            while (!this._isOpen) {
                this._monitor.wait(timeout);

                if (this._cancelled) {
                    throw new CancellationException();
                }

                if (System.currentTimeMillis() - t >= timeout) {
                    return false;
                }
            }

            this._isOpen = false;
        }

        return true;
    }

    /**
     * Aquire this lock
     */
    public void set() {
        synchronized (this._monitor) {
            this._isOpen = true;
            this._monitor.notifyAll();
        }
    }

    /**
     * Cancel all threads which are currently waiting for this lock
     */
    public void cancel() {
        synchronized (this._monitor) {
            this._cancelled = true;
            this._monitor.notifyAll();
        }
    }

    /**
     * Reset the lock
     */
    public void reset() {
        this._isOpen = false;
        this._cancelled = false;
    }
}
