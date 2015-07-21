/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Marking;


public class TransitionTreeForThreatNetBFS extends TransitionTreeForThreatNet {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForThreatNetBFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}

	public void expand() throws CancellationException{
		int searchDepth = systemOptions.getSearchDepth();
		Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (node.getLevel()<= searchDepth && !node.getTransition().isAttackTransition() && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				createChildren(node);
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
				for (TransitionTreeNode child: node.children())
					if (!child.isNegative())
						queue.addLast(child);
			}
		}
	}

/*	
	public void expand1() throws CancellationException{
		int searchDepth = systemOptions.getSearchDepth();
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (node.getLevel()<= searchDepth && !node.getTransition().isAttackTransition() && !isStateExpanded(root, node)) {
				createChildren(node);
				for (TransitionTreeNode child: node.children())
					if (!child.isNegative())
						queue.addLast(child);
			}
		}
	}
*/	
	// use the same search strategy as tree generation
	protected TransitionTreeNode findNonLeafNodeWithSameState(TransitionTreeNode node){
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			TransitionTreeNode currentNode = queue.poll();
//			if (currentNode!=node && currentNode.getMarking().equals(node.getMarking())	&& hasAttackLeaves(currentNode))
			if (currentNode!=node && !currentNode.isLeaf() && currentNode.getMarking().equals(node.getMarking()))
				return currentNode;
			for (TransitionTreeNode child: currentNode.children())
				queue.addLast(child);
		}
		return null;
	}

	protected ArrayList<TransitionTreeNode> getLeaves(){
		ArrayList<TransitionTreeNode> leaves = new ArrayList<TransitionTreeNode>(); 
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			TransitionTreeNode node = queue.poll();
			if (node.isLeaf())
				leaves.add(node);
			for (TransitionTreeNode child: node.children())
				queue.addLast(child);
		}
		return leaves;
	}

}
