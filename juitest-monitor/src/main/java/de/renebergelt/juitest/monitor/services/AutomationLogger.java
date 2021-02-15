package de.renebergelt.juitest.monitor.services;

import java.util.function.Supplier;

public class AutomationLogger {
    // --
}

/**
 * Logger which adds log messages to the currently running script's log history (if any)
 */
/*public class AutomationLogger implements Logger {

    Supplier<ScriptViewModel> runningScriptFunc;

    public AutomationLogger(Supplier<ScriptViewModel> runningScriptFunc) {
        this.runningScriptFunc = NullGuard.forArgument("runningScriptFunc", runningScriptFunc);
    }

    @Override
    public void log(LogLevel logLevel, LogGroup logGroup, String message) {
        ScriptViewModel svm = runningScriptFunc.get();
        if (svm != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(logLevel.toString()).append("] ");

            if (logGroup != LogGroup.None) {
                sb.append("<").append(logGroup.toFriendlyString()).append("> ");
            }

            sb.append(message);

            svm.getLogMessages().add(sb.toString());
        }
    }
}*/
