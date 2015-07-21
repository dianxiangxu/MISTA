/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import kernel.CancellationException;
import kernel.SystemOptions;
import mid.GeneralUnifier;
import mid.MID;
import mid.Marking;
import mid.PairwiseUnifier;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;

public class TransitionTreeForStateCoverageBFS extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;
	private Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
	
	public TransitionTreeForStateCoverageBFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}

	public TransitionTreeForStateCoverageBFS(MID mid, SystemOptions systemOptions, boolean areSinkEventsEnabled) {
		super(mid, systemOptions, areSinkEventsEnabled);
	}

	public void expand() throws CancellationException {
	    int searchDepth = systemOptions.getSearchDepth();
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children()){
			queue.addLast(initNode);
			if (expandedMarkings.get(initNode.getMarking().getKeyString(mid.getPlaces()))==null && !systemOptions.searchForHomeStates())
				expandedMarkings.put(initNode.getMarking().getKeyString(mid.getPlaces()), initNode.getMarking());
		}
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (node.getLevel()<= searchDepth) {
				createChildren(node);
				for (TransitionTreeNode child: node.children()){
//					if (!child.isNegative()){
						queue.addLast(child);
//					}
				}
			}
		}
		numberOfStates = expandedMarkings.size();
	}

	protected void createChildren(TransitionTreeNode node) throws CancellationException {
		for (String event: mid.getEvents()){
			checkForCancellation();
			ArrayList<Transition> transitions = mid.getTransitionsForEvent(event);
			if (transitions!=null && transitions.size()>0){
				for (Transition transition: transitions) {
					checkForCancellation();
		    		GeneralUnifier unifier = systemOptions.isPairwiseTesting() && mid.isCombinatorialTestingApplicable(transition)? 
		    				new PairwiseUnifier(transition, node.getMarking()): 
		    				new Unifier(transition, node.getMarking());
		    		for (Substitution substitution:  unifier.getSubstitutions()){
		    			checkForCancellation();
		    			if (systemOptions.isTotalOrdering() || !isIndependentFiring(transition, substitution, node.children())){
		    				Marking newMarking = mid.fireTransition(node.getMarking(), transition, substitution);
		    				String newMarkingKey= newMarking.getKeyString(mid.getPlaces());
	    					if (expandedMarkings.get(newMarkingKey)==null){
	    						expandedMarkings.put(newMarkingKey, newMarking);
	    						TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);
	    						node.add(newNode);
	    					}
		    			} 	
		    		}
		 		}
			}
		}
    	node.setExpanded(true);
   		node.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}

	public Hashtable<String, Marking> getExpandedMarkings(){
		return expandedMarkings;
	}
}
