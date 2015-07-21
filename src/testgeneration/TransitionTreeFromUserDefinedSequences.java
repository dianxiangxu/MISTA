/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;

import kernel.CancellationException;
import kernel.SystemOptions;

import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Transition;
import mid.UserDefinedSequences;

public class TransitionTreeFromUserDefinedSequences extends TransitionTreeFromSequences {
	
	private static final long serialVersionUID = 1L;

	private ArrayList<UserDefinedSequences> allSequences; 
	
	public TransitionTreeFromUserDefinedSequences(MID mid, SystemOptions systemOptions, ArrayList<UserDefinedSequences> allSequences) {
		super(mid, systemOptions);
		this.allSequences = allSequences;
		root = new TransitionTreeNode(null, null, null);
	}

	public void generateTransitionTree() throws CancellationException{
		for (int initIndex=0; initIndex<mid.getInitialMarkings().size(); initIndex++) {
			ArrayList<FiringSequence> sequences = allSequences.get(initIndex).getSequences();
			if (sequences.size()>0){
				Marking init = mid.getInitialMarkings().get(initIndex);
				TransitionTreeNode initNode = createNewPositiveNode(root, new Transition(MID.ConstructorEvent), null, init);
				for (FiringSequence firingSequence: sequences){
					checkForCancellation();
					createFiringSequence(initNode, firingSequence);
				}
			}
		}	
		if (systemOptions.getMaxIdDepth()>0 || mid.getInitialMarkings().size()>1)
			root.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}
}
