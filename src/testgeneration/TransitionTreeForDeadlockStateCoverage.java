/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;

import kernel.CancellationException;
import kernel.SystemOptions;
import mid.GeneralUnifier;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;

public class TransitionTreeForDeadlockStateCoverage extends TransitionTree {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForDeadlockStateCoverage(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}

	public void generateTransitionTree() throws CancellationException {
		boolean SinkEventsEnabled = false; 
		TransitionTreeForStructureCoverage stateTree = systemOptions.isBreadthFirstSearch()? 
//				new TransitionTreeForRoundTripBFS(mid, systemOptions, TransitionTreeForStructureCoverage.DisallowDirtyTests)
//				: new TransitionTreeForRoundTripDFS(mid, systemOptions, TransitionTreeForStructureCoverage.DisallowDirtyTests);
				new TransitionTreeForStateCoverageBFS(mid, systemOptions, SinkEventsEnabled)
				: new TransitionTreeForStateCoverageDFS(mid, systemOptions, SinkEventsEnabled);
		stateTree.setProgressDialog(this.getProgressDialog());
		stateTree.generateTransitionTree();
		root = stateTree.getRoot();
		for (TransitionTreeNode leaf: getAllTests()){
			checkForCancellation();
			if (isLiveState(leaf.getMarking())){
				removePath(leaf);
			}
		}
		root.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}
		
	private boolean isLiveState(Marking marking) throws CancellationException {
		for (String event: mid.getEvents()){
			ArrayList<Transition> transitions = mid.getTransitionsForEvent(event);
			if (transitions!=null && transitions.size()>0){
				for (Transition transition: transitions) {
					checkForCancellation();
					GeneralUnifier unifier = new Unifier(transition, marking);
					ArrayList<Substitution> substitutions = unifier.getSubstitutions();
					if (substitutions.size()>0)
						return true;
				}
			}
		}
		return false;	// deadlock state
	}
		
	private void removePath(TransitionTreeNode node){
		TransitionTreeNode currentNode = node;
		while (!currentNode.isRoot()){
			TransitionTreeNode parent = currentNode.getParent();
			parent.children().remove(currentNode);
			if (parent.children().size()>0)
				return;
			else
				currentNode = parent;
		}
	}
	
}
