package de.renebergelt.juitest.monitor.viewmodels;

import de.renber.quiterables.QuIterables;
import de.renber.quiterables.Queriable;
import de.renber.quiterables.grouping.Group;
import de.renber.quiterables.grouping.GroupedQueriable;
import de.renebergelt.juitest.core.TestDescriptor;
import de.renebergelt.juitest.core.TestSet;
import de.renebergelt.juitest.monitor.config.TestMonitorConfiguration;
import de.renebergelt.juitest.monitor.services.DialogService;
import de.renebergelt.juitest.monitor.utils.ObservableListHelper;
import de.renebergelt.juitest.core.services.TestStatusListener;
import de.renebergelt.juitest.core.services.TestRunnerService;
import de.renebergelt.juitest.core.utils.NullGuard;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.jdesktop.xbindings.commands.Command;
import org.jdesktop.xbindings.commands.RelayCommand;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainViewModel extends ViewModelBase {

    private static I18n i18n = I18nFactory.getI18n(MainViewModel.class);

    private DialogService dialogService;
    private TestRunnerService testRunner;
    private TestStatusListener testStatusListener;

    private TestMonitorConfiguration configuration;

    protected boolean running;
    protected boolean attached;
    protected boolean isStopRequested;

    public boolean isRunning() {
        return running;
    }

    public boolean isAttached(){ return attached; }
    private void setAttached(boolean newValue) { changeProperty("attached", newValue); }

    protected void setRunning(boolean newValue) {
        changeProperty("running", newValue);
    }

    ObservableList<TestSetViewModel> testSets = ObservableListHelper.observableList(TestSetViewModel.class);

    public ObservableList<TestSetViewModel> getTestSets() {
        return testSets;
    }

    TestSetViewModel selectedTestSet = null;

    public TestSetViewModel getSelectedTestSet() {
        return selectedTestSet;
    }

    public void setSelectedTestSet(TestSetViewModel newValue) {
        if (changeProperty("selectedTestSet", newValue)) {
           updateTestSet();
        }
    }

    ObservableList<TestViewModel> tests = ObservableListHelper.observableList(TestViewModel.class);

    public ObservableList<TestViewModel> getTests() {
        return tests;
    }

    Thread testRunnerThread;

    AtomicReference<TestViewModel> currentlyRunningTest = new AtomicReference<>();

    ObservableList<TestViewModel> selectedTests = ObservableListHelper.observableList(TestViewModel.class);

    public ObservableList<TestViewModel> getSelectedTests() {
        return selectedTests;
    }

    /**
     * Returns the selected script in case only one script is selected
     * (i.e. getSelectedScripts().size == 1)
     */
    public TestViewModel getSingleSelectedTest() {
        if (getSelectedTests().size() == 1)
            return getSelectedTests().get(0);

        return null;
    }

    String stateText = i18n.tr("Ready");

    public String getStateText() {
        return stateText;
    }

    protected void setStateText(String newValue) {
        changeProperty("stateText", newValue);
    }

    Command attachCommand;
    Command detachCommand;

    Command runAllCommand;
    Command runCommand;
    Command stopCommand;

    Command reloadTestCasesCommand;

    public MainViewModel(TestMonitorConfiguration configuration, DialogService dialogService, TestRunnerService testRunnerService) {
        this.configuration = NullGuard.forArgument("configuration", configuration);
        this.dialogService = NullGuard.forArgument("dialogService", dialogService);
        this.testRunner = NullGuard.forArgument("testRunnerService", testRunnerService);

        testStatusListener = new TestStatusListener() {
            @Override
            public void onTestExecutionPaused(String message) {
                SwingUtilities.invokeLater( () -> {
                    String dlgMessage = i18n.tr("Execution of the current test has been paused with the following message:\n {0} \n\nClick OK to continue test execution.");
                    dialogService.showInformation(dlgMessage);

                    testRunner.resumeTest();
                });
            }

            @Override
            public void onLogMessageReceived(String message) {
                TestViewModel svm = currentlyRunningTest.get();

                if (svm != null) {
                    SwingUtilities.invokeLater(() -> {
                        svm.getLogMessages().add(message);
                    });
                }
            }

            @Override
            public void connectionTerminated(Throwable cause) {
                SwingUtilities.invokeLater( () -> {
                    setAttached(false);

                    if (testRunnerThread != null) {
                        testRunnerThread.interrupt();
                    }
                });
            }
        };

        testRunner.addTestStatusListener(testStatusListener);

        // register the logger which collects log messages during a script's execution
        //LogService.registerLogger(new AutomationLogger(() -> currentlyRunningTest.get()));
        //loadTestSets();

        if (getTestSets().size() > 0) {
            setSelectedTestSet(getTestSets().get(0));
        }

        // update single selected test
        selectedTests.addObservableListListener(new ObservableListListener() {
            @Override
            public void listElementsAdded(ObservableList list, int index, int length) {
                notifySingleSelection();
            }

            @Override
            public void listElementsRemoved(ObservableList list, int index, List oldElements) {
                notifySingleSelection();
            }

            @Override
            public void listElementReplaced(ObservableList list, int index, Object oldElement) {
                notifySingleSelection();
            }

            @Override
            public void listElementPropertyChanged(ObservableList list, int index) {
                notifySingleSelection();
            }

            private void notifySingleSelection() {
                firePropertyChanged("singleSelectedTest", new Object(), getSingleSelectedTest());
            }
        });

        // commands
        attachCommand = new RelayCommand(() -> attach(), () -> !isAttached());
        detachCommand = new RelayCommand(() -> detach(), () -> isAttached());

        runAllCommand = new RelayCommand(() -> run(tests), () -> !running);
        runCommand = new RelayCommand( () -> run(selectedTests), () -> !running && selectedTests.size() > 0);

        stopCommand = new RelayCommand(() -> {
            isStopRequested = true;
            testRunner.cancelRunningTest();
        },
        () -> running && !isStopRequested);

        reloadTestCasesCommand = new RelayCommand( () -> {
            loadTestSets();
        }, () -> isAttached() && !running);
    }

    private void loadTestSets() {
        // save  selection of set and tests
        String formerSelectedSet = selectedTestSet != null ? selectedTestSet.getName() : null;
        Set<String> formerSelectedTests = QuIterables.query(selectedTests).select(x -> x.getName()).toSet();

        selectedTests.clear();
        testSets.clear();

        List<TestDescriptor> availableTests = null;

        try {
            // test cases are directly requested from the TestRunner instance
            availableTests = testRunner.discoverTests();
        } catch (Exception e) {
            dialogService.showErrorMessage("Unable to retrieve available test cases from TestRunner instance");
            return;
        }

        for(Group<TestDescriptor> g: QuIterables.query(availableTests).groupSingle(x -> x.getTestClassName())) {
            TestSet newTestSet = new TestSet(g.getKey().first().toString());
            newTestSet.getTests().addAll(g);
            testSets.add(new TestSetViewModel(newTestSet));
        }

        // add <All Tests>
        TestSetViewModel allVm = new TestSetViewModel(new TestSet(i18n.tr("<All tests>")));
        for(TestSetViewModel ss: testSets) {
            allVm.getScripts().addAll(ss.getScripts());
        }
        testSets.add(0, allVm);

        // restore selection of set and tests when reloading (by name, if the user changed the name we will not be able to reselect)
        TestSetViewModel restoredSet = QuIterables.query(testSets).firstOrDefault(x -> Objects.equals(formerSelectedSet, x.getName()) );
        if (restoredSet != null) {
            setSelectedTestSet(restoredSet);
            selectedTests.addAll(QuIterables.query(tests).where(x -> formerSelectedTests.contains(x.getName())).toList());
        } else {
            setSelectedTestSet(QuIterables.query(testSets).firstOrDefault());
        }
    }

    private void updateTestSet() {
        getTests().clear();

        if (getSelectedTestSet() != null) {
            getTests().addAll(getSelectedTestSet().getScripts());
        }
    }

    public int[] getTestStatus() {
        Queriable<TestViewModel> qscripts = QuIterables.query(tests);
        int succeeded = qscripts.count(x -> x.getStatus() == TestExecutionStatus.SUCCESS);
        int failed = qscripts.count(x -> x.getStatus() == TestExecutionStatus.FAILURE || x.getStatus() == TestExecutionStatus.CANCELED || x.getStatus() == TestExecutionStatus.TIMEOUT);
        int waiting = qscripts.count(x -> x.getStatus() == TestExecutionStatus.WAITING);

        return new int[]{succeeded, failed, waiting};
    }

    private void notifyTestStatusChanged() {
        SwingUtilities.invokeLater( () -> {
            firePropertyChanged("testStatus", null, getTestStatus());
        });
    }

    private void run(List<TestViewModel> testsToRun) {
        setRunning(true);

        // make a copy of the list
        List<TestViewModel> testList = new ArrayList(testsToRun);

        testRunnerThread = new Thread(() -> {
            try {
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        for (TestViewModel test : testsToRun) {
                            test.reset();
                            test.setStatus(testList.contains(test) ? TestExecutionStatus.WAITING : TestExecutionStatus.IDLE);
                        }
                    });
                    notifyTestStatusChanged();

                    // connect to the application under test
                    if (!__attach()) return;
                } catch (InterruptedException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

                SwingUtilities.invokeLater(() -> {
                    setRunning(true);
                    setStateText(i18n.tr("Running tests"));
                });

                for (TestViewModel test : testList) {
                    if (isStopRequested) {
                        SwingUtilities.invokeLater(() -> test.setStatus(TestExecutionStatus.CANCELED));
                    } else {
                        currentlyRunningTest.set(test);
                        test.run(testRunner);
                    }

                    notifyTestStatusChanged();
                }
            } finally {
                currentlyRunningTest.set(null);
                isStopRequested = false;

                SwingUtilities.invokeLater(() -> {
                    setRunning(false);
                    setStateText(i18n.tr("Ready"));

                    for (TestViewModel test : testList) {
                        if (test.getStatus() == TestExecutionStatus.WAITING)
                            test.setStatus(TestExecutionStatus.IDLE);
                    }

                    notifyTestStatusChanged();
                });

                testRunnerThread = null;
            }
        });
        testRunnerThread.start();
    }

    public void attach() {
        setRunning(true);

        Thread t = new Thread(() -> {
            __attach();
            if (isAttached() && testSets.isEmpty()) {
                SwingUtilities.invokeLater(() -> loadTestSets());
            }
        });
        t.start();
    }

    private boolean __attach() {
        try {
            if (testRunner.isAttached()) {
                return true;
            } else {
                SwingUtilities.invokeAndWait(() -> {
                    setRunning(true);
                    setStateText(i18n.tr("Launching Application under test"));
                });

                try {
                    testRunner.attach(configuration.getRunner().getLaunchArguments());
                    setAttached(true);

                    return true;
                } catch (Exception e) {
                    setAttached(false);

                    dialogService.showErrorMessage(i18n.tr("Could not connect to the test runner. Please ensure that it has been started.\nError message: {0}", e.getMessage()));
                    return false;
                }
            }
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            SwingUtilities.invokeLater(() -> {
                setStateText(i18n.tr("Ready"));
                setRunning(false);
            });
        }
    }

    public void detach() {
        if (testRunner.isAttached()) {
            testRunner.disattach();
        }

        if (testRunnerThread != null) {
            TestViewModel svm = currentlyRunningTest.get();

            testRunnerThread.stop();
            testRunnerThread = null;

            if (svm != null) {
                svm.setStatus(TestExecutionStatus.CANCELED);
            }

            isStopRequested = true;
        }

        setAttached(false);
    }

    public Command getAttachCommand() {
        return attachCommand;
    }

    public Command getDetachCommand() {
        return detachCommand;
    }

    public Command getRunAllCommand() {
        return runAllCommand;
    }

    public Command getRunCommand() {
        return runCommand;
    }

    public Command getStopCommand() {
        return stopCommand;
    }

    public Command getReloadTestCasesCommand() { return reloadTestCasesCommand; }
}
