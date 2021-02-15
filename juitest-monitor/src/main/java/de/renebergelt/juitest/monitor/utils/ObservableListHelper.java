package de.renebergelt.juitest.monitor.utils;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;

import java.util.ArrayList;

public class ObservableListHelper {
    private ObservableListHelper() {
    }

    public static <T> ObservableList<T> observableList(Class<T> classType) {
        ArrayList<T> uList = new ArrayList();
        return ObservableCollections.observableList(uList);
    }

    public static ObservableList observableList() {
        ArrayList uList = new ArrayList();
        return ObservableCollections.observableList(uList);
    }
}
