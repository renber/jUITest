package de.renebergelt.juitest.core.parameterfunctions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {{p:parameter}} in a test description with the parameter values
 */
public class TestDescriptionResolver {

    public String resolve(String name, Object[] parameters) {

        Pattern regex = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher m = regex.matcher(name);
        while (m.find()) {
            String innerText = m.group(1);

            if (innerText.startsWith("p:")) {
                String pValue = getParamValue(innerText.substring(2), parameters);

                // m.replaceFirst removes special chars (such as forward slashes) from pValue
                //name = m.replaceFirst(pValue);
                name = name.replace("{{" + innerText + "}}", pValue);
            }

            m = regex.matcher(name);
        }

        return name;
    }

    private String getParamValue(String paramName, Object[] parameters) {
        for(int i = 0; i < parameters.length; i+=2) {
            if (parameters[i].equals(paramName)) {
                return String.valueOf(parameters[i+1]);
            }
        }

        throw new IllegalArgumentException("Parameter name " + paramName + " does not exist");
    }

}
