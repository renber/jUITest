package de.renebergelt.juitest.core;

import de.renebergelt.juitest.core.utils.NullGuard;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups a number of UIAutomationTest objects
 */
public class TestSet {

    private String name;

    /**
     * Returns the name of this set
     * @return The name of this set
     */
    public String getName() {
        return name;
    }

    private List<TestDescriptor> tests = new ArrayList<TestDescriptor>();

    /**
     * Returns all tests which are part if this test set
     * @return List of tests
     */
    public List<TestDescriptor> getTests() {
        return tests;
    }

    /**
     * Create a new test set
     * @param name Nam eof the test set
     */
    public TestSet(String name) {
        this.name = NullGuard.forArgument("name", name);
    }

}
