package simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import netconverter.NetConverter;

import kernel.CancellationException;
import kernel.Kernel;
import kernel.ProgressDialog;

import edit.GeneralEditor;

import locales.LocaleBundle;
import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.UserDefinedSequences;
import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;
import testgeneration.TransitionTreeForStateCoverageBFS;
import utilities.FileExtensionFilter;
import utilities.FileUtil;

@SuppressWarnings("serial")
public class OnTheFlyTestingAnalyzer extends JDialog implements ActionListener {

	private JCheckBox listFailureTestsBox, findShortestPathsBox;
	private JCheckBox evaluateTransitionCoverageBox, evaluateStateCoverageBox;
	private JCheckBox listCoveredStatesBox, listUncoveredStatesBox;	
	private JTextField testLogFileField, failureLogFileField;
	private File testLogFile, failureLogFile;
	
	private static final String BROWSE_FAILURELOG = "Browse Failure Log";
	private static final String BROWSE_TESTLOG= "Browse Test Log";
	
	private static final String APPLY = "Apply";
	private static final String CANCEL = "Cancel";

	private GeneralEditor editor;
	private MID mid;
	
	public OnTheFlyTestingAnalyzer(GeneralEditor editor, MID mid){
    	super(editor.getKernel().getParentFrame(), LocaleBundle.bundleString("TEST_ANALYSIS"), true);
		this.editor = editor;
		this.mid = mid;
		editor.getKernel().getFileChooser().setCurrentDirectory(editor.getMidFile().getParentFile());
		setMainContentPane();
		pack();
        if (editor.getKernel() != null)
    		setLocationRelativeTo(editor.getKernel().getParentFrame());
		setVisible(true); 

	}
	
    private void setMainContentPane() {
    	
        JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createOptionsPanel(),BorderLayout.NORTH);
        contentPane.add(createButtonPane(),BorderLayout.SOUTH);
        setContentPane(contentPane);
    }
    
	private JPanel createOptionsPanel(){		
    	testLogFileField = new JTextField(30);
    	failureLogFileField = new JTextField(30);
    	File[] logFiles = FileUtil.getRecentTestLogFiles(editor.getMidFile());
    	if (logFiles[0]!=null)
    		testLogFileField.setText(logFiles[0].getName());
    	if (logFiles[1]!=null)
    		failureLogFileField.setText(logFiles[1].getName());
		
    	JPanel failureLogFilePanel = new JPanel();
		JLabel failureLogFileLabel = new JLabel(LocaleBundle.bundleString("Failure log file"));
		failureLogFilePanel.add(failureLogFileLabel);
		failureLogFilePanel.add(failureLogFileField);
		failureLogFilePanel.add(createJButton("BROWSE", BROWSE_FAILURELOG));
		
    	JPanel failureLogOptionsPanel = new JPanel();
    	failureLogOptionsPanel.setLayout(new GridLayout(1,2));
    	listFailureTestsBox = new JCheckBox(LocaleBundle.bundleString("LIST_FAILURE_TESTS"));
        listFailureTestsBox.setSelected(editor.getKernel().getSystemOptions().listFailureTests());
        findShortestPathsBox = new JCheckBox(LocaleBundle.bundleString("FIND_SHORTEST_PATHS_TO_FAILURE_STATES"));
        findShortestPathsBox.setSelected(editor.getKernel().getSystemOptions().findShortestPaths());
        failureLogOptionsPanel.add(listFailureTestsBox);
        failureLogOptionsPanel.add(findShortestPathsBox);
        
        JPanel failurePanel = new JPanel();
        failurePanel.setLayout(new BorderLayout());
        failurePanel.add(failureLogFilePanel, BorderLayout.NORTH);
        failurePanel.add(failureLogOptionsPanel, BorderLayout.CENTER);
        failurePanel.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString(""), 0, 0, null, Color.black));
        
    	JPanel testLogFilePanel = new JPanel();
		JLabel testLogFileLabel = new JLabel(LocaleBundle.bundleString("Test log file"));
		testLogFilePanel.add(testLogFileLabel);
		testLogFilePanel.add(testLogFileField);
		testLogFilePanel.add(createJButton("BROWSE", BROWSE_TESTLOG));
		
    	JPanel testLogOptionsPanel = new JPanel();
    	testLogOptionsPanel.setLayout(new GridLayout(2,2));
        evaluateTransitionCoverageBox = new JCheckBox(LocaleBundle.bundleString("EVALUATE_TRANSITION_COVERAGE"));
        evaluateTransitionCoverageBox.setSelected(editor.getKernel().getSystemOptions().evaluateTransitionCoverage());
        evaluateStateCoverageBox = new JCheckBox(LocaleBundle.bundleString("EVALUATE_STATE_COVERAGE"));
        evaluateStateCoverageBox.setSelected(editor.getKernel().getSystemOptions().evaluateStateCoverage());
        evaluateStateCoverageBox.addActionListener(this);
        testLogOptionsPanel.add(evaluateTransitionCoverageBox);
        testLogOptionsPanel.add(evaluateStateCoverageBox);

    	listCoveredStatesBox = new JCheckBox(LocaleBundle.bundleString("LIST_COVERED_STATES"));
    	listCoveredStatesBox.setSelected(editor.getKernel().getSystemOptions().listCoveredStates());
    	
    	listUncoveredStatesBox = new JCheckBox(LocaleBundle.bundleString("LIST_UNCOVERED_STATES"));	
    	listUncoveredStatesBox.setSelected(editor.getKernel().getSystemOptions().listUncoveredStates());
    	listUncoveredStatesBox.setEnabled(evaluateStateCoverageBox.isSelected());
    	
        testLogOptionsPanel.add(listCoveredStatesBox);
        testLogOptionsPanel.add(listUncoveredStatesBox);

        JPanel testPanel = new JPanel();
        testPanel.setLayout(new BorderLayout());
        testPanel.add(testLogFilePanel, BorderLayout.NORTH);
        testPanel.add(testLogOptionsPanel, BorderLayout.CENTER);
        testPanel.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString(""), 0, 0, null, Color.black));

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BorderLayout());
        optionsPanel.add(testPanel, BorderLayout.NORTH);
        optionsPanel.add(failurePanel, BorderLayout.CENTER);
 //       optionsPanel.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString(""), 0, 0, null, Color.black));
        
		return optionsPanel;
	}
   
	private JButton createJButton(String title, String command){
		JButton button = new JButton(LocaleBundle.bundleString(title));
		button.setActionCommand(command);
		button.addActionListener(this);
		return button;
	} 

    private JComponent createButtonPane() {
        JPanel pane = new JPanel(); 
        pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pane.add(createJButton(APPLY, APPLY));
        pane.add(createJButton(CANCEL, CANCEL));
        return pane;
    }
     
	private void saveOptions(){
		editor.getKernel().getSystemOptions().setListFailureTests(listFailureTestsBox.isSelected());
		editor.getKernel().getSystemOptions().setFindShortestPaths(findShortestPathsBox.isSelected());
		editor.getKernel().getSystemOptions().setEvaluateTransitionCoverage(evaluateTransitionCoverageBox.isSelected());
		editor.getKernel().getSystemOptions().setEvaluateStateCoverage(evaluateStateCoverageBox.isSelected());
		editor.getKernel().getSystemOptions().setListCoveredStates(listCoveredStatesBox.isSelected());
		editor.getKernel().getSystemOptions().setListUncoveredStates(listUncoveredStatesBox.isSelected());
		editor.getKernel().getSystemOptions().saveSystemOptionsToFile();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == APPLY) {
			saveOptions();
         	if (listFailureTestsBox.isSelected() || findShortestPathsBox.isSelected() || 
         			evaluateTransitionCoverageBox.isSelected() || evaluateStateCoverageBox.isSelected() ||
         			listCoveredStatesBox.isSelected()){
        		analyzeTests();
        	}
           	dispose();
		} else
		if (e.getActionCommand() == CANCEL){
			dispose();
		} else 
		if (e.getActionCommand() == BROWSE_FAILURELOG) {
			selectLogFile(failureLogFileField);
		} else 
		if (e.getActionCommand() == BROWSE_TESTLOG) {
			selectLogFile(testLogFileField);
		} else
		if (e.getSource()==evaluateStateCoverageBox){
			listUncoveredStatesBox.setEnabled(evaluateStateCoverageBox.isSelected());
		}	
	}

	private void selectLogFile(JTextField logFileNameField){
		JFileChooser fc = editor.getKernel().getFileChooser();
		fc.setSelectedFile( new File(logFileNameField.getText()));		
		File target = FileUtil.chooseFile(editor.getKernel().getParentFrame(), fc, LocaleBundle.bundleString("Open File"),  
				new FileExtensionFilter(FileUtil.LOGFileExtension), 
				FileUtil.OPENFILE);
		if (target!=null)
			logFileNameField.setText(target.getName());
	}
		
	public void analyzeTests(){
		ProgressDialog progressDialog = new ProgressDialog(editor.getKernel().getParentFrame(), LocaleBundle.bundleString("TEST_ANALYSIS"), LocaleBundle.bundleString("ANALYZING_TESTS"));
		Thread testAnalysisThread = new Thread(new TestAnalysisThread(editor, progressDialog));
		testAnalysisThread.start();
		progressDialog.setVisible(true);
	} 

	class TestAnalysisThread implements Runnable {
		
		private GeneralEditor editor;
		private ProgressDialog progressDialog;
		
		TestAnalysisThread(GeneralEditor editor, ProgressDialog progressDialog) {
			this.progressDialog = progressDialog;
			this.editor = editor;
		}
	
		public void run () {
			editor.printInConsoleArea(LocaleBundle.bundleString(""), true);
			try {
				if (evaluateTransitionCoverageBox.isSelected() || evaluateStateCoverageBox.isSelected() || listCoveredStatesBox.isSelected())
					evaluateTestCoverage(progressDialog);
				if (listFailureTestsBox.isSelected() || findShortestPathsBox.isSelected())
					analyzeTestFailures(progressDialog);
			}
			catch (CancellationException e){
				editor.printInConsoleArea(LocaleBundle.bundleString("TEST_ANALYSIS_CANCELLED"), false);
			} 
			catch (Exception exception) {
				if (Kernel.IS_DEBUGGING_MODE)
					exception.printStackTrace();
			}
			progressDialog.dispose();
		}
		
	}
	
	private int getTotalNumberOfSequences(ArrayList<UserDefinedSequences> allSequences){
		int total =0;
		for (UserDefinedSequences sequences: allSequences)
			total +=sequences.getSequences().size();
		return total;
	}

	private void analyzeTestFailures(ProgressDialog progressDialog) throws Exception{
		String failureLogFileName = failureLogFileField.getText().trim();
		if (failureLogFileName.equals("")){
			editor.printInConsoleArea(LocaleBundle.bundleString("No failure log file"));
			return;
		}
		if (failureLogFileName.indexOf(FileUtil.FailLog)<0){
			editor.printInConsoleArea(LocaleBundle.bundleString("Invalid failure log file name"));
			return;
		}		
		failureLogFile = new File(editor.getKernel().getFileChooser().getCurrentDirectory()+File.separator+failureLogFileName);
		if (failureLogFile==null || !failureLogFile.exists()){
			editor.printInConsoleArea(LocaleBundle.bundleString("TEST_FAILURE_LOG_IS_NOT_FOUND")+": "+failureLogFile.getName());
			return;
		}
		try {
			ArrayList<UserDefinedSequences> allFailedTests = MIDParser.parseUserDefinedTestSequences(FileUtil.readTextFile(failureLogFile), mid);
			int totalFailedTests = getTotalNumberOfSequences(allFailedTests);
			if (totalFailedTests>0){
				editor.printInConsoleArea(LocaleBundle.bundleString("TOTAL_NUMBER_OF_FAILED_TESTS")+": "+totalFailedTests, false);
				analyzeTestFailures(allFailedTests, progressDialog);
			} else {
				editor.printInConsoleArea(LocaleBundle.bundleString("THE_LOG_SHOWS_NO_TEST_FAILURE")+": "+failureLogFile.getName());
			}
		}
		catch (ParseException e) {
			editor.printInConsoleArea(LocaleBundle.bundleString("Invalid failure log file") + ": "+failureLogFile.getName(), false);
        }
        catch (TokenMgrError e){
			editor.printInConsoleArea(LocaleBundle.bundleString("Invalid failure log file") + ": "+failureLogFile.getName(), false);
        }
		catch (Exception e){
			throw e;
		}
		
	}

	private void analyzeTestFailures(ArrayList<UserDefinedSequences> allSequences, ProgressDialog progressDialog) throws CancellationException{
		if (findShortestPathsBox.isSelected())
			initLogFile();
		try {
			ArrayList<Marking> initialMarkings = mid.getInitialMarkings();
			for (int initIndex=0; initIndex< initialMarkings.size(); initIndex++) {
				editor.printInConsoleArea(LocaleBundle.bundleString("Initial State")+": "+initialMarkings.get(initIndex), false);
				UserDefinedSequences sequences = allSequences.get(initIndex);
				if (sequences.hasSequences()) {
					ArrayList<Marking> resultMarkings = getResultantMarkings(sequences, initialMarkings.get(initIndex), progressDialog);
					if (findShortestPathsBox.isSelected())
						findShortestPaths(initIndex, resultMarkings, progressDialog);
				}	
			}
			checkForCancellation(progressDialog);
		}
		catch (CancellationException e){
			if (findShortestPathsBox.isSelected()){
				closeLogFile();
			}
			throw e;
		}
		if (findShortestPathsBox.isSelected())
			closeLogFile();
	}
		
	private ArrayList<Marking> getResultantMarkings(UserDefinedSequences sequences, Marking initMarking, ProgressDialog progressDialog) throws CancellationException{
		if (listFailureTestsBox.isSelected()){
			editor.printInConsoleArea(LocaleBundle.bundleString("Failed tests"));
		}
		int numberOfSequences=1;
		ArrayList<Marking> resultMarkings = new ArrayList<Marking>();
		for (FiringSequence firingSequence: sequences.getSequences()){
			Marking newMarking = findResultantMarking(mid, initMarking, firingSequence);
			if (listFailureTestsBox.isSelected()){
				editor.printInConsoleArea("\t"+LocaleBundle.bundleString("Test")+ " "+ numberOfSequences+": "+firingSequence.toCompactString(), false);
				editor.printInConsoleArea("\t\t"+LocaleBundle.bundleString("Failure state")+ ": "+ newMarking, false);
			}
			if (newMarking!=null && !markingExists(resultMarkings, newMarking)) {
				resultMarkings.add(newMarking);
			}
			numberOfSequences++;
			checkForCancellation(progressDialog);
		}
		return resultMarkings;
	}
		
	private void findShortestPaths(int initIndex, ArrayList<Marking> goalMarkings, ProgressDialog progressDialog) throws CancellationException{
		Marking initMarking = mid.getInitialMarkings().get(initIndex);
		ArrayList<FiringSequence> firingPaths = findPGAPaths(initMarking, goalMarkings, progressDialog);
		boolean done = true;		
		for (FiringSequence path: firingPaths) {
			if (path==null) {
				done = false;
				break;
			}
		}
	// use BFS
		if (!done) {
			BFSPathFinder bfsPathFinder = new  BFSPathFinder(progressDialog, mid, editor.getKernel().getSystemOptions().getSearchDepth(), initMarking, goalMarkings, firingPaths);
			firingPaths = bfsPathFinder.findPaths();
		}
		reportPaths(initIndex, firingPaths, goalMarkings);
	}
	
	private ArrayList<FiringSequence> findPGAPaths(Marking initMarking, ArrayList<Marking> goalMarkings, ProgressDialog progressDialog) throws CancellationException{
		// use Planning graph
		NetConverter converter = new NetConverter(mid);
		ArrayList<FiringSequence> firingPaths = converter.analyzePlanningGraph(initMarking, goalMarkings, editor.getKernel().getSystemOptions().getSearchDepth(), progressDialog);
		for (int i=0; i<firingPaths.size(); i++) {
			if (findResultantMarking(mid, initMarking, firingPaths.get(i))==null)
				firingPaths.set(i, null) ;
		}
		return firingPaths;
	}
	
	protected Marking findResultantMarking(MID mid, Marking initMarking, FiringSequence firingSequence) {
	    Marking currentMarking = initMarking;
	    for (Firing firing: firingSequence.getSequence()){
	        Transition transition = firing.getTransition();
	        Substitution substitution = firing.getSubstitution();
			if (mid.isFirable(currentMarking, transition, substitution)){
				currentMarking = mid.fireTransition(currentMarking, transition, substitution);
			}
			else {// not firable
				return null; 
			}
	    }
	    return currentMarking;
	}
	
	private boolean markingExists(ArrayList<Marking> markings, Marking marking){
		for (Marking currentMarking: markings){
			if (marking.equals(currentMarking))
				return true;
		}
		return false;
	}

    private BufferedWriter pathsLogFileWriter;

    private void initLogFile(){
    	try {
    		File[] recentLogFiles = FileUtil.getRecentTestLogFiles(editor.getMidFile());
    		testLogFile = recentLogFiles[0];
    		failureLogFile = recentLogFiles[1];
    		File shortestPathsFile = FileUtil.getPathsLogFile(failureLogFile);
    		pathsLogFileWriter = new BufferedWriter(new FileWriter(shortestPathsFile));    		
    	}
		catch (Exception e) {
		}
    }
    
    private void closeLogFile(){
		if (pathsLogFileWriter!=null){
			try{ pathsLogFileWriter.close();} 
			catch (Exception e) {}
		}

    }

    private String getPathString(int initMarkingIndex, FiringSequence firingPath){
    	StringBuffer pathString = new StringBuffer("SEQUENCE (INIT "+initMarkingIndex+") ");
    	ArrayList<Firing> sequence = firingPath.getSequence();
    	if (sequence.size()>0) {
    		Transition transition = sequence.get(0).getTransition();
    		Substitution substitution = sequence.get(0).getSubstitution();
    		pathString.append (transition.getEvent() + " "+mid.getTransitionIndex(transition) + substitution.toString(transition.getAllVariables()));
    	}
    	for (int i=1; i<sequence.size(); i++){
       		Transition transition = sequence.get(0).getTransition();
    		Substitution substitution = sequence.get(0).getSubstitution();
    		pathString.append(", ");
       		pathString.append(transition.getEvent() + " "+mid.getTransitionIndex(transition) + substitution.toString(transition.getAllVariables()));
       	}
    	pathString.append("\n");
    	return pathString.toString();
    }
    
    private void reportPaths(int initMarkingIndex, ArrayList<FiringSequence> firingPaths, ArrayList<Marking> resultMarkings){
		editor.printInConsoleArea(LocaleBundle.bundleString("SHORTEST_PATHS_TO_FAILURE_STATES")+": ", false);
    	try {
    		for (int index=0; index<firingPaths.size(); index++){
    			FiringSequence sequence=firingPaths.get(index);
    			if (sequence!=null) {
    				editor.printInConsoleArea("\t"+LocaleBundle.bundleString("Failure state")+" "+(index+1)+": "+resultMarkings.get(index), false);
    				editor.printInConsoleArea("\t\t"+LocaleBundle.bundleString("Shortest path")+": "+sequence.toCompactString(), false);
    				pathsLogFileWriter.write(getPathString(initMarkingIndex, sequence));
    			}
    		}
    	} catch (Exception e){
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
      	}
    }

	private void evaluateTestCoverage(ProgressDialog progressDialog) throws Exception {
		String testLogFileName = testLogFileField.getText().trim();
		testLogFile = new File(editor.getKernel().getFileChooser().getCurrentDirectory()+File.separator+testLogFileName);
		if (testLogFileName.equals("") || testLogFile==null){
			editor.printInConsoleArea(LocaleBundle.bundleString("No test log file"));
			return;
		}
		if (!testLogFile.exists()){
			editor.printInConsoleArea(LocaleBundle.bundleString("TESTING_LOG_FILE_IS_NOT_FOUND")+": "+testLogFile.getName());
			return;
		}
		try {
			ArrayList<UserDefinedSequences> allTests = MIDParser.parseUserDefinedTestSequences(FileUtil.readTextFile(testLogFile), mid);
			int totalTests = getTotalNumberOfSequences(allTests);
			if (totalTests>0){
				editor.printInConsoleArea(LocaleBundle.bundleString("TOTAL_NUMBER_OF_TESTS")+": "+totalTests, false);
				if (evaluateTransitionCoverageBox.isSelected())
					evaluateTransitionCoverage(allTests, progressDialog);
				if (evaluateStateCoverageBox.isSelected() || listCoveredStatesBox.isSelected())
					evaluateStateCoverage(allTests, progressDialog);
			} else {
				editor.printInConsoleArea(LocaleBundle.bundleString("THE_LOG_HAS_NO_TESTS")+": "+testLogFile.getName());
			}
		}
		catch (ParseException e) {
			editor.printInConsoleArea(LocaleBundle.bundleString("Invalid test log file") + ": "+testLogFile.getName(), false);
		}
		catch (TokenMgrError e){
			editor.printInConsoleArea(LocaleBundle.bundleString("Invalid test log file") + ": "+testLogFile.getName(), false);
		}
		catch (Exception e){
			throw e;
		}
	}

    private void evaluateTransitionCoverage(ArrayList<UserDefinedSequences> allTests, ProgressDialog progressDialog) throws CancellationException{
    	ArrayList<Transition> allTransitions = new ArrayList<Transition>();
    	for (Transition transition: mid.getTransitions())
    		allTransitions.add(transition);
    	editor.printInConsoleArea(LocaleBundle.bundleString("TOTAL_NUMBER_OF_TRANSITIONS")+": "+mid.getTransitions().size());    	
    	ArrayList<Transition> coveredTransitions = removeCoveredTransitions(allTests, allTransitions, progressDialog);
    	if (coveredTransitions.size()!=mid.getTransitions().size()) {
    		editor.printInConsoleArea(LocaleBundle.bundleString("NUMBER_OF_COVERED_TRANSITIONS")+": "+coveredTransitions.size());
    		for (Transition transition: coveredTransitions)
    			editor.printInConsoleArea("\t"+(coveredTransitions.indexOf(transition)+1)+".\t"+transition.toString().replaceAll("\n", "\n\t\t"));
    		}
    	editor.printInConsoleArea(LocaleBundle.bundleString("NUMBER_OF_UNCOVERED_TRANSITIONS")+": "+(allTransitions.size())
    			+ " "+ getPercentage(allTransitions.size(), mid.getTransitions().size()));
    	for (Transition transition: allTransitions)
        	editor.printInConsoleArea("\t"+(allTransitions.indexOf(transition)+1)+".\t"+transition.toString().replaceAll("\n", "\n\t\t"));
	}
    
    private ArrayList<Transition> removeCoveredTransitions(ArrayList<UserDefinedSequences> allTests, ArrayList<Transition> allTransitions, ProgressDialog progressDialog) throws CancellationException{
    	ArrayList<Transition> coveredTransitions = new ArrayList<Transition>();
    	ArrayList<Marking> initialMarkings = mid.getInitialMarkings();
		for (int initIndex=0; initIndex< initialMarkings.size() && allTransitions.size()>0; initIndex++) {
			UserDefinedSequences sequences = allTests.get(initIndex);
			if (sequences.hasSequences()) {
				for (FiringSequence firingSequence: sequences.getSequences()){
					for (Firing firing: firingSequence.getSequence()) {
						if (allTransitions.contains(firing.getTransition())){
							coveredTransitions.add(firing.getTransition());
							allTransitions.remove(firing.getTransition());
						}
						checkForCancellation(progressDialog);
						if (allTransitions.size()==0)
							return coveredTransitions;
					}	
				}
			}
		}
		return coveredTransitions;
    }
    
    private String getPercentage(int portion, int total) {
    	return total==0? "": "("+(portion*100)/total +"%)";
    }
    
    private void evaluateStateCoverage(ArrayList<UserDefinedSequences> allTests, ProgressDialog progressDialog) throws CancellationException{
    	ArrayList<Marking> coveredMarkings = getCoveredMarkings(allTests, progressDialog);
    	editor.printInConsoleArea(LocaleBundle.bundleString("NUMBER_OF_COVERED_STATES")+": "+coveredMarkings.size());
    	listCoveredStates(coveredMarkings, progressDialog);
    	if (evaluateStateCoverageBox.isSelected()){
    		TransitionTreeForStateCoverageBFS stateTree = new TransitionTreeForStateCoverageBFS(mid, editor.getKernel().getSystemOptions().getSystemOptionForStateGeneration());
    		stateTree.setProgressDialog(progressDialog);
    		stateTree.generateTransitionTree();
    		Hashtable<String, Marking> allReachableMarkings = stateTree.getExpandedMarkings();
    		editor.printInConsoleArea(
    			LocaleBundle.bundleString("TOTAL_NUMBER_OF_STATES")+": "+allReachableMarkings.size()
    			+"\n"+LocaleBundle.bundleString("NUMBER_OF_UNCOVERED_STATES")+": "+(allReachableMarkings.size()-coveredMarkings.size())
    			+" " +getPercentage(allReachableMarkings.size()-coveredMarkings.size(), allReachableMarkings.size())
    			);
    		listUncoveredMarkings(coveredMarkings, allReachableMarkings, progressDialog);
    	}
    }
    
    private void listCoveredStates(ArrayList<Marking> coveredMarkings, ProgressDialog progressDialog) throws CancellationException{
    	if (listCoveredStatesBox.isSelected() && coveredMarkings.size()>0){
    		for (int index=0; index<coveredMarkings.size(); index++){
				editor.printInConsoleArea("\t"+(index+1)+".\t"+coveredMarkings.get(index));
				checkForCancellation(progressDialog);
    		}
    	}
    }
    
    private void listUncoveredMarkings(ArrayList<Marking> coveredMarkings, Hashtable<String, Marking> allReachableMarkings, ProgressDialog progressDialog)  throws CancellationException{
    	if (listUncoveredStatesBox.isSelected()){
        	removeCoveredMarkingsFromAllReachableMarkings(coveredMarkings, allReachableMarkings, progressDialog);
        	if (allReachableMarkings.size()>0){
    			int index=0;
    			Enumeration<String> keys = allReachableMarkings.keys();
    			while (keys.hasMoreElements()){
    				String key = (String)keys.nextElement();
    				Marking marking = allReachableMarkings.get(key);
    				editor.printInConsoleArea("\t"+(index+1)+".\t"+marking.toString());
    				index++;
    				checkForCancellation(progressDialog);
    			}
        	}
    	}
    }
    
    private ArrayList<Marking> getCoveredMarkings(ArrayList<UserDefinedSequences> allTests, ProgressDialog progressDialog) throws CancellationException{
    	ArrayList<Marking> coveredMarkings = new ArrayList<Marking>();
		ArrayList<Marking> initialMarkings = mid.getInitialMarkings();
		for (int initIndex=0; initIndex< initialMarkings.size(); initIndex++) {
			UserDefinedSequences sequences = allTests.get(initIndex);
			if (sequences.hasSequences()) {
				Marking	initMarking = initialMarkings.get(initIndex);
				for (FiringSequence firingSequence: sequences.getSequences()){
					Marking	currentMarking = initMarking; 
					for (Firing firing: firingSequence.getSequence()) {
						currentMarking = mid.fireTransition(currentMarking, firing.getTransition(), firing.getSubstitution());
						if (!markingExists(coveredMarkings, currentMarking))
							coveredMarkings.add(currentMarking);
						checkForCancellation(progressDialog);
					}	
				}
			}
		}
		return coveredMarkings;
	}

    private void removeCoveredMarkingsFromAllReachableMarkings(ArrayList<Marking> coveredMarkings, Hashtable<String, Marking> allMarkings, ProgressDialog progressDialog)  throws CancellationException {
    	for (Marking coveredMarking: coveredMarkings) {
			allMarkings.remove(coveredMarking.getKeyString(mid.getPlaces()));
     		checkForCancellation(progressDialog);
    	}
    }
    
	private void checkForCancellation(ProgressDialog progressDialog) throws CancellationException {
		if (progressDialog!=null && progressDialog.isCancelled())
			throw new CancellationException(LocaleBundle.bundleString("TEST_ANALYSIS_CANCELLED"));
	}

}
