package de.renebergelt.juitest.monitor.services;

import javax.swing.*;
import java.net.URL;

public class DefaultIconService implements IconService {

    public DefaultIconService() {
        // --
    }

    String iconBasePath = "de/renebergelt/juitest/monitor/icons";
    /**
     * Get the icon by name
     * @param name the name of the icon resource without path and extensions
     * @return
     */
    @Override
    public ImageIcon getIcon(String name) {
        // get the theme dependent path
        URL url = getIcon(name, ".png", ".gif", "");
        if (url != null)
            return new ImageIcon(url);
        else
            return null;
    }

    private URL getIcon(String name, String...extensions) {
        String path = iconBasePath + "/" + name;

        for(String ext: extensions) {
            URL url = DefaultIconService.class.getClassLoader().getResource(path + ext);
            if (url != null)
                return url;
        }

        return null;
    }

}