package verification;

import java.util.Hashtable;
import java.util.LinkedList;

import kernel.CancellationException;
import kernel.ProgressDialog;

import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.GoalProperty;
import testgeneration.TransitionTreeNode;

public class GoalVerifierBFS extends GoalVerifier{

	public GoalVerifierBFS(MID mid, int searchDepth, boolean searchForHomeStates, ProgressDialog progressDialog) throws CancellationException {
		super(mid, searchDepth, progressDialog, searchForHomeStates);
		verify();
	}
	
	// for combining traditional reachability analysis with another verifier (e.g., planning graph)
	// existingFiringSequences are produced by another verifier 
	public GoalVerifierBFS(MID mid, int searchDepth, boolean searchForHomeStates, ProgressDialog progressDialog, FiringSequence[][] existingFiringSequences, boolean repeatSearch) throws CancellationException {
		super(mid, searchDepth, progressDialog, searchForHomeStates);
		this.firingSequences = existingFiringSequences;
		if (repeatSearch)
			verify();
	}
	
 	protected void verify() throws CancellationException{
 	   	Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		for (TransitionTreeNode initNode: root.children()){
			expandedMarkings.clear();
			setGoals();
			removeGoalsWithExistingFiringSequences(root.children().indexOf(initNode));	 
			LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
			queue.addLast(initNode);
			while (!queue.isEmpty() && goals.size()!=0) {
				checkForCancellation();
				TransitionTreeNode node = queue.poll();
				if (node!=initNode || !searchForHomeStates)
					verifyGoals(node);
				if (node.getLevel()<=searchDepth) {
					if (expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null)
						createChildren(node);
					if (node!=initNode || !searchForHomeStates)
						expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
					for (TransitionTreeNode child: node.children()) {
						if (expandedMarkings.get(child.getMarking().getKeyString(mid.getPlaces()))==null)
							queue.addLast(child);
					}
				}
			}
		}
	}


	private void removeGoalsWithExistingFiringSequences(int initIndex){
		for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
			GoalProperty goalProperty = mid.getGoalProperties().get(goalIndex);
			FiringSequence firingSequence = firingSequences[initIndex][goalIndex];
			if (firingSequence!=null) {
				if (firingSequence.getSequence().size()!=0 || !searchForHomeStates)
					goals.remove(goalProperty);
			}
		}
	}
}