package de.renebergelt.juitest.monitor.controls;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * Container which is a section of the DialogToolbar
 * @author berre
 */
public class StatusBarComponent extends JPanel {

    public StatusBarComponent() {
        // Create this component's border
        EmptyBorder eBorder = new EmptyBorder(2, 4, 2, 4);
        BevelBorder bBorder = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cBorder = new CompoundBorder(bBorder, eBorder);
        this.setName("statusBar");
        this.setBorder(cBorder);
    }

}
