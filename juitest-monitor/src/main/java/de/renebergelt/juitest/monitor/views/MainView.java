package de.renebergelt.juitest.monitor.views;

import de.renebergelt.juitest.monitor.controls.AppStateComponent;
import de.renebergelt.juitest.monitor.controls.ConnectionStateControl;
import de.renebergelt.juitest.monitor.controls.TestResultRatioComponent;
import de.renebergelt.juitest.monitor.converters.TimeFormatConverter;
import de.renebergelt.juitest.monitor.renderers.TestExecutionStatusTableCellRenderer;
import de.renebergelt.juitest.monitor.services.IconService;
import de.renebergelt.juitest.monitor.viewmodels.BaseDialog;
import de.renebergelt.juitest.core.utils.NullGuard;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.jdesktop.xbindings.BindingContext;
import org.jdesktop.xbindings.XSelectionBinding;
import org.jdesktop.xbindings.commands.CommandManager;
import org.jdesktop.xbindings.context.DataContext;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MainView extends BaseDialog {

    private static I18n i18n = I18nFactory.getI18n(MainView.class);

    IconService iconService;

    DataContext dataContext;
    BindingContext bindingContext;
    CommandManager commandManager;

    ConnectionStateControl stateControl;

    JComboBox comboTestSet;

    JButton btnAttach;
    JButton btnDetach;

    JButton btnRun;
    JButton btnRunAll;
    JButton btnStop;

    JButton btnReloadTestCases;

    JTable tableTestCases;

    JTextArea txtTestError;
    JList listLogMessages;
    ListDataListener autoScrollListener;

    public MainView(Window owner, DataContext dataContext, IconService iconService) {
        super(true);
        this.dataContext = dataContext;
        this.iconService = NullGuard.forArgument("iconService", iconService);

        // we need the icon service for this component
        // so we cannot create it in customizeStatusbar
        getStatusBar().addStatusComponent(new AppStateComponent(i18n.tr("Ready"), iconService.getIcon("busy")), 0.5);
        getStatusBar().addStatusComponent(new TestResultRatioComponent(), 0.5);

        initComponents();
        initBindings();

        this.pack();
        this.setSize(800, 800);
        setLocationRelativeTo(null);

        this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    }

    @Override
    protected boolean customizeStatusbar() {
        return true;
    }

    private void initComponents() {
        setTitle("jUITest Automation Test Monitor");
        setIconImage(iconService.getIcon("app_icon").getImage());

        getContentPane().setLayout(new MigLayout("fill", "", "[][60%][40%]"));

        // "application under test" state
        JPanel panelConnectionState = new JPanel(new MigLayout("ins 0", "[]push[][]"));
        panelConnectionState.setBorder(BorderFactory.createTitledBorder(i18n.tr("State") + " "));

        stateControl = new ConnectionStateControl();
        panelConnectionState.add(stateControl);

        btnAttach = new JButton(i18n.tr("Connect"));
        panelConnectionState.add(btnAttach);
        btnDetach = new JButton(i18n.tr("Disconnect"));
        panelConnectionState.add(btnDetach);

        getContentPane().add(panelConnectionState, "pushx, growx, wrap");

        JPanel panelTestCases = new JPanel(new MigLayout("ins 0"));
        panelTestCases.setBorder(BorderFactory.createTitledBorder(i18n.tr("Test cases") + " "));

        // test sets
        comboTestSet = new JComboBox();
        panelTestCases.add(comboTestSet, "pushx, growx, wrap");

        // button panel
        JPanel panelButtons = new JPanel(new MigLayout("ins 0 2", "[][]20px[]push[]"));
        panelTestCases.add(panelButtons, "growx, wrap");

        btnRun = new JButton(i18n.tr("Run"));
        btnRun.setIcon(iconService.getIcon("run"));
        panelButtons.add(btnRun);

        btnRunAll = new JButton(i18n.tr("Run all"));
        btnRunAll.setIcon(iconService.getIcon("run-all"));
        panelButtons.add(btnRunAll);

        btnStop = new JButton(i18n.tr("STOP"));
        btnStop.setIcon(iconService.getIcon("stop"));
        panelButtons.add(btnStop, "push");

        btnReloadTestCases = new JButton(i18n.tr("Reload test case"));
        btnReloadTestCases.setIcon(iconService.getIcon("refresh"));
        panelButtons.add(btnReloadTestCases, "");

        // tests table
        tableTestCases = new JTable();
        JScrollPane scrollPaneTestCases = new JScrollPane(tableTestCases);
        panelTestCases.add(scrollPaneTestCases, "wmin 200, hmin 100, push, grow, wrap");

        getContentPane().add(panelTestCases, "push, grow, wrap");

        // detail pane
        JPanel panelDetails = new JPanel(new MigLayout("ins 0", "[50%][50%]"));
        panelDetails.setBorder(BorderFactory.createTitledBorder(i18n.tr("Test details")));

        panelDetails.add(new JLabel(i18n.tr("Exception information (if test has failed):")), "");
        panelDetails.add(new JLabel(i18n.tr("Log messages:")), "wrap");

        txtTestError = new JTextArea();
        txtTestError.setEditable(false);
        JScrollPane scrollPaneTestError = new JScrollPane(txtTestError);
        panelDetails.add(scrollPaneTestError, "hmin 100, push, grow");

        listLogMessages = new JList();
        JScrollPane scrollPaneLogMessages = new JScrollPane(listLogMessages);

        autoScrollListener = new ListDataListener() {

            private void scrollToBottom() {
                int lastIdx = listLogMessages.getModel().getSize() - 1;
                if (lastIdx >= 0) {
                    SwingUtilities.invokeLater(() -> listLogMessages.ensureIndexIsVisible(lastIdx));
                }
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                scrollToBottom();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) { }

            @Override
            public void contentsChanged(ListDataEvent e) {
                scrollToBottom();
            }
        };

        // catch the ListBinding.Model to register for data events
        listLogMessages.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("model".equals(evt.getPropertyName())) {
                    if (evt.getOldValue() != null) {
                        ((ListModel) evt.getOldValue()).removeListDataListener(autoScrollListener);
                    }
                    if (evt.getNewValue() != null) {
                        ((ListModel) evt.getNewValue()).addListDataListener(autoScrollListener);
                    }
                }
            }
        });

        panelDetails.add(scrollPaneLogMessages, "hmin 100, push, grow");

        getContentPane().add(panelDetails, "grow");
    }

    private void initBindings() {
        bindingContext = new BindingContext();
        commandManager = new CommandManager();

        bindingContext.bind(dataContext.path("attached"), stateControl, "connected");

        JComboBoxBinding cb = SwingBindings.createJComboBoxBinding(AutoBinding.UpdateStrategy.READ, dataContext.path("testSets"), comboTestSet);
        bindingContext.register(cb);
        bindingContext.bind(dataContext.path("selectedTestSet"), comboTestSet, "selectedItem", AutoBinding.UpdateStrategy.READ_WRITE);

        JTableBinding tb = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ, dataContext.path("tests"), tableTestCases);
        tb.addColumnBinding(BeanProperty.create("name")).setColumnName(i18n.tr("Name"));
        tb.addColumnBinding(BeanProperty.create("status")).setColumnName(i18n.tr("State")).setRenderer(new TestExecutionStatusTableCellRenderer());
        tb.addColumnBinding(BeanProperty.create("executionTime")).setColumnName(i18n.tr("Duration")).setConverter(new TimeFormatConverter());
        bindingContext.register(tb);

        fixColumnWidth(tableTestCases.getColumnModel().getColumn(1), 140);
        fixColumnWidth(tableTestCases.getColumnModel().getColumn(2), 60);
        tableTestCases.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        bindingContext.register(XSelectionBinding.bindMultiSelection(dataContext.path("tests"), (ObservableList)dataContext.path("selectedTests").getValue(), tableTestCases));

        bindingContext.bind(dataContext.path("stateText"), getStatusBar().getStatusComponent(AppStateComponent.class), "statusText", AutoBinding.UpdateStrategy.READ);
        bindingContext.bind(dataContext.path("running"), getStatusBar().getStatusComponent(AppStateComponent.class), "busy", AutoBinding.UpdateStrategy.READ);

        bindingContext.bind(dataContext.path("singleSelectedTest.errorText"), txtTestError, "text", AutoBinding.UpdateStrategy.READ);

        dataContext.path("testStatus").addPropertyStateListener((pse) -> {
            if (pse.getNewValue() instanceof  int[]) {
                int[] v = (int[])pse.getNewValue();
                getStatusBar().getStatusComponent(TestResultRatioComponent.class).setCounts(v[0], v[1], v[2]);
            }
        });

        JListBinding lb = SwingBindings.createJListBinding(dataContext.path("singleSelectedTest.logMessages"), listLogMessages);
        bindingContext.register(lb);

        commandManager.bind(btnAttach, dataContext.path("attachCommand"));
        commandManager.bind(btnDetach, dataContext.path("detachCommand"));

        commandManager.bind(btnRunAll, dataContext.path("runAllCommand"));
        commandManager.bind(btnRun, dataContext.path("runCommand"));
        commandManager.bind(btnStop, dataContext.path("stopCommand"));

        commandManager.bind(btnReloadTestCases, dataContext.path("reloadTestCasesCommand"));

        commandManager.start();
    }

    private void fixColumnWidth(TableColumn column, int width) {
        column.setWidth(width);
        column.setMaxWidth(width);
        column.setMinWidth(width);
        column.setPreferredWidth(width);

    }

}
