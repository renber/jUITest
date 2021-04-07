package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterContainer;
import de.renebergelt.juitest.core.annotations.TestParameterMarker;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterSetContainer;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ParameterSetContainer.class)
@TestParameterMarker(evaluationClass = ParameterSetEvaluationFunc.class)
public @interface ParameterSet {
    int index();
    String name() default "";

    int[] intValues() default {};
    float[] floatValues() default {};
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