package de.renebergelt.juitest.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for methods which are UiTests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UITest {

    String description() default "";

    /**
     * Parameters and values of the test as array of pairs:
     * (param1_name, param1_value, param2_name, param2_value, ...)
     */
    String[] parameters() default {};

}
