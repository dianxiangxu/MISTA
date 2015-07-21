package edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;
import mid.MID;
import mid.Predicate;
import mid.Transition;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;
import simulation.PrTEngine;
import simulation.PrTOnlineTester;
import simulation.PrTSimulator;
import edit.GeneralEditor.SimulatorType;
import edit.GeneralTablePanel.MIDTableType;

public class ModelPanelTabularNet extends ModelPanel implements ActionListener, DocumentListener{
	private static final long serialVersionUID = 1L;
	private static final int MINROWS  = 30;
	
	private static final String newInitialState = "Add an Initial State";
	private static final String deleteInitialState = "Remove an Empty Initial State";
	private static final String newGoalState = "Add a Goal State";
	private static final String deleteGoalState = "Remove an Empty Goal State";
	private static final String showGoalStates = "Show Goal States";
	private static final String showSinkEvents = "Show Sink Events";
	private static final String showUnitTests = "Show Unit Tests";
	private static final String showSequencesFile = "Show Sequences File";

	private GeneralTablePanel modelTablePanel; 
	private ArrayList<JTextArea> initialStateAreas;
	private ArrayList<JTextArea> goalStateAreas;
	private JTextArea sinkEventsArea;
	private JTextArea unitTestsArea;
	private JTextField sequencesFileField;
	
	private JMenuItem newInitialStateItem, deleteInitialStateItem, newGoalStateItem, deleteGoalStateItem;
	private JCheckBoxMenuItem showGoalStatesItem, showSinkEventsItem, showUnitTestsItem, showSequencesFileItem;

	private MouseAdapter mouseAdapter;
	
	private Vector<Vector<Object>> coreModelRows;
	
	public ModelPanelTabularNet(XMIDEditor editor){
		super(editor);
		createModelMenuItems();
		coreModelRows = new Vector<Vector<Object>>();
		MIDTableType midTableType = getMidTableType(editor.getModelType());
		modelTablePanel = GeneralTablePanel.createModelTablePanel(editor, midTableType, coreModelRows);
		modelTablePanel.setMinRows(MINROWS);
		initialStateAreas = new ArrayList<JTextArea>();
		initialStateAreas.add(createTextArea(""));
		goalStateAreas = new ArrayList<JTextArea>();
		goalStateAreas.add(createTextArea(""));
		sinkEventsArea = createTextArea("");
		unitTestsArea = createTextArea("");
		sequencesFileField = new JTextField(60);
		createModelPanel();
	}
	
	public ModelPanelTabularNet(XMIDEditor editor, Sheet sheet){
		super(editor);
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadModel(sheet);
		createModelMenuItems();
		coreModelRows = xmidLoader.getCoreModelRows();
		MIDTableType midTableType= getMidTableType(editor.getModelType());
		modelTablePanel = GeneralTablePanel.createModelTablePanel(editor, midTableType, coreModelRows);
		modelTablePanel.setMinRows(MINROWS);
		
		initialStateAreas = new ArrayList<JTextArea>();
		for (String initialState: xmidLoader.getInitialStates())
			initialStateAreas.add(createTextArea(initialState));
		goalStateAreas = new ArrayList<JTextArea>();
		for (String goalState: xmidLoader.getGoalStates())
			goalStateAreas.add(createTextArea(goalState));
		String sinkEvents = xmidLoader.getSinkEvents();
		if (!sinkEvents.equals(""))
			showSinkEventsItem.setSelected(true);
		sinkEventsArea = createTextArea(sinkEvents);
		unitTestsArea = createTextArea(xmidLoader.getUnitTests());
		sequencesFileField = new JTextField(xmidLoader.getSequencesFile());
		createModelPanel();
	}

	private MIDTableType getMidTableType(ModelType modelType){
		if (modelType==null)
			return MIDTableType.FUNCTIONNET;
		switch (modelType){
			case FUNCTIONNET: return MIDTableType.FUNCTIONNET;
			case ABAC: return MIDTableType.FUNCTIONNET;
			case STATEMACHINE: return MIDTableType.STATEMACHINE;
			case CONTRACT: return MIDTableType.CONTRACT;
			case THREATNET: return MIDTableType.THREATNET;
			default: return MIDTableType.FUNCTIONNET;
		}
	} 

	@Override
	public PrTEngine createSimulator(MID mid, SimulatorType simulatorType){
		try {
			if (simulatorType==SimulatorType.ONLINE_TEST_EXECUTION)
				return new PrTOnlineTester(editor, null, editor.getTransitionTree());	   	
			else
				return new PrTSimulator(editor, null, mid, simulatorType);	   	
		}
		catch (Exception e){
			return null;
		}
	}

	private void createModelPanel(){
		modelTablePanel.setFont(editor.getTextFont());
		removeAll();
		JSplitPane wholePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPane(), createLowerPane());
        wholePane.setOneTouchExpandable(false);
        wholePane.setDividerLocation((int)(0.35*editor.getKernel().getParentFrame().getHeight()));
        setLayout(new BorderLayout());
        add(wholePane, BorderLayout.CENTER);
        this.addMouseListener(mouseAdapter);
	}
	
	private JTextArea createTextArea(String text){
		JTextArea editArea = editor.isEditing()? new JTextArea(3, 60): new JTextArea();
		editArea.setLineWrap(true);
		editArea.setWrapStyleWord(true);
		editArea.setText(text);
		editArea.setEditable(editor.isEditing());
		editArea.setName("EditArea");
		editArea.setEnabled(true);
		editArea.setBackground(Color.WHITE);
		editArea.setForeground(Color.BLACK);
		editArea.setFont(editor.getTextFont());
		editArea.setTabSize(2);
		editArea.setMargin(new Insets(1,5,5,5));
		editArea.setCaretPosition(0);
		editArea.getDocument().addDocumentListener(this);
		editArea.addMouseListener(mouseAdapter);
		return editArea;
	}
	
	private JPanel createUpperPane(){
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(modelTablePanel, BorderLayout.CENTER);
		tablePanel.addMouseListener(mouseAdapter);
		return tablePanel;
	}

	private JComponent createLowerPane(){
		JPanel lowerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;

			for (JTextArea initState: initialStateAreas){
				initState.addMouseListener(mouseAdapter);
				lowerPanel.add(createTextAreaScrollPane(initState, LocaleBundle.bundleString("Initial State")+" ["+(initialStateAreas.indexOf(initState)+1)+"] "), c);
			}
			if (showGoalStatesItem.isSelected())	
				for (JTextArea goalState: goalStateAreas){
					goalState.addMouseListener(mouseAdapter);
					lowerPanel.add(createTextAreaScrollPane(goalState, LocaleBundle.bundleString("Goal State")+" ["+(goalStateAreas.indexOf(goalState)+1)+"] "), c);
				}
			if (showSinkEventsItem.isSelected())
				lowerPanel.add(createTextAreaScrollPane(sinkEventsArea, LocaleBundle.bundleString("Sink Events")), c);
			if (showUnitTestsItem.isSelected())
				lowerPanel.add(createTextAreaScrollPane(unitTestsArea, LocaleBundle.bundleString("Unit Tests")), c);
			if (showSequencesFileItem.isSelected()){
				JPanel sequencePanel = new JPanel();
				sequencePanel.setLayout(new BorderLayout());
				JLabel sequenceLabel = new JLabel(LocaleBundle.bundleString("Sequences File")+": ");
				sequenceLabel.setFont(editor.getTextFont());
				sequencePanel.add(sequenceLabel, BorderLayout.WEST);
				sequencePanel.add(sequencesFileField, BorderLayout.CENTER);
				lowerPanel.add(sequencePanel, c);
			}
        lowerPanel.addMouseListener(mouseAdapter);
        return lowerPanel;
	}
	
	private JScrollPane createTextAreaScrollPane(JTextArea textArea, String title){
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.addMouseListener(mouseAdapter);
		scrollPane.setBorder(BorderFactory.createTitledBorder(null, title, 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
		return scrollPane;
	}
		
	public JMenu getModelMenu(){
		JMenu modelMenu = new JMenu(LocaleBundle.bundleString("Model"));
		if (editor.isEditing()){
			modelMenu.add(newInitialStateItem);
			modelMenu.add(deleteInitialStateItem);
			modelMenu.addSeparator();
			modelMenu.add(newGoalStateItem);
			modelMenu.add(deleteGoalStateItem);
		}
		modelMenu.add(showGoalStatesItem);
		if (editor.getModelType()!=ModelType.STATEMACHINE && editor.getModelType()!=ModelType.THREATNET){
			modelMenu.addSeparator();
			modelMenu.add(showSinkEventsItem);
			modelMenu.add(showUnitTestsItem);
			modelMenu.add(showSequencesFileItem);
		}
		return modelMenu;
	}
	
	private void createModelMenuItems() {
		newInitialStateItem = createMenuItem(newInitialState, newInitialState);
		deleteInitialStateItem = createMenuItem(deleteInitialState, deleteInitialState);
		newGoalStateItem = createMenuItem(newGoalState, newGoalState);
		deleteGoalStateItem = createMenuItem(deleteGoalState, deleteGoalState);
		showGoalStatesItem = createCheckBoxMenuItem(showGoalStates, showGoalStates, true, this);
		showSinkEventsItem = createCheckBoxMenuItem(showSinkEvents, showSinkEvents, false, this);
		showUnitTestsItem = createCheckBoxMenuItem(showUnitTests, showUnitTests, false, this);
		showSequencesFileItem = createCheckBoxMenuItem(showSequencesFile, showSequencesFile, false, this);

		mouseAdapter = new MouseAdapterForModel(this);
	}

	private JCheckBoxMenuItem showGoalStatesPopItem, showSinkEventsPopItem, showUnitTestsPopItem, showSequencesFilePopItem;
	
	private class MouseAdapterForModel extends MouseAdapter{
		private ActionListener listener;
		MouseAdapterForModel(ActionListener listener){
			this.listener = listener;
		}
		public void mousePressed( MouseEvent e ) { 
			checkForTriggerEvent(e); 
		} 
		public void mouseReleased( MouseEvent e ) { 
			checkForTriggerEvent(e); 
		}
		private void checkForTriggerEvent( MouseEvent e ) { 
			if ( e.isPopupTrigger()) { 
				JPopupMenu popupMenu = new JPopupMenu();
				if (editor.isEditing()){
					popupMenu.add(createMenuItem(newInitialState, newInitialState));
					popupMenu.add(createMenuItem(deleteInitialState, deleteInitialState));
					popupMenu.addSeparator();
				}
				if (editor.isEditing() && showGoalStatesItem.isSelected()){
					popupMenu.add(createMenuItem(newGoalState, newGoalState));
					popupMenu.add(createMenuItem(deleteGoalState, deleteGoalState));
				}
				showGoalStatesPopItem = createCheckBoxMenuItem(showGoalStates, showGoalStates, showGoalStatesItem.isSelected(), listener);
				
				popupMenu.add(showGoalStatesPopItem);
				
				if (editor.getModelType()!=ModelType.STATEMACHINE  && editor.getModelType()!=ModelType.THREATNET){
					popupMenu.addSeparator();
					showSinkEventsPopItem = createCheckBoxMenuItem(showSinkEvents, showSinkEvents, showSinkEventsItem.isSelected(), listener);
					showUnitTestsPopItem = createCheckBoxMenuItem(showUnitTests, showUnitTests, showUnitTestsItem.isSelected(), listener);
					showSequencesFilePopItem = createCheckBoxMenuItem(showSequencesFile, showSequencesFile, showSequencesFileItem.isSelected(), listener);

					popupMenu.add(showSinkEventsPopItem);
					popupMenu.add(showUnitTestsPopItem);
					popupMenu.add(showSequencesFilePopItem);
				}
				popupMenu.show( e.getComponent(), e.getX(), e.getY() );
			}	
		} 
	}
	
	private JMenuItem createMenuItem(String title, String command){
		JMenuItem menuItem = new JMenuItem(LocaleBundle.bundleString(title));
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}
	
	private JCheckBoxMenuItem createCheckBoxMenuItem(String title, String command, boolean selected, ActionListener listener){
		JCheckBoxMenuItem checkBoxItem = new JCheckBoxMenuItem(LocaleBundle.bundleString(title));
		checkBoxItem.setSelected(selected);
		checkBoxItem.setActionCommand(command);
		checkBoxItem.addActionListener(listener);
		return checkBoxItem;
	} 

	public void updateFont(){
		Font font = editor.getTextFont();
		modelTablePanel.setFont(font);
		for (JTextArea initArea: initialStateAreas){
			initArea.setFont(font);
		}
		for (JTextArea goalArea: goalStateAreas){
			goalArea.setFont(font);
		}
		sinkEventsArea.setFont(font);
		unitTestsArea.setFont(font);
		sequencesFileField.setFont(font);
		updateUI();
	}
	
	 // implements ActionListener
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == newInitialState){
			initialStateAreas.add(createTextArea(""));
		} else
		if (cmd == deleteInitialState){
			removeState(initialStateAreas);
		}else
		if (cmd == newGoalState){
			goalStateAreas.add(createTextArea(""));
		} else
		if (cmd == deleteGoalState){
			removeState(goalStateAreas);
		} else
		if (cmd == showGoalStates) {
			if (e.getSource() == showGoalStatesPopItem)
				showGoalStatesItem.setSelected(showGoalStatesPopItem.isSelected());
			newGoalStateItem.setEnabled(showGoalStatesItem.isSelected());
			deleteGoalStateItem.setEnabled(showGoalStatesItem.isSelected());			
		} else
		if (cmd == showSinkEvents) {
				if (e.getSource() == showSinkEventsPopItem)
					showSinkEventsItem.setSelected(showSinkEventsPopItem.isSelected());
		} else
		if (cmd == showUnitTests) {
			if (e.getSource() == showUnitTestsPopItem)
				showUnitTestsItem.setSelected(showUnitTestsPopItem.isSelected());
		} else
		if (cmd == showSequencesFile) {
			if (e.getSource() == showSequencesFilePopItem)
				showSequencesFileItem.setSelected(showSequencesFilePopItem.isSelected());
		}
		createModelPanel();
		updateUI();
	}
	
	public void saveModel(File file, Sheet sheet, CellStyle lineWrapStyle){
		int rowIndex = saveModelHeader(sheet, lineWrapStyle);
       GeneralTablePanel.MIDTableType midTableType = getMidTableType(editor.getModelType());
       rowIndex = XMIDProcessor.createTableModelTypeRow(editor.getModelType(), sheet, rowIndex);
       rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(midTableType), sheet, rowIndex);
       rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.MODEL_KEYWORD, coreModelRows, sheet, rowIndex, lineWrapStyle);
       for (JTextArea stateArea: initialStateAreas)
			rowIndex = XMIDProcessor.createSplitColumnsRow(XMIDProcessor.INIT_KEYWORD, stateArea, sheet, rowIndex, lineWrapStyle);
       for (JTextArea stateArea: goalStateAreas)
			rowIndex = XMIDProcessor.createSplitColumnsRow(XMIDProcessor.GOAL_KEYWORD, stateArea, sheet, rowIndex, lineWrapStyle);
       rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.SINKS_KEYWORD, sinkEventsArea.getText(), sheet, rowIndex, lineWrapStyle);
       rowIndex = XMIDProcessor.createSplitColumnsRow(XMIDProcessor.UNITTESTS_KEYWORD, unitTestsArea, sheet, rowIndex, lineWrapStyle);
       rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.SEQUENCES_KEYWORD, sequencesFileField.getText(), sheet, rowIndex, lineWrapStyle);
 	}

	/////////////////////////////////////////////////////
	public void parse(MID mid) throws ParseException {
		parseCoreModelRows(mid);
		parseInitialState(mid, initialStateAreas);
		parseGoalProperty(mid, goalStateAreas);
		parseSinkEvents(mid);
		parseUnitTests(mid);
		parseSequencesFile(mid);
	}
	
	private void parseCoreModelRows(MID mid) throws ParseException{
		for (int index=0; index<coreModelRows.size(); index++){
			Vector<Object> row = coreModelRows.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			String rowInfo = LocaleBundle.bundleString("Model Row")+" "+(index+1)+" - ";
			if (row.get(1)==null || row.get(1).toString().trim().equals(""))
				throw new ParseException(rowInfo+LocaleBundle.bundleString("transition-module-event is expected"));				
			String eventSignature = row.get(1).toString();
			String precondString = row.get(2)!=null? row.get(2).toString(): "";
			String postcondString = row.get(3)!=null? row.get(3).toString(): "";
			String whenString = row.get(4)!=null? row.get(4).toString(): "";
			String effectString = "";
			if (row.size()>5 && row.get(5)!=null)
				effectString = row.get(5).toString();
			String guardString = "";
			if (row.size()>6 && row.get(6)!=null)
				guardString = row.get(6).toString();
			Transition transition = null; 
			try {
				switch (editor.getModelType()) {
					case CONTRACT:  
						if (precondString.equals("") && postcondString.equals(""))
							throw new ParseException(LocaleBundle.bundleString("precondition-postcondition expected"));
						transition = MIDParser.parseContractTransition(eventSignature, precondString, postcondString, whenString, effectString); 
						mid.addTransition(transition);
						break;
					case THREATNET:	
					case ABAC: 
					case FUNCTIONNET: 
						if (precondString.equals("") && postcondString.equals(""))
							throw new ParseException(LocaleBundle.bundleString("precondition-postcondition expected"));
						transition = MIDParser.parseNetTransition(eventSignature, precondString, postcondString, whenString, effectString, guardString); 
						mid.addTransition(transition);
						break;
					case STATEMACHINE: 
						if (precondString.equals("") || postcondString.equals(""))
							throw new ParseException(LocaleBundle.bundleString("both start and end states are expected"));						
						transition = MIDParser.parseStateMachineTransition(precondString, postcondString, eventSignature, whenString, effectString); 
						mid.addTransition(transition);
						break;
					default: transition = MIDParser.parseNetTransition(eventSignature, precondString, postcondString, whenString, effectString, guardString); 
						break;
				}
			}
			catch (ParseException e) {
				throw new ParseException(rowInfo+e.toString());
			}
			catch (TokenMgrError e){
				throw new ParseException(rowInfo+": "+LocaleBundle.bundleString("Lexical error"));				
			}
		}
	}
	
	private void parseSinkEvents(MID mid) throws ParseException {
		String sinkEventsString = sinkEventsArea.getText().trim();
		if (sinkEventsString.equals(""))
			return;
		try {
			ArrayList<String> sinkEvents = MIDParser.parseIdentifierListString(sinkEventsString);
			mid.setSinkEvents(sinkEvents);
		}
		catch (ParseException e) {
			throw new ParseException(sinkEventsString+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(sinkEventsString+": "+LocaleBundle.bundleString("Lexical error"));				
		}
	}
	
	private void parseUnitTests(MID mid) throws ParseException {
		String unitTestString = unitTestsArea.getText().trim();
		if (unitTestString.equals(""))
			return;
		ArrayList<Predicate> unitTests = null;
		try {
			unitTests = MIDParser.parseConditionString(unitTestString);
		}
		catch (ParseException e) {
			throw new ParseException(LocaleBundle.bundleString("Unit tests")+": "+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(LocaleBundle.bundleString("Unit tests")+": "+LocaleBundle.bundleString("Lexical error"));				
		}
		for (Predicate test: unitTests)
			mid.addUnitTest(test);
	}

	private void parseSequencesFile(MID mid) throws ParseException{
		mid.setSequencesFile(sequencesFileField.getText().trim());
	}

}
