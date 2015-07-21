/* 	
	Author Dianxiang Xu
*/
package edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import pipeprt.dataLayer.PipeTransition;
import pipeprt.gui.CreateGui;
import pipeprt.gui.PrTPanel;

import testgeneration.TransitionTree;
import testinterface.TestTreePanel;
import utilities.FileExtensionFilter;
import utilities.FileUtil;

import kernel.Commands;
import kernel.Kernel;
import kernel.SystemOptions;
import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

import mid.MID;

abstract public class GeneralEditor implements ActionListener, CaretListener, ChangeListener, HyperlinkListener {
	
	protected static String testTreeTab = "Test Tree";
	protected static String testCodeTab = "Test Code"; 
	
	public static Font tabFont = new Font(SystemOptions.DefaultFontName, Font.PLAIN, 13);
	public static final Font titleFont = new Font(SystemOptions.DefaultFontName, Font.PLAIN, 12);
	public static final Color titleColor = new Color(0.0f, 0.0f, 0.7f); // Color.BLUE;

	protected Kernel kernel; 

	protected File midFile;
	protected ModelType modelType;
	protected boolean isEditable;

	private static final String clearText = "ClearText";
	private static final String setLineWrap = "SetLineWrap";
	
	protected JTextArea consoleArea = new JTextArea();
	protected JTabbedPane editTabbedPane;
	
	protected TestTreePanel testTreeComponent = null;
	protected JComponent testCodeComponent = null;
	
	protected ArrayList<SubModelPanel> subModelPanels = new ArrayList<SubModelPanel>();
	
	protected boolean xmidSaved = true;

	public GeneralEditor(Kernel kernel, boolean editable){
		this.kernel = kernel;
		this.isEditable = editable;
		editTabbedPane = new JTabbedPane();
		editTabbedPane.setFont(tabFont);
		editTabbedPane.setBorder(BorderFactory.createEtchedBorder(0));
		setPopupMenuForConsoleArea();
	}

	public ModelType getModelType(){
			return modelType;
	} 

	public Kernel getKernel(){
		return kernel;
	}
		
	public void clearConsoleArea(){
		consoleArea.setText("");		
	}

	public void printInConsoleArea(String info, boolean clearText){
		if (clearText)
			consoleArea.setText("");
		printInConsoleArea(info);
	}

	public void printInConsoleArea(String info){
		int previousCaretPosition = consoleArea.getDocument().getLength();
		consoleArea.append(info+"\n");
		consoleArea.setCaretPosition(previousCaretPosition);
	} 
	
	public void printInConsoleAreaWithoutTabReset(String info){
		int previousCaretPosition = consoleArea.getDocument().getLength();
		consoleArea.append(info+"\n");
		consoleArea.setCaretPosition(previousCaretPosition);
	}
	
	public void setToModelTab(){
		editTabbedPane.setSelectedIndex(0);		
	}
	
	final JCheckBoxMenuItem lineWrapItem = new JCheckBoxMenuItem(LocaleBundle.bundleString("Line Wrap"));
	
	private void setPopupMenuForConsoleArea() {

		final JPopupMenu popupMenu = new JPopupMenu();

		final JMenuItem clearItem = createPopupMenuItem(popupMenu, LocaleBundle.bundleString("Clear Text"), clearText);
		
		lineWrapItem.setActionCommand(setLineWrap);
		lineWrapItem.addActionListener(this);
		lineWrapItem.setSelected(true);
		popupMenu.add(lineWrapItem);

		consoleArea.setEditable(false);
		consoleArea.setTabSize(2);
		consoleArea.setWrapStyleWord(true);
		consoleArea.setLineWrap(true);

		consoleArea.addMouseListener( new MouseAdapter() { 
			public void mousePressed( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			} 
			public void mouseReleased( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			}
			private void checkForTriggerEvent( MouseEvent e ) { 
				if ( e.isPopupTrigger()) { 
					popupMenu.show( e.getComponent(), e.getX(), e.getY() );					
					clearItem.setEnabled(consoleArea.getText()!=null);
				}	
			} 
		}); 
	}

	private JMenuItem createPopupMenuItem(JPopupMenu popupMenu, String title, String command){
		JMenuItem menuItem = popupMenu.add(title);
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}

	// for tabular MID file editing
	// to be overridden by TextEditor - don't use tabbedPane when editing is the only activity
	public void addComponentToTabbedPane(String tab, String iconPath, JComponent component){
		editTabbedPane.addTab(LocaleBundle.bundleString(tab), component);
		editTabbedPane.setTabComponentAt(editTabbedPane.getTabCount()-1, new ButtonTabComponent(LocaleBundle.bundleString(tab), iconPath, editTabbedPane, this));
		editTabbedPane.setSelectedComponent(component);
	}

	// not needed for tabular MID file editing
	// to be overridden by TextEditor - check if editing is the only activity
	public void updateTabbedPane(){
		stateChanged(new ChangeEvent(editTabbedPane.getSelectedComponent()));
	}
	

	public void setTestTreeComponent(TestTreePanel treePane){
		if (testTreeComponent!=null)
			editTabbedPane.remove(testTreeComponent);
		testTreeComponent = treePane;
		addComponentToTabbedPane(testTreeTab, "images/tree.gif", testTreeComponent);
	}

	public TestTreePanel getTestTreeComponent(){
		return testTreeComponent;
	}
	
	public void removeTestTreeComponent(){
		if (testTreeComponent!=null){
			editTabbedPane.remove(testTreeComponent);
			testTreeComponent = null;
			updateTabbedPane();
		}
	}
	
	public void setTestCodeComponent(JComponent codePane){
		if (testCodeComponent!=null)
			editTabbedPane.remove(testCodeComponent);
		testCodeComponent = codePane;
		addComponentToTabbedPane(testCodeTab, "images/code.gif", testCodeComponent);
	}

	public JComponent getTestCodeComponent(){
		return testCodeComponent;
	}
	
	public boolean hasTestCodeComponent(){
		return testCodeComponent!=null;
	}

	public boolean isClosingTestTree(int index){
		JComponent component = (JComponent)editTabbedPane.getComponentAt(index);
		return testTreeComponent!=null && component instanceof TestTreePanel && component==testTreeComponent;
	}

	public boolean hasTestTree(){
		return testTreeComponent!=null && testTreeComponent.getTestTree()!=null;
	} 

	public TransitionTree getTransitionTree(){
		if (hasTestTree())
			return testTreeComponent.getTestTree().getTransitionTree();
		else
			return null;
	}
	
	public boolean isTestTreeSelected(){
		return testTreeComponent!=null && editTabbedPane.getSelectedComponent()==testTreeComponent;
	} 

	public boolean closeTestTree(){
		return kernel.getTestingManager().closeTree();
	}

	protected void removeComponentFromTabbedPane(int index){
		if (index<editTabbedPane.getTabCount()) {
			JComponent component = (JComponent)editTabbedPane.getComponentAt(index);
			editTabbedPane.remove(component);
			if (component==testTreeComponent){
				testTreeComponent =null;
				kernel.getTestingManager().cleanUp();
//			System.out.println("tree tab removed  ");
			}	
			else
			if (component==testCodeComponent){
//			System.out.println("code tab removed  ");
				testCodeComponent =null;
				testCodePane = null;
				testCodeFile = null;
	        	kernel.getStatusPanel().setStatus("");
			} 
			else 
			if (subModelPanels.contains(component)){
				closeSubModelTab((SubModelPanel)component);
			}
			updateTabbedPane();
		}
	}
		
    protected File testCodeFile; 
	protected JTextPane testCodePane;
	protected boolean isTestSuitePage;  
	
	private void setPopupMenuForTestCode() {
		final JPopupMenu popupMenu = new JPopupMenu();
//		final JMenuItem saveCodeAsItem = createPopupMenuItem(popupMenu, "Save Test Code As ...", Commands.SaveTestCodeAs);
		final JMenuItem backToMainPageItem = new JMenuItem(LocaleBundle.bundleString("Back to Test Suite Page"));
		backToMainPageItem.setActionCommand(Commands.BackToMainPage);
		backToMainPageItem.addActionListener(this);
		final boolean isHTMLFile = FileUtil.getExtension(testCodeFile.getName()).equalsIgnoreCase("HTML");
		if (isHTMLFile)
			popupMenu.add(backToMainPageItem);
		
		testCodePane.addMouseListener( new MouseAdapter() { 
			public void mousePressed( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			} 
			public void mouseReleased( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			}
			private void checkForTriggerEvent( MouseEvent e ) { 
//				if ( e.isPopupTrigger()) { 
				if ( e.isPopupTrigger() && isHTMLFile) { 
					popupMenu.show( e.getComponent(), e.getX(), e.getY() );
//					saveCodeAsItem.setEnabled(testCodePane.getText()!=null && isTestSuitePage);
//					if (isHTMLFile)
						backToMainPageItem.setEnabled(!isTestSuitePage);
				}	
			} 
		}); 
	}

/*	private JMenuItem createPopupMenuItem(JPopupMenu popupMenu, String title, String command){
		JMenuItem menuItem = popupMenu.add(title);
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}
*/
	
	public void createAndSetTestCodePane(File testCodeFile) {
		setTestCodeComponent(createCodePane(testCodeFile));
	}
	
	private JScrollPane createCodePane(File testCodeFile){
		this.testCodeFile = testCodeFile;
		isTestSuitePage = true;
		testCodePane = new JTextPane();
		testCodePane.setEditable(false);
		testCodePane.addHyperlinkListener(this);
		try {
			URL codeURL = testCodeFile.toURI().toURL();
	        testCodePane.setPage(codeURL);
	    } catch (IOException e) {
		    System.err.println("Bad test code file: " +testCodeFile.getName());
		}
		testCodePane.setFont(kernel.getSystemOptions().getTextFont());
		testCodePane.setCaretPosition(0);
		testCodePane.setMargin(new Insets(2,5,5,5));
		setTabs(testCodePane, 4 );
		setPopupMenuForTestCode();
		return new JScrollPane(testCodePane);
	}

	public static void setTabs( JTextPane textPane, int charactersPerTab){
		FontMetrics fm = textPane.getFontMetrics( textPane.getFont() );
		int charWidth = fm.charWidth( 'w' );
		int tabWidth = charWidth * charactersPerTab; 
		TabStop[] tabs = new TabStop[10];
		for (int j = 0; j < tabs.length; j++){
			int tab = j + 1;
			tabs[j] = new TabStop( tab * tabWidth );
		}
 
		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = textPane.getDocument().getLength();
		textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
	}

	public void setEditorEnabled(boolean enabled){
			editTabbedPane.setEnabled(enabled);
	}
	
	////////////////////////////////////////////////////////////////////
	public boolean saveTestCode() {
		String validFileExtension = FileUtil.getExtension(testCodeFile.getName());
		JFileChooser fc = kernel.getFileChooser();
//		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.addChoosableFileFilter(new FileExtensionFilter(validFileExtension));
		fc.setSelectedFile(testCodeFile);
		int returnVal = fc.showSaveDialog(kernel.getParentFrame());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return false;
		File saveFile = fc.getSelectedFile();
		if (!saveFile.getName().endsWith("."+validFileExtension)){
			saveFile = new File(saveFile.getAbsolutePath()+"."+validFileExtension);
		}
		String defaultTestClassName = " "+FileUtil.getPrefix(testCodeFile.getName());
		String newTestClassName = " "+FileUtil.getPrefix(saveFile.getName());
		TransitionTree tree = getTransitionTree();
		SystemOptions options = tree!=null? tree.getSystemOptions(): kernel.getSystemOptions();
		if (options.isOOLanguage() && !defaultTestClassName.equals(newTestClassName)){
//	System.out.println(defaultTestClassName+" vs "+newTestClassName);		
			FileUtil.saveStringToTextFile(testCodePane.getText().replaceAll(defaultTestClassName, newTestClassName), saveFile);
		}
		else
			FileUtil.saveStringToTextFile(testCodePane.getText(), saveFile);
		return true;
	}

	public File getMidFile(){
		return midFile;
	}

	public void setMidFile(File file){
		this.midFile = file;
	}
	
	public boolean isXMIDSaved() {
		return xmidSaved;
	}
	
	public void setXMIDSaved(boolean saved) {
		xmidSaved = saved;
	}

	// implements CaretListener
	public void caretUpdate(CaretEvent e){
		if (kernel.getFileManager().hasWorkingFile()) {
			JTextArea textArea = (JTextArea)e.getSource();
			getLineAndColumnAtCaret(textArea);
		}	
	}

	protected void getLineAndColumnAtCaret(JTextArea textArea){
		int caretPosition = textArea.getCaretPosition();
		Element root = textArea.getDocument().getDefaultRootElement();
 		int line= root.getElementIndex( caretPosition ) + 1;
		int lineStart = root.getElement(line-1).getStartOffset();
 		int column = caretPosition - lineStart + 1;
		kernel.getStatusPanel().setStatus(String.format("%6d", line) + " : "+ String.format("%-4d", column)+"  "+
				textArea.getLineCount()+" lines");
	}

	// implement HyperlinkListener
	public void hyperlinkUpdate(HyperlinkEvent e) {
		/* three types of events ENTERED, ACTIVATED, EXITED */
		if ( e.getEventType () == HyperlinkEvent.EventType.ACTIVATED ) {
			try {
				testCodePane.setPage (e.getURL()) ;
				isTestSuitePage = false;
			}
			catch (Exception exc ) {}
		}
	}
	
	// implements ChangeListener for TabbedPane
	public synchronized void stateChanged(ChangeEvent evt) {
		// for general editor
        Component selection = editTabbedPane.getSelectedComponent();
       if (selection==testTreeComponent) {
        	kernel.setMenuAndToolBarEnabled(true);
        	kernel.updateToolBar(null);
       } else 
       if (selection==testCodeComponent){
           	kernel.setMenuAndToolBarEnabled(true);
//           	if (testCodeFile!=null && testCodePane!=null)
//           		kernel.getStatusPanel().setStatus(testCodeFile.getAbsolutePath());
        	kernel.updateToolBar(null);
       } else
       if (subModelPanels.contains(selection) && ((SubModelPanel)selection).getSubModelPanel() instanceof PrTPanel){
    	   PrTPanel prtPanel = (PrTPanel) (((SubModelPanel)selection).getSubModelPanel());
       		kernel.updateToolBar(prtPanel.getPaletteToolBar());
       		kernel.updateModelMenu(createModelMenu(prtPanel.getPrTMenu()));
       		CreateGui.setPrTPanel(prtPanel);
          	kernel.setMenuAndToolBarEnabled(true);
      }
       // subclass will override this method but call super first
	}
 
	public JMenu createModelMenu(JMenu existingMenu) {
		JMenu modelMenu = existingMenu;
		modelMenu.setText(LocaleBundle.bundleString("Model"));
		if (kernel.getSystemOptions().isNetHierarchyEnabled()){
			modelMenu.addSeparator();
//			createMenuItem(modelMenu, LocaleBundle.bundleString("Create a submodel"), Commands.CREATE_A_SUB_MODEL);
			createMenuItem(modelMenu, LocaleBundle.bundleString("Open submodels"), Commands.OPEN_SUB_MODELS);
			JMenuItem closeAllItem = createMenuItem(modelMenu, LocaleBundle.bundleString("Close submodels"), Commands.CLOSE_SUB_MODELS);
			createMenuItem(modelMenu, LocaleBundle.bundleString("List model hierarchy"), Commands.PRINT_MODEL_HIERARCHY);
			closeAllItem.setEnabled(subModelPanels.size()>0);
		}
		return modelMenu;
	}
	
	private JMenuItem createMenuItem(JMenu menu, String title, String command){
		JMenuItem menuItem = menu.add(title);
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}

    // implements ActionListener
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == clearText) {
			consoleArea.setText("");
		}
		else if (cmd == setLineWrap) {
			consoleArea.setLineWrap(lineWrapItem.isSelected());
		} else
		if (cmd == Commands.SaveTestCodeAs){
	        saveTestCode();
		} else
		if (cmd == Commands.BackToMainPage){
			try {
				URL codeURL = testCodeFile.toURI().toURL();
		        testCodePane.setPage(codeURL);
				isTestSuitePage = true;
		    } catch (IOException ioe) {
			    System.err.println("Bad test code file: " +testCodeFile.getName());
			}
		} else
		if (cmd == Commands.OPEN_SUB_MODELS){
			openAllSubModels(kernel.getFileManager().getWorkingFile());				
		} else
		if (cmd == Commands.CLOSE_SUB_MODELS){
			closeSubModelPanels();
		} else
		if (cmd == Commands.PRINT_MODEL_HIERARCHY){
			printModelHierarchy();
		}
	}		
	
	//
	public boolean isEditing(){
		return isEditable;
	}

	public Font getTextFont(){
		return kernel.getSystemOptions().getTextFont();
	}

	// to be overridden by subclass
	public void updateLanguage(){
	}

	// to be overridden by subclasses
	public void setTextFont(Font newFont){
		consoleArea.setFont(newFont);
		consoleArea.repaint();
		
    	if (testCodeComponent!=null){
    		Component component = ((JScrollPane)testCodeComponent).getViewport().getView();
    		component.setFont(newFont);
    		component.repaint();
    	}

	}
	
	public boolean isSubModelSelected(){
		return subModelPanels.contains(editTabbedPane.getSelectedComponent());
	}

	public void selectSubModelPanel(PrTPanel prtPanel){
		for (SubModelPanel subModelPanel: subModelPanels){
			if (subModelPanel.getSubModelPanel()==prtPanel){
				editTabbedPane.setSelectedComponent(subModelPanel);
				break;
			}
		}		
	}

	public void selectSubModelPanel(File file){
		for (SubModelPanel subModelPanel: subModelPanels){
			if (subModelPanel.isSubModelForFile(file)){
				editTabbedPane.setSelectedComponent(subModelPanel);
				break;
			}
		}		
	}
	
	private void addSubModelPanel(File file, JPanel modelPanel){
		SubModelPanel subModelPanel = new SubModelPanel(file, modelPanel);
		subModelPanels.add(subModelPanel);
		addComponentToTabbedPane(subModelPanel.getTitle(), "", subModelPanel);
	}

	private void closeSubModelPanels(){
		if (subModelPanels.size()>0){
			if (areSubModelsChanged()) {
	    		int choice = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
	    				LocaleBundle.bundleString("Save file")+"?", LocaleBundle.bundleString("Confirm Save"),
	    				JOptionPane.YES_NO_OPTION);
	    		if (choice == JOptionPane.YES_OPTION)
	    			saveSubModels();
			}
			for (SubModelPanel subModelPanel: subModelPanels)
				editTabbedPane.remove(subModelPanel);
			subModelPanels.clear();
			setToModelTab();
			updateTabbedPane();
		}
	}
	
	public boolean confirmCloseTab(int index){
		JComponent component = (JComponent)editTabbedPane.getComponentAt(index);
		if (subModelPanels.contains(component) && ((SubModelPanel)component).getSubModelPanel() instanceof PrTPanel){
			PrTPanel subModel = (PrTPanel)(((SubModelPanel)component).getSubModelPanel());
	    	if (subModel.isNetChanged()) { 
	    		int choice = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
	    				LocaleBundle.bundleString("Save file")+"?", LocaleBundle.bundleString("Confirm Save"),
	    				JOptionPane.YES_NO_CANCEL_OPTION);
	    		if (choice == JOptionPane.CANCEL_OPTION)	 
	    			return false;
	    		else
	    		if (choice == JOptionPane.YES_OPTION)
	    			subModel.saveNet();
	    	}
		}
		return true;
	}

	private void closeSubModelTab(SubModelPanel subModelPanel){
		subModelPanels.remove(subModelPanel);
		if (subModelPanels.size()==0)
			setToModelTab();
	}

	
	public void saveSubModels(){
		if (subModelPanels.size()>0){
			for (SubModelPanel subModelPanel: subModelPanels){
				PrTPanel prtPanel = (PrTPanel) subModelPanel.getSubModelPanel();
				if (prtPanel.isNetChanged()){
					CreateGui.setPrTPanel(prtPanel);
					prtPanel.saveNet();
				}
			}
			editTabbedPane.updateUI();
			stateChanged(null); // make the setting for the current tab is correct
		}
	}
	
	public boolean areSubModelsChanged(){
		if (subModelPanels.size()>0){
			for (SubModelPanel subModelPanel: subModelPanels){
				PrTPanel prtPanel = (PrTPanel) subModelPanel.getSubModelPanel();
				if (prtPanel.isNetChanged())
					return true;
			}
		}
		return false;
	}

	private ArrayList<String> allModelFileNames = new ArrayList<String>();
	
	public boolean hasForErrorsInSubModelFileHierarchy(){
		ModelPanel modelPanel = getModelPanel();
		if (modelPanel instanceof ModelPanelVisualNet){
			PrTPanel prtPanel = ((ModelPanelVisualNet)modelPanel).getMainNet();
			allModelFileNames.clear();
			allModelFileNames.add(prtPanel.getFile().getAbsolutePath());
			int numberOfErrors = checkForInvalidSubModelFiles (prtPanel);
			return numberOfErrors>0;
		} else
		return false;
	}

	private int checkForInvalidSubModelFiles(PrTPanel prtPanel){
		int numberOfErrors = 0;
		for (PipeTransition transition: prtPanel.getModel().getTransitions())
			if (transition.hasValidSubnetFile()) {
				if (transition.getSubnetFile().equalsIgnoreCase(prtPanel.getFile().getName())){
					consoleArea.append(LocaleBundle.bundleString("SUBMODEL_FILE_SAME_AS_CURRENT")+": "+LocaleBundle.bundleString("transition")+" "+transition.getName()+" -> "+LocaleBundle.bundleString("file")+" "+prtPanel.getFile().getAbsolutePath()+"\n");
					numberOfErrors++;
				}
				else 
				if (!transition.getSubnetFileHandler().exists()){
					consoleArea.append(LocaleBundle.bundleString("SUBMODEL_FILE_DOESNOT_EXIST")+": "+prtPanel.getFile().getAbsolutePath()+" -> "+LocaleBundle.bundleString("transition")+" "+transition.getName()+" -> "+LocaleBundle.bundleString("file")+" "+transition.getSubnetFile()+"\n");	
					numberOfErrors++;
				} else {
					File subModelFile = transition.getSubnetFileHandler();
					if (allModelFileNames.contains(subModelFile.getAbsolutePath())){
						consoleArea.append(LocaleBundle.bundleString("DUPLICATE_SUBMODEL_FILE")+": "+prtPanel.getFile().getAbsolutePath()+" -> "+LocaleBundle.bundleString("transition")+" "+transition.getName()+" -> "+LocaleBundle.bundleString("file")+" "+subModelFile.getAbsolutePath()+"\n");	
						numberOfErrors++;
					} else {
						allModelFileNames.add(subModelFile.getAbsolutePath());
						numberOfErrors+=checkForInvalidSubModelFiles(findPrTPanelForFile(subModelFile));
					}
				}
			}
			else {
				if (!transition.getSubnetFile().trim().equals("")){
					consoleArea.append(LocaleBundle.bundleString("INVALID_SUBMODEL_FILE")+": "+prtPanel.getFile().getAbsolutePath()+" -> "+LocaleBundle.bundleString("transition")+" "+transition.getName()+" -> "+LocaleBundle.bundleString("file")+" "+transition.getSubnetFile()+"\n");
					numberOfErrors++;
				}
			}
		return numberOfErrors;
 	}
	
	public PrTPanel findPrTPanelForFile(File file){
		for (SubModelPanel subModelPanel: subModelPanels)
			if (subModelPanel.isSubModelForFile(file) && subModelPanel.getSubModelPanel() instanceof PrTPanel)
				return (PrTPanel) (subModelPanel.getSubModelPanel());
		PrTPanel subPrTPanel = new PrTPanel(file, isEditable);
		addSubModelPanel(file, subPrTPanel);
		return subPrTPanel;
	}

	public void openAllSubModels(String midFileName){
		if (kernel.getSystemOptions().isNetHierarchyEnabled() && getModelPanel() instanceof ModelPanelVisualNet){
     		PrTPanel prtPanel = ((ModelPanelVisualNet)getModelPanel()).getMainNet();
     		if (hasSubNets(prtPanel)) {
    			allModelFileNames.clear();
    			allModelFileNames.add(prtPanel.getFile().getAbsolutePath());
//System.out.println("MID file: "+midFileName);
     			if (kernel.getFileManager().isDefaultFileName(midFileName))
     				kernel.printDialogMessage(LocaleBundle.bundleString("PLEASE_SAVE_THE_TOP_LEVEL_NET_INTO_NON_DEFAULT_FILE"));
     			else
     				openAllSubModels(prtPanel);
     		} 
     	}
	}
	
	private boolean hasSubNets(PrTPanel prtNet){
		for (PipeTransition transition: prtNet.getModel().getTransitions()){
			if (transition.hasValidSubnetFile())
				return true;
		}
		return false;
	}
	
	protected void openAllSubModels(PrTPanel prtPanel){
		for (PipeTransition transition: prtPanel.getModel().getTransitions()){
			if (transition.hasValidSubnetFile()) {
				File subnetFile = transition.getSubnetFileHandler();
				if (!allModelFileNames.contains(subnetFile.getAbsolutePath())){
					allModelFileNames.add(subnetFile.getAbsolutePath());
//				if (!isMainModelFile(subnetFile)){
					PrTPanel subPrTPanel = findPrTPanelForFile(subnetFile);
					openAllSubModels(subPrTPanel);
				} else
					consoleArea.append(LocaleBundle.bundleString("DUPLICATE_SUBMODEL_FILE")+": "+prtPanel.getFile().getAbsolutePath()+" -> "+LocaleBundle.bundleString("transition")+" "+transition.getName()+" -> "+LocaleBundle.bundleString("file")+" "+transition.getSubnetFile()+"\n");
			}
		}
 	}
	
/*	private boolean isMainModelFile(File file){
		if (getModelPanel() instanceof ModelPanelVisualNet)
			return ((ModelPanelVisualNet)getModelPanel()).getMainNet().getFile().getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath());
		return false;
	}
*/
	
	public ArrayList<JPanel> getSubModels(){
		ArrayList<JPanel> subModels = new ArrayList<JPanel>();
		for (SubModelPanel subModelPanel: subModelPanels){
			subModels.add(subModelPanel.getSubModelPanel());
		}	
		return subModels;
	}
	
	private void printModelHierarchy(){
		if (!hasForErrorsInSubModelFileHierarchy()){
			ModelPanel modelPanel = getModelPanel();
			if (modelPanel instanceof ModelPanelVisualNet){
				PrTPanel mainNet = ((ModelPanelVisualNet)modelPanel).getMainNet();
				if (hasSubNets(mainNet)){
					consoleArea.append("\n"+LocaleBundle.bundleString("Model Hierarchy")+":\n");
					printModelHierarchy(mainNet, 0);
				} else
					consoleArea.append("\n"+LocaleBundle.bundleString("No submodels")+"\n");					
			}
		}
	}
	
	private void printModelHierarchy(PrTPanel prtPanel, int numberOfTabs){
		int numberOfSubModels=0;
		for (PipeTransition transition: prtPanel.getModel().getTransitions())
			if (transition.hasValidSubnetFile()) 
					numberOfSubModels++;
		if (numberOfSubModels==0)
			return;
		for (int i=0; i<numberOfTabs; i++)
			consoleArea.append("\t");
		int newNumberOfTabs = numberOfTabs+1;
		consoleArea.append(prtPanel.getFile().getName()+" \u2192");
		for (PipeTransition transition: prtPanel.getModel().getTransitions())
			if (transition.hasValidSubnetFile()) {
				consoleArea.append(" ["+transition.getName()+"] ");
				consoleArea.append(transition.getSubnetFileHandler().getName());
			}
		consoleArea.append("\n");
		for (PipeTransition transition: prtPanel.getModel().getTransitions())
			if (transition.hasValidSubnetFile()) {
				PrTPanel subModel = findPrTPanelForFile(transition.getSubnetFileHandler());
				printModelHierarchy(subModel, newNumberOfTabs);
			}
		printInConsoleArea("");
	}
	
	// to be overriden
	public ModelPanel getModelPanel() {
		return null;
	}
	
	public JSplitPane getMainInterface(){
		JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder(null, "", 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
//			consoleScrollPane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Console"), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
		JSplitPane wholePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getEditingJComponent(), consoleScrollPane);
	    wholePane.setOneTouchExpandable(true);
	    return wholePane;
	}
	
	public static enum SimulatorType {MODEL_SIMULATION, ONLINE_TEST_EXECUTION, ON_THE_FLY_TESTING};

	abstract public boolean hasSimulator();
	
	abstract public void startSimulator(SimulatorType simulatorType);
	
	abstract public void resetSimulator();

	abstract public JComponent getEditingJComponent();
	
	abstract public void saveSpecificationToFile(String fileName, boolean needDialog);
	
	abstract public void saveSpecificationToFile(File file, boolean needDialog);
	
	abstract public MID parse();
}
