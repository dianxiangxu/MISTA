package verification;

import java.util.Hashtable;
import java.util.Stack;

import kernel.CancellationException;
import kernel.ProgressDialog;

import mid.MID;
import mid.Marking;
import testgeneration.TransitionTreeNode;

// Bounded depth-first search is incomplete.
// A reachable state within the max depth may not be found for the following reason:
// Suppose the path to a reachable state is e1,e2,..., en
// If the state after some ei (i<n) was expended when the depth is close to the maximum, then this path won't be found. 

public class GoalVerifierDFS extends GoalVerifier{

	public GoalVerifierDFS(MID mid, int searchDepth, boolean searchForHomeStates, ProgressDialog progressDialog)  throws CancellationException {
		super(mid, searchDepth, progressDialog, searchForHomeStates);
		verify();
	}

	protected void verify()  throws CancellationException{
	    Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		for (TransitionTreeNode initNode: root.children()){
			expandedMarkings.clear();
			setGoals();
			Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
			stack.push(initNode);
			while (!stack.isEmpty() && goals.size()!=0) {
				checkForCancellation();
				TransitionTreeNode node = stack.pop(); 
				if (node!=initNode || !searchForHomeStates)
					verifyGoals(node);
				if (node.getLevel()<=searchDepth) {
					createChildren(node);
					if (node!=initNode || !searchForHomeStates)
						expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
					for (int i= node.children().size()-1; i>=0; i--){
						TransitionTreeNode child = node.children().get(i);
						if (expandedMarkings.get(child.getMarking().getKeyString(mid.getPlaces()))==null)
							stack.push(child);
					}
				} 
			}
		}
	}

}