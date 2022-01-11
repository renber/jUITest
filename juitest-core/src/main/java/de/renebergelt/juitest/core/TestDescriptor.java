package de.renebergelt.juitest.core;

import de.renebergelt.juitest.core.utils.NullGuard;

/**
 * Represents an Automation test
 */
public class TestDescriptor {

    private String description;

    /**
     * Textual description if this test
     * @return The description
     */
    public String getDescription() {
        if (description == null || description.isEmpty()) {
            // auto-generate name from method name and parameters
            StringBuilder sb = new StringBuilder();
            sb.append(testMethodName);

            if (getParameters().length > 0) {
                sb.append(" (");
                for(int i = 0; i < getParameters().length; i+=2) {
                    sb.append(getParameters()[i]).append(": ").append(getParameters()[i+1]);
                    if (i < getParameters().length - 2) {
                        sb.append(", ");
                    }
                }
                sb.append(")");
            }

            description = sb.toString();
        }

        return description;
    }

    /**
     * Update the description of this test
     * @param description The new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    private String testMethodName;

    /**
     * Return the name of the test method
     * @return Method name
     */
    public String getTestMethodName() {
        return testMethodName;
    }

    private String testClassName;

    private String testSetName;

    /**
     * Return the name of the test set this test belongs to
     * @return Name of the test set
     */
    public String getTestSetName() {
        if (testSetName == null || testSetName.isEmpty()) {
            return testClassName;
        }
        return testSetName;
    }

    /**
     * Set the name of the test set this test belongs to
     * @param testSetName  Name of the test set
     */
    public void setTestSetName(String testSetName) {
        this.testSetName = testSetName;
    }

    Object[] namedParameters;

    /**
     * Returns the parameters of this test as tuples (Parameter name, Value), if any
     * @return Parameters
     */
    public Object[] getParameters() {
        return namedParameters;
    }

    /**
     * Return the class name which contains the test method
     * @return The class name
     */
    public String getTestClassName() {
        return testClassName;
    }

    /**
     * Create a new TestDescriptor
     * @param testClassName Name of the class which contains the test method
     * @param testMethodName Nam eof the test method
     * @param namedParameters Test parameters
     */
    public TestDescriptor(String testClassName, String testMethodName, Object...namedParameters) {

        if (namedParameters.length % 2 != 0) {
            throw new IllegalArgumentException("namedParameters must be in the format: paramName1, paramValue1, paramName2, paramValue2, ...");
        }

        this.testMethodName = NullGuard.forArgument("testMethodName", testMethodName);
        this.testClassName = NullGuard.forArgument("testClassName", testClassName);
        this.namedParameters = namedParameters;
    }

}
