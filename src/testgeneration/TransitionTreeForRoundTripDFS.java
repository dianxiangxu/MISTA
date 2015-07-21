/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.Hashtable;
import java.util.Stack;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Marking;


public class TransitionTreeForRoundTripDFS extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForRoundTripDFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}

	protected void expand() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (areSinkEventsEnabled && mid.isSinkTransition(node.getTransition()) && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());				
			}
			if (node.getLevel()<= searchDepth && (!areSinkEventsEnabled || !mid.isSinkTransition(node.getTransition())) && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
//			if (node.getLevel()<= searchDepth  && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
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

}
