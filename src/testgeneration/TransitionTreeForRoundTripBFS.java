/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.Hashtable;
import java.util.LinkedList;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;
import mid.Marking;


public class TransitionTreeForRoundTripBFS extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForRoundTripBFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
		if (systemOptions.areDirtyTestsNeeded())
			areSinkEventsEnabled = TransitionTreeForStructureCoverage.SINK_EVENTS_DISABLED;
	}

	public void expand() throws CancellationException {
	    int searchDepth = systemOptions.getSearchDepth();
		Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (areSinkEventsEnabled && mid.isSinkTransition(node.getTransition()) && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());				
			}
			if (node.getLevel()<= searchDepth && (!areSinkEventsEnabled || !mid.isSinkTransition(node.getTransition())) && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
//			if (node.getLevel()<= searchDepth && expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null) {
				createChildren(node);
				expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
				for (TransitionTreeNode child: node.children())
					if (!child.isNegative() )
						queue.addLast(child);
			}
		}
		numberOfStates = expandedMarkings.size();
	}


}
