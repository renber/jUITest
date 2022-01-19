package de.renebergelt.juitest.core.utils;

import java.util.function.Function;

/**
 * Convenience methods for checking null values
 * @author berre
 */
public class NullGuard {

    private NullGuard() {
        // --
    }

    /**
     * Returns value if value is not null, otherwise throws an ArgumentNullException
     * Should be used to ensure fast-fail on null arguments
     * @param argumentName The name of the argument to be checked (gets inserted in the exception message)
     * @param <T> The argument type
     * @param value The value of teh argument
     * @return value if it is not null
     */
    public static <T> T forArgument(String argumentName, T value) {
        if (value == null)
            throw new IllegalArgumentException("Argument '" + argumentName + "' must not be null");

        return value;
    }

    /**
     * Returns value, or defaultValue if value is null
     * @param value The value to check
     * @param defaultValue The default value (must not be null)
     * @param <T> Type of the value
     * @return value, or defaultValue when value is null
     */
    public static <T> T defaultForNull(T value, T defaultValue) {
        NullGuard.forArgument("defaultValue", defaultValue);

        return value == null ? defaultValue : value;
    }

}
