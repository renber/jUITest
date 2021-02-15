package de.renebergelt.juitest.monitor.testsetdefinition;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.TestSet;
import de.renebergelt.juitest.monitor.testsetdefinition.functions.ParameterFunctionResolver;
import de.renebergelt.juitest.monitor.testsetdefinition.functions.TestNameResolver;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.beans.IntrospectionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Reads script set definitions from a yaml file
 */
public class TestSetDefinitionReader {

    TestCasesDefaults defaults = new TestCasesDefaults();
    TestNameResolver nameResolver = new TestNameResolver();
    ParameterFunctionResolver paramFunctionResolver = new ParameterFunctionResolver();

    public List<TestSet> readFromFile(String filename) {
        try (FileInputStream stream = new FileInputStream(filename)) {
            return readFromStream(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TestSet> readFromStream(InputStream stream) {
        Yaml yaml = new Yaml();
        Map doc = (Map)yaml.load(stream);

        ArrayList<TestSet> sets = new ArrayList<>();

        Object nodeDefaults = doc.get("defaults");
        if (nodeDefaults instanceof Map) {
            readDefaults((Map)nodeDefaults);
        }

        Object nodeSets = doc.get("test-sets");
        if (nodeSets instanceof List) {
            for(Object nodeSet: (List)nodeSets) {
                TestSet ss = readScriptSet(nodeSet);

                if (ss != null) {
                    sets.add(ss);
                }
            }
        }

        return sets;
    }

    private void readDefaults(Map map) {
        defaults = new TestCasesDefaults();
        defaults.defaultPackage = String.valueOf(map.getOrDefault("default-package", ""));
    }

    private TestSet readScriptSet(Object node) {
        if (!(node instanceof  Map))
           return null;

        TestSet set = null;

        Map map = (Map)node;
        for(Object entry: map.entrySet()) {
            String name = ((Map.Entry)entry).getKey().toString();
            set = new TestSet(name);

            set.getTests().addAll(readTestDefinitions(((Map.Entry) entry).getValue()));

        }

        return set;
    }

    private List<TestDescriptor> readTestDefinitions(Object node) {
        if (!(node instanceof  List))
            return Collections.emptyList();

        List<TestDescriptor> tests = new ArrayList<>();

        for(Object nodeTest: (List)node) {
            tests.addAll(readTestClass(nodeTest));
        }

        return tests;
    }

    private List<TestDescriptor> readTestClass(Object node) {
        if (!(node instanceof  Map))
            return null;

        List<TestDescriptor> tests = new ArrayList<>();

        Map map = (Map)node;
        for(Object entry: map.entrySet()) {
            String testClass = ((Map.Entry) entry).getKey().toString();

            if (testClass.startsWith(".")) {
                // test class name is relative to teh default package
                testClass = defaults.defaultPackage + testClass;
            }

            List parameterList = (List) ((Map.Entry) entry).getValue();
            for (Object nodeParameter : parameterList) {
                tests.addAll(readTestClassParameters(testClass, nodeParameter));
            }
        }

        return tests;
    }

    private List<TestDescriptor> readTestClassParameters(String testClass, Object node) {
        if (node instanceof String) {
            // there is only one element
            return Arrays.asList(new TestDescriptor(node.toString(), testClass));
        }

        if (!(node instanceof  Map))
            return Collections.emptyList();

        List<TestDescriptor> rList = new ArrayList<>();

        Map map = (Map)node;
        for(Object entry: map.entrySet()) {
            String name = ((Map.Entry)entry).getKey().toString();

            List<Object> parameters = new ArrayList<>();

            // read parameters
            Map params = (Map)((Map.Entry) entry).getValue();
            for(Object param: params.entrySet()) {
                parameters.add(((Map.Entry)param).getKey());
                parameters.add(((Map.Entry)param).getValue());
            }

            // resolve functions in script parameters
            // and build all permutations (=parameter sets)
            for(Object[] pset: paramFunctionResolver.resolveParameterSets(parameters.toArray()))
            {
                // resolve parameters in script name
                String testname = nameResolver.resolve(name, pset);

                // create a script with this parameter set and name
                rList.add(new TestDescriptor(testname, testClass, pset));
            }
        }
        return rList;
    }

    static class TestCasesDefaults {
        String defaultPackage = "";
    }
}

class YamlRoot {

    public DefaultsNode defaults;

}

class DefaultsNode {
    public String defaultPackage;
}
