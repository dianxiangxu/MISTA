/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import mid.Substitution;


// TransitionTreeNodes of clean tests where transitions and substitutions 
// can potentially reused for dirty tests, not including nodes with empty substitutions.

public class SubstitutionsForEvents {
	// key: event
	// value: TransitionTreeNodes of transitions and substitutions for the event
	//        No nodes with the identical substitutions
	private Hashtable <String, ArrayList<TransitionTreeNode>> substitutionNodes; 
	private TransitionTreeNode root; 
	
	public SubstitutionsForEvents(TransitionTreeNode root){
		this.root = root;
		substitutionNodes = new Hashtable <String, ArrayList<TransitionTreeNode>>();
	}
	
	public ArrayList<TransitionTreeNode> findSubstitutionsForEvent(String event) {
		ArrayList<TransitionTreeNode> substitutionsForEvent = substitutionNodes.get(event);
		if (substitutionsForEvent == null) {
			substitutionsForEvent = searchForSubstitutionsForEvent(event);
//	System.out.println("\n substitutions for " + event);
//	for (Substitution substitution: substitutionsForEvent)
//	System.out.println("\n"+substitution);
	
			substitutionNodes.put(event, substitutionsForEvent);
		}
		return substitutionsForEvent;
	}
	
	// find each node where the event is involved in the transition but the substitution is not duplicate
	private ArrayList<TransitionTreeNode> searchForSubstitutionsForEvent(String event){
		ArrayList<TransitionTreeNode> substitutionsForEvent = new ArrayList<TransitionTreeNode>();
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			TransitionTreeNode node = queue.poll();
			if (node.getEvent().equals(event) && !isDuplicateSubstitution(substitutionsForEvent, node.getSubstitution())){
//System.out.println("\n substitutions for " + node.getEvent()+ "\n"+node);
				substitutionsForEvent.add(node);
			}
			for (TransitionTreeNode child: node.children())
				queue.addLast(child);
		}
		return substitutionsForEvent;
	}
	
	private boolean isDuplicateSubstitution(ArrayList<TransitionTreeNode> substitutionsForEvent, Substitution substitution){
		if (!substitution.hasBindings()) // empty substitution is duplicate and does not count
			return true;
		for (TransitionTreeNode node: substitutionsForEvent){
			if (node.getSubstitution().equals(substitution))
				return true;
		}
		return false;
	}
}
