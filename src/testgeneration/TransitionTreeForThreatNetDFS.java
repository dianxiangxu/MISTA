/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Marking;


public class TransitionTreeForThreatNetDFS extends TransitionTreeForThreatNet {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForThreatNetDFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}

	/*
	protected void expand() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (node.getLevel()<= searchDepth  && !node.getTransition().isAttackTransition() && !isStateExpanded(root, node)) {
				createChildren(node);
				for (int i=node.children().size()-1; i>=0; i--){
					TransitionTreeNode child = node.children().get(i);
					if (!child.isNegative())
						stack.push(child);
				}
			}
		}
	}
	*/
	
	protected void expand() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (node.getLevel()<= searchDepth  && !node.getTransition().isAttackTransition() && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				createChildren(node);
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());				
				for (int i=node.children().size()-1; i>=0; i--){
					TransitionTreeNode child = node.children().get(i);
					if (!child.isNegative())
						stack.push(child);
				}
			}
		}
	}

	// use the same search strategy as tree generation
	protected TransitionTreeNode findNonLeafNodeWithSameState(TransitionTreeNode node){
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode currentNode = stack.pop();
//			if (currentNode!=node && currentNode.getMarking().equals(node.getMarking()) && hasAttackLeaves(currentNode))
			if (currentNode!=node && !currentNode.isLeaf() && currentNode.getMarking().equals(node.getMarking()))
				return currentNode;
			for (int i=currentNode.children().size()-1; i>=0; i--)
				stack.push(currentNode.children().get(i));
		}
		return null;
	}
	
	protected ArrayList<TransitionTreeNode> getLeaves(){
		ArrayList<TransitionTreeNode> leaves = new ArrayList<TransitionTreeNode>(); 
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf())
				leaves.add(node);
			for (int i=node.children().size()-1; i>=0; i--)
				stack.push(node.children().get(i));
		}
		return leaves;
	}

}
