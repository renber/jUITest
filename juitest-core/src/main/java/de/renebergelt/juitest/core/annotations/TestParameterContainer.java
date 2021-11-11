package de.renebergelt.juitest.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation which indicates
 * that a annotation contains multiple Annotations marked
 * with TestParameterMarker in a value "field"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TestParameterContainer {
}
