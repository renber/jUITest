package de.renebergelt.juitest.monitor.controls;

import javax.swing.*;
import java.awt.*;

public class AppStateComponent extends StatusBarComponent {

    private JLabel statusTextLbl;
    private ImageIcon busyIcon;

    public AppStateComponent(String initialText, ImageIcon busyIcon) {
        this.setLayout(new BorderLayout());

        statusTextLbl = new JLabel(initialText);
        this.busyIcon = busyIcon;

        this.add(statusTextLbl, BorderLayout.CENTER);
    }

    public void setStatusText(String newValue) {
        statusTextLbl.setText(newValue);
    }

    public void setBusy(boolean newValue) {
        statusTextLbl.setIcon(newValue ? busyIcon : null);
    }
}
