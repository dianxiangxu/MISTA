/* 	
	Author Dianxiang Xu
*/
package testgeneration;
import kernel.CancellationException;
import kernel.SystemOptions;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Transition;
import verification.AbstractPropertyVerifier;

public class TransitionTreeFromVerificationResults extends TransitionTreeFromSequences {
	private static final long serialVersionUID = 1L;
	
	private AbstractPropertyVerifier verificationResult;
	
	public TransitionTreeFromVerificationResults(MID mid, SystemOptions systemOptions, AbstractPropertyVerifier verificationResult) {
		super(mid, systemOptions);
		this.verificationResult = verificationResult;
		root = new TransitionTreeNode(null, null, null);
	}

	public void generateTransitionTree() throws CancellationException {
		FiringSequence[][] firingSequences = verificationResult.getFiringSequences();	
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			Marking init = mid.getInitialMarkings().get(initIndex);
			if (hasSequences(firingSequences, initIndex)) {
				TransitionTreeNode initNode = createNewPositiveNode(root, new Transition(MID.ConstructorEvent), null, init);
				initNode.setOutlineNumber((initIndex+1)+"");
				for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
					checkForCancellation();
					if (firingSequences[initIndex][goalIndex]!=null) {
						createFiringSequence(initNode, firingSequences[initIndex][goalIndex]);
					}
				}
				initNode.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
			}
		}	
	}

	private boolean hasSequences(FiringSequence[][] firingSequences, int initIndex){
		for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
			if (firingSequences[initIndex][goalIndex]!=null) 
				return true;
		}
		return false;
	}
}
