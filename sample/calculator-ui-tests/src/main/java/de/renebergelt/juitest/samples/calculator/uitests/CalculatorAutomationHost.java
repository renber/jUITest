package de.renebergelt.juitest.samples.calculator.uitests;

import de.renebergelt.juitest.core.exceptions.UITestException;
import de.renebergelt.juitest.core.services.IPCTransmitter;
import de.renebergelt.juitest.host.testscripts.UIAutomationHost;
import de.renebergelt.juitest.samples.calculator.CalculatorFrame;

import javax.swing.*;

public class CalculatorAutomationHost implements UIAutomationHost {

    protected CalculatorFrame frame;

    public JFrame getFrame() {
        return frame;
    }

    @Override
    public void setTransmitter(IPCTransmitter transmitter) {
        // we don't need it
    }

    @Override
    public void launchApplicationUnderTest(String... arguments) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                frame = new CalculatorFrame();
                frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
                frame.setVisible(true);
            });
        } catch (Exception e) {
            throw new RuntimeException("Could not run calculator application: " + e.getMessage());
        }
    }

    @Override
    public boolean hasLaunched() {
        return frame != null;
    }

    @Override
    public void cleanup_after_test() {
        if (hasLaunched()) {
            frame.reset();
        }
    }

    @Override
    public void teardown() {
        if (hasLaunched()) {
            frame.setVisible(false);
            frame = null;
        }
    }
}
