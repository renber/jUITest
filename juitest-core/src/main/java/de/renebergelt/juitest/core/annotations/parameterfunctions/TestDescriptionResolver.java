package de.renebergelt.juitest.core.annotations.parameterfunctions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {{p:parameter}} in a test description with the parameter values
 */
public class TestDescriptionResolver {

    /**
     * Replaces {{p:parameter}} in a test description with the parameter values
     * @param text The text to replace in
     * @param parameters The available parameters
     * @return The text with inserted parameter names
     */
    public String resolve(String text, Object[] parameters) {

        Pattern regex = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher m = regex.matcher(text);
        while (m.find()) {
            String innerText = m.group(1);

            if (innerText.startsWith("p:")) {
                String pValue = getParamValue(innerText.substring(2), parameters);

                // m.replaceFirst removes special chars (such as forward slashes) from pValue
                //text = m.replaceFirst(pValue);
                text = text.replace("{{" + innerText + "}}", pValue);
            }

            m = regex.matcher(text);
        }

        return text;
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
