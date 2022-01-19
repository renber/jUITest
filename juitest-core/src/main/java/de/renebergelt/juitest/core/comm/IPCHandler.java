package de.renebergelt.juitest.core.comm;

import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import de.renebergelt.juitest.core.services.IPCTransmitter;

/**
 * Interface for classed which need access to IPC
 */
public interface IPCHandler {

    /**
     * Called after a connection has been established.
     * Announces the interface over which messages can be sent
     * @param transmitter The transmitter which can be used to send messages
     */
    void registerTransmitter(IPCTransmitter transmitter);

    /**
     * Called when a message has been received
     * @param message The message which has been received
     * @return the response message to send, if any
     */
    IPCProtocol.IPCMessage handleMessage(IPCProtocol.IPCMessage message);

}
