package de.renebergelt.juitest.core.comm;

import de.renebergelt.juitest.core.Timeout;
import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Helper class for constructing IPC messages
 */
public class IPCMessages {

    private IPCMessages() {
        // --
    }

    /**
     * Create a new AttachMessage
     * @param programArguments The arguments to pass to the application-under-test
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createAttachMessage(String...programArguments) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        builder.setAttach(IPCProtocol.AttachMessage.newBuilder().addAllProgramArguments(Arrays.asList(programArguments)).build());
        return builder.build();
    }

    /**
     * Create a new SimpleResponseMessage which indicates success or failure of a prior request
     * @param status The status of the original request
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createSimpleResponseMessage(IPCProtocol.ResponseStatus status) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        builder.setSimpleResponse(IPCProtocol.SimpleResponseMessage.newBuilder().setStatus(status).build());
        return builder.build();
    }

    /**
     * Create a new GetTestsMessage to request a list of available tests
     * from a test host
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createGetTestsMessage() {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        return builder.setGetTests(IPCProtocol.GetTestsMessage.newBuilder().build()).build();
    }

    /**
     * Create a new TestListMessage which lists available tests
     * @param tests The list of tests to include in the message
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createTestListMessage(List<TestDescriptor> tests) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        IPCProtocol.TestListMessage.Builder mBuilder = IPCProtocol.TestListMessage.newBuilder();
        for (TestDescriptor td: tests) {
            IPCProtocol.TestDescriptor.Builder tBuilder = IPCProtocol.TestDescriptor.newBuilder();
            tBuilder.setTestClassName(td.getTestClassName());
            tBuilder.setTestMethodName(td.getTestMethodName());
            tBuilder.setTestSetName(td.getTestSetName());
            tBuilder.setDescription(td.getDescription());

            for(int i = 0; i < td.getParameters().length; i+=2) {
                tBuilder.addParameters(convertParameter(String.valueOf(td.getParameters()[i]), td.getParameters()[i+1]));
            }

            mBuilder.addTests(tBuilder.build());
        }

        builder.setTestList(mBuilder.build());
        return builder.build();
    }

    /**
     * Create a new RunTestMessage which instructs teh test host to run a certain test
     * @param testClassName The full-qualified name of the class which contains the test method
     * @param testMethodName The name of the test method
     * @param parameters The parameters of the test as list of tuples (parameter name, value)
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createRunTestMessage(String testClassName, String testMethodName, Object...parameters) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        // build test parameters
        List<IPCProtocol.TestParameter> params = new ArrayList<>();
        for(int pidx = 0; pidx < parameters.length; pidx += 2) {
            IPCProtocol.TestParameter.Builder pBuilder = IPCProtocol.TestParameter.newBuilder();
            pBuilder.setName(String.valueOf(parameters[pidx]));

            Object pvalue = parameters[pidx + 1];

            if (pvalue instanceof String) {
                pBuilder.setStrValue(String.valueOf(pvalue));
            } else if (pvalue instanceof Integer) {
                pBuilder.setIntValue((Integer) pvalue);
            } else if (pvalue instanceof Long) {
                pBuilder.setLongValue((Long) pvalue);
            } else if (pvalue instanceof Boolean) {
                pBuilder.setBoolValue((Boolean)pvalue);
            } else if (pvalue instanceof Float) {
                pBuilder.setFloatValue((Float) pvalue);
            }  else if (pvalue instanceof Timeout) {
                // timeout values are transferred as milliseconds
                pBuilder.setLongValue(((Timeout) pvalue).getMilliseconds());
            } else {
                throw new IllegalArgumentException("Unsupported parameter type: " + pvalue.getClass());
            }

            params.add(pBuilder.build());
        }

        builder.setRunTest(IPCProtocol.RunTestMessage.newBuilder()
                .setTestClassName(testClassName)
                .setTestMethodName(testMethodName)
                .addAllParameters(params).build());
        return builder.build();
    }

    /**
     * Converts the given name and value to a TestParameter
     * @param name The name of the parameter
     * @param value The value of the parameter
     * @return The TestParameter instance
     */
    private static IPCProtocol.TestParameter convertParameter(String name, Object value) {
        IPCProtocol.TestParameter.Builder builder = IPCProtocol.TestParameter.newBuilder();
        builder.setName(name);

        if (value instanceof Integer) {
            builder.setIntValue((int)value);
        } else if (value instanceof Float) {
            builder.setFloatValue((float)value);
        } else if (value instanceof Long) {
            builder.setLongValue((long)value);
        } else if (value instanceof Boolean) {
            builder.setBoolValue((boolean)value);
        } else {
            builder.setStrValue(String.valueOf(value));
        }

        return builder.build();
    }

    /**
     * Extracts the tests from a TestListMessage
     * @param message The message to parse
     * @return List of tests contained in the message
     */
    public static List<TestDescriptor> readTestListMessage(IPCProtocol.TestListMessage message) {

        List<TestDescriptor> rList = new ArrayList<>();

        for (IPCProtocol.TestDescriptor ptd : message.getTestsList()) {

            List<Object> parameters = new ArrayList<>();

            for (IPCProtocol.TestParameter param : ptd.getParametersList()) {
                parameters.add(param.getName());
                switch (param.getValueCase()) {
                    case STR_VALUE:
                        parameters.add(param.getStrValue());
                        break;
                    case INT_VALUE:
                        parameters.add(param.getIntValue());
                        break;
                    case LONG_VALUE:
                        parameters.add(param.getLongValue());
                        break;
                    case FLOAT_VALUE:
                        parameters.add(param.getFloatValue());
                        break;
                    case BOOL_VALUE:
                        parameters.add(param.getBoolValue());
                        break;
                    case VALUE_NOT_SET:
                        parameters.add(null);
                        break;
                }
            }

            TestDescriptor td = new TestDescriptor(ptd.getTestClassName(), ptd.getTestMethodName(), parameters.toArray());
            td.setTestSetName(ptd.getTestSetName());
            td.setDescription(ptd.getDescription());
            rList.add(td);
        }

        return rList;
    }

    /**
     * Extracts the TestDescriptor for the test to run from a RunTestMessage
     * @param message The message to parse
     * @return TestDescriptor containing the test information from the message
     */
    public static TestDescriptor readRunTestMessage(IPCProtocol.RunTestMessage message) {

        List<Object> parameters = new ArrayList<>();
        for(IPCProtocol.TestParameter param: message.getParametersList()) {
            parameters.add(param.getName());
            switch (param.getValueCase()) {
                case STR_VALUE:
                    parameters.add(param.getStrValue());
                    break;
                case INT_VALUE:
                    parameters.add(param.getIntValue());
                    break;
                case LONG_VALUE:
                    parameters.add(param.getLongValue());
                    break;
                case FLOAT_VALUE:
                    parameters.add(param.getFloatValue());
                    break;
                case BOOL_VALUE:
                    parameters.add(param.getBoolValue());
                    break;
                case VALUE_NOT_SET:
                    parameters.add(null);
                    break;
            }
        }

        return new TestDescriptor(message.getTestClassName(), message.getTestMethodName(), parameters.toArray());
    }

    /**
     * Create a TestResult message which indicates the result of a run test
     * @param result The test result
     * @param errorText The error text, if the test failed
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createTestResultMessage(IPCProtocol.TestResult result, Optional<String> errorText) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.TestResultMessage.Builder resultBuilder = IPCProtocol.TestResultMessage.newBuilder();
        resultBuilder.setResult(result);
        if (errorText.isPresent()) {
            resultBuilder.setErrorDescription(errorText.get());
        }

        return builder.setTestResult(resultBuilder).build();
    }

    /**
     * Create a CancelTest message which instructs the test host to cancel the currently running test
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createCancelTestMessage() {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        builder.setCancelTest(IPCProtocol.CancelTestMessage.newBuilder().build());
        return builder.build();
    }

    /**
     * Create a TestLogMessage which contains a log message of a test in execution
     * @param testId Id of the test which output the log message
     * @param logMessage The log message
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createTestLogMessage(String testId, String logMessage) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.TestLogMessage.Builder logBuilder = IPCProtocol.TestLogMessage.newBuilder();
        logBuilder.setTestId(testId);
        logBuilder.setText(logMessage);

        return builder.setTestLog(logBuilder).build();
    }

    /**
     * Create a TestStatusMessage which indicates the current status of a test
     * @param testId Id of the test
     * @param message A message describing the current status
     * @param status Status of the test
     * @return Instance of the message
     */
    private static IPCProtocol.IPCMessage createTestStatusMessage(String testId, String message, IPCProtocol.TestStatus status) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.TestStatusMessage.Builder statusBuilder = IPCProtocol.TestStatusMessage.newBuilder();
        statusBuilder.setTestId(testId);
        statusBuilder.setStatus(status);
        statusBuilder.setMessage(message);

        return builder.setTestStatus(statusBuilder).build();
    }

    /**
     * Create a TestStartedMessage which indicates that a test has been started execution
     * @param testId Id of the test which has been started
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createTestStartedMessage(String testId) {
        return createTestStatusMessage(testId, "", IPCProtocol.TestStatus.RUNNING);
    }

    /**
     * Create a TestFailedToStartMessage which indicates that a test could not be run
     * @param testId Id of the test which has been started
     * @param message Message which describes the cause for the failure
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createTestFailedToStartMessage(String testId, String message) {
        return createTestStatusMessage(testId, message, IPCProtocol.TestStatus.FAILED_TO_START);
    }

    /**
     * Create a TestPausedMessage which indicates that a test was paused
     * @param testId Id of the test which was paused
     * @param message A message to display to the user
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createTestPausedMessage(String testId, String message) {
        return createTestStatusMessage(testId, message, IPCProtocol.TestStatus.PAUSED);
    }

    /**
     * Create a ResumeTestMessage which instructs the test host to resume a paused test
     * @param testId Id of the test to resume
     * @return Instance of the message
     */
    public static IPCProtocol.IPCMessage createResumeTestMessage(String testId) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.ResumeTestMessage.Builder resumeBuilder = IPCProtocol.ResumeTestMessage.newBuilder();
        resumeBuilder.setTestId(testId);

        return builder.setResumeTest(resumeBuilder).build();
    }
}
