package de.renebergelt.juitest.core;

import de.renebergelt.juitest.core.utils.NullGuard;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups a number of UIAutomationTest objects
 */
public class TestSet {

    private String name;

    public String getName() {
        return name;
    }

    private List<TestDescriptor> tests = new ArrayList<TestDescriptor>();

    public List<TestDescriptor> getTests() {
        return tests;
    }

    public TestSet(String name) {
        this.name = NullGuard.forArgument("name", name);
    }

}
