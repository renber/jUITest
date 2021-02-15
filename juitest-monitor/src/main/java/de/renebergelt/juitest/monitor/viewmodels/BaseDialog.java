package de.renebergelt.juitest.monitor.viewmodels;

import de.renebergelt.juitest.monitor.controls.DialogStatusBar;

import javax.swing.*;
import java.awt.*;

public abstract class BaseDialog extends JFrame {

    private boolean enableContentPaneOverride = false;
    private JPanel contentPanel;

    private DialogStatusBar statusBar;

    public BaseDialog(boolean showStatusBar) {
        init(showStatusBar);
    }

    private void init(boolean addStatusBar) {
        this.getContentPane().setLayout(new BorderLayout());

        contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);
        statusBar = new DialogStatusBar();

        if (addStatusBar)
            add(statusBar, BorderLayout.SOUTH);

        enableContentPaneOverride = true;
    }

    /**
     * Returns the status bar of this dialog
     */
    public DialogStatusBar getStatusBar() {
        return statusBar;
    }

    @Override
    public Container getContentPane() {
        if (enableContentPaneOverride) // avoid IllegalArgumentException: adding
        // container's parent to itself in
        // initComponents()
        {
            // the dialogs content pane is now the content panel without the
            // status bar
            return contentPanel;
        } else {
            return super.getContentPane();
        }
    }

    /**
     * You can add custom components to the status bar in this function (return
     * true in this case) If you return false the dialog will receive the
     * default status bar
     */
    protected abstract boolean customizeStatusbar();

}
