package de.renebergelt.juitest.monitor.config;

public class TestMonitorConfiguration {

    String language = "sys";
    TestRunnerConfiguration runner;

    public String getLanguage() { return language; }
    public void setLanguage(String value) { language = value; }

    public void setRunner(TestRunnerConfiguration value) { runner = value; }
    public TestRunnerConfiguration getRunner() { return runner; }

    public static class TestRunnerConfiguration {
        String[] launchArguments;

        public String[] getLaunchArguments() {
            return launchArguments;
        }

        public void setLaunchArguments(String[] launchArguments) {
            this.launchArguments = launchArguments;
        }
    }
}
