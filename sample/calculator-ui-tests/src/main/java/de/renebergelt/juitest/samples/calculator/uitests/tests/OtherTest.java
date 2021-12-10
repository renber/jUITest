package de.renebergelt.juitest.samples.calculator.uitests.tests;

import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.UITestClass;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.samples.calculator.uitests.CalculatorAutomationTest;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

@UITestClass(testSetName = "Tests for jUITest features")
public class OtherTest extends CalculatorAutomationTest {

    @UITest(description="Long running test (try to cancel it!)")
    public void longRunningTest() throws CancellationException, TimeoutException, UITestException {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

}
