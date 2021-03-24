package de.renebergelt.juitest.core;

import de.renebergelt.juitest.core.utils.NullGuard;

/**
 * Represents an Automation test
 */
public class TestDescriptor {

    private String description;

    public String getDescription() {
        if (description == null || description.isEmpty()) {
            // auto-generate name from method name and parameters
            StringBuilder sb = new StringBuilder();
            sb.append(testMethodName);

            if (getParameters().length > 0) {
                sb.append(" (");
                for(int i = 0; i < getParameters().length; i+=2) {
                    sb.append(getParameters()[i]).append(": ").append(getParameters()[i+1]);
                    if (i < getParameters().length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")");
            }

            description = sb.toString();
        }

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String testMethodName;

    public String getTestMethodName() {
        return testMethodName;
    }

    private String testClassName;

    Object[] namedParameters;

    public Object[] getParameters() {
        return namedParameters;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public TestDescriptor(String testClassName, String testMethodName, Object...namedParameters) {

        if (namedParameters.length % 2 != 0) {
            throw new IllegalArgumentException("namedParameters must be in the format: paramName1, paramValue1, paramName2, paramValue2, ...");
        }

        this.testMethodName = NullGuard.forArgument("testMethodName", testMethodName);
        this.testClassName = NullGuard.forArgument("testClassName", testClassName);
        this.namedParameters = namedParameters;
    }

    public TestDescriptor(String name, Class<?> testClass, Object...namedParameters) {
        this(name, NullGuard.forArgument("testClass", testClass).getName(), namedParameters);
    }

}
