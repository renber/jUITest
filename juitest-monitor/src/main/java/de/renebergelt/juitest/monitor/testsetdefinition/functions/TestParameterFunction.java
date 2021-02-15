package de.renebergelt.juitest.monitor.testsetdefinition.functions;

import java.util.List;

/**
 * TestSet definition functions can be used in the
 * test-cases.yaml file. The are written in double
 * curly braces, have a name and parameters
 */
public interface TestParameterFunction {

    String getName();

    List<Object> eval(String parameters);

}
