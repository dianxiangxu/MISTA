/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Stack;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;

// Transition tree, where each leaf node is a potential attack

public abstract class TransitionTreeForThreatNet extends TransitionTree {
	private static final long serialVersionUID = 1L;

	public TransitionTreeForThreatNet(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
		for (Transition transition: mid.getTransitions())
			if (transition.isAttackTransition())
				mid.addHiddenPlaceOrEvent(transition.getEvent());
	}
	
	public void generateTransitionTree() throws CancellationException {
		createRootNode();
		expand();
		CoverageCriterion coverage = systemOptions.getCoverageCriterion();
		if (coverage == SystemOptions.ThreatNetPathCoverage)
			expandLeafNodes();			
		removeNonAttackPaths();
		removeAttackLeafNodes();
	}
	
	// Tree generation depends on the search strategy
	abstract protected void expand() throws CancellationException;  
	

	// must use the same search strategy as tree generation
	abstract protected TransitionTreeNode findNonLeafNodeWithSameState(TransitionTreeNode node);
	
	// Leaf nodes should be retrieved in the same order as they are generated. 
	// This is important for expanding non-attack leaf nodes whose states have occurred before.   
	abstract protected ArrayList<TransitionTreeNode> getLeaves(); 

	protected void createChildren(TransitionTreeNode node){
		for (String event: mid.getEvents()){
			ArrayList<Transition> transitions = mid.getTransitionsForEvent(event);
			if (transitions!=null && transitions.size()>0)
				createTestNodes(transitions, node);
		}
    	node.setExpanded(true);
   		node.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}

	private void createTestNodes(ArrayList<Transition> transitions, TransitionTreeNode node){
		Marking currentMarking = node.getMarking();
		for (Transition transition: transitions) {
    		Unifier unifier = new Unifier(transition, currentMarking);
    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
    		for (Substitution substitution: substitutions) {
//System.out.println(transition.getEvent()+": "+substitution.toString(transition.getAllVariables()));
    			Marking newMarking = mid.fireTransition(currentMarking, transition, substitution);
    			TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);
    			node.add(newNode);
//    			System.out.println("New marking: "+newNode.getMarking());
    		}
		}
	}

	/*
	protected boolean isStateExpanded(TransitionTreeNode targetNode, TransitionTreeNode current) throws CancellationException{
		checkForCancellation();
		if (isStateFound(targetNode, current)) {
			return true;
		}	
    	for (TransitionTreeNode child: targetNode.children()){
			if (isStateExpanded(child, current))
				return true;
		}
		return false;
	}
	
	private boolean isStateFound(TransitionTreeNode targetNode, TransitionTreeNode current){
		return targetNode.isExpanded() && targetNode.getMarking().equals(current.getMarking());
	}
	*/
	
	private void removeNonAttackPaths(){
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf() && !node.getTransition().isAttackTransition())
				removeNonAttackLeafNode(node);
			else
				for (int i=node.children().size()-1; i>=0; i--)
					stack.push(node.children().get(i));
		}
	}
	
	private void removeNonAttackLeafNode(TransitionTreeNode node){
		TransitionTreeNode toBeRemoved = node;
		do {
			TransitionTreeNode parent = toBeRemoved.getParent();
			parent.children().remove(toBeRemoved);
			toBeRemoved.setParent(null);
			toBeRemoved = !parent.isRoot() && parent.isLeaf()? parent: null;
		} while (toBeRemoved!=null);		
	}
	
	// remove leaf nodes with attack transitions whose postconditions are empty. 
	// If attack leaf nodes need to be displayed, add a postcondition (output place) to attack transition
	private void removeAttackLeafNodes(){
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf()){
				Transition transition = node.getTransition();
				if (transition.isAttackTransition() && transition.getPostcondition().size()==0){
					TransitionTreeNode parent = node.getParent();
					parent.children().remove(node);
					node.setParent(null);
				}
			}
			else
				for (int i=node.children().size()-1; i>=0; i--)
					stack.push(node.children().get(i));
		}
	}

	private boolean hasEnabledTransitions(ArrayList<Transition> transitions, TransitionTreeNode node){
		Marking currentMarking = node.getMarking();
		for (Transition transition: transitions) {
    		Unifier unifier = new Unifier(transition, currentMarking);
    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
    		if (substitutions.size()>0)
    			return true;
		}
		return false;
	}

	/*
	protected boolean hasAttackLeaves(TransitionTreeNode node){
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		stack.push(node);
		while (!stack.isEmpty()) {
			TransitionTreeNode currentNode = stack.pop();
			if (currentNode.isLeaf() && currentNode.getTransition().isAttackTransition())
				return true;
			for (int i=currentNode.children().size()-1; i>=0; i--)
				stack.push(currentNode.children().get(i));
		}
		return false;
		
	}
*/	
	
	private boolean isStateRepeatedInPath(TransitionTreeNode node){
		TransitionTreeNode currentNode = node.getParent();
		while (!currentNode.isRoot()){
			if (currentNode.getMarking().equals(node.getMarking()))
				return true;
			currentNode = currentNode.getParent();
		}
		return false;
	}

	// full coverage of threat paths
	private void expandLeafNodes(){
		boolean done;
		do {
			done = true;
			ArrayList<TransitionTreeNode> leaves = getLeaves();
			for (int index=0; index<leaves.size(); index++){
				TransitionTreeNode node = leaves.get(index);
//System.out.println("Leaf node: " + node);			
				if (!node.getTransition().isAttackTransition() && hasEnabledTransitions(mid.getTransitions(), node)
						&& !isStateRepeatedInPath(node)){
					TransitionTreeNode nonLeafNodeWithSameState = findNonLeafNodeWithSameState(node);				
					if (nonLeafNodeWithSameState!=null){
//System.out.println("Leaf node: " + node);			
//System.out.println("Leaf node: " + node.getTransition()+"\nFound non-leaf node with same state: "+ sameStateNonLeaf.getTransition() + "\n");			
						for (TransitionTreeNode child: nonLeafNodeWithSameState.children())
							node.add(child.clone());
						node.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
						done = false;
					}
				} 
			}
		} while (!done);
	}
}
