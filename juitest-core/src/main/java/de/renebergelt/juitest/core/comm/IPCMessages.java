package de.renebergelt.juitest.core.comm;

import de.renebergelt.juitest.core.Timeout;
import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.comm.messages.IPCProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class IPCMessages {

    private IPCMessages() {

    }

    public static IPCProtocol.IPCMessage createAttachMessage(String...programArguments) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        builder.setAttach(IPCProtocol.AttachMessage.newBuilder().addAllProgramArguments(Arrays.asList(programArguments)).build());
        return builder.build();
    }

    public static IPCProtocol.IPCMessage createSimpleResponseMessage(IPCProtocol.ResponseStatus status) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        builder.setSimpleResponse(IPCProtocol.SimpleResponseMessage.newBuilder().setStatus(status).build());
        return builder.build();
    }

    public static IPCProtocol.IPCMessage createGetTestsMessage() {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        return builder.setGetTests(IPCProtocol.GetTestsMessage.newBuilder().build()).build();
    }

    public static IPCProtocol.IPCMessage createTestListMessage(List<TestDescriptor> tests) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        IPCProtocol.TestListMessage.Builder mBuilder = IPCProtocol.TestListMessage.newBuilder();
        for (TestDescriptor td: tests) {
            IPCProtocol.TestDescriptor.Builder tBuilder = IPCProtocol.TestDescriptor.newBuilder();
            tBuilder.setTestClassName(td.getTestClassName());
            tBuilder.setTestMethodName(td.getTestMethodName());
            tBuilder.setDescription(td.getDescription());

            for(int i = 0; i < td.getParameters().length; i+=2) {
                tBuilder.addParameters(convertParameter(String.valueOf(td.getParameters()[i]), td.getParameters()[i+1]));
            }

            mBuilder.addTests(tBuilder.build());
        }

        builder.setTestList(mBuilder.build());
        return builder.build();
    }

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
            td.setDescription(ptd.getDescription());
            rList.add(td);
        }

        return rList;
    }

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

    public static IPCProtocol.IPCMessage createTestResultMessage(IPCProtocol.TestResult result, Optional<String> errorText) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.TestResultMessage.Builder resultBuilder = IPCProtocol.TestResultMessage.newBuilder();
        resultBuilder.setResult(result);
        if (errorText.isPresent()) {
            resultBuilder.setErrorDescription(errorText.get());
        }

        return builder.setTestResult(resultBuilder).build();
    }

    public static IPCProtocol.IPCMessage createCancelTestMessage() {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();
        builder.setCancelTest(IPCProtocol.CancelTestMessage.newBuilder().build());
        return builder.build();
    }

    public static IPCProtocol.IPCMessage createTestLogMessage(String testId, String logMessage) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.TestLogMessage.Builder logBuilder = IPCProtocol.TestLogMessage.newBuilder();
        logBuilder.setTestId(testId);
        logBuilder.setText(logMessage);

        return builder.setTestLog(logBuilder).build();
    }

    public static IPCProtocol.IPCMessage createTestPausedMessage(String testId, String message) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.TestPausedMessage.Builder pausedBuilder = IPCProtocol.TestPausedMessage.newBuilder();
        pausedBuilder.setTestId(testId);
        pausedBuilder.setMessage(message);

        return builder.setTestPaused(pausedBuilder).build();
    }

    public static IPCProtocol.IPCMessage createResumeTestMessage(String testId) {
        IPCProtocol.IPCMessage.Builder builder = IPCProtocol.IPCMessage.newBuilder();

        IPCProtocol.ResumeTestMessage.Builder resumeBuilder = IPCProtocol.ResumeTestMessage.newBuilder();
        resumeBuilder.setTestId(testId);

        return builder.setResumeTest(resumeBuilder).build();
    }
}
