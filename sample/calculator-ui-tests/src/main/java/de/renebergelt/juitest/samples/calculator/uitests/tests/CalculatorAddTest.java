package de.renebergelt.juitest.samples.calculator.uitests.tests;

import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.host.testscripts.SwingAutomationTest;
import de.renebergelt.juitest.samples.calculator.uitests.CalculatorAutomationHost;
import de.renebergelt.juitest.samples.calculator.uitests.CalculatorAutomationTest;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class CalculatorAddTest extends CalculatorAutomationTest {

    Integer operandOne = null;
    Integer operandTwo = null;

    @Override
    public String getName() {
        return "AddTests";
    }

    @Override
    public void setParameter(String parameterName, Object parameterValue) {
        switch(parameterName) {
            case "operandOne": operandOne = (int)parameterValue; break;
            case "operandTwo": operandTwo = (int)parameterValue; break;
        }
    }

    @Override
    protected void doRun(CalculatorAutomationHost context) throws CancellationException, TimeoutException, UITestException {
        super.doRun(context);

        if (operandOne == null || operandTwo == null) {
            throw new UITestException("Missing parameters");
        }

        inputNumber(operandOne);
        AbstractButton addBtn = findComponent(context.getFrame(), AbstractButton.class, (b) -> "+".equals(b.getText()));
        assertNotNull(addBtn);
        uiActionWait(() -> addBtn.doClick());
        // the add button should now be pressed
        assertTrue(addBtn.isSelected());
        inputNumber(operandTwo);

        AbstractButton eqBtn = findComponent(context.getFrame(), AbstractButton.class, (b) -> "=".equals(b.getText()));
        assertNotNull(eqBtn);
        uiActionWait(() -> eqBtn.doClick());

        // check the result
        assertEquals(String.valueOf(operandOne + operandTwo), findComponent(context.getFrame(), JTextField.class).getText());
        // add button should now be unpressed
        assertFalse(addBtn.isSelected());
    }
}
