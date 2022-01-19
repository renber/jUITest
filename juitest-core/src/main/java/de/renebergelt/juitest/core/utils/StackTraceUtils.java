package de.renebergelt.juitest.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utils for StackTrace handling
 */
public class StackTraceUtils {

    private StackTraceUtils() {

    }

    /**
     * Converts the stack trace of the given Throwable instance to a srting
     * @param e The throwable to extract the stack trace from
     * @return The string representation of the stack trace
     */
    public static String stackTraceToString(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return  writer.toString();
    }

}
