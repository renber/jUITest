package de.renebergelt.juitest.monitor.testsetdefinition.functions;

import java.util.*;

public class ParameterFunctionResolver {

    HashMap<String, TestParameterFunction> functions = new HashMap<>();

    public ParameterFunctionResolver() {

        for(TestParameterFunction f: getAvailableFunctions()) {
            functions.put(f.getName(), f);
        }
    }

    private List<TestParameterFunction> getAvailableFunctions() {
        List<TestParameterFunction> availableFunctions = new ArrayList<>();

        availableFunctions.add(new SetParameterFunction());
        availableFunctions.add(new FilesInDirParameterFunction());

        return availableFunctions;
    }

    public List<Object[]> resolveParameterSets(Object[] template) {

        // loop through parameter values
        List<List<Object>> pvalues = new ArrayList<>();

        for (int i = 1; i < template.length; i += 2) {
            // right now we support only value or a single function
            // for a parameter
            String s = template[i].toString();
            if (s.startsWith("{{") && s.endsWith("}}")) {
                // this is a function
                pvalues.add(resolveFunction(s.substring(2, s.length() - 2)));
            } else {
                // we need lists for the permutations
                if ((template[i] instanceof List)) {
                    pvalues.add((List)template[i]);
                } else {
                    pvalues.add(Arrays.asList(template[i]));
                }
            }
        }

        int noParams = template.length / 2;
        int[] currIndices = new int[noParams];
        int[] maxIndices = new int[noParams];
        for(int i = 0; i < noParams; i++) {
            maxIndices[i] = pvalues.get(i).size();
        }

        List<Object[]> psets = new ArrayList<>();

        do {
            Object[] set = new Object[template.length];

            // copy parameter names
            for(int i = 0; i <template.length; i+=2) {
                set[i] = template[i];
            }

            // select values for this permutation
            for(int i = 0; i < noParams; i++) {
                set[i*2 + 1] = pvalues.get(i).get(currIndices[i]);
            }

            psets.add(set);

        } while (incPerm(currIndices, maxIndices));

        return psets;
    }

    /**
     * Increases the permutation step
     * Returns false if end of permutations has been reached
     */
    boolean incPerm(int[] currIndices, int[] maxIndices) {
        // add 1 to the last counter, if it overflows
        // reset it and increment the counter before and so on
        for(int i = currIndices.length - 1; i >= 0; i--) {
            currIndices[i] = currIndices[i] + 1;

            if (currIndices[i] >= maxIndices[i]) {
                // roll over and get up one level
                currIndices[i] = 0;

                if (i == 0) return false;
            } else {
                return true;
            }
        }

        return false;
    }

    private List<Object> resolveFunction(String funcText) {
        String[] p = funcText.split(":", 2);
        String funcName = p[0];
        String params = p.length > 1 ? p[1] : "";

        TestParameterFunction func = functions.get(funcName);
        if (func == null)
            throw new IllegalArgumentException("Unknown parameter function: " + funcName);

        return func.eval(params);
    }

}
