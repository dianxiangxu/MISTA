/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import kernel.TestingManager;

import mid.Firing;
import mid.FiringSequence;
import mid.Marking;
import mid.Substitution;
import mid.Transition;


public class TransitionTreeNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String RIGHTARROW = "\u2192";
	public static final int STATESTRLENGTH = 200;
	
	private Transition transition;
	private Substitution substitution;
	private Marking	marking;
	private boolean negative;
	
	private ParaTableModel paraTable;

	private boolean traversed = false;
	private boolean expanded = false;

	private String outlineNumber = ""; // example: 1.2.3 
	private int depth = 0;
	
	private String testInputCode;
	private String testOracleCode;
	
	public TransitionTreeNode(Transition transition, Substitution substitution, Marking marking, boolean negative) {
		this.transition = transition;
		this.substitution = substitution;
		this.marking = marking;
		this.negative = negative;
		paraTable = new ParaTableModel();
	}

	public TransitionTreeNode(Transition transition, Substitution substitution, Marking marking) {
		this(transition, substitution, marking, false);
	}	

	public ParaTableModel getParaTable() {
		return paraTable;
	}
	
	public void setParaTable(ParaTableModel table) {
		this.paraTable = table;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	public String getOutlineNumber() {
		return outlineNumber;
	}

	public String setOutlineNumber(String outlineNumber) {
		return this.outlineNumber=outlineNumber;
	}

	public void resetChildrenOutlineNumbers(int maxDepth) {
		if (children.size()<1 || getLevel()>maxDepth)
			return;
		for (int i=0; i<children.size(); i++) {
			TransitionTreeNode child = (TransitionTreeNode)children.get(i);
			child.depth = depth+1;
			child.outlineNumber = isRoot()? "" + (i+1) : outlineNumber + "." + (i+1); 		
			child.resetChildrenOutlineNumbers(maxDepth);
		}
	}

	public boolean isExpanded(){
		return expanded;
	}

	public void setExpanded(boolean expanded){
		this.expanded = expanded;
	}
	
	public boolean isTraversed(){
		return traversed;
	}
	
	public void setTraversed(boolean flag){
		traversed = flag;
	}
	
	public void resetTraversedFlag(){
		traversed = false;
	}
	
	public Transition getTransition() {
		return transition;
	}
	
	public void setTransition(Transition t) {
		 transition = t;
	}

	public Substitution getSubstitution() {
		return substitution;
	}

	public void setSubstitution(Substitution substitution) {
		this.substitution = substitution;
	}

	public String getEvent() {
		return transition.getEvent();
	}

	public Marking getMarking(){
		return marking;
	}

	public String getTestCaseId() {
		return outlineNumber.replace('.', '_');
	}

	// used as the node title in the test tree
	public String toString() {
		return nodeIdentityString(TestingManager.DisplayStatesInTestTree);
	}

	// used as the node title in the test tree or parameter table 
	public String nodeIdentityString(boolean includeState) {
		if (isRoot())
			return "ROOT";
		StringBuffer buffer = new StringBuffer(outlineNumber);
		buffer.append("  ");
		buffer.append(transition.getEvent());
		buffer.append(getActualParameterList());
		if (transition.hasGuard()) {
			buffer.append("[");
			buffer.append(transition.getGuard());
			buffer.append("]");
		}
		if (negative) 
			buffer.append(" [-]");
		else {
			if (includeState) {
				buffer.append(" ");
				buffer.append(RIGHTARROW);
				buffer.append(" ");
				String markingString = marking.toString();
				if (markingString.length()<STATESTRLENGTH) 
					buffer.append(markingString);
				else
					buffer.append(markingString.substring(0, STATESTRLENGTH));
			}
		}
		return buffer.toString();
	}

	public String getActualParameterList() {
		ArrayList<String> formalParameters = transition.getArguments();
		if (formalParameters==null)
			formalParameters = transition.getAllVariables();
		if (formalParameters==null || formalParameters.size()==0)
			return "";
		StringBuffer buffer = new StringBuffer("(");
		buffer.append(substitution.getBinding(formalParameters.get(0)));
		for (int i=1; i<formalParameters.size(); i++){
			String value = substitution.getBinding(formalParameters.get(i));
			buffer.append(", ");
			buffer.append(value); 
		}
		buffer.append(")"); 
		return buffer.toString(); 
	}

	public int getInitMarkingIndex(){
		TransitionTreeNode currentNode = this;
		while (!currentNode.parent.isRoot())
			currentNode = currentNode.parent;
		return currentNode.parent.children.indexOf(currentNode)+1;
//		return Integer.valueOf(outlineNumber.substring(0, outlineNumber.indexOf(".")));
	}
	
	public ArrayList<TransitionTreeNode> getFiringSequence() {
		ArrayList<TransitionTreeNode> nodes = new ArrayList<TransitionTreeNode>();
		TransitionTreeNode node = this;
		while (!node.getParent().isRoot()) {
			nodes.add(0, node);
			node = node.getParent();
		}
		return nodes;
	}

	public FiringSequence createFiringSequence() {
		ArrayList<Firing> firings = new ArrayList<Firing>();
		TransitionTreeNode node = this;
		while (!node.getParent().isRoot()) {
			firings.add(0, new Firing(node.getTransition(), node.getSubstitution()));
			node = node.getParent();
		}
		return new FiringSequence(firings);
	}
	
	public String getTestInputCode(){
		return testInputCode;
	}
	
	public void setTestInputCode(String code){
		testInputCode = code;
	}
	
	public String getTestOracleCode(){
		return testOracleCode;
	}
	
	public void setTestOracleCode(String code){
		testOracleCode = code;
	}

/*	
	public String reportFiringSequence(){
		ArrayList<TransitionTreeNode> nodes = getFiringSequence();
		String result ="";
		for (int i=0; i<nodes.size(); i++) {
			TransitionTreeNode node = nodes.get(i); 
			result += "\t"+node.getEvent()+node.getSubstitution().toString(node.getTransition().getAllVariables())+"\n";
		}
		return result;
	}
*/
	// general tree node 
	private TransitionTreeNode parent = null;
	private Vector<TransitionTreeNode> children = new Vector<TransitionTreeNode>();

	public void setParent(TransitionTreeNode parent) {
		if (parent!=null)
			depth = parent.depth+1;
		this.parent = parent;
	}
	
	public TransitionTreeNode getParent() {
		return parent;
	}

	public boolean isRoot(){ 
		return parent == null;
	}

	public boolean isLeaf(){
		return children.size() == 0;
	}

	public Vector<TransitionTreeNode> children(){
		return children;
	}

	public boolean hasChildren(){
		return children.size()>0;
	}

	public void add(TransitionTreeNode child){
		children.add(child);
		child.setParent(this);
	}

	public void insert(int index, TransitionTreeNode child){
		children.add(index, child);
		child.setParent(this);
	}

	public void removeNodeFromChildren(TransitionTreeNode child) {
		child.parent = null;
		children.remove(child);
	}

	public int getChildNodePosition(TransitionTreeNode child) {
		return children.indexOf(child);
	}
	
	// number of successors
	// only for checking consistency of test data when restoring a tree from data file 
	private int numberOfSuccessors =0 ;			// i.e., number of children
	
	public int getNumberOfSuccessors(){
		return numberOfSuccessors;
	}
	
	public void setNumberrOfSuccessors(int number){
		numberOfSuccessors = number;
	}
	// end for number of successors
	
	public TransitionTreeNode clone() {
		TransitionTreeNode newCopy = cloneNode();
		newCopy.parent = this.parent; 
		return newCopy;
	}

	// due to recursion, the parent node of the copy of this node is not set properly - this is done in clone()
	// therefore should call clone
	private TransitionTreeNode cloneNode() {
		TransitionTreeNode copy = new TransitionTreeNode(this.transition, this.substitution, this.marking);
		copy.paraTable = this.paraTable.clone();
		Vector<TransitionTreeNode> childrenCopy = new Vector<TransitionTreeNode>();
		for (TransitionTreeNode child: this.children){
			TransitionTreeNode childCopy = child.cloneNode();
			childCopy.parent = copy;
			childrenCopy.add(childCopy);
		}
		copy.children = childrenCopy;
		return copy; 
	}
	
	public ArrayList<TransitionTreeNode> getAllLeafNodes(){
		ArrayList<TransitionTreeNode> leaves = new ArrayList<TransitionTreeNode>();
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=children().size()-1; i>=0; i--){
			stack.push(children().get(i));
		}
		while (!stack.isEmpty()) {
			TransitionTreeNode current =stack.pop();
			if (current.isLeaf())
				leaves.add(current);
			for (int i=current.children().size()-1; i>=0; i--){
				stack.push(current.children().get(i));
			}
		}
		return leaves;
	}

	public ArrayList<TransitionTreeNode> getAllNonLeafNodes(){
		ArrayList<TransitionTreeNode> nonleaves = new ArrayList<TransitionTreeNode>();
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=children().size()-1; i>=0; i--){
			stack.push(children().get(i));
		}
		while (!stack.isEmpty()) {
			TransitionTreeNode current =stack.pop();
			if (!current.isLeaf())
				nonleaves.add(current);
			for (int i=current.children().size()-1; i>=0; i--){
				stack.push(current.children().get(i));
			}
		}
		return nonleaves;
	}

	public DefaultMutableTreeNode setToMutableNode() {
		DefaultMutableTreeNode mutableTreeNode = new DefaultMutableTreeNode(this);
		if (hasChildren()) {
			for (Object kid : children) {
				DefaultMutableTreeNode mutableTreechild = ((TransitionTreeNode)kid).setToMutableNode();
				mutableTreeNode.add(mutableTreechild);
			}
		}
		return mutableTreeNode;
	}
	
	// could use outlineNumber.split("\\.").length for getLevel;
	// if outlineNumber is always set properly, 
	// However, GoalVerifierBFS/DFS have not done so
	//
	// For complex models, seems speed is as critical as memory
	public int getLevel() {
/*		int level = 1;
		TransitionTreeNode currentNode = this;
		while (!currentNode.getParent().isRoot()){
			currentNode = currentNode.getParent();
			level++;
		}
		return level; 
*/
		return depth;
		}
}