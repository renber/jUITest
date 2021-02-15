package de.renebergelt.juitest.monitor.controls;

import de.renebergelt.juitest.monitor.renderers.TestExecutionStatusTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import java.awt.*;

public class ConnectionStateControl extends JPanel {

    private static I18n i18n = I18nFactory.getI18n(TestExecutionStatusTableCellRenderer.class);

    public final static int CIRCLE_DIAMETER = 16;

    boolean connected = false;

    private CircleControl circle;
    private JLabel label;

    public ConnectionStateControl() {
        initComponents();
        refreshUI();
    }

    private void initComponents() {
        setLayout(new MigLayout("ins 0"));

        circle = new CircleControl();
        this.add(circle, "");

        label = new JLabel();
        this.add(label);
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean newValue) {
        connected = newValue;

        refreshUI();
    }

    private void refreshUI() {
        circle.setColor(connected ? Color.green : Color.yellow);
        label.setText(connected ? i18n.tr("Connected") : i18n.tr("Not connected"));
    }

    private static class CircleControl extends Canvas {

        Color color  = Color.yellow;

        public CircleControl() {
            this.setSize(CIRCLE_DIAMETER+1, CIRCLE_DIAMETER+1);
        }

        public void setColor(Color newValue) {
            color = newValue;
            this.repaint();
        }

        public Color getColor() {
            return color;
        }

        @Override
        public void paint(Graphics g) {
            ((Graphics2D) g).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

            g.setColor(color);
            g.fillOval(0, 0, CIRCLE_DIAMETER, CIRCLE_DIAMETER);

            g.setColor(Color.black);
            g.drawOval(0, 0, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        }
    }

}
