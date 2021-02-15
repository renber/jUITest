package de.renebergelt.juitest.monitor.viewmodels;

import java.awt.*;

public enum TestExecutionStatus {
    IDLE,
    WAITING,
    RUNNING,
    SUCCESS,
    FAILURE,
    TIMEOUT,
    CANCELED;

    Color successColor = new Color(52, 123, 49);
    Color failedColor = new Color(201, 67, 17);

    public Color getDisplayBackground() {
        switch (this) {
            case IDLE: return null;
            case WAITING: return null;
            case RUNNING: return Color.yellow;
            case SUCCESS: return successColor;
            case FAILURE: return failedColor;
            case TIMEOUT: return Color.orange;
            case CANCELED: return Color.gray;

            default:
                throw new IllegalArgumentException("status");
        }
    }
}
