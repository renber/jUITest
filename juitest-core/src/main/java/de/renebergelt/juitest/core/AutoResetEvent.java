package de.renebergelt.juitest.core;

import java.util.concurrent.CancellationException;

public class AutoResetEvent {
        private final Object _monitor = new Object();
        private volatile boolean _isOpen = false;

        private boolean _cancelled = false;

        public AutoResetEvent(boolean open) {
            this._isOpen = open;
        }

        public void waitOne() throws InterruptedException, CancellationException {
            Object var1 = this._monitor;
            synchronized(this._monitor) {
                while(!this._isOpen) {

                    if (this._cancelled) {
                        throw new CancellationException();
                    }

                    this._monitor.wait();
                }

                this._isOpen = false;
            }
        }

        public boolean waitOne(long timeout) throws InterruptedException, CancellationException {
            Object var3 = this._monitor;
            synchronized(this._monitor) {
                long t = System.currentTimeMillis();

                while(!this._isOpen) {
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

        public void set() {
            Object var1 = this._monitor;
            synchronized(this._monitor) {
                this._isOpen = true;
                this._monitor.notifyAll();
            }
        }

        public void cancel() {
            Object var1 = this._monitor;
            synchronized(this._monitor) {
                this._cancelled = true;
                this._monitor.notifyAll();
            }
        }

        public void reset() {
            this._isOpen = false;
            this._cancelled = false;
        }
}
