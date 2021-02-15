package de.renebergelt.juitest.monitor;

import de.renebergelt.juitest.monitor.config.TestMonitorConfiguration;
import de.renebergelt.juitest.monitor.config.TestMonitorConfigurationReader;
import de.renebergelt.juitest.monitor.services.DefaultDialogService;
import de.renebergelt.juitest.monitor.services.DefaultIconService;
import de.renebergelt.juitest.monitor.services.IconService;
import de.renebergelt.juitest.monitor.services.RemoteTestRunnerService;
import de.renebergelt.juitest.monitor.viewmodels.MainViewModel;
import de.renebergelt.juitest.monitor.views.MainView;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;
import org.xnap.commons.i18n.I18nManager;

import javax.swing.*;
import java.util.Locale;

public class TestMonitor {

    static DataContext mainDataContext;
    static IconService iconService = new DefaultIconService();
    static MainView mainView;

    private static void setLookAndFeel(String lookAndFeelName) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lookAndFeelName.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // requested LaF is not available
        }
    }

    private static TestMonitorConfiguration loadConfig() {
        TestMonitorConfigurationReader config = new TestMonitorConfigurationReader();
        return config.readFromFile("test-files/test-config.yaml");
    }

    public static void main(String[] args) {

        StarterOptions opt = StarterOptions.readFromArgs(args);
        org.apache.log4j.Logger.getLogger("io.netty").setLevel(org.apache.log4j.Level.ERROR);

        org.jdesktop.beansbinding.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.SEVERE);
        setLookAndFeel("Nimbus");

        TestMonitorConfiguration config = loadConfig();
        if (!"sys".equals(config.getLanguage())) {
            Locale loc = Locale.forLanguageTag(config.getLanguage());

            I18nManager.getInstance().setDefaultLocale(loc);
            Locale.setDefault(loc);
        }

        // create the GUI in the EDT
        SwingUtilities.invokeLater(() -> {
            DefaultDialogService dialogService = new DefaultDialogService();
            mainDataContext =new BeansDataContext(new MainViewModel(config, dialogService, new RemoteTestRunnerService(opt.host, opt.port)));
            mainView = new MainView(null,mainDataContext, iconService);
            dialogService.setMainWindow(mainView);
            mainView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainView.setVisible(true);
        });
    }
}