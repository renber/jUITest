package de.renebergelt.juitest.core.comm;

import de.renebergelt.juitest.core.comm.messages.IPCProtocol;

/**
 * Interface for classes which consume received IPC messages
 */
public interface IPCMessageListener {

    /**
     * Called when a message has been received
     * @param message The message which has been received
     * @return True, if the message has already been handled
     */
    boolean onMessageReceived(IPCProtocol.IPCMessage message);

    /**
     * Called when the established connection is closed due to a protocol error
     * or after the remote-host terminated
     * @param cause The underlying cause, if any
     */
    void connectionTerminated(Throwable cause);

}
