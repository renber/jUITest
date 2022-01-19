package de.renebergelt.juitest.core.services;

import de.renebergelt.juitest.core.comm.messages.IPCProtocol;

/**
 * Interface for writing to a connection
 */
public interface IPCTransmitter {

    /**
     * Send the given message to the other connection endpoint
     * @param message The message to send
     */
    void sendMessage(IPCProtocol.IPCMessage message);

}
