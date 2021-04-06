package de.renebergelt.juitest.samples.calculator.uitests.tests;

import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.UITestClass;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.samples.calculator.CalculatorFrame;
import de.renebergelt.juitest.samples.calculator.uitests.CalculatorAutomationHost;
import de.renebergelt.juitest.samples.calculator.uitests.CalculatorAutomationTest;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@UITestClass(testSetName = "Test other functions")
public class CalculatorFunctionTest extends CalculatorAutomationTest {

    @UITest
    public void clear() {
        CalculatorFrame frame = context.getFrame();
        JTextField txtDisplay = findComponent(frame, JTextField.class);

        // ensure that the initial state is correct
        assertEquals("0", txtDisplay.getText());
        assertEquals(0, frame.getDisplayValue());

        // input some number
        inputNumber(34511);
        assertTrue(frame.getDisplayValue() != 0);

        // press clear
        AbstractButton btnClear = findComponent(context.getFrame(), AbstractButton.class, (b) -> "C".equals(b.getText()));
        assertNotNull(btnClear);
        uiActionWait(() -> btnClear.doClick());

        assertEquals("0", txtDisplay.getText());
        assertEquals(0, frame.getDisplayValue());
    }

    @UITest
    public void swapSign() {
        CalculatorFrame frame = context.getFrame();
        JTextField txtDisplay = findComponent(frame, JTextField.class);

        inputNumber(42);

        // swap the sign
        // note: in this sample this causes an unhandled exception to be thrown
        // which will be caught by jUITest and the test will be marked as FAILED
        AbstractButton btnSwapSign = findComponent(context.getFrame(), AbstractButton.class, (b) -> "+/-".equals(b.getText()));
        assertNotNull(btnSwapSign);
        uiActionWait(() -> btnSwapSign.doClick());
        assertEquals("-42", txtDisplay.getText());
        assertEquals(-42, frame.getDisplayValue());

        // swap again
        uiActionWait(() -> btnSwapSign.doClick());
        assertEquals("42", txtDisplay.getText());
        assertEquals(42, frame.getDisplayValue());
    }
}
