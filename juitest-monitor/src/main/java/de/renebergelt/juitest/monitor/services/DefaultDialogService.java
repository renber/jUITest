package de.renebergelt.juitest.monitor.services;

import de.renebergelt.juitest.monitor.viewmodels.MainViewModel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class DefaultDialogService implements DialogService {

    private static I18n i18n = I18nFactory.getI18n(MainViewModel.class);

    Window mainWindow;

    /**
     * Returns the currently active window which should be used as dialog parent.
     */
    private Window getDialogParent() {
        Window currentWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
        return currentWindow != null ?  currentWindow : mainWindow;
    }

    public void setMainWindow(Window mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public void showErrorMessage(String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                // open the dialog in the EDT
                SwingUtilities.invokeAndWait(() -> showErrorMessage(message));
                return;
            } catch (InterruptedException | InvocationTargetException e) {
                // --
            }
        }

        JOptionPane.showMessageDialog(getDialogParent(), message, i18n.tr("Error"), JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInformation(String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                // open the dialog in the EDT
                SwingUtilities.invokeAndWait(() -> showInformation(message));
                return;
            } catch (InterruptedException | InvocationTargetException e) {
                // --
            }
        }

        JOptionPane.showMessageDialog(getDialogParent(), message, i18n.tr("Information"), JOptionPane.INFORMATION_MESSAGE);
    }
}
