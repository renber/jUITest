package de.renebergelt.juitest.core.annotations.parameterfunctions;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Specifies a function which returns the actual values for
 * a defined TestParameter
 */
public interface TestParameterEvaluationFunc<T extends Annotation> {

    List<Object> eval(T parameterAnnotation);

}
