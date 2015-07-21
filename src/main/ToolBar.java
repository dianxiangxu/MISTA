/* 	
	Author Dianxiang Xu
*/
package main;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import testcode.TargetLanguage;
import testgeneration.CoverageCriterion;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import kernel.Commands;
import kernel.Kernel;
import kernel.SystemOptions;
import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

public class ToolBar extends JToolBar implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final String ICONIMAGEPATH = "images/";

    private MISTA mainFrame;
    private SystemOptions systemOptions;
    
    // Package visibility - used by MainFrame
    JButton newButton, openButton, saveButton, //printButton, 
    		refreshButton,
    		parseButton, checkButton,
    		simulationButton,
    		optionsButton,
    		generateTestTreeButton, 
    		//addNodeButton, 
    		printTreeButton,
    		saveTestTreeButton, generateTestCodeButton;
    private JLabel coverageLabel;
    private JComboBox coverageCriterionBox, languageBox, testFrameworkBox;
   
   	private JPanel optionsWrapperPanel;

    public ToolBar(MISTA gui) {
        super();
        this.mainFrame = gui;
        systemOptions = gui.getSystemOptions();
        createButtons();
        createOptionsPanel();
        updateTooBarComponents(null);
        setFloatable(false);
    }

    protected void createButtons() {
        openButton = 		createButton(ICONIMAGEPATH+"open.gif", Commands.OPEN, LocaleBundle.bundleString("Open file"), LocaleBundle.bundleString("Open"), mainFrame.getFileManager());
        refreshButton = 	createButton(ICONIMAGEPATH+"refresh.gif", Commands.REFRESH, LocaleBundle.bundleString("Refresh file"), LocaleBundle.bundleString("Refresh"), mainFrame.getFileManager());
        newButton = 		createButton(ICONIMAGEPATH+"new.gif", SystemOptions.FUNCTIONNET_KEYWORD, LocaleBundle.bundleString("New "+SystemOptions.FUNCTIONNET_KEYWORD.toLowerCase()), LocaleBundle.bundleString("New"), mainFrame.getFileManager());
        saveButton = 		createButton(ICONIMAGEPATH+"save.gif", Commands.SAVE, LocaleBundle.bundleString("Save file"), LocaleBundle.bundleString("Save"), mainFrame.getFileManager());
        
        parseButton = 			createButton(ICONIMAGEPATH+"parse.gif", Commands.PARSE, LocaleBundle.bundleString("Compile"), LocaleBundle.bundleString("Compile"), mainFrame.getFileManager());
        checkButton = 			createButton(ICONIMAGEPATH+"check.gif", Commands.IntegratedGoalReachabilityAnalysis, LocaleBundle.bundleString("Verify goal state reachability"), "Check", mainFrame.getVerificationManager());
        simulationButton = 		createButton(ICONIMAGEPATH+"simulation.gif", Commands.SIMULATION, LocaleBundle.bundleString("Simulate"), "Simulate", mainFrame.getFileManager());
       
        generateTestCodeButton =	createButton(ICONIMAGEPATH+"code.gif", Commands.GenerateTestCode, LocaleBundle.bundleString("Generate test code"), "SVCode", mainFrame.getTestingManager());
        optionsButton = createButton(ICONIMAGEPATH+"options.gif", Commands.SetSystemOptions, LocaleBundle.bundleString("Options..."), "Options",mainFrame.getTestingManager());
        generateTestTreeButton =createButton(ICONIMAGEPATH+"tree.gif", Commands.GenerateTree, LocaleBundle.bundleString("Generate test tree"), "Generate",mainFrame.getTestingManager());

        saveTestTreeButton=	createButton(ICONIMAGEPATH+"exportTestData.gif", Commands.SaveTree, LocaleBundle.bundleString("Save test tree"), "SVData", mainFrame.getTestingManager());
        printTreeButton = 		createButton(ICONIMAGEPATH+"print.gif", Commands.PrintTree, LocaleBundle.bundleString("Print test tree"), "PrintTree", mainFrame.getTestingManager());
     }

    protected void updateTooBarComponents(JToolBar additionalToolBar) {
    	removeAll();
        add(openButton); 
        add(refreshButton);
        add(newButton);
        add(saveButton);
        
        addSeparator();

        add(parseButton);
        add(checkButton);
        add(simulationButton);

        addSeparator();
        add(optionsButton);
        
        add(generateTestTreeButton);
//        add(saveTestTreeButton);

        add(generateTestCodeButton);

//        add(printTreeButton);

        if (additionalToolBar!=null){
            addSeparator();
            addSeparator();
        	add(additionalToolBar);
        }
        
        addSeparator();
        add(optionsWrapperPanel);
    }

    private JButton createButton(String imageName,
    					String actionCommand,
    					String toolTipText,
    					String altText,
    					ActionListener listener) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL imageResource = classLoader.getResource(imageName);

        if (imageResource != null) //image found
            button.setIcon(new ImageIcon(imageResource));
        else   //image not found
            button.setText(altText);

        Insets margins = new Insets(1, 1, 1, 1); 
        button.setMargin(margins);
    	button.setBorderPainted(false);

    	button.addActionListener(listener);
    	button.setName(actionCommand);
        button.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        return button;
    }

    private void createOptionsPanel(){

    	optionsWrapperPanel = new JPanel(new BorderLayout());
    	
    	JPanel optionsPanel = new JPanel();
 
		coverageLabel = new JLabel("");
    	
    	optionsPanel.add(coverageLabel);
    	coverageCriterionBox = new JComboBox();
    	coverageCriterionBox.addActionListener(this);
    	optionsPanel.add(coverageCriterionBox);
    	updateModelType();
    	
//    	optionsPanel.add(new JLabel("  "+LocaleBundle.bundleString("Language")));
		languageBox = new JComboBox(systemOptions.getAllLanguageNamesForSelection());
		languageBox.setSelectedIndex(systemOptions.getLanguageIndex());
		languageBox.setMaximumRowCount(languageBox.getItemCount());
    	languageBox.addActionListener(this);
    	optionsPanel.add(languageBox);

//    	optionsPanel.add(new JLabel("  "+LocaleBundle.bundleString("Tool")));
		testFrameworkBox = new JComboBox(systemOptions.getLanguage().getTestFrameworkList());
    	testFrameworkBox.setSelectedIndex(systemOptions.getTestFrameworkIndex());
		testFrameworkBox.addActionListener(this);
		optionsPanel.add(testFrameworkBox);
    			
		optionsWrapperPanel.add(optionsPanel, BorderLayout.EAST);
    	add(optionsWrapperPanel);
	}

    public void updateModelType(){
		ModelType modelType = mainFrame.getFileManager().getEditor().getModelType();
//		String coverageLabelText = LocaleBundle.bundleString("Test coverage");
//		coverageLabel.setText(coverageLabelText);
		int selectedIndex = coverageCriterionBox.getSelectedIndex();
		coverageCriterionBox.removeActionListener(this);
		coverageCriterionBox.removeAllItems();
		if (modelType!=null){
			for (String coverageString: systemOptions.getCoverageStringList(modelType))
				coverageCriterionBox.addItem(LocaleBundle.bundleString(coverageString));
			coverageCriterionBox.setMaximumRowCount(coverageCriterionBox.getItemCount());
		} else
			coverageCriterionBox.addItem("N/A");
		// keep the previous selectedIndex if it is still in the range.
		if (selectedIndex<0 || selectedIndex>=coverageCriterionBox.getItemCount())
			selectedIndex=0;
		// avoid random generation option, which triggers a dialog
		if (SystemOptions.getCoverageObject(systemOptions.getCoverageStringList(modelType)[selectedIndex])==SystemOptions.RandomGeneration){
			selectedIndex=0;
		}
		coverageCriterionBox.addActionListener(this);
		coverageCriterionBox.setSelectedIndex(selectedIndex);
		systemOptions.setCoverageCriterion(selectedIndex, modelType);	
		coverageCriterionBox.setEnabled(coverageCriterionBox.getItemCount()>1);
	}

    private void updateLanguage(int selectedIndex){
		if (selectedIndex != systemOptions.getLanguageIndex()){
			systemOptions.setLanguage(selectedIndex);
			updateTestFrameworkBox();
		   	mainFrame.getFileManager().getEditor().updateLanguage();
		}
    }

    public void updateLanguage(TargetLanguage newLanguage){
    	if (newLanguage!=systemOptions.getLanguage()){
    		systemOptions.setLanguage(newLanguage);
    		languageBox.setSelectedIndex(systemOptions.getLanguageIndex());
    		updateTestFrameworkBox();
    		mainFrame.getFileManager().getEditor().updateLanguage();
    	}
    }
    
    public void setCoverageCriterion(CoverageCriterion newCoverage){
    	ModelType modelType = mainFrame.getFileManager().getEditor().getModelType();
    	int newCoverageIndex = systemOptions.getCoverageIndex(modelType, newCoverage);
		systemOptions.setCoverageCriterion(newCoverageIndex, modelType);
		if (coverageCriterionBox!=null)
			coverageCriterionBox.setSelectedIndex(newCoverageIndex);
    }
    
	private void updateTestFrameworkBox(){
		testFrameworkBox.removeAllItems();
		String[] frameworkList = systemOptions.getLanguage().getTestFrameworkList();
    	for (int i=0; i<frameworkList.length; i++)
    		testFrameworkBox.addItem(frameworkList[i]);
    	testFrameworkBox.setSelectedIndex(systemOptions.getTestFrameworkIndex());
    	testFrameworkBox.setEnabled(testFrameworkBox.getItemCount()>1);
	}

	public void setCoverageCriterionBoxEnabled(boolean enabled){
		if (coverageCriterionBox!=null)
			coverageCriterionBox.setEnabled(enabled && coverageCriterionBox.getItemCount()>1);
	}

	public void setLanguageBoxEnabled(boolean enabled){
		if (languageBox!=null)
			languageBox.setEnabled(enabled);
	}

	public void setTestFrameworkBoxEnabled(boolean enabled){
		if (testFrameworkBox!=null)
			testFrameworkBox.setEnabled(enabled && testFrameworkBox.getItemCount()>1);
	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		
	}
	
/////////////////////////////////////////////////////////////	
	private void readMaxRandomTests(){
		boolean inputDone = false;
	    while (!inputDone){
		    String input = JOptionPane.showInputDialog(LocaleBundle.bundleString("Maximum random tests"), systemOptions.getMaxRandomTests());
		    if (input==null)
		    	inputDone = true;
		    else
		    	try {
		    		int maxRandomTests = Integer.parseInt(input);
		    		if (maxRandomTests>0) {
		    			if (Kernel.IS_LIMITATION_SET && maxRandomTests>Kernel.MAX_RANDOM_TESTS_FOR_LIMITATION)
		    				mainFrame.printDialogMessage(LocaleBundle.bundleString("INVALID_SETTING_FOR_EVALUAITON_VERSION"));
		    			else {
		    				systemOptions.setMaxRandomTests(maxRandomTests);
		    				inputDone = true;
		    			}
		    		}
		    		else 
		    			mainFrame.printDialogMessage(LocaleBundle.bundleString("MAXIMUM_RANDOM_TESTS_SHOULD_BE_A_POSITIVE_INTEGER"));
		    	}
		    	catch (Exception ex) {
		    		mainFrame.printDialogMessage(LocaleBundle.bundleString("MAXIMUM_RANDOM_TESTS_SHOULD_BE_A_POSITIVE_INTEGER"));
		    	}
	    }
	}
	
	public void actionPerformed(ActionEvent e) {
		ModelType modelType = mainFrame.getFileManager().getEditor().getModelType();
		if (e.getSource()==coverageCriterionBox) {
			systemOptions.setCoverageCriterion(coverageCriterionBox.getSelectedIndex(), modelType);
			if (systemOptions.getCoverageCriterion()==SystemOptions.RandomGeneration)
				readMaxRandomTests();
		} if (e.getSource()==languageBox) {
			updateLanguage(languageBox.getSelectedIndex());
			mainFrame.updateMenuAndToolBarForLanguageChange();
		} else if (e.getSource()==testFrameworkBox) {
			systemOptions.setTestFrameworkIndex(testFrameworkBox.getSelectedIndex());
		}
	}

}