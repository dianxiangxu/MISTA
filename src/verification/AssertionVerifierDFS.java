package verification;

import java.util.Hashtable;
import java.util.Stack;

import kernel.CancellationException;
import kernel.ProgressDialog;

import mid.MID;
import mid.Marking;
import testgeneration.TransitionTreeNode;

public class AssertionVerifierDFS extends AssertionVerifier{

	public AssertionVerifierDFS(MID mid, int searchDepth, ProgressDialog progressDialog)  throws CancellationException {
		super(mid, searchDepth, progressDialog);
		verify();
	}

	protected void verify()  throws CancellationException{
	    Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		for (TransitionTreeNode initNode: root.children()){
			expandedMarkings.clear();
			setWorkingAssertions();
			Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
			stack.push(initNode);
			while (!stack.isEmpty() && workingAssertions.size()!=0) {
				checkForCancellation();
				TransitionTreeNode node = stack.pop(); 
				if (node!=initNode) // assertions are not verified against the initial markings 
					verifyAssertions(node);
				if (node.getLevel()<=searchDepth) {
					createChildren(node);
//					if (node!=initNode)
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