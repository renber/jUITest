package de.renebergelt.juitest.monitor.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestMonitorConfigurationReader {

    public TestMonitorConfiguration readFromFile(String filename) {
        try (FileInputStream stream = new FileInputStream(filename)) {
            return readFromStream(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TestMonitorConfiguration readFromStream(InputStream stream) {
        Yaml yaml = new Yaml();
        TestMonitorConfiguration args = yaml.loadAs(stream, TestMonitorConfiguration.class);
        return args;
    }

}
