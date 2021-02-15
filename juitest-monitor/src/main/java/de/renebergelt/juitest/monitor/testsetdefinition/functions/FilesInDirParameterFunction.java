package de.renebergelt.juitest.monitor.testsetdefinition.functions;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FilesInDirParameterFunction implements TestParameterFunction {
    @Override
    public String getName() {
        return "files-in-dir";
    }

    @Override
    public List<Object> eval(String parameters) {

        String[] p = parameters.split(",");
        if (p.length != 2) {
            throw new IllegalArgumentException("Parameters for " + getName() + "need to be given as path,filemask");
        }

        List<Object> filenames = new ArrayList<>();

        File[] files = new File(p[0]).listFiles((FilenameFilter)new WildcardFileFilter(p[1]));
        for(File f: files) {
            filenames.add(f.getPath());
        }

        return filenames;
    }
}
