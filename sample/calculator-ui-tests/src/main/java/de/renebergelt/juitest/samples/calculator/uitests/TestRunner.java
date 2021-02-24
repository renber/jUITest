package de.renebergelt.juitest.samples.calculator.uitests;

import de.renebergelt.juitest.host.UITestRunner;

public class TestRunner {

    public static void main(String[] args) {
        UITestRunner host = new UITestRunner("127.0.0.1", 5612, new CalculatorAutomationHost());
        host.start();
    }

}
