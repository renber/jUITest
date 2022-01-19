package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterContainer;
import de.renebergelt.juitest.core.annotations.TestParameterMarker;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for TestParameter resolving
 */
public class TestParameterResolver {

    /**
     * Retrieve the index of the given parameter annotation
     * @param a The TestParameter annotation
     * @return The index
     */
    protected Integer getParameterIndex(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("index");
            return (Integer)m.invoke(a);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the name of the given parameter annotation
     * @param a The TestParameter annotation
     * @return The name
     */
    protected String getParameterName(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("name");
            return String.valueOf(m.invoke(a));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the names of the test parameters of the given methid
     * @param uiTestMethod The method
     * @return List o ftest parameter names
     */
    public List<String> getDeclaredParameterNames(Method uiTestMethod) {
        List<Annotation> parameters = getParameters(uiTestMethod);
        parameters.sort(Comparator.comparing(this::getParameterIndex));
        return parameters.stream().map(x -> getParameterName(x)).collect(Collectors.toList());
    }

    /**
     * Return whether the given method has tets parameter annotations
     * @param uiTestMethod The method
     * @return True, if the method has test parameters
     */
    public boolean hasParameters(Method uiTestMethod) {
        return Arrays.stream(uiTestMethod.getAnnotations()).anyMatch(x -> x.annotationType().isAnnotationPresent(TestParameterContainer.class) || x.annotationType().isAnnotationPresent(TestParameterMarker.class));
    }

    /**
     * Extracts the annotations contained in the given container annotation
     * @param containerAnnotation The annotation to flatten
     * @return List of contained annotations
     */
    public List<Annotation> unrollParameterContainer(Annotation containerAnnotation) {
        try {
            return Arrays.asList((Annotation[])containerAnnotation.annotationType().getMethod("value").invoke(containerAnnotation));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve a list of all test parameter annotations present at the given method
     * @param uiTestMethod The method
     * @return List of tets parameter annotations
     */
    public List<Annotation> getParameters(Method uiTestMethod) {
        // get all annotations which are marked with TestParameterMarker
        List<Annotation> alist = new ArrayList<>();
        alist.addAll(Arrays.stream(uiTestMethod.getAnnotations()).filter(x -> x.annotationType().isAnnotationPresent(TestParameterMarker.class)).collect(Collectors.toList()));
        // and "unroll" all TestParameterContainer annotations
        alist.addAll(Arrays.stream(uiTestMethod.getAnnotations()).filter(x -> x.annotationType().isAnnotationPresent(TestParameterContainer.class)).map(x -> unrollParameterContainer(x)).flatMap(List::stream).collect(Collectors.toList()));
        return alist;
    }

    /**
     * Resolves the test parameters (including all permutations) of the given method
     * @param uiTestMethod The method
     * @return List of parameter configurations as (name, value) tuples
     */
    public List<Object[]> resolveParameterSets(Method uiTestMethod) {

        // get all annotations of the methods which have a TestParameter annotation
        List<Annotation> parameters = getParameters(uiTestMethod);
        parameters.sort(Comparator.comparing(this::getParameterIndex));

        List<String> pnames = parameters.stream().map(x -> getParameterName(x)).collect(Collectors.toList());
        List<List<Object>> pvalues = new ArrayList<>();

        for(Annotation p: parameters) {
            pvalues.add(resolveParameter(p));
        }

        int noParams = pvalues.size();
        int[] currIndices = new int[noParams];
        int[] maxIndices = new int[noParams];
        for(int i = 0; i < noParams; i++) {
            maxIndices[i] = pvalues.get(i).size();
        }

        List<Object[]> psets = new ArrayList<>();

        do {
            Object[] set = new Object[pnames.size()*2];

            // copy parameter names
            for(int i = 0; i < pnames.size(); i++) {
                set[i*2] = pnames.get(i);
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

    private List<Object> resolveParameter(Annotation parameterAnnotation) {
        TestParameterMarker tp = parameterAnnotation.annotationType().getAnnotation(TestParameterMarker.class);

        try {
            TestParameterEvaluationFunc func = tp.evaluationClass().newInstance();
            return func.eval(parameterAnnotation);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
