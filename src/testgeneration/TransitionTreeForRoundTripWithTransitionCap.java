/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Marking;
import mid.Transition;


public class TransitionTreeForRoundTripWithTransitionCap extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;

	private ArrayList<Transition> transitions = new ArrayList<Transition>();	

	public TransitionTreeForRoundTripWithTransitionCap(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions, TransitionTreeForStructureCoverage.SINK_EVENTS_DISABLED);
		for (Transition transition: mid.getTransitions())
			transitions.add(transition);
	}
	
	public void generateTransitionTree() throws CancellationException {
		createRootNode();
		expand();
	}

	public void expand() throws CancellationException {
		if (systemOptions.isBreadthFirstSearch())
			expandBFS();
		else
			expandDFS();
	}

	public void expandBFS() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty() && transitions.size()>0) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (transitions.contains(node.getTransition()))
				transitions.remove(node.getTransition());
			if (node.getLevel()<=searchDepth && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				createChildren(node);
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
				for (TransitionTreeNode child: node.children())
					if (!child.isNegative())
						queue.addLast(child);
			}
		}
		numberOfStates = expandedMarkings.size();
//		System.out.println("#Transitions unreachable: "+transitions.size());
	}
	
	public void expandDFS() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (transitions.contains(node.getTransition()))
				transitions.remove(node.getTransition());
			if (node.getLevel()<= searchDepth  && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				createChildren(node);
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
				for (int i=node.children().size()-1; i>=0; i--){
					TransitionTreeNode child = node.children().get(i);
					if (!child.isNegative())
						stack.push(child);
				}
			}
		}
		numberOfStates = expandedMarkings.size();
	}

	public boolean hasDeadTransitions(){
		return transitions.size()>0; 		
	}
	
	public String getDeadTransitions(){
		String deadTransitions ="";
		for (Transition t: transitions)
			deadTransitions += t.getTransitionIdentityString()+"\n";
		return deadTransitions;
	}

}
