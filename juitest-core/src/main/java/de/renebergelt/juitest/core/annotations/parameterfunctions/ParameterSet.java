package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterContainer;
import de.renebergelt.juitest.core.annotations.TestParameterMarker;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterSetContainer;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A TestParameter which represents a set of values
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ParameterSetContainer.class)
@TestParameterMarker(evaluationClass = ParameterSetEvaluationFunc.class)
public @interface ParameterSet {
    /**
     * The index of this parameter
     * @return index
     */
    int index();

    /**
     * The name of this parameter
     * @return name
     */
    String name() default "";

    /**
     * Values of this set if integers are used
     * @return The integer values contained in this set
     */
    int[] intValues() default {};

    /**
     * Values of this set if floats are used
     * @return The float values contained in this set
     */
    float[] floatValues() default {};

    /**
     * Values of this set if strings are used
     * @return The string values contained in this set
     */
    String[] stringValues() default {};
}

class ParameterSetEvaluationFunc implements TestParameterEvaluationFunc<ParameterSet> {

    @Override
    public List<Object> eval(ParameterSet parameterAnnotation) {
        List<Object> values = new ArrayList<>();
        for(int i: parameterAnnotation.intValues()) {
            values.add(i);
        }
        for(float f: parameterAnnotation.floatValues()) {
            values.add(f);
        }
        for(String s: parameterAnnotation.stringValues()) {
            values.add(s);
        }
        Arrays.stream(parameterAnnotation.stringValues()).forEach(s -> values.add(s));
        return values;
    }
}