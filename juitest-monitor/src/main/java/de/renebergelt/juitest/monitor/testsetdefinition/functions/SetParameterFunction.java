package de.renebergelt.juitest.monitor.testsetdefinition.functions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SetParameterFunction implements TestParameterFunction {

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public List<Object> eval(String parameters) {

        String[] values = parameters.split(",");
        List<Object> rList = new ArrayList<>();
        for(String v: values) {
            rList.add(guessDataType(v));
        }
        return rList;
    }

    private Object guessDataType(String v)
    {
        if (StringUtils.isNumeric(v))
            return Integer.valueOf(v);

        // keep as String
        return v;
    }
}
