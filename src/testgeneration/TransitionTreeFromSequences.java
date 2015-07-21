/*  
	Author Dianxiang Xu
*/
package testgeneration;

import kernel.SystemOptions;
import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;

public abstract class TransitionTreeFromSequences extends TransitionTree {

	private static final long serialVersionUID = 1L;
	private String testGenerationMessage = "";

	public TransitionTreeFromSequences(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}

	protected TransitionTreeNode createNewPositiveNode(TransitionTreeNode currentNode, Transition transition, Substitution substitution, Marking newMarking) {
// merging sequence outperformed not-merging: reduce manual inputs and duplicate code.  
		if (currentNode.hasChildren()){
			for (TransitionTreeNode child: currentNode.children()){
				if (child.getTransition()==transition && child.getSubstitution().equals(substitution))
					return child;
			}
		}
		TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);  
		currentNode.add(newNode);
		return newNode;
	}

	private void createNewNegativeNode(TransitionTreeNode currentNode, Transition transition, Substitution substitution) {
		TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, currentNode.getMarking(), true);  
		currentNode.add(newNode);
	}

	protected void createFiringSequence(TransitionTreeNode currentNode, FiringSequence firingSequence) {
	    Marking currentMarking = currentNode.getMarking();
	    for (Firing firing: firingSequence.getSequence()){
	        Transition transition = firing.getTransition();
	        Substitution substitution = firing.getSubstitution();
			if (mid.isFirable(currentMarking, transition, substitution)){
				currentMarking = mid.fireTransition(currentMarking, transition, substitution);
				currentNode = createNewPositiveNode(currentNode, transition, substitution, currentMarking);
			}
			else {// not firable, become dirty test
				createNewNegativeNode(currentNode, transition, substitution);
				String substitutionString = substitution.printAllBindings();
				if (!substitutionString.equals(""))
					substitutionString = " with substitution "+substitutionString;
				testGenerationMessage += "\nTransition "+transition.getEvent()+"\nis not firable" + substitutionString +" under state "+currentMarking+"\n";
				return; 
			}
					
	    }
	}

	public String getTestGenerationMessage() {
		return testGenerationMessage;
	}

}