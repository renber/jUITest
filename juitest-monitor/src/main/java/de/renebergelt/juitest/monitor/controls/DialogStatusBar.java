package de.renebergelt.juitest.monitor.controls;

import java.awt.*;

/**
 * Toolbar for the application
 * @author berre
 */
public class DialogStatusBar extends javax.swing.JPanel  {

    /**
     * Creates a DialogStatusBar
     */
    public DialogStatusBar() {
        this.setLayout(new GridBagLayout());
    }

    /**
     * Adds component without horizontal stretch
     */
    public void addStatusComponent(StatusBarComponent comp) {
        addStatusComponent(comp, 0);
    }

    /**
     * Adds component with horizontal stretch enabled
     */
    public void addStatusComponent(StatusBarComponent comp, double weightx) {
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = weightx;
        if (weightx > 0) {
            c.fill = GridBagConstraints.BOTH;
        } else
            c.fill = GridBagConstraints.VERTICAL;

        this.add(comp, c);
    }

    @SuppressWarnings("unchecked")
    public <T extends StatusBarComponent> T getStatusComponent(Class<T> componentClass) {
        for(Component sc: this.getComponents()) {
            if (componentClass.isInstance(sc)) {
                return (T) sc;
            }
        }

        return null;
    }

}
