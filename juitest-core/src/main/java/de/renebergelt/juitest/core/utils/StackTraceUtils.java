package de.renebergelt.juitest.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtils {

    private StackTraceUtils() {

    }

    public static  String stackTraceToString(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return  writer.toString();
    }

}
