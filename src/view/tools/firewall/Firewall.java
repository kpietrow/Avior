package view.tools.firewall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.overview.Port;
import model.overview.Switch;
import model.tools.firewall.Rule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.swt.layout.grouplayout.GroupLayout;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Button;

import controller.floodlightprovider.FloodlightProvider;
import controller.tools.firewall.json.FirewallJSON;
import controller.tools.firewall.push.FirewallPusher;
import controller.tools.firewall.table.RuleToTable;

import controller.util.JSONException;

import view.About;
import view.util.DisplayMessage;

import org.eclipse.swt.widgets.Combo;

public class Firewall {

    private static Shell shell;
    public Tree tree_rules;
    protected Table table_rule;
    protected TableEditor editor;
    final int EDITABLECOLUMN = 1;
    public static Switch currSwitch;
    public static Rule rule;
    protected List<Switch> switches = new ArrayList<Switch>();
    protected static List<Rule> rules = new ArrayList<Rule>();
    protected static boolean unsavedProgress;
    protected Button btnEnable;
    private static Text name, priority;
    private static Combo dpid, port, action;

    /**
     * Launch the application.
     * 
     * @param args
     */

    public Firewall() {
        open();
    }

    public void open() {
        Display display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
    
    public static Shell getShell(){
        return shell;
    }

    public void disposeEditor() {
        // Dispose the editor do it doesn't leave a ghost table item
        if (editor.getEditor() != null) {
            editor.getEditor().dispose();
        }
    }


    private void populateRuleTree() {

        // Clear the trees of any old data
        table_rule.removeAll();
        tree_rules.removeAll();
        rule = null;
        
        name.setText("");
        dpid.setText("");
        port.setText("");
        action.setText("");
        priority.setText("");

        rules = FloodlightProvider.getRules();

        for (Rule r : rules) {
            new TreeItem(tree_rules, SWT.NONE).setText(String.valueOf(r
                    .getRuleid()));
        }
    }

    private void viewRule(int index) {

        disposeEditor();
        table_rule.removeAll();
        rule = rules.get(index);
        
        name.setText(rule.getName());
        dpid.setText(rule.getDpid());
        port.setText(rule.getIn_port());
        action.setText(rule.getAction());
        priority.setText(rule.getPriority());
        
        for (String[] row : RuleToTable.getRuleTableFormat(rule)) {
            new TableItem(table_rule, SWT.NONE).setText(row);
        }
    }

    // This method creates a new flow with default values.
    public void setupNewRule() {

        disposeEditor();
        rule = new Rule();
        table_rule.removeAll();
        name.setText("");
        dpid.setText("");
        port.setText("");
        action.setText("");
        priority.setText("");

        List<String> dpids = FloodlightProvider.getSwitchDpids();
        dpid.setItems(dpids.toArray(new String[dpids.size()]));
        for (String[] row : RuleToTable.getNewRuleTableFormat()) {
            new TableItem(table_rule, SWT.NONE).setText(row);
        }
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shell = new Shell();
        shell.setSize(1200, 409);
        shell.setText("Floodlight Firewall");

        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // Create an "are you sure" box in the event that there is
                // unsaved progress
                if (unsavedProgress) {
                    int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
                    MessageBox messageBox = new MessageBox(shell, style);
                    messageBox.setText("Are you sure?!");
                    messageBox
                            .setMessage("Are you sure you wish to exit the firewall? Any unsaved changes will not be pushed.");
                    event.doit = messageBox.open() == SWT.YES;
                    unsavedProgress = false;
                }
            }
        });

        MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
        mntmNewSubmenu.setText("File");

        Menu menu_1 = new Menu(mntmNewSubmenu);
        mntmNewSubmenu.setMenu(menu_1);

        MenuItem mntmClose = new MenuItem(menu_1, SWT.NONE);
        mntmClose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.dispose();
            }
        });
        mntmClose.setText("Close");

        MenuItem mntmAbout = new MenuItem(menu, SWT.CASCADE);
        mntmAbout.setText("Help");

        Menu menu_2 = new Menu(mntmAbout);
        mntmAbout.setMenu(menu_2);

        MenuItem mntmInfo = new MenuItem(menu_2, SWT.NONE);
        mntmInfo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new About();
            }
        });
        mntmInfo.setText("About");

        Composite composite = new Composite(shell, SWT.NONE);
        GroupLayout gl_shell = new GroupLayout(shell);
        gl_shell.setHorizontalGroup(gl_shell.createParallelGroup(
                GroupLayout.TRAILING).add(GroupLayout.LEADING, composite,
                GroupLayout.DEFAULT_SIZE, 1198, Short.MAX_VALUE));
        gl_shell.setVerticalGroup(gl_shell.createParallelGroup(
                GroupLayout.LEADING).add(
                gl_shell.createSequentialGroup()
                        .add(composite, GroupLayout.PREFERRED_SIZE, 352,
                                GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(36, Short.MAX_VALUE)));
        composite.setLayout(null);

        Composite composite_1 = new Composite(composite, SWT.NONE);
        composite_1.setBounds(210, 42, 978, 307);
        composite_1.setLayout(null);

        table_rule = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.NO_FOCUS);
        table_rule.setBounds(0, 43, 978, 264);
        table_rule.setHeaderVisible(true);
        table_rule.setLinesVisible(true);
        table_rule.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                disposeEditor();

                // Identify the selected row
                TableItem item = (TableItem) e.item;
                if (item == null)
                    return;

                // The control that will be the editor must be a child of the
                // Table
                Text newEditor = new Text(table_rule, SWT.NONE);
                newEditor.setText(item.getText(EDITABLECOLUMN));
                newEditor.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent me) {
                        Text text = (Text) editor.getEditor();
                        editor.getItem()
                                .setText(EDITABLECOLUMN, text.getText());
                        unsavedProgress = true;
                    }
                });
                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item, EDITABLECOLUMN);
            }
        });

        editor = new TableEditor(table_rule);
        // The editor must have the same size as the cell and must
        // not be any smaller than 50 pixels.
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        table_rule.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                disposeEditor();

                // Identify the selected row
                TableItem item = (TableItem) e.item;
                if (item == null)
                    return;

                // The control that will be the editor must be a child of the
                // Table
                Text newEditor = new Text(table_rule, SWT.NONE);
                newEditor.setText(item.getText(EDITABLECOLUMN));
                newEditor.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent me) {
                        Text text = (Text) editor.getEditor();
                        editor.getItem()
                                .setText(EDITABLECOLUMN, text.getText());
                        unsavedProgress = true;
                    }
                });
                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item, EDITABLECOLUMN);
            }
        });

        TableColumn param = new TableColumn(table_rule, SWT.NONE);
        param.setWidth(300);
        param.setText("Parameter");

        TableColumn value = new TableColumn(table_rule, SWT.NONE);
        value.setWidth(300);
        value.setText("Value");

        TableColumn wildcard = new TableColumn(table_rule, SWT.NONE);
        wildcard.setWidth(300);
        wildcard.setText("Wildcard");

        Label lblName = new Label(composite_1, SWT.NONE);
        lblName.setBounds(0, 10, 45, 17);
        lblName.setText("Name");

        name = new Text(composite_1, SWT.BORDER);
        name.setBounds(51, 5, 139, 27);

        Label lblSwitch = new Label(composite_1, SWT.NONE);
        lblSwitch.setBounds(196, 10, 50, 17);
        lblSwitch.setText("Switch");

        dpid = new Combo(composite_1, SWT.NONE);
        dpid.setBounds(253, 5, 212, 29);
        dpid.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String selection = dpid
                        .getItem(dpid.getSelectionIndex());
                List<Port> ports = FloodlightProvider.getSwitch(selection, false).getPorts();
                port.setItems(RuleToTable.getPortComboFormat(ports));
            }
        });

        Label lblPort = new Label(composite_1, SWT.NONE);
        lblPort.setBounds(471, 10, 30, 17);
        lblPort.setText("Port");

        port = new Combo(composite_1, SWT.NONE);
        port.setBounds(508, 5, 139, 29);

        Label lblAction = new Label(composite_1, SWT.NONE);
        lblAction.setBounds(653, 10, 50, 17);
        lblAction.setText("Action");

        action = new Combo(composite_1, SWT.NONE);
        action.setBounds(709, 5, 103, 29);
        action.setItems(new String[] { "ALLOW", "DENY" });

        Label lblPriority = new Label(composite_1, SWT.NONE);
        lblPriority.setBounds(831, 10, 56, 17);
        lblPriority.setText("Priority");

        priority = new Text(composite_1, SWT.BORDER);
        priority.setBounds(893, 5, 75, 27);

        Composite composite_2 = new Composite(composite, SWT.NONE);
        composite_2.setBounds(10, 0, 194, 350);

        tree_rules = new Tree(composite_2, SWT.BORDER | SWT.NO_FOCUS | SWT.NONE);
        tree_rules.setBounds(0, 33, 185, 317);
        tree_rules.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] switch_selection = tree_rules.getSelection();
                if (switch_selection.length != 0) {
                    viewRule(tree_rules.indexOf(switch_selection[0]));
                }
            }
        });

        Label lblSwitches = new Label(composite_2, SWT.NONE);
        lblSwitches.setBounds(0, 10, 70, 17);
        lblSwitches.setText("Rules");

        Composite composite_3 = new Composite(composite, SWT.NONE);
        composite_3.setBounds(210, 0, 978, 35);
        composite_3.setLayout(null);

        Button btnRefresh = new Button(composite_3, SWT.NONE);
        btnRefresh.setBounds(3, 3, 125, 29);
        btnRefresh.setText("Refresh");
        btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateRuleTree();
            }
        });

        Button btnNewFLow = new Button(composite_3, SWT.NONE);
        btnNewFLow.setBounds(131, 3, 125, 29);
        btnNewFLow.setText("New Rule");
        btnNewFLow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setupNewRule();
            }
        });

        // Save button logic
        Button btnSave = new Button(composite_3, SWT.NONE);
        btnSave.setBounds(259, 3, 125, 29);
        btnSave.setText("Push");
        btnSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (rule != null) {
                    if (FirewallPusher.errorChecksPassed(table_rule.getItems())) {

                        // Parse the changes made to the flow
                        rule = FirewallPusher.parseTableChanges(rule, table_rule
                                .getItems());

                        rule.setName(name.getText());
                        rule.setDpid(dpid.getText());
                        rule.setIn_port(port.getText());
                        rule.setAction(action.getText());
                        rule.setPriority(priority.getText());

                        System.out.println(rule.serialize());
                        // Push the rule and get the response
                        String response = "";
                        try {
                            response = FirewallPusher.push(rule);
                        } catch (IOException | JSONException e1) {
                            DisplayMessage.displayError(shell,"Problem pushing firewall rule. Check log for details.");
                            e1.printStackTrace();
                        }

                        if (response.equals("Rule successfully pushed!")) {
                            populateRuleTree();
                            unsavedProgress = false;
                        }

                        disposeEditor();

                        // Display the response from pushing the flow
                        DisplayMessage.displayStatus(shell,response);
                    }
                } else {
                    DisplayMessage.displayError(shell,"You do not have a rule to push!");
                }
            }
        });

        Button btnClear = new Button(composite_3, SWT.NONE);
        btnClear.setBounds(387, 3, 125, 29);
        btnClear.setText("Clear");
        btnClear.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setupNewRule();
            }
        });

        Button btnDeleteFlow = new Button(composite_3, SWT.NONE);
        btnDeleteFlow.setBounds(515, 3, 125, 29);
        btnDeleteFlow.setText("Delete Rule");
        btnDeleteFlow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (rule != null) {
                    try {
                        try {
                            String response = FirewallPusher.remove(rule);
                            // If successfully deleted, populate the flow tree
                            // with the new results
                            if (response.equals("Rule deleted"))
                                populateRuleTree();

                            disposeEditor();

                            // Displays the response
                            DisplayMessage.displayStatus(shell,response);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    DisplayMessage.displayError(shell, "You must select a flow to delete!");
                }
            }
        });

        // Delete all flows button logic
        Button btnDeleteAllFlows = new Button(composite_3, SWT.NONE);
        btnDeleteAllFlows.setBounds(643, 3, 125, 29);
        btnDeleteAllFlows.setText("Delete All Rules");
        btnDeleteAllFlows.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // are you sure? warning
                int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
                MessageBox messageBox = new MessageBox(shell, style);
                messageBox.setText("Are you sure?!");
                messageBox
                        .setMessage("Are you sure you wish to delete all rules?");
                if (messageBox.open() == SWT.YES) {
                    FirewallPusher.removeAll();
                    populateRuleTree();
                }
            }
        });

        btnEnable = new Button(composite_3, SWT.NONE);
        try {
            if (FirewallJSON.isEnabled())
                btnEnable.setText("Disable");
            else
                btnEnable.setText("Enable");
        } catch (JSONException | IOException e2) {
            e2.printStackTrace();
        }
        btnEnable.setBounds(774, 3, 125, 29);
        btnEnable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String status = "";
                try {
                    if (!FirewallJSON.isEnabled()) {
                        try {
                            status = FirewallJSON.enable(true);
                            if (status.equals("success")) {
                                btnEnable.setText("Disable");
                            }
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    } else {
                        status = FirewallJSON.enable(false);
                        if (status.equals("success")) {
                            btnEnable.setText("Enable");
                        }
                    }
                } catch (JSONException | IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        // Populate the switch tree with the current switches on the network on
        // construction
        populateRuleTree();
        if(FloodlightProvider.getSwitches(false).isEmpty())
            FloodlightProvider.getSwitches(true);
        shell.setLayout(gl_shell);
    }
}
