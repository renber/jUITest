package de.renebergelt.juitest.samples.calculator.uitests.tests;

import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.UITestClass;
import de.renebergelt.juitest.core.annotations.parameterfunctions.ParameterRange;
import de.renebergelt.juitest.core.annotations.parameterfunctions.ParameterSet;
import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.samples.calculator.uitests.CalculatorAutomationTest;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@UITestClass
public class CalculatorAddTest extends CalculatorAutomationTest {

    @UITest(description="Sum of 25 and 13")
    public void add() throws CancellationException, TimeoutException, UITestException {
        inputNumber(25);
        AbstractButton addBtn = findComponent(context.getFrame(), AbstractButton.class, (b) -> "+".equals(b.getText()));
        assertNotNull(addBtn);
        uiActionWait(() -> addBtn.doClick());
        // the add button should now be pressed
        assertTrue(addBtn.isSelected());
        inputNumber(13);

        AbstractButton eqBtn = findComponent(context.getFrame(), AbstractButton.class, (b) -> "=".equals(b.getText()));
        assertNotNull(eqBtn);
        uiActionWait(() -> eqBtn.doClick());

        // check the result
        assertEquals(String.valueOf(25 + 13), findComponent(context.getFrame(), JTextField.class).getText());
        // add button should now be unpressed
        assertFalse(addBtn.isSelected());
    }

    @UITest
    @ParameterRange(index = 0, name = "operandOne", start = 6, end = 10)
    @ParameterSet(index = 1, name = "operandTwo", intValues = {26, 8})
    public void add_with_params(int operandOne, int operandTwo) throws CancellationException, TimeoutException, UITestException {
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
