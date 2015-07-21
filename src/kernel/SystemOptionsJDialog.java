/* 	
	Author Dianxiang Xu
*/
package kernel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import testcode.GoalTagCode;
import testcode.TargetLanguage;

import locales.LocaleBundle;

public class SystemOptionsJDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final String Apply = "Apply";
	private static final String Cancel = "Cancel";

	private JRadioButton breadthFirstSearchButton, depthFirstSearchButton;
	private JRadioButton searchForHomeStatesButton, donotSearchForHomeStatesButton;
	private JRadioButton totalOrderingButton, partialOrderingButton;
	private JRadioButton generalFiringRuleButton, pairwiseButton;
	private JTextField searchDepthField, nodeIdDepthField, maxExpansionLevelField;
	
	private JCheckBox viewTestCodeBox, generateSeparateTestFilesBox, includeSeqIndicesInTestIDBox, showStatesInNodesBox;
	private JCheckBox testParameterBox;  
	private JCheckBox includeAssertDefinitionBox;  	
	private JCheckBox createObjectReferenceBox, referenceForMethodBox, referenceForAccessorBox, referenceForMutatorBox;  
	private JCheckBox markingBox, postconditionBox, negatedConditionBox, effectBox, firstOccurrenceBox, createGoalTagBox;  
	private JRadioButton goalTagsAtBeginningOfTestsButton, goalTagsInsideTestsButton;
	private JCheckBox dirtyTestStateBox, dirtyTestExceptionBox;  
	
	private boolean includeSearchOptions;
	
	private Kernel kernel;
	private SystemOptions options;
	
	public SystemOptionsJDialog(Kernel kernel, String title, SystemOptions systemOptions, boolean includeSearchOptions) {
		super(kernel.getParentFrame(), title, true);
		this.kernel = kernel;
		this.options = systemOptions;
		this.includeSearchOptions = includeSearchOptions;
		createContentPane();
		pack();
		if (kernel != null) 
    		setLocationRelativeTo(kernel.getParentFrame());
		setVisible(true); 
	}
	
	private void createContentPane() { 
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(createSearchOptionsPanel(), BorderLayout.NORTH);
		mainPanel.add(createTestOptionsPanel(), BorderLayout.CENTER);
		mainPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
		setContentPane(mainPanel);		
	}
	
	private JPanel createSearchOptionsPanel() {
	    JPanel searchStrategyPanel = new JPanel();
	    searchStrategyPanel.setLayout(new GridBagLayout());
	    GridBagConstraints gridBagConstraints = new GridBagConstraints();

	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    searchStrategyPanel.add(createSearchStrategyPanel(), gridBagConstraints);

	    gridBagConstraints.gridy = 1;
	    searchStrategyPanel.add(searchForHomeStatesPanel(), gridBagConstraints);
	    
	    gridBagConstraints.gridy = 2;
	    searchStrategyPanel.add(createCombinatorialTestingPanel(), gridBagConstraints);
	    
	    gridBagConstraints.gridy = 3;
	    searchStrategyPanel.add(createOrderingPanel(), gridBagConstraints);

	    searchStrategyPanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("Search")));
		return searchStrategyPanel;
	}

	private JPanel createSearchStrategyPanel(){
		breadthFirstSearchButton = new JRadioButton(LocaleBundle.bundleString("Breadth first"));
		breadthFirstSearchButton.addActionListener(this);
		breadthFirstSearchButton.setSelected(options.isBreadthFirstSearch());
		breadthFirstSearchButton.setEnabled(includeSearchOptions);
				
		depthFirstSearchButton = new JRadioButton(LocaleBundle.bundleString("Depth first"));
		depthFirstSearchButton.addActionListener(this);
		depthFirstSearchButton.setSelected(!options.isBreadthFirstSearch());
		depthFirstSearchButton.setEnabled(includeSearchOptions);
		
		ButtonGroup group = new ButtonGroup();
		group.add(breadthFirstSearchButton);
		group.add(depthFirstSearchButton);

		JPanel panel = new JPanel();
		JLabel searchStrategyLabel = new JLabel(LocaleBundle.bundleString("Strategy"));
		searchStrategyLabel.setEnabled(includeSearchOptions);
		panel.add(searchStrategyLabel);

		panel.add(breadthFirstSearchButton);
		panel.add(depthFirstSearchButton);

		JLabel searchDepthLabel = new JLabel(LocaleBundle.bundleString("Maximum search depth"));
		searchDepthLabel.setEnabled(includeSearchOptions);
		panel.add(searchDepthLabel);
		
		searchDepthField = new JTextField(options.getSearchDepth()+"");
		searchDepthField.setPreferredSize(new Dimension(50, 20));
		searchDepthField.setEnabled(includeSearchOptions);
		searchDepthField.setEditable(includeSearchOptions);
		panel.add(searchDepthField);

		return panel;
	}

	private JPanel createOrderingPanel(){
		totalOrderingButton = new JRadioButton(LocaleBundle.bundleString("Total ordering"));
		totalOrderingButton.addActionListener(this);
		totalOrderingButton.setSelected(options.isTotalOrdering());
		totalOrderingButton.setEnabled(includeSearchOptions);
				
		partialOrderingButton = new JRadioButton(LocaleBundle.bundleString("Partial ordering"));
		partialOrderingButton.addActionListener(this);
		partialOrderingButton.setSelected(!options.isTotalOrdering());
		partialOrderingButton.setEnabled(includeSearchOptions);
		
		ButtonGroup group = new ButtonGroup();
		group.add(totalOrderingButton);
		group.add(partialOrderingButton);

		JPanel panel = new JPanel();
		JLabel orderingLabel = new JLabel(LocaleBundle.bundleString("Concurrent firings"));
		orderingLabel.setEnabled(includeSearchOptions);
		panel.add(orderingLabel);

		panel.add(totalOrderingButton);
		panel.add(partialOrderingButton);

		return panel;
	}

	private JPanel createCombinatorialTestingPanel(){
		generalFiringRuleButton = new JRadioButton(LocaleBundle.bundleString("General firing rule"));
		generalFiringRuleButton.addActionListener(this);
		generalFiringRuleButton.setSelected(!options.isPairwiseTesting());
		generalFiringRuleButton.setEnabled(includeSearchOptions);
				
		pairwiseButton = new JRadioButton(LocaleBundle.bundleString("Pairwise when applicable"));
		pairwiseButton.addActionListener(this);
		pairwiseButton.setSelected(options.isPairwiseTesting());
		pairwiseButton.setEnabled(includeSearchOptions);
		
		ButtonGroup group = new ButtonGroup();
		group.add(generalFiringRuleButton);
		group.add(pairwiseButton);

		JPanel panel = new JPanel();
		JLabel orderingLabel = new JLabel(LocaleBundle.bundleString("Input combinations"));
		orderingLabel.setEnabled(includeSearchOptions);
		panel.add(orderingLabel);

		panel.add(generalFiringRuleButton);
		panel.add(pairwiseButton);

		return panel;
	}

	private JPanel searchForHomeStatesPanel(){
		JPanel panel = new JPanel();
		JLabel homeStatesLabel = new JLabel(LocaleBundle.bundleString("Home states"));
		homeStatesLabel.setEnabled(includeSearchOptions);
		panel.add(homeStatesLabel);

		searchForHomeStatesButton = new JRadioButton(LocaleBundle.bundleString("SEARCH_FOR_HOME_STATES"));
		searchForHomeStatesButton.setSelected(options.searchForHomeStates());
		searchForHomeStatesButton.setEnabled(includeSearchOptions);

		donotSearchForHomeStatesButton = new JRadioButton(LocaleBundle.bundleString("DONOT_SEARCH_FOR_HOME_STATES"));
		donotSearchForHomeStatesButton.setSelected(!options.searchForHomeStates());
		donotSearchForHomeStatesButton.setEnabled(includeSearchOptions);

		
		ButtonGroup group = new ButtonGroup();
		group.add(searchForHomeStatesButton);
		group.add(donotSearchForHomeStatesButton);

		panel.add(searchForHomeStatesButton);		
		panel.add(donotSearchForHomeStatesButton);		
		return panel;
	}

	private JPanel createTestOptionsPanel() {
	    JPanel testOptionsPanel = new JPanel();
	    testOptionsPanel.setLayout(new BorderLayout());
    	testOptionsPanel.add(createTestIdentificationAndTreeViewPanel(), BorderLayout.NORTH);
	    testOptionsPanel.add(createTestCodeViewPanel(), BorderLayout.CENTER);
	    testOptionsPanel.add(createInputAndOraclePanel(), BorderLayout.SOUTH);
		return testOptionsPanel;
	}

	private JButton createJButton(String command){
		JButton button = new JButton(LocaleBundle.bundleString(command));
		button.setActionCommand(command);
		button.addActionListener(this);
		return button;
	} 
	
	private JPanel createButtonsPanel(){
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(createJButton(Apply));
		buttonsPanel.add(createJButton(Cancel));
		return buttonsPanel;
	}
	
	private JCheckBox createJCheckBox(String title, boolean selected) {
		JCheckBox box = new JCheckBox(title);
		box.setSelected(selected);
		box.addActionListener(this);
		return box;
	}

	private JPanel createTestIdentificationAndTreeViewPanel(){
		JPanel testIdentificationAndTreeViewPanel = new JPanel();
		
		testIdentificationAndTreeViewPanel.setLayout(new GridBagLayout());
	    GridBagConstraints gridBagConstraints = new GridBagConstraints();

		showStatesInNodesBox = createJCheckBox(LocaleBundle.bundleString("SHOW_RESULTANT_STATES_IN_NODES"), options.showStatesInNodes());
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    testIdentificationAndTreeViewPanel.add(showStatesInNodesBox, gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.gridy = 0;
	    testIdentificationAndTreeViewPanel.add(createMaxExpansionPanel(), gridBagConstraints);

		includeSeqIndicesInTestIDBox = createJCheckBox(LocaleBundle.bundleString("Include node indices in tests"), options.includeSeqIndicesInTestID());
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 1;
	    testIdentificationAndTreeViewPanel.add(includeSeqIndicesInTestIDBox, gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.gridy = 1;
	    testIdentificationAndTreeViewPanel.add(createIdDepthPanel(), gridBagConstraints);

		testIdentificationAndTreeViewPanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("TEST_IDENTIFICATION_AND_TREE_PRESENTATION")));
		return testIdentificationAndTreeViewPanel;
		
	}

	private JPanel createIdDepthPanel(){
		JPanel idDepthPanel = new JPanel();
		JLabel nodeIdDepthLabel = new JLabel(LocaleBundle.bundleString("MAXIMUM_ID_DEPTH"));
		nodeIdDepthLabel.setEnabled(includeSearchOptions);
		idDepthPanel.add(nodeIdDepthLabel);
		nodeIdDepthField = new JTextField(options.getMaxIdDepth()+"");
		nodeIdDepthField.setPreferredSize(new Dimension(50, 20));
		nodeIdDepthField.setEnabled(includeSearchOptions);
		nodeIdDepthField.setEditable(includeSearchOptions);
		idDepthPanel.add(nodeIdDepthField);
		return idDepthPanel;
	}

	private JPanel createMaxExpansionPanel(){
		JPanel maxExpansionPanel = new JPanel();
		JLabel maxExpansionDepthLabel = new JLabel(LocaleBundle.bundleString("Maximum depth of expansion"));
		maxExpansionDepthLabel.setEnabled(includeSearchOptions);
		maxExpansionPanel.add(maxExpansionDepthLabel);	
		maxExpansionLevelField = new JTextField(options.getMaxLevelOfNodeExpansion()+"");
		maxExpansionLevelField.setPreferredSize(new Dimension(50, 20));
		maxExpansionLevelField.setEnabled(includeSearchOptions);
		maxExpansionLevelField.setEditable(includeSearchOptions);
		maxExpansionPanel.add(maxExpansionLevelField);
		return maxExpansionPanel;
	}

	private JPanel createTestCodeViewPanel(){
		JPanel testCodeViewPanel = new JPanel();
		testCodeViewPanel.setLayout(new GridLayout(1,2));
		viewTestCodeBox = createJCheckBox(LocaleBundle.bundleString("View code after generation"), options.viewTestCode());
		testCodeViewPanel.add(viewTestCodeBox);
		generateSeparateTestFilesBox = createJCheckBox(LocaleBundle.bundleString("Generate file for each test"), options.generateSeparateTestFiles());
		testCodeViewPanel.add(generateSeparateTestFilesBox);

		testCodeViewPanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("Test code view")));
		return testCodeViewPanel;
	}
	
	private JPanel createInputAndOraclePanel(){
		JPanel inputAndOraclePanel = new JPanel();
		inputAndOraclePanel.setLayout(new BorderLayout());
		inputAndOraclePanel.add(createTestInputPanel(), BorderLayout.NORTH);
		inputAndOraclePanel.add(createCleanTestOraclePanel(), BorderLayout.CENTER);
		inputAndOraclePanel.add(createDirtyTestOraclePanel(), BorderLayout.SOUTH);
		return inputAndOraclePanel;
	}

	private JPanel createTestInputPanel(){
		int rows = options.isOOLanguage()? 3: 1;
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(rows,2));
		testParameterBox = createJCheckBox(LocaleBundle.bundleString("Generate test parameters"), options.generateTestParameters());
		inputPanel.add(testParameterBox);
		if (options.isOOLanguage()){
			createObjectReferenceBox = createJCheckBox(LocaleBundle.bundleString("Declare object reference"), options.createObjectReference());
			referenceForMethodBox = createJCheckBox(LocaleBundle.bundleString("Add object reference to methods"), options.generateReferenceForMethodCall());
			referenceForAccessorBox = createJCheckBox(LocaleBundle.bundleString("Add object reference to accessors"), options.generateReferenceForAccessorCall());
			referenceForMutatorBox = createJCheckBox(LocaleBundle.bundleString("Add object reference to mutators"), options.generateReferenceForMutatorCall());
			inputPanel.add(createObjectReferenceBox);
			inputPanel.add(referenceForMethodBox);
			inputPanel.add(referenceForAccessorBox);
			inputPanel.add(referenceForMutatorBox);
		} else 
		if (options.getLanguage()==TargetLanguage.C){
			includeAssertDefinitionBox = createJCheckBox(LocaleBundle.bundleString("INCLUDE_ASSERT_DEFITION"), options.includeAssertDefintionForC());
			inputPanel.add(includeAssertDefinitionBox);
		}
		inputPanel.setBorder(new TitledBorder(new EtchedBorder(), 
			options.getLanguage()!=TargetLanguage.C? LocaleBundle.bundleString("Test input"): LocaleBundle.bundleString("TEST_INPUT_AND_ASSERT_DEFINITION")));
		return inputPanel;
	}

	private JPanel createCleanTestOraclePanel(){
		markingBox = createJCheckBox(LocaleBundle.bundleString("Verify resultant states"), options.verifyMarkings());
		postconditionBox = createJCheckBox(LocaleBundle.bundleString("Verify positive postconditions"), options.verifyPostconditions());
		negatedConditionBox = createJCheckBox(LocaleBundle.bundleString("Verify negative postconditions"), options.verifyNegatedConditions());
		effectBox = createJCheckBox(LocaleBundle.bundleString("Verify effects"), options.verifyEffects());
		firstOccurrenceBox = createJCheckBox(LocaleBundle.bundleString("Verify first occurrence only"), options.verifyFirstOccurrence());
		createGoalTagBox = createJCheckBox(LocaleBundle.bundleString("Create goal tags"), options.createGoalTags());
		createGoalTagBox.setEnabled(kernel.getSystemOptions().hasTagCodeForTestFramework());

		goalTagsAtBeginningOfTestsButton = new JRadioButton(LocaleBundle.bundleString("GOAL_TAGS_AT_THE_BEGINNING"));
		goalTagsAtBeginningOfTestsButton.addActionListener(this);
		goalTagsAtBeginningOfTestsButton.setSelected(options.areGoalTagsAtBeginningOfTests());
		goalTagsAtBeginningOfTestsButton.setEnabled(createGoalTagBox.isEnabled() && createGoalTagBox.isSelected());

		goalTagsInsideTestsButton = new JRadioButton(LocaleBundle.bundleString("GOAL_TAGS_INSIDE_TETS"));
		goalTagsInsideTestsButton.addActionListener(this);
		goalTagsInsideTestsButton.setSelected(!options.areGoalTagsAtBeginningOfTests());
		goalTagsInsideTestsButton.setEnabled(createGoalTagBox.isEnabled() && createGoalTagBox.isSelected());

		
		ButtonGroup group = new ButtonGroup();
		group.add(goalTagsAtBeginningOfTestsButton);
		group.add(goalTagsInsideTestsButton);

		JPanel cleanTestOraclePanel = new JPanel();
		cleanTestOraclePanel.setLayout(new GridLayout(4,2));
		cleanTestOraclePanel.add(markingBox);
		cleanTestOraclePanel.add(postconditionBox);
		cleanTestOraclePanel.add(negatedConditionBox);
		cleanTestOraclePanel.add(effectBox);
		cleanTestOraclePanel.add(firstOccurrenceBox);
		cleanTestOraclePanel.add(createGoalTagBox);

		cleanTestOraclePanel.add(goalTagsAtBeginningOfTestsButton);
		cleanTestOraclePanel.add(goalTagsInsideTestsButton);

		cleanTestOraclePanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("Clean test oracle")));
		return cleanTestOraclePanel;
	}

	private JPanel createDirtyTestOraclePanel(){
		dirtyTestStateBox = createJCheckBox(LocaleBundle.bundleString("Verify state preservation"), options.verifyDirtyTestState());
		dirtyTestExceptionBox = createJCheckBox(LocaleBundle.bundleString("Verify exception throwing"), options.verifyDirtyTestException());
		dirtyTestExceptionBox.setEnabled(options.isOOLanguage());
		JPanel dirtyTestPanel = new JPanel();
		dirtyTestPanel.setLayout(new GridLayout(1,2));
		dirtyTestPanel.add(dirtyTestStateBox);
		dirtyTestPanel.add(dirtyTestExceptionBox);
		dirtyTestPanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("Dirty test oracle")));
		return dirtyTestPanel;
	}

	private boolean saveOptions(){
		if (includeSearchOptions){
			int searchDepth =0;
			try {
				searchDepth = Integer.parseInt(searchDepthField.getText());
				if (searchDepth>0) {
					if (Kernel.IS_LIMITATION_SET && searchDepth>Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION){
						kernel.printDialogMessage(LocaleBundle.bundleString("INVALID_SETTING_FOR_EVALUAITON_VERSION"));
						return false;
					}
					else
						options.setSearchDepth(searchDepth);
					kernel.showSystemOptionsInInfoPanel();
				}
				else {
					kernel.printDialogMessage(LocaleBundle.bundleString("Maximum search depth should be a positive integer"));
					return false;
				}
			}
			catch (Exception e) {
				kernel.printDialogMessage(LocaleBundle.bundleString("Maximum search depth should be a positive integer"));
				return false;
			}
			options.setBreadthFirstSearch(breadthFirstSearchButton.isSelected());
			options.setSearchForHomeStates(searchForHomeStatesButton.isSelected());
			options.setTotalOrdering(totalOrderingButton.isSelected());
			options.setPairwiseTesting(pairwiseButton.isSelected());
			try {
				int idDepth = Integer.parseInt(nodeIdDepthField.getText());
				if (idDepth>=0) {
					if (idDepth>searchDepth){
						kernel.printDialogMessage(LocaleBundle.bundleString("ID depth should not be greater"));
						return false;
					} 
					if (Kernel.IS_LIMITATION_SET && idDepth>Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION){
						kernel.printDialogMessage(LocaleBundle.bundleString("INVALID_SETTING_FOR_EVALUAITON_VERSION"));
						return false;
					}
					else
						options.setMaxIdDepth(idDepth);
				}
				else {
					kernel.printDialogMessage(LocaleBundle.bundleString("Maximum ID depth should be a non_negative integer"));
					return false;
				}
			}
			catch (Exception e) {
				kernel.printDialogMessage(LocaleBundle.bundleString("Maximum ID depth should be a non_negative integer"));
				return false;
			}

/*			try {
				int maxRandomTests = Integer.parseInt(maxTestsField.getText());
				if (maxRandomTests>0) {
					if (Kernel.IS_EVALUATION_VERSION && maxRandomTests>Kernel.MAX_RANDOM_TESTS_FOR_EVALUATION_VERSION){
						kernel.print(LocaleBundle.bundleString("INVALID_SETTING_FOR_EVALUAITON_VERSION"));
						return false;
					}
					else
						options.setMaxRandomTests(maxRandomTests);
				}
				else {
					kernel.print(LocaleBundle.bundleString("MAXIMUM_RANDOM_TESTS_SHOULD_BE_A_POSITIVE_INTEGER"));
					return false;
				}
			}
			catch (Exception e) {
				kernel.print(LocaleBundle.bundleString("MAXIMUM_RANDOM_TESTS_SHOULD_BE_A_POSITIVE_INTEGER"));
				return false;
			}
*/			
			try {
				int expansionDepth = Integer.parseInt(maxExpansionLevelField.getText());
				if (expansionDepth>=0) {
					if (expansionDepth>searchDepth){
						kernel.printDialogMessage(LocaleBundle.bundleString("Max expansion depth should not be greater"));
						return false;
					} 
					if (Kernel.IS_LIMITATION_SET && expansionDepth>Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION){
						kernel.printDialogMessage(LocaleBundle.bundleString("INVALID_SETTING_FOR_EVALUAITON_VERSION"));
						return false;
					}
					else
						options.setMaxLevelOfNodeExpansion(expansionDepth);
				}
				else {
					kernel.printDialogMessage(LocaleBundle.bundleString("MAX_EXPANSION_DEPTH_SHOULD_BE_A_POSITIVE_INTERGER"));
					return false;
				}
			}
			catch (Exception e) {
				kernel.printDialogMessage(LocaleBundle.bundleString("MAX_EXPANSION_DEPTH_SHOULD_BE_A_POSITIVE_INTERGER"));
				return false;
			}

		}

		options.setShowsStatesInNodes(showStatesInNodesBox.isSelected());
		TestingManager.DisplayStatesInTestTree = showStatesInNodesBox.isSelected();
		kernel.getTestingManager().updateTreePresentation();
		
		options.setViewTestCode(viewTestCodeBox.isSelected());
		options.setGenerateSeparateTestFile(generateSeparateTestFilesBox.isSelected());
		options.setIncludeSeqIndicesInTestID(includeSeqIndicesInTestIDBox.isSelected());
		if (options.isOOLanguage()){
			options.setCreateObjectReference(createObjectReferenceBox.isSelected());
			options.setGenerateReferenceForMethodCall(referenceForMethodBox.isSelected());
			options.setGenerateReferenceForAccessorCall(referenceForAccessorBox.isSelected());
			options.setGenerateReferenceForMutatorCall(referenceForMutatorBox.isSelected());
		} else
		if (options.getLanguage()==TargetLanguage.C) 
			options.enabeIncludeAssertDefintionForC(includeAssertDefinitionBox.isSelected());
			
		options.setGenerateTestParameters(testParameterBox.isSelected());
		options.setVerifyPostconditions(postconditionBox.isSelected());
		options.setVerifyNegatedConditions(negatedConditionBox.isSelected());
		options.setVerifyMarkings(markingBox.isSelected());
		options.setVerifyEffects(effectBox.isSelected());
		options.setVerifyFirstOccurrence(firstOccurrenceBox.isSelected());
		options.setCreateGoalTags(createGoalTagBox.isSelected());
		options.setGoalTagsAtBeginningOfTests(goalTagsAtBeginningOfTestsButton.isSelected());
		options.setVerifyDirtyTestState(dirtyTestStateBox.isSelected());
		options.setVerifyDirtyTestException(dirtyTestExceptionBox.isSelected());
		if (options == kernel.getSystemOptions()) // not for imported tree
			options.saveSystemOptionsToFile();
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == postconditionBox || e.getSource() == negatedConditionBox || e.getSource() == markingBox || e.getSource() == effectBox) {
			firstOccurrenceBox.setEnabled(postconditionBox.isSelected() || negatedConditionBox.isSelected() || markingBox.isSelected() || effectBox.isSelected());
			if (e.getSource() == postconditionBox && postconditionBox.isSelected()) {
				markingBox.setSelected(false);
			} else 
			if (e.getSource() == markingBox && markingBox.isSelected()) {
				postconditionBox.setSelected(false);
			} 
		} else
		if (e.getSource() == createGoalTagBox) {
			if (createGoalTagBox.isSelected()){
				GoalTagCode goalTag = kernel.getSystemOptions().getGoalTag();
				if (goalTag!=null)
					new TestFrameworkGoalTagEditor(kernel.getParentFrame(), goalTag);
			}
			goalTagsAtBeginningOfTestsButton.setEnabled(createGoalTagBox.isSelected());
			goalTagsInsideTestsButton.setEnabled(createGoalTagBox.isSelected());
		} else
		if (e.getSource() == createObjectReferenceBox) {
			
		} else 
		if (e.getActionCommand() == Apply) {
			if (saveOptions())
				dispose();
		} else 
		if (e.getActionCommand() == Cancel) {
			dispose();
		}
	}
}
