package de.renebergelt.juitest.samples.calculator.uitests;

import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.host.testscripts.SwingAutomationTest;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Basic implementation for our UI tests
 */
public abstract class CalculatorAutomationTest  extends SwingAutomationTest<CalculatorAutomationHost> {

    protected CalculatorAutomationHost context;

    @Override
    protected void doRun(CalculatorAutomationHost context) throws CancellationException, TimeoutException, UITestException {
        this.context = context;
    }

    /**
     * Inputs the given number into the calculator by
     * pressing the appropriate digit buttons and checks
     * if the UI displays the correct number afterwards
     */
    protected void inputNumber(int number) {
        // Input the number digit by digit
        String digits = String.valueOf(number);
        for(int i = 0; i <digits.length(); i++) {
            String digit = String.valueOf(digits.charAt(i));
            // find the button for this digit + press it
            AbstractButton btn = findComponent(context.getFrame(), AbstractButton.class, (b) -> digit.equals(b.getText()));
            assertNotNull(btn);
            uiActionWait(() -> btn.doClick());
        }

        // check if number was input correctly
        assertEquals(String.valueOf(number), findComponent(context.getFrame(), JTextField.class).getText());
    }



}
