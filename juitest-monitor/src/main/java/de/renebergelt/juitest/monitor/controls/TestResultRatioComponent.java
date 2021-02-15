package de.renebergelt.juitest.monitor.controls;

import de.renebergelt.juitest.monitor.viewmodels.TestExecutionStatus;

import javax.swing.*;
import java.awt.*;

public class TestResultRatioComponent extends StatusBarComponent {

    protected RatioPanel panel = new RatioPanel();

    public TestResultRatioComponent() {
        super();

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
    }

    public void setCounts(int succeeded, int failed, int waiting) {
        panel.setCounts(succeeded, failed, waiting);
    }
}

class RatioPanel extends JPanel {

    protected int succeeded = 0;
    protected int failed = 0;
    protected int waiting = 0;

    Color waitingColor = Color.lightGray;

    public void setCounts(int succeeded, int failed, int waiting) {
        this.succeeded = succeeded;
        this.failed = failed;
        this.waiting = waiting;

        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int total = succeeded + failed + waiting;

        if (total == 0) {
            super.paintComponent(g);
        } else {
            Graphics2D g2d = (Graphics2D)g;
            int w = getWidth();

            g2d.setColor(TestExecutionStatus.SUCCESS.getDisplayBackground());
            int pw = (int) (succeeded * w / total);
            g2d.fillRect(0, 0, pw, getHeight());

            g2d.setColor(waitingColor);
            int pw2 = (int) ((succeeded + waiting) * w / total);
            g2d.fillRect(pw, 0, pw2 - pw, getHeight());

            g2d.setColor(TestExecutionStatus.FAILURE.getDisplayBackground());
            pw = (int) ((succeeded + waiting) * w / total);
            g2d.fillRect(pw, 0, w - pw, getHeight());
        }
    }
}
