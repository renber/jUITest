package de.renebergelt.juitest.monitor.viewmodels;

import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.monitor.utils.ObservableListHelper;
import de.renebergelt.juitest.core.utils.NullGuard;
import de.renebergelt.juitest.core.services.TestRunnerService;
import org.jdesktop.observablecollections.ObservableList;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

public class TestViewModel extends ViewModelBase {

    protected TestDescriptor model;

    private Timer timer;

    private TestExecutionStatus status = TestExecutionStatus.IDLE;

    protected void setStatus(TestExecutionStatus newValue) {
        changeProperty("status", newValue);
    }

    public TestExecutionStatus getStatus() {
        return status;
    }

    public String getName() {
        return model.getName();
    }

    String errorText = "";

    public String getErrorText() {
        return errorText;
    }

    protected void setErrorText(String newValue) {
        changeProperty("errorText", newValue);
    }

    // the log messages which have been generated while this script was running
    public ObservableList<String> logMessages = ObservableListHelper.observableList(String.class);

    public ObservableList<String> getLogMessages() {
        return logMessages;
    }

    public TestViewModel(TestDescriptor model) {
        this.model = NullGuard.forArgument("model", model);
    }

    public void reset()  {
        setStatus(TestExecutionStatus.IDLE);
        setErrorText("");
        logMessages.clear();
        startTime = 0;
        endTime = 0;
        updateExecutionTime();
    }

    long startTime = 0;
    long endTime = 0;

    public long getExecutionTime() {
        if (startTime == 0)
            return 0;
        if (endTime == 0)
            // still running
            return System.currentTimeMillis() - startTime;

        return endTime - startTime;
    }

    private void updateExecutionTime() {
        SwingUtilities.invokeLater(() -> {
            if (status != TestExecutionStatus.RUNNING) {
                if (timer != null) {
                    timer.cancel();
                }
            }

            firePropertyChanged("executionTime", null, getExecutionTime());
        });
    }

    private void startExecutionTimer() {
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateExecutionTime();
            }
        }, 0, 500);
    }

    public void run(TestRunnerService testRunner) {
        try {
            startTime = System.currentTimeMillis();
            endTime = 0;

            SwingUtilities.invokeAndWait(() -> { setStatus(TestExecutionStatus.RUNNING); setErrorText(""); } );
            startExecutionTimer();

            testRunner.runTest(model);
            SwingUtilities.invokeLater(() -> setStatus(TestExecutionStatus.SUCCESS));
        }
        catch (CancellationException e) {
            SwingUtilities.invokeLater(() -> {setStatus(TestExecutionStatus.CANCELED); setErrorText(e.getMessage()); } );
        }
        catch(TimeoutException e) {
            SwingUtilities.invokeLater(() -> {setStatus(TestExecutionStatus.TIMEOUT); setErrorText(e.getMessage()); } );
        }
        catch(Exception e) {
            SwingUtilities.invokeLater(() -> {setStatus(TestExecutionStatus.FAILURE); setErrorText(e.getMessage() + "\n" + stackTraceToString(e)); } );
        } finally {
            endTime = System.currentTimeMillis();

            timer.cancel();
            updateExecutionTime();
        }
    }

    private String stackTraceToString(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return  writer.toString();
    }

}
