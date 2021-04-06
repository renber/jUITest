package de.renebergelt.juitest.core.annotations;

import de.renebergelt.juitest.core.annotations.parameterfunctions.TestParameterEvaluationFunc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for annotations which are test-parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TestParameterMarker {

    Class<? extends TestParameterEvaluationFunc> evaluationClass();
}
