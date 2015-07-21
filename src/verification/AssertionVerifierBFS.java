package verification;

import java.util.Hashtable;
import java.util.LinkedList;

import kernel.CancellationException;
import kernel.ProgressDialog;

import mid.MID;
import mid.Marking;
import testgeneration.TransitionTreeNode;

public class AssertionVerifierBFS extends AssertionVerifier{

	public AssertionVerifierBFS(MID mid, int searchDepth, ProgressDialog progressDialog) throws CancellationException {
		super(mid, searchDepth, progressDialog);
		verify();
	}
	
 	protected void verify() throws CancellationException{
 	   	Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		for (TransitionTreeNode initNode: root.children()){
			expandedMarkings.clear();
			setWorkingAssertions();
			LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
			queue.addLast(initNode);
			while (!queue.isEmpty() && workingAssertions.size()!=0) {
				checkForCancellation();
				TransitionTreeNode node = queue.poll();
				if (node!=initNode)
					verifyAssertions(node); // assertions are not verified against the initial markings
				if (node.getLevel()<=searchDepth) {
					if (expandedMarkings.get(node.getMarking().getKeyString(mid.getPlaces()))==null)
						createChildren(node);
//					if (node!=initNode)
						expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
					for (TransitionTreeNode child: node.children()) {
						if (expandedMarkings.get(child.getMarking().getKeyString(mid.getPlaces()))==null)
							queue.addLast(child);
					}
				}
			}
		}
	}

}