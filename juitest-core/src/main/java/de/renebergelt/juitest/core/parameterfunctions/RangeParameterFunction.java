package de.renebergelt.juitest.core.parameterfunctions;

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
