package de.renebergelt.juitest.samples.calculator;

import javax.swing.*;

public class Starter {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorFrame f = new CalculatorFrame();
            f.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

}
