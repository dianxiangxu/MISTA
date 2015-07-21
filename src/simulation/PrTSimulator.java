package simulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import locales.LocaleBundle;

import edit.GeneralEditor;
import edit.GeneralEditor.SimulatorType;

import mid.AssertionProperty;
import mid.Firing;
import mid.GoalProperty;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;
import pipeprt.dataLayer.PipeTransition;
import pipeprt.gui.PrTPanel;
import testcode.TestCodeGeneratorOnline;
import utilities.FileUtil;

public class PrTSimulator extends PrTEngine implements ActionListener, ItemListener {
	
	private static final long serialVersionUID = 1L;

	private JComboBox initialMarkingsList = new JComboBox();
	private int initMarkingIndex = 0;

	private JTextField firingTextField = new JTextField();
	private JComboBox firableEventsList = new JComboBox();
	private JComboBox firableSubstitutionsList = new JComboBox();
	private JLabel stepNumberText = new JLabel();
	private int maxNumberOfSteps = 1;
	
	private JTextArea currentMarkingTextArea = new JTextArea(3, 30);
	
	private static final String DETERMINISTICPLAY = "Play";
	private static final String RANDOMPLAY = "Random Play";
	private static final String GOBACK = "Go Back";
	private static final String RESET = "Reset";
	private static final String STARTRANDOMSIMULATION = "Start Random Simulation";
	private static final String STOPRANDOMSIMULATION  = "Stop Random Simulation";
	private static final String EXIT = "Exit";

	private static final String EXECUTESELECTEDFIRING = "EXECUTE_SELECTED_FIRING";
	private static final String EXECUTERANDOMFIRING = "EXECUTE_RANDOM_FIRING";
	private static final String STARTRANDOMTESTING = "START_RANDOM_TESTING";
	private static final String STOPRANDOMTESTING  = "STOP_RANDOM_TESTING";

	private JButton playButton;
	private JButton randomButton;
	private JButton gobackButton;
	private JButton resetButton;
	private JButton startRandomSimulationButton;
	private JButton stopRandomSimulationButton;

    private SimulationOptions options = SimulationOptions.readSimulationOptionsFromFile();

	private JTextField timeIntervalTextField = new JTextField(5);

	private JCheckBox autoRestartCheckBox;

	private JCheckBox showCurrentStatesCheckBox;
	private JCheckBox printCurrentStatesCheckBox;
	private JCheckBox verifyGoalsAndAssertionsCheckBox;
	private JCheckBox createLogsCheckBox;
	private final boolean showInitialStatesInConsoleWindow = true;

	private JPanel checkBoxesPanel;
	private JPanel buttonsPanel;
	private JPanel transitionPanel =  new JPanel();
	private JPanel transitionFiringPanel = new JPanel();
	
	private boolean isRandomPlayMode = false;
	private Random randomIndexGenerator = new Random(); 
	
	private ArrayList<Marking> markings = new ArrayList<Marking>(); 
	
	// all possible firings
	private Hashtable<Transition, ArrayList<Substitution>> possibleFirings = new Hashtable<Transition, ArrayList<Substitution>>();
	
	private SimulatorType simulatorType;
		
	public PrTSimulator(GeneralEditor editor, PrTPanel prtPanel, MID mid, SimulatorType simulatorType) throws Exception{
		super(editor, mid, prtPanel);
	   	this.simulatorType = simulatorType;
	    setTitle(getControlPanelTitle());
//	    setTitle(getControlPanelTitle() + (Kernel.IS_EVALUATION_VERSION? " ["+LocaleBundle.bundleString("Max Steps for Evaluation Version")+": "+Kernel.MAX_SEARCH_DEPTH_FOR_EVALUATION_VERSION+"]":""));
	    initializeSimulator();
	    if (simulatorType==SimulatorType.ON_THE_FLY_TESTING){
			codeGenerator = new TestCodeGeneratorOnline(editor, mid);
			if (!codeGenerator.hasTestEngine()){
				setModelPanelsEditingEnabled(true);
				throw new Exception("Online execution failure");
			}
			executeSetup();
		}
	    setAlwaysOnTop(true);	    
		setMainContentPane();

		JFrame parentFrame = editor.getKernel().getParentFrame();
        if (parentFrame != null) {
            Dimension parentSize = parentFrame.getSize(); 
            Point p = parentFrame.getLocation(); 
            setLocation(p.x + parentSize.width /8, p.y + (int)(parentSize.height*0.7));
//            setLocation(p.x, p.y);
        }
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
		        	doExit();
		    }
		});
		pack();
		setVisible(true); 
	}
	
	private String getControlPanelTitle(){
		return simulatorType==SimulatorType.ON_THE_FLY_TESTING? LocaleBundle.bundleString("On The Fly Testing Title")
				: LocaleBundle.bundleString("Simulation Control Panel");
	}

	private void initializeSimulator() throws Exception{
		initLogFiles();
	    buttonsPanel = createButtonsPanel();
	    checkBoxesPanel = createCheckBoxesPanel();
	   	maxNumberOfSteps = editor.getKernel().getSystemOptions().getSearchDepth()+1;
	   	firingTextField.setEnabled(false);
	   	firingTextField.setEditable(false);
	   	firingTextField.setMinimumSize(new Dimension(250, 20));
		timeIntervalTextField.setText(""+options.getTimeInterval());
		timeIntervalTextField.setMinimumSize(new Dimension(50, 20));
	    createInitialMarkingsList();
		resetMarkingTextArea(currentMarkingTextArea);
		if (showInitialStatesInConsoleWindow)
			editor.printInConsoleAreaWithoutTabReset(LocaleBundle.bundleString("Initial state") +": "+ markings.get(0));
		setModelPanelsEditingEnabled(false);
		updateChangeOfMarking();
	}
	
	private void createButtons(){
		playButton = createJButton(simulatorType==SimulatorType.ON_THE_FLY_TESTING? EXECUTESELECTEDFIRING: DETERMINISTICPLAY);
		randomButton = createJButton(simulatorType==SimulatorType.ON_THE_FLY_TESTING? EXECUTERANDOMFIRING: RANDOMPLAY);
		gobackButton = createJButton(GOBACK);
		resetButton = createJButton(RESET);
		startRandomSimulationButton = createJButton(simulatorType==SimulatorType.ON_THE_FLY_TESTING? STARTRANDOMTESTING: STARTRANDOMSIMULATION);
		stopRandomSimulationButton = createJButton(simulatorType==SimulatorType.ON_THE_FLY_TESTING? STOPRANDOMTESTING: STOPRANDOMSIMULATION);
	}
	
    private JPanel createButtonsPanel() {
    	createButtons();
        JPanel pane = new JPanel(); 
        pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pane.add(playButton);
        pane.add(randomButton);
        if (simulatorType==SimulatorType.MODEL_SIMULATION)
        	pane.add(gobackButton);
        pane.add(startRandomSimulationButton);
        pane.add(stopRandomSimulationButton);
        pane.add(resetButton);
        pane.add(createJButton(EXIT));
        return pane;
    }

 	private JCheckBox createJCheckBox(String title, boolean selected) {
		JCheckBox box = new JCheckBox(title);
		box.setSelected(selected);
		box.addActionListener(this);
		return box;
	}

	private JPanel createCheckBoxesPanel(){
		autoRestartCheckBox = createJCheckBox(LocaleBundle.bundleString("AUTO_RESTART"), options.getAutomaticRestart());
		
		showCurrentStatesCheckBox = createJCheckBox(LocaleBundle.bundleString("SHOW_CURRENT_STATE"), options.getShowCurrentStates());
		printCurrentStatesCheckBox = createJCheckBox(LocaleBundle.bundleString("PRINT_CURRENT_STATE"), options.getPrintCurrentStates());
		verifyGoalsAndAssertionsCheckBox = createJCheckBox(LocaleBundle.bundleString("VERIFY_GOALS_AND_ASSERTIONS"), options.getVerifyGoalsAndAssertions());
		createLogsCheckBox = createJCheckBox(LocaleBundle.bundleString("CREATE_LOGS"), options.getCreateLogs());

		verifyGoalsAndAssertionsCheckBox.setEnabled(mid.getGoalProperties().size()>0 || mid.getAssertionProperties().size()>0);
		
		JPanel checkBoxesPanel = new JPanel();
		checkBoxesPanel.add(showCurrentStatesCheckBox);
		checkBoxesPanel.add(printCurrentStatesCheckBox);
		checkBoxesPanel.add(verifyGoalsAndAssertionsCheckBox);
		checkBoxesPanel.add(createLogsCheckBox);
//		checkBoxesPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));
		return checkBoxesPanel;
	}
	//
	
	private JPanel createRestartPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(timeIntervalTextField, BorderLayout.WEST);
		panel.add(autoRestartCheckBox, BorderLayout.EAST);
		autoRestartCheckBox.addActionListener(this);
		return panel;
	}

    private void setMainContentPane() {
        JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        contentPane.setLayout(new BorderLayout());

        JPanel firing = new JPanel();
        firing.setLayout(new BorderLayout());

        firing.add(checkBoxesPanel,BorderLayout.CENTER);
        updateTransitionFiringPanel();  // transitionFiringPanel depends on check boxes
        firing.add(transitionFiringPanel,BorderLayout.NORTH);   
		firing.setBorder(new TitledBorder(new EtchedBorder(), ""));
        
        contentPane.add(firing, BorderLayout.CENTER);
        contentPane.add(buttonsPanel,BorderLayout.SOUTH);
        
        setGUIComponentsEnabled();

        setContentPane(contentPane);
    }
    
	private void resetMarkingTextArea(JTextArea textArea){
		textArea.setFont(editor.getTextFont());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
	}
	
	private void computePossibleFirings(){
		possibleFirings.clear();
		Marking marking = markings.get(markings.size()-1);
		for (Transition transition: mid.getTransitions()){
			Unifier unifier = new Unifier(transition, marking);
			ArrayList<Substitution> substitutions =  unifier.getSubstitutions();
			if (substitutions.size()>0)
				possibleFirings.put(transition, substitutions);
		}
		if (possibleFirings.size()==0){
			editor.printInConsoleArea(LocaleBundle.bundleString("No transition is firable"), false);
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
    private ArrayList<Transition> getFirableTransitions(){
		ArrayList<Transition> firableTransitions = new ArrayList<Transition>();
/*		
		Enumeration<Transition> keys = possibleFirings.keys();
		while (keys.hasMoreElements()) {
			firableTransitions.add(keys.nextElement());
		}
*/
		// 6/13/2012: ensure the ordering of firable transitions is the same for different sessions
		for (Transition transition: mid.getTransitions()) {
			if (possibleFirings.get(transition)!=null)
				firableTransitions.add(transition);
		}
    	return firableTransitions;
    }

    private ArrayList<Transition> getOtherNonDeterministicFirableTransitions(Transition transition, Substitution substitution){
		ArrayList<Transition> nondeterministicTransitions = new ArrayList<Transition>();
		Enumeration<Transition> keys = possibleFirings.keys();
		while (keys.hasMoreElements()) {
			Transition currentTransition = keys.nextElement();
			if (currentTransition!=transition && currentTransition.getEvent().equals(transition.getEvent())) {
				for (Substitution sub: possibleFirings.get(currentTransition))
					if (sub.equals(substitution)){
						nondeterministicTransitions.add(currentTransition);
						break;
					}
			}
		}
    	return nondeterministicTransitions;
    }

    
	private void highlightFirableTransitionsInPrTPanel(){
		for (Transition transition: mid.getTransitions()){
//			String transitionId = transition.getId();
//			PipeTransition pipeTransition = prtPanel.getModel().getTransitionById(transitionId);
			PipeTransition pipeTransition = mid.getPipeTransition(transition);
			pipeTransition.setHighlighted(possibleFirings.get(transition)!=null);
		}
	}
	
	private void createInitialMarkingsList(){
		initMarkingIndex = 0;
		markings.add(mid.getInitialMarkings().get(initMarkingIndex));
		for (int index=0; index<mid.getInitialMarkings().size(); index++)
			initialMarkingsList.addItem(Integer.toString(index+1)+"  ");
		initialMarkingsList.setSelectedIndex(initMarkingIndex);
		initialMarkingsList.addItemListener(this);
	}

    private JPanel createMarkingPanel(){    	
    	JPanel upperPart = new JPanel();
    	upperPart.add(new JLabel(LocaleBundle.bundleString("Current State")+": "));	
    	stepNumberText.setText((markings.size()-1) + " ");
       	upperPart.add(stepNumberText);
        upperPart.add(new JLabel(LocaleBundle.bundleString("STEP_FROM_INITIAL_STATE")));
    	upperPart.add(initialMarkingsList);
    	currentMarkingTextArea.setText(formatMarkingString(markings.get(markings.size()-1)));
    	
       	JPanel markingPanel = new JPanel();
       	markingPanel.setLayout(new BorderLayout());
       	markingPanel.add(upperPart, BorderLayout.NORTH);
       	markingPanel.add(new JScrollPane(currentMarkingTextArea), BorderLayout.CENTER);
		markingPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));

    	return markingPanel;
    }

    private boolean containVariable(ArrayList<String> arguments, String variable){
        for (String argument: arguments)
                if (argument.equals(variable))
                        return true;
        return false;
    }
    
    private ArrayList<String> collectVariables(Transition transition, Substitution substitution){
		ArrayList<String> variables = new ArrayList<String>();
		for (String var: transition.getFormalParameters())
			variables.add(var);
		for (String var: substitution.getAllVariables())
			if (!containVariable(variables, var))
				variables.add(var);
		return variables;
    }

    synchronized private void updateFirableSubstitutionsList(Transition transition){
    	firableSubstitutionsList = new JComboBox();
    	if (transition!=null){
    		ArrayList<Substitution> firableSubstitutions = possibleFirings.get(transition); 
    		if (firableSubstitutions!=null){
    			int substitutionIndex=1;
    			for (Substitution substitution: firableSubstitutions){
    				firableSubstitutionsList.addItem(substitutionIndex+". "+substitution.toString(collectVariables(transition, substitution)));
    				substitutionIndex++;
    			}
    		}
    	}
    	firableSubstitutionsList.setEnabled(!isRandomPlayMode && firableSubstitutionsList.getItemCount()>0);
		firableSubstitutionsList.setMinimumSize(new Dimension(250, 20));
		if (firableSubstitutionsList.getItemCount()>0)
			firableSubstitutionsList.setSelectedIndex(0);
    	firableSubstitutionsList.addItemListener(this);
    	firableSubstitutionsList.updateUI();
    }
    
    synchronized private void updateFirableEventsList(){
    	firableEventsList = new JComboBox(); 
    	ArrayList<Transition> firableTransitions = getFirableTransitions(); 
    	int transitionIndex=1;     	// JComboBox does not handle duplicate items properly 
     	for (Transition istaTransition: firableTransitions){
    		firableEventsList.addItem(transitionIndex+". "+istaTransition.getEvent()+istaTransition.printVariableList(istaTransition.getFormalParameters()));
    		transitionIndex++;
    	}
    	firableEventsList.setEnabled(!isRandomPlayMode && firableTransitions.size()>0);
		firableEventsList.setMinimumSize(new Dimension(250, 20));
		if (firableTransitions.size()>0)
			firableEventsList.setSelectedIndex(0);
    	firableEventsList.addItemListener(this);
    	firableEventsList.updateUI();
    }
    
    private void updateFiringTextField(){
    	if (firableEventsList.getItemCount()==0) {
    		firingTextField.setText("     ");
    	} else {
    		Transition transition = getFirableTransitions().get(firableEventsList.getSelectedIndex());
    		Substitution substitution = possibleFirings.get(transition).get(firableSubstitutionsList.getSelectedIndex());
    		firingTextField.setText(" "+transition.getEvent()+getActualParameterList(transition, substitution)+" ");
    	}
    } 
    
    private JPanel updateTransitionPanel(){    	
    	transitionPanel.removeAll();
       	transitionPanel.setLayout(new GridBagLayout());

	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    transitionPanel.add(new JLabel(LocaleBundle.bundleString("SELECT FIRING")+": "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    transitionPanel.add(firingTextField, gridBagConstraints);
//	    transitionPanel.add(new JLabel(LocaleBundle.bundleString("Transition firing")), gridBagConstraints);

	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    transitionPanel.add(new JLabel(LocaleBundle.bundleString("Event")+": "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    transitionPanel.add(firableEventsList, gridBagConstraints);

	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 2;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    transitionPanel.add(new JLabel(LocaleBundle.bundleString("Parameters")+": "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    transitionPanel.add(firableSubstitutionsList, gridBagConstraints);

	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 3;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    transitionPanel.add(new JLabel(LocaleBundle.bundleString("Interval")+"(ms): "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    transitionPanel.add(createRestartPanel(), gridBagConstraints);
	    
	    transitionPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));
       	
	    transitionPanel.updateUI();
    	return transitionPanel;
    }
	
	private void updateTransitionFiringPanel() {
		transitionFiringPanel.removeAll();
		int numberOfColumns =1;
		if (showCurrentStatesCheckBox.isSelected())
			numberOfColumns++;
		transitionFiringPanel.setLayout(new GridLayout(1,numberOfColumns));
		if (showCurrentStatesCheckBox.isSelected())
			transitionFiringPanel.add(createMarkingPanel());
		transitionFiringPanel.add(transitionPanel);
//		transitionFiringPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));
		transitionPanel.updateUI();
	}

	private void checkGoalsAndAssertions(Marking marking){
		for (GoalProperty goal: mid.getGoalProperties())
			if (marking.isFirable(goal))
				editor.printInConsoleArea(LocaleBundle.bundleString("THE_CURRENT_STATE_HAS_REACHED_THE_FOLLOWING_GOAL")+": "+goal.getPropertyString());
		for (AssertionProperty assertion: mid.getAssertionProperties())		
			if (!marking.isAssertionTrue(assertion))
				editor.printInConsoleArea(LocaleBundle.bundleString("THE_CURRENT_STATE_HAS_VIOLATED_THE_FOLLOWING_ASSERTION")+": "+assertion.getAssertionString());
	}
	
   void executeTransitionFiring() {
		Marking currentMarking = markings.get(markings.size()-1);
		Transition transition = getFirableTransitions().get(firableEventsList.getSelectedIndex());
		Substitution substitution = possibleFirings.get(transition).get(firableSubstitutionsList.getSelectedIndex());
		record(transition, substitution);
		editor.printInConsoleAreaWithoutTabReset(markings.size()+". "+transition.getEvent()+getActualParameterList(transition, substitution));
		Marking newMarking = mid.fireTransition(currentMarking, transition, substitution);
		if (printCurrentStatesCheckBox.isSelected())
			editor.printInConsoleAreaWithoutTabReset("\t"+newMarking);
		if (verifyGoalsAndAssertionsCheckBox.isSelected())
			checkGoalsAndAssertions(currentMarking);
		if (simulatorType==SimulatorType.ON_THE_FLY_TESTING && !mid.isHidden(transition.getEvent())){
			try {
				codeGenerator.executeTestInput(transition, substitution);
				ArrayList<Transition> nondeterministicTransitions = getOtherNonDeterministicFirableTransitions(transition, substitution);
				Transition successfulTransition = codeGenerator.executeNondeterministicTestOracles(currentMarking, transition, substitution, newMarking, nondeterministicTransitions);
				if (successfulTransition==null) {
					logFailure();
					testFailure(LocaleBundle.bundleString("ON_THE_FLY_TESTING_FAILED"));
					return;
				} else if (successfulTransition!=transition){ // non-deterministic case
					firingLog.set(firingLog.size()-1, new Firing(successfulTransition, substitution));
					newMarking = mid.fireTransition(currentMarking, successfulTransition, substitution);
				}
			}
			catch (Exception exception){
				unrecord();
				testFailure(LocaleBundle.bundleString("FAIL_TO_EXECUTE_THE_TEST")+exceptionMessage(exception));
				return;
			}
		}
		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled() && editor.getSubModels().size()>0){
			PrTPanel subModel = findSubModelForTransition(transition);
			if (subModel!=null)
				editor.selectSubModelPanel(subModel);
			else 
				editor.setToModelTab();
		} else
			editor.setToModelTab();
		markings.add(newMarking);
		updateChangeOfMarking();
    }
    
    private void testFailure(String message){
		if (simulatorType==SimulatorType.ON_THE_FLY_TESTING && isRandomPlayMode && autoRestartCheckBox.isSelected()) {
			editor.printInConsoleAreaWithoutTabReset(message);		
			restart();
		} else {
			editor.getKernel().printDialogMessage(message);					
			if (randomSimulationTask!=null){
				randomSimulationTask.cancel(true);
				try {
					Thread.sleep(200);
					randomSimulationTask.get();
				} catch (Exception ex) {}
				randomSimulationTask=null;
			}
			setRandomPlayMode(false);
			playButton.setEnabled(false);
			randomButton.setEnabled(false);
			startRandomSimulationButton.setEnabled(false);
			stopRandomSimulationButton.setEnabled(false);
			resetButton.setEnabled(true);
		}
    }
    
    private void updateChangeOfMarking(){
		currentMarkingTextArea.setText(formatMarkingString(markings.get(markings.size()-1)));
		stepNumberText.setText(""+(markings.size()-1));
		computePossibleFirings();
		updateFirableEventsList();
		if (firableEventsList.getItemCount()>0)
			updateFirableSubstitutionsList(getFirableTransitions().get(firableEventsList.getSelectedIndex()));
		else
			updateFirableSubstitutionsList(null);
		updateFiringTextField();
		updateTransitionPanel();
		setGUIComponentsEnabled();
		if (prtPanel!=null){
			highlightFirableTransitionsInPrTPanel();
			setTokensInPrTPlaceForSimulation(markings.get(markings.size()-1));
			updateModelPanelUIs();
		}
    }
   
    private void setGUIComponentsEnabled(){
    	playButton.setEnabled(!isRandomPlayMode && possibleFirings.size()>0 && markings.size()<maxNumberOfSteps);
    	randomButton.setEnabled(!isRandomPlayMode && possibleFirings.size()>0 && markings.size()<maxNumberOfSteps);
    	gobackButton.setEnabled(!isRandomPlayMode && markings.size()>1);
    	resetButton.setEnabled(!isRandomPlayMode);
//		randomSimulationButton.setText(isRandomPlayMode? LocaleBundle.bundleString(STOPRANDOMSIMULATION): LocaleBundle.bundleString(STARTRANDOMSIMULATION));
//    	randomSimulationButton.setEnabled(possibleFirings.size()>0 && markings.size()<maxNumberOfSteps);
    	startRandomSimulationButton.setEnabled(!isRandomPlayMode && possibleFirings.size()>0 && markings.size()<maxNumberOfSteps);
    	stopRandomSimulationButton.setEnabled(isRandomPlayMode);
    	initialMarkingsList.setEnabled(!isRandomPlayMode);
    	firableEventsList.setEnabled(!isRandomPlayMode);
    	firableSubstitutionsList.setEnabled(!isRandomPlayMode);
    	timeIntervalTextField.setEnabled(!isRandomPlayMode);
		createLogsCheckBox.setEnabled(markings.size()<=1);
    }
    
    private void doRandomSelection(){
    	firableEventsList.setSelectedIndex(randomIndexGenerator.nextInt(firableEventsList.getItemCount()));
    	firableSubstitutionsList.setSelectedIndex(randomIndexGenerator.nextInt(firableSubstitutionsList.getItemCount()));
    }
    
	class RandomSimulationTask extends SwingWorker<Boolean, Void> {
		int timeInterval=1000;
		RandomSimulationTask(int timeInterval){
			this.timeInterval = timeInterval;
		}
	     @Override
	    public Boolean doInBackground() {
	    	do {
	    		while (!isCancelled() && possibleFirings.size()>0 && markings.size()<maxNumberOfSteps){
	    			doRandomSelection();
	    			executeTransitionFiring();
	    			try {
	    				Thread.sleep(timeInterval);
	    			} catch (Exception e){}
	    		}
	    	} while (!isCancelled() && restart());
		    return true;
	    }
	     
	    public void done() {
		    if (possibleFirings.size()==0 || markings.size()>=maxNumberOfSteps)
		    	setRandomPlayMode(false);
	    }
	}

	private boolean restart(){
		if (isRandomPlayMode && autoRestartCheckBox.isSelected()) {
			editor.printInConsoleArea("", true);
			int newInitIndex = randomIndexGenerator.nextInt(initialMarkingsList.getItemCount());
			if (initialMarkingsList.getSelectedIndex()==newInitIndex)
				resetInitialMarking();
			else
				initialMarkingsList.setSelectedIndex(newInitIndex);
			return true;
		} else
			return false;
	}
	
	private void executeSetup(){
		try { 
			codeGenerator.executeSetUp(markings.get(0));
		}
		catch (Exception exception) {
			editor.printInConsoleAreaWithoutTabReset(LocaleBundle.bundleString("FAIL_TO_EXECUTE_THE_SETUP")+exceptionMessage(exception));
		}
	}
	
	private void resetInitialMarking(){
		logTrace();
		markings.clear();
		markings.add(mid.getInitialMarkings().get(initMarkingIndex));
		currentMarkingTextArea.setText(formatMarkingString(markings.get(0)));
		if (showInitialStatesInConsoleWindow)
			editor.printInConsoleAreaWithoutTabReset("\n"+LocaleBundle.bundleString("Initial state") +": "+ markings.get(0));
		updateChangeOfMarking();
		repaint();
		if (simulatorType==SimulatorType.ON_THE_FLY_TESTING)
			executeSetup();		
	}
	
	synchronized public void itemStateChanged(ItemEvent e){
       	if (e.getSource() == initialMarkingsList && initialMarkingsList.getSelectedIndex()>=0){
//       		if (mid.getInitialMarkings().get(initialMarkingsList.getSelectedIndex())!= markings.get(0)){
       		if (initialMarkingsList.getSelectedIndex()!= initMarkingIndex){
       			initMarkingIndex = initialMarkingsList.getSelectedIndex();
       			resetInitialMarking();
       		}
       	} else
       	if (e.getSource() == firableEventsList){
			updateFirableSubstitutionsList(getFirableTransitions().get(firableEventsList.getSelectedIndex()));
			updateFiringTextField();
			updateTransitionPanel();
       	} else if (e.getSource() == firableSubstitutionsList){
//    		Transition transition = getFirableTransitions().get(firableEventsList.getSelectedIndex());
//    		Substitution substitution = possibleFirings.get(transition).get(firableSubstitutionsList.getSelectedIndex());
//    		initialMarkingTextArea.setText("Selected firing: "+transition.getEvent()+" "+substitution.toString(transition.getAllVariables()));
			updateFiringTextField();
			transitionPanel.updateUI();
       	}
    }
    
	synchronized void setRandomPlayMode(boolean randomPlay){
		isRandomPlayMode = randomPlay;
		setGUIComponentsEnabled();
	}
	
	public void doExit(){
		if (randomSimulationTask!=null){
			randomSimulationTask.cancel(true);
		}
		possibleFirings.clear();
		if (prtPanel!=null){
			highlightFirableTransitionsInPrTPanel();
			setTokensInPrTPlaceForSimulation(new Marking());
			setModelPanelsEditingEnabled(true);			
			updateModelPanelUIs();
		}
		editor.resetSimulator();
		logTrace();
		closeLogFiles();
		saveOptions();
		dispose();
		if (codeGenerator!=null)
			codeGenerator.terminate();
	}
	
	private void saveOptions(){
		int timeInterval = 0;
		try {
			timeInterval = Integer.parseInt(timeIntervalTextField.getText().trim());
			if (timeInterval>0)
				options.setTimeInterval(timeInterval);
		} catch (Exception ex){
		}
		options.setShowCurrentStates(showCurrentStatesCheckBox.isSelected());
		options.setPrintCurrentStates(printCurrentStatesCheckBox.isSelected());
		options.setVerifyGoalsAndAssertions(verifyGoalsAndAssertionsCheckBox.isSelected());
		options.setCreateLogs(createLogsCheckBox.isSelected());
		options.setAutomaticRestart(autoRestartCheckBox.isSelected());
		options.saveSimulationOptionsToFile();
	}
	
	private ArrayList<Firing> firingLog = new ArrayList<Firing>();
    private BufferedWriter tracesLogFileWriter;
    private BufferedWriter failuresLogFileWriter;
	private File tracesLogFile, failuresLogFile;
	
    private void initLogFiles(){
    	if (editor.getMidFile().getParent()==null)	// newFile: no log
    		return;
    	try {
    		if (simulatorType==SimulatorType.ON_THE_FLY_TESTING) {
    			File[] newLogFiles = FileUtil.getNewTestLogFiles(editor.getMidFile());
    			tracesLogFile = newLogFiles[0];
        		tracesLogFileWriter = new BufferedWriter(new FileWriter(tracesLogFile));
        		failuresLogFile = newLogFiles[1];
        		failuresLogFileWriter = new BufferedWriter(new FileWriter(failuresLogFile));    		
    		} else {
    			tracesLogFile = FileUtil.getNewSimulationLogFile(editor.getMidFile());
        		tracesLogFileWriter = new BufferedWriter(new FileWriter(tracesLogFile));
    		}
    	}
		catch (Exception e) {
		}
    }
    
    private void closeLogFiles(){
    	if (editor.getMidFile().getParent()==null)	// newFile: no log
    		return;
		if (tracesLogFileWriter!=null){
			try{ tracesLogFileWriter.close();} 
			catch (Exception e) {}
		}
		if (failuresLogFileWriter!=null){
			try{ failuresLogFileWriter.close();} 
			catch (Exception e) {}
		}
		if (totalTests>0) {
			if (simulatorType==SimulatorType.ON_THE_FLY_TESTING) {
				editor.printInConsoleArea(LocaleBundle.bundleString("TOTAL_NUMBER_OF_TESTS")+": "+totalTests+"; "+LocaleBundle.bundleString("Log file")+": "+tracesLogFile.getName());
				editor.printInConsoleArea(LocaleBundle.bundleString("TOTAL_NUMBER_OF_FAILED_TESTS")+": "+totalFailures+"; "+LocaleBundle.bundleString("Log file")+": "+failuresLogFile.getName());
			} else {
				if (tracesLogFile!=null)
					editor.printInConsoleArea(LocaleBundle.bundleString("Log file")+": "+tracesLogFile.getAbsolutePath());
			}
		} else {
			try {
				if (tracesLogFile!=null)
					tracesLogFile.delete();
				if (simulatorType==SimulatorType.ON_THE_FLY_TESTING && failuresLogFile!=null)
					failuresLogFile.delete();
			}
			catch (Exception e){}
		}
    }
    
    private void record(Transition transition, Substitution substitution){
    	if (createLogsCheckBox.isSelected())
    		firingLog.add(new Firing(transition, substitution));
    }
    
    private void unrecord(){
    	if (createLogsCheckBox.isSelected() && firingLog.size()>0)
    		firingLog.remove(firingLog.size()-1);
    }
    
    private int totalTests = 0;
    private void logTrace(){
    	if (createLogsCheckBox.isSelected() && firingLog.size()!=0){
    		if (tracesLogFileWriter!=null)
    			writeToFile(tracesLogFileWriter);
			firingLog.clear();
			totalTests++;
    	}
    }

    private int totalFailures =0;
    private void logFailure(){
    	if (createLogsCheckBox.isSelected() && firingLog.size()!=0){
    		if (failuresLogFileWriter!=null)
    			writeToFile(failuresLogFileWriter);
    		totalFailures++;
    	}
    }

    private void writeToFile(BufferedWriter logFileWriter){
    	try {
    		logFileWriter.write("SEQUENCE ");
        	if (mid.getInitialMarkings().size()>0)
        		logFileWriter.write("(INIT "+initMarkingIndex+") ");
        	Firing firing = firingLog.get(0);
        	logFileWriter.write(firing.getTransition().getEvent() + " "+
        		mid.getTransitionIndex(firing.getTransition()) + 
        		firing.getSubstitution().toString(firing.getTransition().getAllVariables()));
        	for (int i=1; i<firingLog.size(); i++){
        		firing = firingLog.get(i);
        		logFileWriter.write(", ");
        		logFileWriter.write(firing.getTransition().getEvent() +" " + mid.getTransitionIndex(firing.getTransition()) + firing.getSubstitution().toString(firing.getTransition().getAllVariables()));        			  
       		}
        	logFileWriter.write("\n");
    	} catch (Exception e){
      	}
    }
    // End of log
        
	private RandomSimulationTask randomSimulationTask;
	
	synchronized public void actionPerformed(ActionEvent e) {
		if (e.getSource() == showCurrentStatesCheckBox){
			updateTransitionFiringPanel();
		} else
		if (e.getActionCommand() == DETERMINISTICPLAY || e.getActionCommand() == EXECUTESELECTEDFIRING) {
			executeTransitionFiring();
		}
		if (e.getActionCommand() == RANDOMPLAY || e.getActionCommand() == EXECUTERANDOMFIRING) {
			doRandomSelection();
			executeTransitionFiring();
		} else
		if	(e.getActionCommand() == GOBACK){
			if (markings.size()>1){
				markings.remove(markings.size()-1);
				unrecord();
				updateChangeOfMarking();
			}
		} else
		if	(e.getActionCommand() == STARTRANDOMSIMULATION || e.getActionCommand() == STARTRANDOMTESTING){
				int timeInterval = 0;
				try {
					timeInterval = Integer.parseInt(timeIntervalTextField.getText().trim());
					if (timeInterval<1)
						JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("Time interval should be a nonnegative integer"));
					else {
						setRandomPlayMode(true);
						createLogsCheckBox.setEnabled(false);
						randomSimulationTask = new RandomSimulationTask(timeInterval);
				        randomSimulationTask.execute();
					}
				} catch (Exception ex){
					JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("Time interval should be a nonnegative integer"));
				}
		} else
		if	(e.getActionCommand() == STOPRANDOMSIMULATION || e.getActionCommand() == STOPRANDOMTESTING){
			if (randomSimulationTask!=null){
				randomSimulationTask.cancel(true);
				try {
					Thread.sleep(200);
					randomSimulationTask.get();
				} catch (Exception ex) {}
				randomSimulationTask=null;
			}
			setRandomPlayMode(false);
			createLogsCheckBox.setEnabled(markings.size()<=1);
		} else
		if	(e.getActionCommand() == RESET){
			resetInitialMarking();
			setGUIComponentsEnabled();
		} else
		if	(e.getActionCommand() == EXIT) {
			doExit();
		}
	}

}
