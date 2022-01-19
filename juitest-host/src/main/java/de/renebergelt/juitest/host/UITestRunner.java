package de.renebergelt.juitest.host;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.UITestClass;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.core.comm.IPCHandler;
import de.renebergelt.juitest.core.comm.IPCMessages;
import de.renebergelt.juitest.core.utils.StackTraceUtils;
import de.renebergelt.juitest.host.comm.IPCServer;
import de.renebergelt.juitest.host.services.SameProcessTestRunnerService;
import de.renebergelt.juitest.host.testscripts.UIAutomationHost;
import de.renebergelt.juitest.core.services.IPCTransmitter;
import de.renebergelt.juitest.host.testscripts.UIAutomationTest;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

/**
 * Class which manages running tests in the test host's process
 */
public class UITestRunner implements IPCHandler {

    SameProcessTestRunnerService localTestRunner;

    private String host;
    private int port;

    /**
     * Create a new instance of UITestRunner
     * @param host The server's host
     * @param port The server's port
     * @param automationHost The automation host
     * @param testBasePackage The base package name to search for automation test classes and methods
     */
    public UITestRunner(String host, int port, UIAutomationHost automationHost, String testBasePackage) {
        this.host = host;
        this.port = port;

        this.localTestRunner = new SameProcessTestRunnerService(automationHost, testBasePackage);
    }

    /**
     * Start the underlying server and wait for incoming connections from a test monitor
     */
    public void start() {
        // run server in separate thread, so that it does not conflict with the application under test
        Thread t = new Thread( () -> {
            try {
                IPCServer server = new IPCServer(host, port, this);
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    /**
     * Return the list of available tests
     * @return List of tests
     */
    public List<TestDescriptor> discoverTests() {
        return localTestRunner.discoverTests();
    }

    @Override
    public void registerTransmitter(IPCTransmitter transmitter) {
        localTestRunner.setTransmitter(transmitter);
    }

    @Override
    public IPCProtocol.IPCMessage handleMessage(IPCProtocol.IPCMessage message) {
        if (message.hasAttach()) {
            try {
                if (!localTestRunner.isAttached()) {
                    localTestRunner.attach(message.getAttach().getProgramArgumentsList().toArray(new String[0]));
                }

                return IPCMessages.createSimpleResponseMessage(IPCProtocol.ResponseStatus.OK);
            } catch (Exception e) {
                return IPCMessages.createSimpleResponseMessage(IPCProtocol.ResponseStatus.FAILED);
            }
        }

        if (message.hasGetTests()) {
            try {
                return IPCMessages.createTestListMessage(discoverTests());
            } catch (Exception e) {
                return IPCMessages.createSimpleResponseMessage(IPCProtocol.ResponseStatus.FAILED);
            }
        }

        if (message.hasRunTest()) {
            String testId = message.getRunTest().getTestId();
            try {
                localTestRunner.runTest(IPCMessages.readRunTestMessage(message.getRunTest()));
                // runTest returns immediately, test is running in the background
                return IPCMessages.createTestStartedMessage(testId);
            } catch (Exception e) {
                return IPCMessages.createTestFailedToStartMessage(testId, StackTraceUtils.stackTraceToString(e));
            }
        }

        if (message.hasCancelTest()) {
            localTestRunner.cancelRunningTest();
            return null;
        }

        if (message.hasResumeTest()) {
            localTestRunner.resumeTest();
            return null;
        }

        return null;
    }
}
