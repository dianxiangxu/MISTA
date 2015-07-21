/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import kernel.CancellationException;
import kernel.Kernel;
import kernel.ProgressDialog;
import kernel.SystemOptions;
import locales.LocaleBundle;

import mid.MID;
import mid.Marking;
import mid.Predicate;
import mid.Transition;
import mid.Tuple;

public class TransitionTree implements Serializable {

	private static final long serialVersionUID = 1L;

	protected MID mid;
	protected SystemOptions systemOptions;
	
	protected ProgressDialog progressDialog;

	protected TransitionTreeNode root;

	protected int testCount =0;
	protected int numberOfStates=0;
	
	private boolean isTreeGenerated = true;
	
	public TransitionTree(MID mid, SystemOptions systemOptions) {
		this.mid = mid;
		this.systemOptions = systemOptions;
	}

	public TransitionTree(MID mid, SystemOptions systemOptions, boolean generated) {
		this(mid, systemOptions);
		isTreeGenerated = generated;
	}

	protected void createRootNode(){
		root = new TransitionTreeNode(null, null, null);
		Transition constructor = new Transition(MID.ConstructorEvent);
		for (Marking initialMarking: mid.getInitialMarkings()){
			TransitionTreeNode initMarkingNode = new TransitionTreeNode(constructor, null, initialMarking);
			root.add(initMarkingNode);
			if (mid.getInitialMarkings().size()>1 || systemOptions.getMaxIdDepth()>0)
				initMarkingNode.setOutlineNumber(root.children().size()+"");
		}
	}
	
	public MID getMID() {
		return mid;
	}

	public void setMID(MID mid) {
		this.mid = mid;
	}

	public CoverageCriterion getCoverageCriterion(){
		return systemOptions.getCoverageCriterion();
	}

	public SystemOptions getSystemOptions() {
		return systemOptions;
	}

	public TransitionTreeNode getRoot() {
		return root;
	}
	
	public void setRoot(TransitionTreeNode root) {
		this.root = root;
	}

	public void setProgressDialog(ProgressDialog progressDialog){
		this.progressDialog = progressDialog;
	}

	public ProgressDialog getProgressDialog(){
		return progressDialog;
	}
	
	public void checkForCancellation() throws CancellationException {
		if (progressDialog!=null && progressDialog.isCancelled())
			throw new CancellationException(LocaleBundle.bundleString("Test generation cancelled"));
	}
	
	// to be overridden; 
	// TransitionTree cannot be abstract because a tree needs to be created when it is imported from a test data file 
	public void generateTransitionTree() throws CancellationException {
	}
	
	public boolean isTreeGenerated(){
		return isTreeGenerated;
	}

	public int getNumberOfStates(){
		return numberOfStates;
	}

	public void clearTraversedFlags(TransitionTreeNode node) {
		node.resetTraversedFlag();
		for (TransitionTreeNode child: node.children()){
			clearTraversedFlags(child);
		}
	}

	/*
	public ArrayList<TransitionTreeNode> simulateFiringSequence(TransitionTreeNode goalNode){
		ArrayList<TransitionTreeNode> nodes = goalNode.getFiringSequence();
		for (TransitionTreeNode currentNode: nodes) {
			if (mid.isFirable(currentNode.getParent().getMarking(),currentNode.getTransition(), currentNode.getSubstitution())){
				Marking currentState = mid.fireTransition(currentNode.getParent().getMarking(), 
					currentNode.getTransition(), currentNode.getSubstitution());
				System.out.println("\n"+currentNode.getOutlineNumber()
					+ " " + currentNode.getEvent()
					+ currentNode.getSubstitution().toString(currentNode.getTransition().getAllVariables()));
				if (!currentState.equals(currentNode.getMarking()))
					System.out.println("\nThe firing sequence does not reach the state!\n"+currentNode.getMarking());
				else
					System.out.println("\nCurrent state: \n"+currentNode.getMarking());
			} else {
				System.out.println("\nTransition "+ currentNode.getTransition()
						+ "\nis not firable by substitution "+currentNode.getSubstitution().toString(currentNode.getTransition().getAllVariables())
						+ "\nunder marking " + currentNode.getParent().getMarking());
				break;
			}
		}
		return nodes;
	}
*/
	
	public DefaultMutableTreeNode setToMutableTree() {
		return root.setToMutableNode();
	}

	// each leaf indicates a test sequence from the initial state node to the leaf
	// this is to obtain all tests in the tree
	public ArrayList<TransitionTreeNode> getAllTests(){
		ArrayList<TransitionTreeNode> leaves = new ArrayList<TransitionTreeNode>(); 
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf() && isValidTest(node))
				leaves.add(node);
			for (int i=node.children().size()-1; i>=0; i--)
				stack.push(node.children().get(i));
		}
		return leaves;
	}

	public int totalStateTransitions(){
		int total=0;
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			total += node.children().size();
			for (int i=node.children().size()-1; i>=0; i--)
				stack.push(node.children().get(i));
		}
		return total;
	}

	// each leaf indicates a test sequence from the initial state node to the leaf
	// this is to obtain all tests in the tree
	public ArrayList<TransitionTreeNode> getAllTestsForCodeGeneration(){
		ArrayList<TransitionTreeNode> tests = getAllTests();
		if (!Kernel.IS_LIMITATION_SET || tests.size()<=Kernel.MAX_TESTS_FOR_LIMITATION)
			return tests;
		ArrayList<TransitionTreeNode> testsForCodeGeneration = new ArrayList<TransitionTreeNode>(); 
		for (int i=0; i<Kernel.MAX_TESTS_FOR_LIMITATION; i++)
			testsForCodeGeneration.add(tests.get(i));
		return testsForCodeGeneration;
	}
	
	// if the event in a leaf node is hidden (i.e., not for test generation) and it is not the first sibling, then it is not a duplicate test (not valid)
	private boolean isValidTest(TransitionTreeNode test){
		if (mid.isHidden(test.getEvent())) {
			assert test.getParent()!=null;
			Vector<TransitionTreeNode> siblings = test.getParent().children();
			return siblings.indexOf(test)==0;
		}
		else
			return true;
	}

	public ArrayList<TransitionTreeNode> getTestSequence(TransitionTreeNode leaf){
		ArrayList<TransitionTreeNode> sequence = new ArrayList<TransitionTreeNode>();
		TransitionTreeNode currentNode = leaf; 
		while (!currentNode.isRoot()) {
			sequence.add(0, currentNode);
			currentNode = currentNode.getParent();
		}
		return sequence;
	}
	
	public String getSequenceString(ArrayList<TransitionTreeNode> testSequence) throws CancellationException {
		String sequenceString = "";
		for (int i=1; i<testSequence.size(); i++) {
			checkForCancellation();
			TransitionTreeNode currentNode = testSequence.get(i);
			String firing = currentNode.getEvent()+currentNode.getActualParameterList();
			sequenceString += (i>1)? ", "+ firing: firing;
		}
		return sequenceString;
	}

	public int getDeepestTestDepth(){
		int max = 0;
		for (TransitionTreeNode leaf: getAllTests())
			if (leaf.getLevel()>max){
				max = leaf.getLevel();
			}
		return max-1;
	}
	
	public boolean hasDirtyTests(){
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf() && node.isNegative() && isValidTest(node))
				return true;
			for (int i=node.children().size()-1; i>=0; i--)
				stack.push(node.children().get(i));
		}
		return false;
	}

	// each leaf indicates a test sequence from the initial state node to the leaf
	// this is to obtain all tests in the tree
	public ArrayList<TransitionTreeNode> getAllDirtyTests(){
		ArrayList<TransitionTreeNode> leaves = new ArrayList<TransitionTreeNode>(); 
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf() && node.isNegative() && isValidTest(node))
				leaves.add(node);
			for (int i=node.children().size()-1; i>=0; i--)
				stack.push(node.children().get(i));
		}
		return leaves;
	}
	
	public int getNumberOfDirtyTests(){
		return getAllDirtyTests().size();
	}
	
	// metrics for the entire test suite
	
	public String getStatisticsString() {
		computeMetrics();
		String matricsInfo = "\n"+LocaleBundle.bundleString("Number of tests")+": " + getTestCount();
		if (systemOptions.areDirtyTestsNeeded() || getNegativeTestCount()>0)
			matricsInfo += "; "+LocaleBundle.bundleString("Number of dirty tests")+": "+getNegativeTestCount();
		if (this instanceof TransitionTreeForStructureCoverage){
			matricsInfo +="; "+LocaleBundle.bundleString("Number of states")+": "+getNumberOfStates();
			matricsInfo +="; "+LocaleBundle.bundleString("Number of state transitions in transition tree")+": "+totalStateTransitions();
			matricsInfo +="; "+LocaleBundle.bundleString("Number of test actions")+": "+getEventCount();
		}
		matricsInfo += "; "+LocaleBundle.bundleString("Length of longest test")+": " + getDeepestTestDepth()
//		  		+ "; "+LocaleBundle.bundleString("Number of calls")+": " + getEventCount()
//				  		+ "; Assertions: " + getAssertionCount()
//				  		+ "; Parameter items: " + getParaCount()
//				  		+ "\n"+LocaleBundle.bundleString("Longest sequence")+": " + getLongestSequence()
				  		;
//		computeInputItemCount();
//		matricsInfo += " (user input: " + getInputParaCount()+")";
//		matricsInfo += "\nAdditional test statements: "+ getStatementCount();
//		matricsInfo += " (user input: " + getInputStatementCount()+")";
//		matricsInfo += "\n";
		return matricsInfo;
	}

	
	private int eventCount =0;
	private int paraCount = 0;
	private int statementCount = 0; 
	private int assertionCount =0;  
	private int negativeTestCount =0;
	
	private void computeMetrics() {
		clearTraversedFlags(root);
		ArrayList<TransitionTreeNode> allLeaves = getAllTests(); 
		testCount= allLeaves.size();
		eventCount =0;
		paraCount = 0;
		statementCount = 0; 
		assertionCount =0;
		negativeTestCount =0;
		for (TransitionTreeNode leaf : allLeaves) {
			testSequenceMetrics(leaf);
			if (leaf.isNegative())
				negativeTestCount++;
		}
	}	
	
	private void testSequenceMetrics(TransitionTreeNode test) {
		ArrayList<TransitionTreeNode> testSequence = getTestSequence(test);
		for (TransitionTreeNode currentNode: testSequence)
			if (!mid.isHidden(currentNode.getEvent())) {
				eventCount++;
				eventCount += getOptionsCount(currentNode.getTransition().getPrecondition());
				if (!currentNode.isTraversed() || !systemOptions.verifyFirstOccurrence()){
					if (systemOptions.verifyPostconditions())
						assertionCount += getAssertionsCount(currentNode.getTransition().getPostcondition());
					else if (systemOptions.verifyMarkings())
						assertionCount += getAssertionsCount(currentNode.getMarking());
					if (systemOptions.verifyEffects()) {
						ArrayList<Predicate> effects = currentNode.getTransition().getEffect();
						if (effects!=null)
							assertionCount += effects.size();
					}
				}
				ParaTableModel paraTable = currentNode.getParaTable();
				paraCount += paraTable.getParaCount();
				statementCount += paraTable.getNonParaCount();
				currentNode.setTraversed(true);
			}
	}	
	
	private int getOptionsCount(ArrayList<Predicate> preconditions){
		int count =0;
		if (preconditions!=null)
			for (Predicate predicate: preconditions)
				if (mid.isOption(predicate.getName()))
					count++;
		return count;
	}

	private int getAssertionsCount(ArrayList<Predicate> postconditions){
		int count =0;
		if (postconditions!=null)
			for (Predicate predicate: postconditions)
				if (!mid.isHidden(predicate.getName()))
					count++;
		return count;
	}

	private int getAssertionsCount(Marking marking){
		int count =0;
		ArrayList<String> places = marking.getPlaces();
		for (String place: places)
			if (!mid.isHidden(place)) {
				ArrayList<Tuple> tuples = marking.getTuples(place);
				if (tuples!=null)
					count += tuples.size();
			}
		return count;
	}


	public int getTestCount() {
		return testCount;
	}
	
	public int getEventCount() {
		return eventCount;
	}
	
	public int getParaCount() {
		return paraCount;
	}
	
	public int getStatementCount() {
		return statementCount;
	}
	
	public int getAssertionCount() {
		return assertionCount;
	}

	public int getNegativeTestCount() {
		return negativeTestCount;
	}

	// metrics of user input (parameters and statements shared by test cases 	
	private int inputParaCount = 0;
	private int inputStatementCount = 0;
	
	public void computeInputItemCount() {
		inputParaCount = 0;		
		inputStatementCount = 0;
		computeInputCount(root);
	}	
	
	public void computeInputCount(TransitionTreeNode currentNode) {
		if (currentNode!=null) {
			ParaTableModel paraTable = currentNode.getParaTable();
			inputParaCount += paraTable.getParaCount();
			inputStatementCount += paraTable.getNonParaCount();
			if (!currentNode.isLeaf()) {
				for (TransitionTreeNode son: currentNode.children())
					computeInputCount(son);
			}
		}
	}

	public int getInputParaCount() {
		return inputParaCount;
	}
	
	public int getInputStatementCount() {
		return inputStatementCount;
	}
	
}