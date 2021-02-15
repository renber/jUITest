package de.renebergelt.juitest.monitor.viewmodels;

import de.renebergelt.juitest.core.TestSet;
import de.renebergelt.juitest.monitor.utils.ObservableListHelper;
import de.renebergelt.juitest.core.utils.NullGuard;
import org.jdesktop.observablecollections.ObservableList;

public class TestSetViewModel {

    private TestSet model;

    public String getName() {
        return  model.getName();
    }

    private ObservableList<TestViewModel> scripts = ObservableListHelper.observableList(TestViewModel.class);

    public ObservableList<TestViewModel> getScripts() {
        return scripts;
    }

    public TestSetViewModel(TestSet testSet) {
        this.model = NullGuard.forArgument("testSet", testSet);

        testSet.getTests().forEach(x -> scripts.add(new TestViewModel(x)));
    }

    @Override
    public String toString() {
        return getName();
    }

}
