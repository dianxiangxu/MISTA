/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Transition;


public class TransitionTreeForTransitionCoverage extends TransitionTree {
	private static final long serialVersionUID = 1L;
	
	private boolean OPTIMIZATION = true;
	
	private ArrayList<Transition> transitions = new ArrayList<Transition>();	

	public TransitionTreeForTransitionCoverage(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
		for (Transition transition: mid.getTransitions())
			transitions.add(transition);
	}

	public void generateTransitionTree() throws CancellationException{
		TransitionTreeForStructureCoverage roundTripTree = new TransitionTreeForRoundTripWithTransitionCap(mid, systemOptions);
		roundTripTree.generateTransitionTree();
		root = roundTripTree.getRoot();
		searchForTransitionCoverage();
	}
	
	private void searchForTransitionCoverage() throws CancellationException{
		clearTraversedFlags(root);
		root.setTraversed(true);
		for (TransitionTreeNode child: root.children())
			child.setTraversed(true);
		if (systemOptions.isBreadthFirstSearch())
			traverseForTransitionCoverageBFS();
		else
			traverseForTransitionCoverageDFS();
		for (TransitionTreeNode child: root.children())
			extractForTransitionCoverage(child);
		if (OPTIMIZATION)
			removeRedundentLeaves();		
		clearTraversedFlags(root);
		root.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}

	private void traverseForTransitionCoverageBFS() throws CancellationException{
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children()) {
			queue.addLast(initNode);
		}
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (transitions.contains(node.getTransition())) {
				transitions.remove(node.getTransition());
				node.setTraversed(true);
			}
			for (TransitionTreeNode child: node.children())
				queue.addLast(child);
		}
	}
	
	private void traverseForTransitionCoverageDFS() throws CancellationException{
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--) {
			stack.push(root.children().get(i));
		}
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (transitions.contains(node.getTransition())) {
				transitions.remove(node.getTransition());
				node.setTraversed(true);
			}
			for (int i=node.children().size()-1; i>=0; i--) {
				stack.push(node.children().get(i));
			}
		}
	}

	
/*
 	// not good
	private void traverseForTransitionCoverage(TransitionTreeNode node){
		if (transitions.contains(node.getTransition())) {
			transitions.remove(node.getTransition());
			node.setTraversed(true);
		}
		for (TransitionTreeNode child: node.children())
			traverseForTransitionCoverage(child);
	}
*/
	// remove those nodes and their descendants that are not used for transition coverage
	private void extractForTransitionCoverage(TransitionTreeNode node) throws CancellationException{
		checkForCancellation();
		if (!node.isTraversed() && !hasTargetTransitionsInDescendents(node)) {
			node.getParent().children().remove(node);
			node.setParent(null);
		}
		else 
			for (int i=node.children().size()-1; i>=0; i--) {
				extractForTransitionCoverage(node.children().get(i));
			}
	}
	
	// any descendents contribute to the transition coverage?  
	private boolean hasTargetTransitionsInDescendents(TransitionTreeNode current){
		if (current.children().size()==0)
			return false;
		for (TransitionTreeNode child: current.children()) {
			if (child.isTraversed())
				return true;
		}
		for (TransitionTreeNode child: current.children()) {
			if (hasTargetTransitionsInDescendents(child))
				return true;
		}
		return false;
	}

	// remove each leaf node whose transition is contained by some other non-leaf node  
	private void removeRedundentLeaves() throws CancellationException{
		ArrayList<TransitionTreeNode> leaves = root.getAllLeafNodes();
		ArrayList<TransitionTreeNode> nonleaves = root.getAllNonLeafNodes();
		while (leafIncludedByNonleaves(leaves, nonleaves)){
			checkForCancellation();
			for (int i=nonleaves.size()-1; i>=0; i--){
				TransitionTreeNode nonleaf = nonleaves.get(i); 
				if (nonleaf.isLeaf()){  // it'd become leaf because children are deleted
					nonleaves.remove(nonleaf);
					leaves.add(nonleaf);
				}
			}
		}
	}

	private boolean leafIncludedByNonleaves(ArrayList<TransitionTreeNode> leaves, ArrayList<TransitionTreeNode> nonleaves) throws CancellationException{
		boolean found = false;
		for (int i=leaves.size()-1; i>=0; i--){
			checkForCancellation();
			TransitionTreeNode leaf = leaves.get(i);
			if (leaf.getParent()==null)
				continue;
			for (TransitionTreeNode nonleaf: nonleaves){
				checkForCancellation();
				if (nonleaf.getTransition()==leaf.getTransition() && leaf.getParent()!=null) {
					Vector<TransitionTreeNode> siblings = leaf.getParent().children();
					siblings.remove(leaf);
					leaf.setParent(null);
					found = true;
				}
			}
		}
		return found;
	}

}
