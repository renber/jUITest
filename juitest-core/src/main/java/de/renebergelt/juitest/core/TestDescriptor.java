package de.renebergelt.juitest.core;

import de.renebergelt.juitest.core.utils.NullGuard;

/**
 * Represents an Automation test
 */
public class TestDescriptor {

    private String name;

    public String getName() {
        return name;
    }

    private String testClassName;

    Object[] namedParameters;

    public Object[] getParameters() {
        return namedParameters;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public TestDescriptor(String name, String testClassName, Object...namedParameters) {

        if (namedParameters.length % 2 != 0) {
            throw new IllegalArgumentException("namedParameters must be in the format: paramName1, paramValue1, paramName2, paramValue2, ...");
        }

        this.name = NullGuard.forArgument("name", name);
        this.testClassName = NullGuard.forArgument("testClassName", testClassName);
        this.namedParameters = namedParameters;
    }

    public TestDescriptor(String name, Class<?> testClass, Object...namedParameters) {
        this(name, NullGuard.forArgument("testClass", testClass).getName(), namedParameters);
    }

}
