package de.renebergelt.juitest.monitor.testsetdefinition.functions;

import java.util.ArrayList;
import java.util.List;

public class RangeParameterFunction implements TestParameterFunction {

    @Override
    public String getName() {
        return "range";
    }

    @Override
    public List<Object> eval(String parameters) {

        List<Object> rList = new ArrayList<>();
        rList.add(1);
        return rList;
    }

}
