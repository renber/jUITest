package de.renebergelt.juitest.core.annotations.parameterfunctions.containers;

import de.renebergelt.juitest.core.annotations.TestParameterContainer;
import de.renebergelt.juitest.core.annotations.parameterfunctions.ParameterSet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation which collects repeated
 * ParameterSet annotations
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestParameterContainer
public @interface ParameterSetContainer {

    /**
     * The ParameterSet objects contained in this container
     * @return ParameterSets
     */
    ParameterSet[] value();
}