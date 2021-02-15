package de.renebergelt.juitest.core.comm;

import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import de.renebergelt.juitest.core.services.IPCTransmitter;

public interface IPCHandler {

    /**
     * Called after a connection has been established.
     * Announces teh inetrface over which messages can be sent
     */
    void registerTransmitter(IPCTransmitter transmitter);

    /**
     * Called when a message has been received
     */
    IPCProtocol.IPCMessage handleMessage(IPCProtocol.IPCMessage message);

}
