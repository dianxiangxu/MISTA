package verification;

import java.util.ArrayList;

import kernel.CancellationException;
import kernel.ProgressDialog;
import testgeneration.TransitionTreeNode;
import locales.LocaleBundle;
import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;

public class AbstractPropertyVerifier {

	protected MID mid;
	protected int searchDepth; 
	protected ProgressDialog progressDialog;
	
	protected FiringSequence[][] firingSequences; 
	
	protected TransitionTreeNode root;

	public AbstractPropertyVerifier(MID mid, int searchDepth, ProgressDialog progressDialog) {
		this.mid = mid;
		this.searchDepth = searchDepth;
		this.progressDialog = progressDialog;
		createRootNode();
	}

	public MID getMID() {
		return mid;
	}

	public FiringSequence[][] getFiringSequences(){
		return firingSequences;
	}
	
	protected Marking getResultantMarking(FiringSequence firingSequence, Marking initialMarking){
		Marking currentMarking = initialMarking;
		for (Firing firing: firingSequence.getSequence()){
			if (mid.isFirable(currentMarking, firing.getTransition(), firing.getSubstitution()))
				currentMarking = mid.fireTransition(currentMarking, firing.getTransition(), firing.getSubstitution());
			else {
				return null;
			}
		}	
		return currentMarking;
	}

	private void createRootNode(){
		root = new TransitionTreeNode(null, null, null);
		Transition constructor = new Transition(MID.ConstructorEvent);
		for (Marking initialMarking: mid.getInitialMarkings()) 
			root.add(new TransitionTreeNode(constructor, null, initialMarking));
    	root.resetChildrenOutlineNumbers(searchDepth);
	}

	protected void createChildren(TransitionTreeNode node) throws CancellationException{
		Marking currentMarking = node.getMarking();
    	for (Transition transition: mid.getTransitions()) {
    			checkForCancellation();
	    		Unifier unifier = new Unifier(transition, currentMarking);
	    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
	    		for (Substitution substitution: substitutions) {
//	    			if (!TransitionTreeForStructureCoverage.isIndependentFiring(transition, substitution, node.children())){
//	    			System.out.println(transition.getEvent()+": "+substitution.toString(transition.getAllVariables()));
	    				Marking newMarking = mid.fireTransition(currentMarking, transition, substitution);
	    				TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);
	    				node.add(newNode);
//	    			System.out.println("New marking: "+newNode.getMarking());
//	    			}
	    		}
    	}
//   		node.resetChildrenOutlineNumbers(searchDepth);
 	}

	public void checkForCancellation() throws CancellationException {
		if (progressDialog!=null && progressDialog.isCancelled())
			throw new CancellationException(LocaleBundle.bundleString("Verification cancelled"));
	}

}