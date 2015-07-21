/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import kernel.CancellationException;
import kernel.SystemOptions;

import mid.GeneralUnifier;
import mid.MID;
import mid.Marking;
import mid.PairwiseUnifier;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;

public class TransitionTreeForStateCoverageDFS extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;
	private Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
	
	public TransitionTreeForStateCoverageDFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}
	
	public TransitionTreeForStateCoverageDFS(MID mid, SystemOptions systemOptions, boolean areSinkEventsEnabled) {
		super(mid, systemOptions, areSinkEventsEnabled);
	}
	
	protected void expand() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--){
			TransitionTreeNode initNode = root.children().get(i);
			stack.push(initNode);
		}
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			Marking marking = node.getMarking();
			String markingKey = marking.getKeyString(mid.getPlaces());
			if (expandedMarkings.get(markingKey)==null){
				if (!root.children().contains(node) || !systemOptions.searchForHomeStates()){
					expandedMarkings.put(markingKey, marking);
				}
				if (node.getLevel()<= searchDepth) {
					createChildren(node);
					for (int i=node.children().size()-1; i>=0; i--){
						TransitionTreeNode child = node.children().get(i);
						stack.push(child);
					}
				}
			}
			else
				node.getParent().removeNodeFromChildren(node);
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
		    		for (Substitution substitution: unifier.getSubstitutions()){
		    			checkForCancellation();
		    			if (systemOptions.isTotalOrdering() || !isIndependentFiring(transition, substitution, node.children())){
		    				Marking newMarking = mid.fireTransition(node.getMarking(), transition, substitution);
		    				TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);
		    				node.add(newNode);
		    			} 	
		    		}
		 		}
			}
		}
    	node.setExpanded(true);
   		node.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}

}
