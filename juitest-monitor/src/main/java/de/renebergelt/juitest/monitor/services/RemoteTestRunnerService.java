package de.renebergelt.juitest.monitor.services;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.comm.IPCMessageListener;
import de.renebergelt.juitest.core.comm.IPCMessages;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import de.renebergelt.juitest.monitor.comm.IPCClient;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.core.services.TestRunnerService;
import de.renebergelt.juitest.core.services.TestStatusListener;
import de.renebergelt.juitest.core.utils.NullGuard;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

public class RemoteTestRunnerService implements TestRunnerService, IPCMessageListener {

    private String host;
    private int port;
    IPCClient client;

    Set<TestStatusListener> testStatusListeners = new LinkedHashSet<>();

    public RemoteTestRunnerService(String host, int port) {
        this.host = NullGuard.forArgument("host", host);
        this.port = port;
    }

    @Override
    public boolean isAttached() {
        return client != null && client.isConnected();
    }

    @Override
    public void attach(String...programArguments) {
        if (!isAttached()) {
            try {
                client = new IPCClient();
                client.connect(host, port, this);

                IPCProtocol.IPCMessage response = client.send(IPCMessages.createAttachMessage(programArguments));
                if (response.hasSimpleResponse()) {
                    IPCProtocol.ResponseStatus status = response.getSimpleResponse().getStatus();
                    if (status != IPCProtocol.ResponseStatus.OK) {
                        throw new IllegalStateException("Could not attach. Attach response status: " + status.name());
                    }
                } else
                    throw new IllegalStateException("Unexpected response message");
            } catch (Exception e) {
                if (client != null) client.close();
                client = null;
                throw e;
            }
        }
    }

    @Override
    public void disattach() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                client = null;
            }
        }
    }

    @Override
    public void runTest(TestDescriptor testDescriptor) throws TimeoutException, CancellationException, UITestException {
        if (!isAttached())
            throw new IllegalStateException("Test monitor not attached");

        IPCProtocol.IPCMessage result = client.send(IPCMessages.createRunTestMessage(testDescriptor.getTestClassName(), testDescriptor.getParameters()));
        if (result.hasTestResult()) {
            switch (result.getTestResult().getResult()) {
                case SUCCESS:
                    return;
                case FAILURE:
                    throw new UITestException(result.getTestResult().getErrorDescription());
                case CANCELED:
                    throw new CancellationException(result.getTestResult().getErrorDescription());
                case TIMEOUT:
                    throw new TimeoutException(result.getTestResult().getErrorDescription());
                default:
                    // connection timed-out?
                    throw new TimeoutException("No response from test host");
            }
        }
    }

    @Override
    public void resumeTest() {
        client.sendAndForget(IPCMessages.createResumeTestMessage(""));
    }

    @Override
    public void cancelRunningTest() {

        if (isAttached()) {
            client.sendAndForget(IPCMessages.createCancelTestMessage());
        } else {

        }
    }

    public void addTestStatusListener(TestStatusListener testStatusListener) {
        testStatusListeners.add(testStatusListener);
    }

    public void removeTestStatusListener(TestStatusListener testStatusListener) {
        testStatusListeners.remove(testStatusListener);
    }

    @Override
    public boolean onMessageReceived(IPCProtocol.IPCMessage message) {
        if (message.hasTestLog()) {
            // remove log messages from the normal flow
            for(TestStatusListener listener: testStatusListeners) {
                listener.onLogMessageReceived(message.getTestLog().getText());
            }
            return true;
        }

        if (message.hasTestPaused()) {
            // remove test paused messages from the normal flow
            for(TestStatusListener listener: testStatusListeners) {
                listener.onTestExecutionPaused(message.getTestPaused().getMessage());
            }
            return true;
        }

        return false;
    }

    @Override
    public void connectionTerminated(Throwable cause) {
        for(TestStatusListener listener: testStatusListeners) {
            listener.connectionTerminated(cause);
        }
    }
}
