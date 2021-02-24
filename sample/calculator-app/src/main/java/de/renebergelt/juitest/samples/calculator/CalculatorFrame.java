package de.renebergelt.juitest.samples.calculator;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The design of this application is unarguably bad, but it is somewhat the point.
 * It is used to show how a legacy app with no clear separation of view and logic can be tested
 * jUITest
 */
public class CalculatorFrame extends JFrame implements ActionListener {

    boolean beginNewOperand = false;
    String currentOperation = "";
    int memory = 0;

    private JTextField txtDisplay;
    private JButton btnZero, btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine;
    private JButton btnClear, btnEquals, btnSwapSign;
    private JToggleButton btnAdd, btnSubtract;

    public CalculatorFrame() {
        this.setTitle("Calculator");
        initComponents();
        wireUpButtons();
        this.pack();
    }

    private void initComponents() {
        Container cp = getContentPane();
        cp.setLayout(new MigLayout("", "[fill][fill][fill][fill]", ""));

        txtDisplay = new JTextField("0");
        txtDisplay.setEditable(false);
        txtDisplay.setBackground(Color.white);
        txtDisplay.setHorizontalAlignment(JTextField.TRAILING);
        cp.add(txtDisplay, "spanx 4, growx, wrap");

        btnSeven = new JButton("7");
        cp.add(btnSeven);
        btnEight = new JButton("8");
        cp.add(btnEight);
        btnNine = new JButton("9");
        cp.add(btnNine);
        btnClear = new JButton("C");
        cp.add(btnClear, "wrap");

        btnFour = new JButton("4");
        cp.add(btnFour);
        btnFive = new JButton("5");
        cp.add(btnFive);
        btnSix = new JButton("6");
        cp.add(btnSix, "");
        btnSubtract = new JToggleButton("-");
        cp.add(btnSubtract, "wrap");

        btnOne = new JButton("1");
        cp.add(btnOne);
        btnTwo = new JButton("2");
        cp.add(btnTwo);
        btnThree = new JButton("3");
        cp.add(btnThree, "");
        btnAdd = new JToggleButton("+");
        cp.add(btnAdd, "wrap");

        btnSwapSign = new JButton("+/-");
        cp.add(btnSwapSign);
        btnZero = new JButton("0");
        cp.add(btnZero);
        btnEquals = new JButton("=");
        cp.add(btnEquals, "skip");
    }

    private void wireUpButtons() {
        for(int i = 0; i < getContentPane().getComponentCount(); i++) {
            if (getContentPane().getComponent(i) instanceof AbstractButton) {
                ((AbstractButton)getContentPane().getComponent(i)).addActionListener(this);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = ((AbstractButton) e.getSource()).getText();

        switch (action) {
            case "C":
                setDisplayValue(0);
                setCurrentOperation("");
                memory = 0;
                beginNewOperand = false;
                break;
            case "=":
                compute();
                break;
            case "+":
            case "-":
                setCurrentOperation(action);
                memory = getDisplayValue();
                beginNewOperand = true;
                break;
            case "+/-":
                setDisplayValue(- getDisplayValue());
                // missing break
            default:
                // a digit button was pressed
                if (beginNewOperand) {
                    beginNewOperand = false;
                    setDisplayValue(Integer.parseInt(action));
                } else {
                    appendDisplayValue(Integer.parseInt(action));
                }
        }
    }

    private void setDisplayValue(int value) {
        txtDisplay.setText(String.valueOf(value));
    }

    private int getDisplayValue() {
        return Integer.valueOf(txtDisplay.getText());
    }

    private void appendDisplayValue(int digit) {
        if ("0".equals(txtDisplay.getText())) {
            setDisplayValue(digit);
        } else {
            txtDisplay.setText(txtDisplay.getText() + String.valueOf(digit));
        }
    }

    private void setCurrentOperation(String operation) {
        currentOperation = operation;
        btnAdd.setSelected("+".equals(operation));
        btnSubtract.setSelected("-".equals(operation));
    }

    private void compute() {
        if ("".equals(currentOperation)) return;

        if ("+".equals(currentOperation)) {
            setDisplayValue(memory + getDisplayValue());
        } else if ("-".equals(currentOperation)) {
            setDisplayValue(memory - getDisplayValue());
        }

        beginNewOperand = true;
        setCurrentOperation("");
    }

    public void reset() {
        memory = 0;
        setCurrentOperation("");
        setDisplayValue(0);
    }
}
