package de.renebergelt.juitest.host;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.UITestClass;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.core.comm.IPCHandler;
import de.renebergelt.juitest.core.comm.IPCMessages;
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

public class UITestRunner implements IPCHandler {

    SameProcessTestRunnerService localTestRunner;

    private String host;
    private int port;

    public UITestRunner(String host, int port, UIAutomationHost automationHost, String testBasePackage) {
        this.host = host;
        this.port = port;

        this.localTestRunner = new SameProcessTestRunnerService(automationHost, testBasePackage);
    }

    public void start() {
        // run server in separate thread, so that it does not conflict with the application under tests
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
            try {
                localTestRunner.runTest(IPCMessages.readRunTestMessage(message.getRunTest()));
                return IPCMessages.createTestResultMessage(IPCProtocol.TestResult.SUCCESS, Optional.empty());
            } catch (AssertionError e) {
                // JUnit Error is not an exception
                return IPCMessages.createTestResultMessage(IPCProtocol.TestResult.FAILURE, Optional.of(stackTraceToString(e)));
            } catch (TimeoutException e) {
                return IPCMessages.createTestResultMessage(IPCProtocol.TestResult.TIMEOUT, Optional.empty());
            } catch (CancellationException e) {
                return IPCMessages.createTestResultMessage(IPCProtocol.TestResult.CANCELED, Optional.empty());
            } catch (UITestException e) {
                return IPCMessages.createTestResultMessage(IPCProtocol.TestResult.FAILURE, Optional.of(stackTraceToString(e)));
            } catch (Throwable e) { // handle generic Exception or Error
                return IPCMessages.createTestResultMessage(IPCProtocol.TestResult.FAILURE, Optional.of(stackTraceToString(e)));
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

    private String stackTraceToString(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return  writer.toString();
    }
}
