package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterMarker;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterFileListContainer;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterRangeContainer;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A TestParameter which represents a range of values
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ParameterRangeContainer.class)
@TestParameterMarker(evaluationClass = ParameterRangeEvaluationFunc.class)
public @interface ParameterRange {
    /**
     * Index of the parameter
     * @return index
     */
    int index();

    /**
     * The name of the parameter
     * @return name
     */
    String name() default "";

    /**
     * inclusive start of the range
     * @return start
     */
    int start();

    /**
     * inclusive end of the range
     * @return end
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
