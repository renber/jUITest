package de.renebergelt.juitest.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be added to UiAutomationTest classes to provide
 * additional information
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UITestClass {

    /**
     * Returns the name of this test set
     * @return the name of this test set
     */
    String testSetName() default "";

}