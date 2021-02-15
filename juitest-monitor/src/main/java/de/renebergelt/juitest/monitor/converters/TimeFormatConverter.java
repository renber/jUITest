package de.renebergelt.juitest.monitor.converters;

import org.jdesktop.beansbinding.Converter;

public class TimeFormatConverter extends Converter<Long, String> {
    @Override
    public String convertForward(Long value) {
        if (value == 0)
            return "";

        value = value / 1000;

        long seconds = value % 60;
        long minutes = (value / 60);

        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public Long convertReverse(String value) {
        throw new UnsupportedOperationException();
    }
}
