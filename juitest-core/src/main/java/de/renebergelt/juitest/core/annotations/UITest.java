package de.renebergelt.juitest.core.annotations;

import java.lang.annotation.*;

/**
 * Marker interface for methods which are UiTests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UITest {
    String description() default "";
}
