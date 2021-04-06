package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterMarker;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TestParameterMarker(evaluationClass = ParameterRangeEvaluationFunc.class)
public @interface ParameterRange {
    int index();
    String name() default "";

    /**
     * inclusive start of the range
     */
    int start();

    /**
     * inclusive end of the range
     */
    int end();
}

class ParameterRangeEvaluationFunc implements TestParameterEvaluationFunc<ParameterRange> {

    @Override
    public List<Object> eval(ParameterRange parameterAnnotation) {
        int start = parameterAnnotation.start();
        int end = parameterAnnotation.end();

        List<Object> values = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            values.add(i);
        }

        return values;
    }
}
