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

    public List<TestDescriptor> discoverTests() {
        if (!isAttached())
            throw new IllegalStateException("Test monitor not attached");

        IPCProtocol.IPCMessage response = client.send(IPCMessages.createGetTestsMessage());
        if (response.hasTestList()) {
            return IPCMessages.readTestListMessage(response.getTestList());
        } else
            throw new IllegalStateException("Unexpected response message");
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

        IPCProtocol.IPCMessage result = client.send(IPCMessages.createRunTestMessage(testDescriptor.getTestClassName(), testDescriptor.getTestMethodName(), testDescriptor.getParameters()));
        if (result.hasTestStatus()) {
            switch (result.getTestStatus().getStatus()) {
                case RUNNING:
                    return;
                case FAILED_TO_START:
                    throw new UITestException("Failed to run test: " + result.getTestStatus().getMessage());
            }
        } else {
            throw new RuntimeException("No response from host");
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
        // handle messages which are not send as response to a request

       if (message.hasTestLog()) {
            for(TestStatusListener listener: testStatusListeners) {
                listener.onLogMessageReceived(message.getTestLog().getText());
            }
            return true;
        }

        if (message.hasTestStatus()) {
            // only handle pause
            if (message.getTestStatus().getStatus() == IPCProtocol.TestStatus.PAUSED) {
                for(TestStatusListener listener: testStatusListeners) {
                        listener.onTestExecutionPaused(message.getTestStatus().getMessage());
                }
            }
            return false;
        }

        if (message.hasTestResult()) {
            String testId = message.getTestResult().getTestId();

            for(TestStatusListener listener: testStatusListeners) {
                switch (message.getTestResult().getResult()) {
                    case SUCCESS:
                        listener.onTestSucceeded(testId);
                        break;
                    case TIMEOUT:
                        listener.onTestTimedout(testId);
                        break;
                    case CANCELLED:
                        listener.onTestCancelledByUser(testId);
                        break;
                    case FAILURE:
                        listener.onTestFailed(testId, message.getTestResult().getErrorDescription());
                        break;
                }
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
