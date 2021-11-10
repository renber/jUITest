package de.renebergelt.juitest.monitor.renderers;

import de.renebergelt.juitest.monitor.viewmodels.TestExecutionStatus;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TestExecutionStatusTableCellRenderer extends DefaultTableCellRenderer {

    private static I18n i18n = I18nFactory.getI18n(TestExecutionStatusTableCellRenderer.class);

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cmp = super.getTableCellRendererComponent(table, getDisplayString((TestExecutionStatus)value), isSelected, hasFocus, row, column);

        if (!isSelected) {
            cmp.setBackground(value instanceof TestExecutionStatus ? ((TestExecutionStatus)value).getDisplayBackground() : null);
        }

        return cmp;
    }

    private String getDisplayString(TestExecutionStatus status) {
        switch (status) {
            case IDLE: return "";
            case WAITING: return i18n.tr("Waiting for execution");
            case RUNNING: return i18n.tr("Running");
            case SUCCESS: return i18n.tr("SUCCESS");
            case FAILURE: return i18n.tr("FAILURE");
            case TIMEOUT: return i18n.tr("TIMEOUT");
            case CANCELLED: return i18n.tr("CANCELLED");

            default:
                throw new IllegalArgumentException("status");
        }
    }

}
