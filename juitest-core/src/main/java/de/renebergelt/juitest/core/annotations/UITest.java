package de.renebergelt.juitest.core.annotations;

import java.lang.annotation.*;

/**
 * Marker interface for methods which are UiTests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UITest {
    /**
     * Description of this test
     * @return The description
     */
    String description() default "";
}
