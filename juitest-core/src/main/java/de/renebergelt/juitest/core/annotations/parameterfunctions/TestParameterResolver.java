package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterContainer;
import de.renebergelt.juitest.core.annotations.TestParameterMarker;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class TestParameterResolver {

    protected Integer getParameterIndex(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("index");
            return (Integer)m.invoke(a);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getParameterName(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("name");
            return String.valueOf(m.invoke(a));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getDeclaredParameterNames(Method uiTestMethod) {
        List<Annotation> parameters = getParameters(uiTestMethod);
        parameters.sort(Comparator.comparing(this::getParameterIndex));
        return parameters.stream().map(x -> getParameterName(x)).collect(Collectors.toList());
    }

    public boolean hasParameters(Method uiTestMethod) {
        return Arrays.stream(uiTestMethod.getAnnotations()).anyMatch(x -> x.annotationType().isAnnotationPresent(TestParameterContainer.class) || x.annotationType().isAnnotationPresent(TestParameterMarker.class));
    }

    private List<Annotation> unrollParameterContainer(Annotation containerAnnotation) {
        try {
            return (List)Arrays.asList(containerAnnotation.annotationType().getMethod("value").invoke(containerAnnotation), new Annotation[0]);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Annotation> getParameters(Method uiTestMethod) {
        // get all annotations which are marked with TestParameterMarker
        List<Annotation> alist = new ArrayList<>();
        alist.addAll(Arrays.stream(uiTestMethod.getAnnotations()).filter(x -> x.annotationType().isAnnotationPresent(TestParameterMarker.class)).collect(Collectors.toList()));
        // and "unroll" all TestParameterContainer annotations
        alist.addAll(Arrays.stream(uiTestMethod.getAnnotations()).filter(x -> x.annotationType().isAnnotationPresent(TestParameterContainer.class)).map(x -> unrollParameterContainer(x)).flatMap(List::stream).collect(Collectors.toList()));
        return alist;
    }

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

        List<Object> rList = new ArrayList<>();

        try {
            TestParameterEvaluationFunc func = tp.evaluationClass().newInstance();
            return func.eval(parameterAnnotation);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
