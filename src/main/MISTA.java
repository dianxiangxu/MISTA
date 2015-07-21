/*  
	Author Dianxiang Xu 
	2009 - 2014 
*/

package main;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.*;

import testcode.TargetLanguage;
import utilities.FileUtil;

import kernel.Commands;
import kernel.FileManager;
import kernel.Helper;
import kernel.Kernel;
import kernel.RecentFiles;
import kernel.StatusPanel;
import kernel.SystemOptions;
import kernel.TestingManager;
import kernel.VerificationManager;
import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

import edit.XMIDEditor;

public class MISTA extends JFrame implements Kernel {
	private static final long serialVersionUID = 1;

	private static final boolean WindowsLookAndFeel = true;

	private static final boolean IMPORTCPNFILE = false;

	private JFileChooser fc;

	protected RecentFiles recentFiles = new RecentFiles();

	protected FileManager fileManager;
	protected VerificationManager verificationManager;
	protected TestingManager testingManager;
	protected Helper helper;

	protected SystemOptions systemOptions;
	protected StatusPanel statusPanel;
	
	private JMenuBar menuBar = new JMenuBar();
	private	JMenu fileMenu, newItemMenu, recentFilesMenu,
			editMenu, checkMenu, 
			testTreeMenu, editTestMenu,
			helpMenu;
	private JMenuItem saveItem, saveAsItem, refreshItem, clearRecentFilesItem;

	private JMenuItem editingPreferencesItem;
	private JMenu modelMenu, mimMenu, helperCodeMenu;

	private JMenuItem parseItem, simulationItem, integratedGoalReachabilityAnalysisItem, transitionReachabilityAnalysisItem, deadlockStateItem, assertionVerificationItem;
	
	private JMenuItem generateTestTreeItem, generateTestCodeItem,
//			addNodeItem, editNodeItem,
			deleteNodeItem, cloneSubtreeItem,  			
			moveNodeItem, saveTestTreeItem, closeTestTreeItem, printTestTreeItem, 
			onlineTestExecutionItem, onTheFlyTestingItem, analyzeOnTheFlyTestsItem; 

	private JMenuItem showSequencesItem;
	
	private ToolBar toolBar;
	
	public MISTA() {
		super();
		initOptions();
		LocaleBundle.setResourceBundle();
		setTitle(SYSTEM_NAME);
		loadSystemIcon();
		setMainFrameSize();	
		createInfoPanel();
		createListeners(); 
		createMenus();
		createMenuBar();
		createToolBar();
		setMainContentPane();
		setMainFrameListener();
  		fileManager.startEditor(recentFiles.getMostRecentFile());
	}

	private void loadSystemIcon(){
		ClassLoader classLoader = this.getClass().getClassLoader();
        URL imageResource = classLoader.getResource("images/ista.png");
        if (imageResource != null) {
			Image SystemIcon = Toolkit.getDefaultToolkit().getImage(imageResource);
			this.setIconImage(SystemIcon);
		}
    }
	
	private void setMainFrameSize() {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		setMaximizedBounds(e.getMaximumWindowBounds());
		setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
		setPreferredSize(new Dimension(e.getMaximumWindowBounds().width, e.getMaximumWindowBounds().height));
		setBackground(Color.white);
	 }
	
	private void setMainFrameListener() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
		        	fileManager.quit();
		    }
		});
	}
	
	private void createListeners(){
		fileManager = new FileManager(this);
		verificationManager = new VerificationManager(this);
		testingManager = new TestingManager(this);
		helper = new Helper();
	}
	
	private void createToolBar() {
	    toolBar = new ToolBar(this);	    
	}
	
	public ToolBar getToolBar(){
		return toolBar;
	}
	
	private void createInfoPanel(){
		statusPanel = new InfoPanel();
	}
	
	// overridden
	public void showSystemOptionsInInfoPanel(){
//		String message = "[Search Depth:"+systemOptions.getSearchDepth()+"]";
//		infoPanel.setMessage(message);
		if (fileManager.getEditor()!=null){
			ModelType modelType = fileManager.getEditor().getModelType();
			String message = modelType!=null? LocaleBundle.bundleString("Model Type")+": "+LocaleBundle.bundleString(SystemOptions.getModelTypeString(modelType)): "";
			if (!fileManager.getEditor().isEditing())
				message += " [ "+LocaleBundle.bundleString("Read Only")+" ]";
			statusPanel.setMessage(message);
		} else
		statusPanel.setMessage("");
	}
		
	private void createMenus() {
		createFileMenu();
		createEditMenu();
		createCheckingMenu();
		createTestTreeMenu();
		createHelpMenu();
	}
	
	private void createMenuBar() {
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(checkMenu);
		menuBar.add(testTreeMenu);
		menuBar.add(helpMenu);

		fileMenu.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
		editMenu.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
		checkMenu.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
		testTreeMenu.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
		helpMenu.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
		
		setJMenuBar(menuBar);
	}
	
	private JMenuItem createMenuItem(JMenu menu, String imageFile, String title, String command, ActionListener listener){
		JMenuItem menuItem = createMenuItem(menu, title, command, listener);
	    ClassLoader classLoader = this.getClass().getClassLoader();
	    URL imageResource = classLoader.getResource(imageFile);
	    if (imageResource != null) 
	        menuItem.setIcon(new ImageIcon(imageResource));
		return menuItem;
	}
    
	private JMenuItem createMenuItem(JMenu menu, String title, String command, ActionListener listener){
		JMenuItem menuItem = menu.add(title);
		menuItem.setActionCommand(command);
		menuItem.setName(command);		// use the command to name the menu item
		menuItem.addActionListener(listener);
		return menuItem;
	}
	
/*	private JMenuItem createMenuItem(JMenu menu, String title, String command, ActionListener listener, char key){
		JMenuItem menuItem = createMenuItem(menu, title, command, listener); 
		menuItem.setAccelerator(KeyStroke.getKeyStroke(key, Event.CTRL_MASK));
		return menuItem;
	}
*/
	private JMenuItem createMenuItem(JMenu menu, String imageFile, String title, String command, ActionListener listener, char key){
		JMenuItem menuItem = createMenuItem(menu, imageFile, title, command, listener); 
		menuItem.setAccelerator(KeyStroke.getKeyStroke(key, Event.CTRL_MASK));
		return menuItem;
	}

	private void createFileMenu() {
		fileMenu = new JMenu(LocaleBundle.bundleString("File"));
		fileMenu.setMnemonic('F');

		if (SystemOptions.ALL_MODEL_TYPES.length==1)
			createMenuItem(fileMenu, "images/new.gif", LocaleBundle.bundleString("New"), SystemOptions.FUNCTIONNET_KEYWORD, fileManager);
		else
			createNewItemMenu();		
		createMenuItem(fileMenu, "images/open.gif",LocaleBundle.bundleString("Open"), Commands.OPEN, fileManager);
		refreshItem = createMenuItem(fileMenu, "images/refresh.gif", LocaleBundle.bundleString("Refresh"), Commands.REFRESH, fileManager);
		saveItem = createMenuItem(fileMenu, "images/save.gif", LocaleBundle.bundleString("Save"), Commands.SAVE, fileManager, 'S');
		saveAsItem = createMenuItem(fileMenu, "images/saveas.gif", LocaleBundle.bundleString("Save As"), Commands.SAVEAS, fileManager);
		fileMenu.addSeparator();
		createRecentFilesMenu();
		fileMenu.addSeparator();
		if (IMPORTCPNFILE){
			createMenuItem(fileMenu, LocaleBundle.bundleString("Import PNML File"), Commands.IMPORTPNMLFILE, fileManager);
			createMenuItem(fileMenu, LocaleBundle.bundleString("Import CPN File"), Commands.IMPORTCPNFILE, fileManager);
			fileMenu.addSeparator();
		}
		createMenuItem(fileMenu, LocaleBundle.bundleString("Exit"), Commands.EXIT, fileManager);
	}

	private void createNewItemMenu() {
		newItemMenu = new JMenu(LocaleBundle.bundleString("New"));
		for (String modelTypeString: SystemOptions.ALL_MODEL_TYPES)
			createMenuItem(newItemMenu, LocaleBundle.bundleString(modelTypeString), modelTypeString, fileManager);
		fileMenu.add(newItemMenu);
	}

	private void createRecentFilesMenu() {
		recentFilesMenu = new JMenu(LocaleBundle.bundleString("Recent Files"));
		for (String filePath: recentFiles.getRecentFilePaths()){
			createMenuItem(recentFilesMenu, filePath, filePath, fileManager);
		}
		recentFilesMenu.addSeparator();
		clearRecentFilesItem = createMenuItem(recentFilesMenu, LocaleBundle.bundleString("Clear File List"), Commands.CLEARFILELIST, fileManager);
		fileMenu.add(recentFilesMenu);
		recentFilesMenu.setEnabled(recentFiles.hasRecentFiles());
	}

	public void updateRecentFilesMenu(){
		if (recentFilesMenu!=null){
			recentFilesMenu.removeAll();
			for (String filePath: recentFiles.getRecentFilePaths())
				createMenuItem(recentFilesMenu, filePath, filePath, fileManager);
			recentFilesMenu.addSeparator();
			clearRecentFilesItem = createMenuItem(recentFilesMenu, LocaleBundle.bundleString("Clear File List"), Commands.CLEARFILELIST, fileManager);
			recentFilesMenu.setEnabled(recentFiles.hasRecentFiles());
			recentFilesMenu.updateUI();
			fileMenu.updateUI();
		}
	}
	
	public void clearRecentFilesMenu(){
		recentFiles.getRecentFilePaths().clear();
		recentFilesMenu.setEnabled(false);
		clearRecentFilesItem.setEnabled(false);
		recentFilesMenu.removeAll();
		recentFilesMenu.updateUI();
		fileMenu.updateUI();
	}
	
	private void createEditMenu() {
		editMenu = new JMenu(LocaleBundle.bundleString("Edit"));
		editMenu.setMnemonic('E');
		
		modelMenu = new JMenu(LocaleBundle.bundleString("Model"));
		editMenu.add(modelMenu);
		
		mimMenu = new JMenu(LocaleBundle.bundleString("MIM"));
		editMenu.add(mimMenu);
		
		helperCodeMenu = new JMenu(LocaleBundle.bundleString("Helper Code"));
		editMenu.add(helperCodeMenu);
		
		editMenu.addSeparator();
		editingPreferencesItem = createMenuItem(editMenu, LocaleBundle.bundleString("Preferences"), Commands.EDITINGPREFERENCES, fileManager);
	}

	public void updateModelMenu(JMenu newModelMenu){
		
		modelMenu = newModelMenu;

		if (editMenu!=null){
			editMenu.removeAll();
			editMenu.add(modelMenu);
			editMenu.add(mimMenu);
			editMenu.add(helperCodeMenu);
			editMenu.addSeparator();
			editMenu.add(editingPreferencesItem);
				
			enableEditMenuItems();
		
			editMenu.updateUI();
		}
	}

	public void updateEditMenu(JMenu newModelMenu, JMenu newMIMMenu, JMenu newHelperCodeMenu){
		
		modelMenu = newModelMenu;
		mimMenu = newMIMMenu;
		helperCodeMenu = newHelperCodeMenu;

		if (editMenu!=null){
			editMenu.removeAll();
			editMenu.add(modelMenu);
			editMenu.add(mimMenu);
			editMenu.add(helperCodeMenu);
			editMenu.addSeparator();
			editMenu.add(editingPreferencesItem);
				
			enableEditMenuItems();
		
			editMenu.updateUI();
		}
	}
	
	private void createCheckingMenu() {
		checkMenu = new JMenu(LocaleBundle.bundleString("Analysis"));
		checkMenu.setMnemonic('C');
		parseItem = createMenuItem(checkMenu,"images/parse.gif", LocaleBundle.bundleString("Compile"), Commands.PARSE, fileManager);
		simulationItem = createMenuItem(checkMenu, "images/simulation.gif", LocaleBundle.bundleString("Simulate"), Commands.SIMULATION, fileManager);
		integratedGoalReachabilityAnalysisItem = createMenuItem(checkMenu, "images/check.gif", LocaleBundle.bundleString("Verify Goal State Reachability"), Commands.IntegratedGoalReachabilityAnalysis, verificationManager);
		transitionReachabilityAnalysisItem = createMenuItem(checkMenu, LocaleBundle.bundleString("Verify Transition Reachability"), Commands.TransitionReachabilityAnalysis, verificationManager);
		deadlockStateItem = createMenuItem(checkMenu, LocaleBundle.bundleString("Check Deadlock States"), Commands.DEADLOCKSTATES, verificationManager);
		assertionVerificationItem = createMenuItem(checkMenu, LocaleBundle.bundleString("CHECK_ASSERTIONS"), Commands.CHECKASSERTIONS, verificationManager);
	}

	private void createTestTreeMenu() {
		testTreeMenu = new JMenu(LocaleBundle.bundleString("Test"));
		testTreeMenu.setMnemonic('T');

		generateTestCodeItem = createMenuItem(testTreeMenu, "images/code.gif", LocaleBundle.bundleString("Generate Test Code"), Commands.GenerateTestCode, testingManager);
		generateTestTreeItem = createMenuItem(testTreeMenu, "images/tree.gif", LocaleBundle.bundleString("Generate Test Tree"), Commands.GenerateTree, testingManager);
		createMenuItem(testTreeMenu,  "images/options.gif", LocaleBundle.bundleString("TESTING_OPTIONS"), Commands.SetSystemOptions, testingManager);

		testTreeMenu.addSeparator();
		editTestMenu = new JMenu(LocaleBundle.bundleString("Edit Test Tree"));
//		addNodeItem = createMenuItem(editTestMenu, LocaleBundle.getBundleString("Add Child Node"), Commands.AddTreeNode, testingManager);
		deleteNodeItem = createMenuItem(editTestMenu, LocaleBundle.bundleString("Delete Node"), Commands.DeleteTreeNode, testingManager);
		cloneSubtreeItem = createMenuItem(editTestMenu, LocaleBundle.bundleString("Clone Node"), Commands.CloneTreeNode, testingManager);
//		editNodeItem = createMenuItem(editTestMenu, LocaleBundle.getBundleString("Modify Node"), Commands.EditTreeNode, testingManager);
		moveNodeItem = createMenuItem(editTestMenu, LocaleBundle.bundleString("Move Node"), Commands.MoveTreeNode, testingManager);
		testTreeMenu.add(editTestMenu);
		
		saveTestTreeItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("Save Test Tree"), Commands.SaveTree, testingManager);
		printTestTreeItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("Print Test Tree"), Commands.PrintTree, testingManager);
		closeTestTreeItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("Close Test Tree"), Commands.CloseTree, testingManager);
		showSequencesItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("Show Test Sequences"), Commands.ShowSequences, testingManager);
		
		onlineTestExecutionItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("Online_Execution"), Commands.OnlineExecution, testingManager);

		testTreeMenu.addSeparator();
		onTheFlyTestingItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("On The Fly Testing"), Commands.OnTheFlyTesting, testingManager);
		analyzeOnTheFlyTestsItem = createMenuItem(testTreeMenu, LocaleBundle.bundleString("ANALYZE_ON_THE_FLY_TESTS"), Commands.AnalyzeOnTheFlyTests, testingManager);
	}

	/*
	private JCheckBoxMenuItem createCheckBoxMenuItem(JMenu menu, String title, String command, boolean selected, ActionListener listener){
		JCheckBoxMenuItem checkBoxItem = new JCheckBoxMenuItem(title);
		checkBoxItem.setSelected(selected);
		menu.add(checkBoxItem);
		checkBoxItem.setActionCommand(command);
		checkBoxItem.addActionListener(listener);
		return checkBoxItem;
	} 
	*/
	
	private void createHelpMenu() {
		helpMenu = new JMenu(LocaleBundle.bundleString("Help"));
		helpMenu.setMnemonic('H');
		createMenuItem(helpMenu, LocaleBundle.bundleString("About_"+SYSTEM_NAME), Commands.About, helper);
	}

	public void setSystemTitle(String message) {
		String title = SYSTEM_NAME;
		if (message!=null && !message.equals(""))
			title = message + " - " + title;
		this.setTitle(title);
	}

	private void setMainContentPane(){
		JPanel newContentPane = new JPanel();
		newContentPane.setLayout(new BorderLayout());
		newContentPane.add(toolBar, BorderLayout.NORTH);
		JSplitPane editingComponent = fileManager.getEditor().getMainInterface();
		newContentPane.add(editingComponent, BorderLayout.CENTER);
		newContentPane.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setPreferredLabelSize();
		showSystemOptionsInInfoPanel();
		setContentPane(newContentPane);
		pack();
		editingComponent.setDividerLocation(0.85);
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public VerificationManager getVerificationManager() {
		return verificationManager;
	}

	public TestingManager getTestingManager() {
		return testingManager;
	}

	public SystemOptions getSystemOptions(){
		return systemOptions;
	}
	
	protected void initOptions() {
		fc = new JFileChooser();
		fc.setCurrentDirectory(FileUtil.getCurrentDirectory());
		TargetLanguage.initializeTestFrameworkList(this);
		systemOptions = SystemOptions.readSystemOptionsFromFile();
	}
	
	public JFrame getParentFrame(){
		return this;
	}

	public JFileChooser getFileChooser() {
		return fc;
	}
	
	public RecentFiles getRecentFiles(){
		return recentFiles;
	}
	
	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

	public void printDialogMessage(String output) {
		JOptionPane.showMessageDialog(null, output);
	}
	
	public void printToConsole(String output){
		if (fileManager.getEditor()!=null)
			fileManager.getEditor().printInConsoleArea(output);	
	}

	public void updateContentPane() {
		fileManager.updateSystemTitle();
		setMainContentPane();
	 }	 

	public void updateToolBar(JToolBar additionalToolBar){
		if (toolBar!=null){
			toolBar.updateTooBarComponents(additionalToolBar);
			toolBar.updateUI();
		}
	}
	
	private void enableFileMenuItems(){
		boolean isEditable = fileManager.getEditor().isEditing();
		boolean isTestTreeFile =  fileManager.isWorkingFileTestTree();
		refreshItem.setEnabled(fileManager.workingFileExists() && !isTestTreeFile);
		saveItem.setEnabled(isEditable); 
		saveAsItem.setEnabled(isEditable);
		clearRecentFilesItem.setEnabled(recentFiles.hasRecentFiles());
	}
	
	private void enableEditMenuItems(){
		if (fileManager.getEditor() instanceof XMIDEditor){
			XMIDEditor editor = (XMIDEditor)fileManager.getEditor() ;
			modelMenu.setEnabled(editor.isModelPanelSelected() || editor.isSubModelSelected());
			mimMenu.setEnabled(editor.isMIMPanelSelected());
			helperCodeMenu.setEnabled(editor.isHelperCodePanelSelected());			
		}
		else {
			modelMenu.setEnabled(false);
			mimMenu.setEnabled(false);
			helperCodeMenu.setEnabled(false);
		}
	}
	
	private void enableCompileMenuItems() {
		ModelType modelType = fileManager.getEditor().getModelType();
		boolean isAnalysisApplicable = modelType!=null && modelType!= ModelType.THREATTREE;
		parseItem.setEnabled(modelType!=null);
		simulationItem.setEnabled(fileManager.getEditor().hasSimulator());
		integratedGoalReachabilityAnalysisItem.setEnabled(isAnalysisApplicable);
		transitionReachabilityAnalysisItem.setEnabled(isAnalysisApplicable);
		deadlockStateItem.setEnabled(isAnalysisApplicable);
		assertionVerificationItem.setEnabled(isAnalysisApplicable);
	}

	void enableTestMenuItems() {
		boolean hasFileForTestGeneration = fileManager.hasWorkingFile() && FileUtil.isTestGenerationFile(fileManager.getWorkingFile());
		boolean hasTestTree = fileManager.getEditor().hasTestTree();
		boolean hasTestCode = fileManager.getEditor().hasTestCodeComponent();

		generateTestCodeItem.setEnabled((hasFileForTestGeneration || hasTestTree) && !hasTestCode 
				&& systemOptions.getLanguage()!=TargetLanguage.RPC
				&& systemOptions.getLanguage()!=TargetLanguage.SELENIUMDRIVER
				);
		generateTestTreeItem.setEnabled(!hasTestTree && hasFileForTestGeneration);
			
		boolean isTreeSelected = hasTestTree && fileManager.getEditor().isTestTreeSelected();
		
		editTestMenu.setEnabled(isTreeSelected);
		enableTestTreeEditMenuItems(isTreeSelected);

		showSequencesItem.setEnabled(isTreeSelected);
		saveTestTreeItem.setEnabled(isTreeSelected);
		closeTestTreeItem.setEnabled(isTreeSelected && !FileUtil.isTestDataFile(fileManager.getWorkingFile()));
		printTestTreeItem.setEnabled(isTreeSelected);
		
		onlineTestExecutionItem.setEnabled(hasTestTree && fileManager.getEditor().hasSimulator() && 
				(systemOptions.getLanguage()==TargetLanguage.RPC || systemOptions.getLanguage()==TargetLanguage.SELENIUMDRIVER)
				);
		onTheFlyTestingItem.setEnabled(fileManager.getEditor().hasSimulator() &&
				(systemOptions.getLanguage()==TargetLanguage.RPC || systemOptions.getLanguage()==TargetLanguage.SELENIUMDRIVER)
				);
		analyzeOnTheFlyTestsItem.setEnabled(fileManager.getEditor().hasSimulator() && 
				(systemOptions.getLanguage()==TargetLanguage.RPC || systemOptions.getLanguage()==TargetLanguage.SELENIUMDRIVER)
				);
	}
	
	public void enableTestTreeEditMenuItems(boolean enabled){
		deleteNodeItem.setEnabled(enabled && testingManager.hasSelectedNode());
		cloneSubtreeItem.setEnabled(enabled && testingManager.hasSelectedNode());
		moveNodeItem.setEnabled(enabled && testingManager.hasSiblingNode());		
	}
	
	public void setMenuAndToolBarEnabled(boolean enabled){
/*		fileMenu.setEnabled(enabled);
		editMenu.setEnabled(enabled);
		checkMenu.setEnabled(enabled);
		testTreeMenu.setEnabled(enabled);
		helpMenu.setEnabled(enabled);
*/
		if (enabled){
//	        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			enableFileMenuItems();
			enableEditMenuItems();
			enableCompileMenuItems();
			enableTestMenuItems();
		} else {
//	        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		fileManager.getEditor().setEditorEnabled(enabled);
		boolean isEditable = fileManager.getEditor().isEditing();
		boolean isTestTreeFile =  fileManager.isWorkingFileTestTree();
		toolBar.openButton.setEnabled(enabled);
		toolBar.newButton.setEnabled(enabled);
		toolBar.refreshButton.setEnabled(enabled && fileManager.workingFileExists() && !isTestTreeFile);
		toolBar.saveButton.setEnabled(enabled && isEditable); 

		ModelType modelType = fileManager.getEditor().getModelType();
		boolean isAnalysisApplicable = modelType!=null && modelType!= ModelType.THREATTREE;
		toolBar.parseButton.setEnabled(enabled && modelType!=null);
		toolBar.checkButton.setEnabled(enabled && isAnalysisApplicable);
		toolBar.simulationButton.setEnabled(enabled && fileManager.getEditor().hasSimulator());

		boolean hasFileForTestGeneration = fileManager.hasWorkingFile() && FileUtil.isTestGenerationFile(fileManager.getWorkingFile());
		boolean hasTestTree = fileManager.getEditor().hasTestTree();
		boolean hasTestCode = fileManager.getEditor().hasTestCodeComponent();
		boolean isTreeSelected = hasTestTree && fileManager.getEditor().isTestTreeSelected();
		toolBar.generateTestCodeButton.setEnabled(enabled && (hasFileForTestGeneration || hasTestTree) && !hasTestCode 
				&& systemOptions.getLanguage()!=TargetLanguage.RPC 
				&& systemOptions.getLanguage()!=TargetLanguage.SELENIUMDRIVER);
		toolBar.generateTestTreeButton.setEnabled(enabled && !hasTestTree && hasFileForTestGeneration);
		toolBar.saveTestTreeButton.setEnabled(enabled && isTreeSelected);

		toolBar.setCoverageCriterionBoxEnabled(enabled && !hasTestTree && !hasTestCode);
		toolBar.setLanguageBoxEnabled(enabled && !hasTestCode);
		toolBar.setTestFrameworkBoxEnabled(enabled && !hasTestCode);		
	}
	
	public void updateModelType(){
		toolBar.updateModelType();
	}

    public void updateLanguage(TargetLanguage newLanguage){
    	toolBar.updateLanguage(newLanguage);
    }
    
    public void updateMenuAndToolBarForLanguageChange(){
		boolean hasFileForTestGeneration = fileManager.hasWorkingFile() && FileUtil.isTestGenerationFile(fileManager.getWorkingFile());
		boolean hasTestTree = fileManager.getEditor().hasTestTree();
		boolean hasTestCode = fileManager.getEditor().hasTestCodeComponent();
		boolean enabled = (hasFileForTestGeneration || hasTestTree) && !hasTestCode 
			&& systemOptions.getLanguage()!=TargetLanguage.RPC  
			&& systemOptions.getLanguage()!=TargetLanguage.SELENIUMDRIVER;
		generateTestCodeItem.setEnabled(enabled);
		toolBar.generateTestCodeButton.setEnabled(enabled);   	
		onlineTestExecutionItem.setEnabled(hasTestTree && fileManager.getEditor().hasSimulator() && 
				(systemOptions.getLanguage()==TargetLanguage.RPC || 
						systemOptions.getLanguage()==TargetLanguage.SELENIUMDRIVER));
		onTheFlyTestingItem.setEnabled(fileManager.getEditor().hasSimulator() && 
				(systemOptions.getLanguage()==TargetLanguage.RPC || 
						systemOptions.getLanguage()==TargetLanguage.SELENIUMDRIVER));
    }
    

	public static void setLookAndFeel() {
		if (System.getProperty("os.name").contains("Windows") && WindowsLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch(Exception e) {
			}
		}
	}

    private static void startGUI() {
    	setLookAndFeel();
		MISTA window = new MISTA();
		window.pack();
		window.setVisible(true);
   }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startGUI();
            }
        });
    }

}
