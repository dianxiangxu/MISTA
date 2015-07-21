package verification;

import java.util.ArrayList;

import kernel.CancellationException;
import kernel.ProgressDialog;
import locales.LocaleBundle;

import mid.AssertionProperty;
import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;

import testgeneration.TransitionTreeNode;

public abstract class AssertionVerifier extends AbstractPropertyVerifier {
	
	private ArrayList<AssertionProperty> givenAssertions = new ArrayList<AssertionProperty>();
	protected ArrayList<AssertionProperty> workingAssertions = new ArrayList<AssertionProperty>();

	public AssertionVerifier(MID mid, int searchDepth, ProgressDialog progressDialog, ArrayList<AssertionProperty> assertions) {
		super(mid, searchDepth, progressDialog);
		firingSequences = new FiringSequence[mid.getInitialMarkings().size()][assertions.size()];
		this.givenAssertions = assertions;
	}

	public AssertionVerifier(MID mid, int searchDepth, ProgressDialog progressDialog) {
		this(mid, searchDepth, progressDialog, mid.getAssertionProperties());
	}
	
	abstract protected void verify() throws CancellationException;
	
	protected void setWorkingAssertions(){
		workingAssertions.clear();
		for (AssertionProperty assertion: givenAssertions)
			workingAssertions.add(assertion);		
	}
	
	protected void verifyAssertions(TransitionTreeNode node) throws CancellationException{
		for (int i=workingAssertions.size()-1; i>=0; i--) {
			AssertionProperty assertion = workingAssertions.get(i);
			if (!node.getMarking().isAssertionTrue(assertion)) {
				int assertionIndex = mid.getAssertionProperties().indexOf(assertion);
				firingSequences[node.getInitMarkingIndex()-1][assertionIndex] = node.createFiringSequence();
				workingAssertions.remove(assertion);
			}
			checkForCancellation();
		}
	} 
	
	boolean replayFiringSequence(FiringSequence firingSequence, Marking initialMarking, AssertionProperty assertion){
		Marking currentMarking = initialMarking;
		for (Firing firing: firingSequence.getSequence()){
			if (mid.isFirable(currentMarking, firing.getTransition(), firing.getSubstitution()))
				currentMarking = mid.fireTransition(currentMarking, firing.getTransition(), firing.getSubstitution());
			else {
/*				System.out.println("\nTransition "+ firing.getTransition()
							+ "\nis not firable by substitution "+firing.getSubstitution().toString(firing.getTransition().getAllVariables())
							+ "\nunder marking " + currentMarking);
*/				return false;
			}
		}	
		if (!currentMarking.isAssertionTrue(assertion)){
//			System.out.println("\nThe firing sequence does not satisfy assertion: "+assertion);
//			System.out.println("\nCurrent marking: "+currentMarking);						
		}
		return currentMarking.isAssertionTrue(assertion);
	}

	public String reportResult(){
		String result = "";
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			Marking initMarking = mid.getInitialMarkings().get(initIndex);
			for (int assertionIndex=0; assertionIndex<firingSequences[initIndex].length; assertionIndex++) {
				if (firingSequences[initIndex][assertionIndex]!=null) {
					result += LocaleBundle.bundleString("COUNTEREXAMPLE")+": Assertion "+ givenAssertions.get(assertionIndex).getAssertionString()
							+ "\n"+LocaleBundle.bundleString("Initial state")+": " + initMarking
							+ "\n"+LocaleBundle.bundleString("Resultant state")+": " + getResultantMarking(firingSequences[initIndex][assertionIndex], initMarking)
							+ "\n"+LocaleBundle.bundleString("Firing sequence")+": "
							+ "\n"+firingSequences[initIndex][assertionIndex]
							+ "\n";
				}
			}
		}
		for (int assertionIndex=0; assertionIndex<firingSequences[0].length; assertionIndex++) {
			if (!isAssertionSatisfied(assertionIndex))
				result += "Assertion "+givenAssertions.get(assertionIndex).getAssertionString()+" "+ LocaleBundle.bundleString("IS_TRUE_UNDER_THE_SEARCH_CONDITION")+"\n";
		}
		return result;
	}
	
	private boolean isAssertionSatisfied(int assertionIndex){
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			if (firingSequences[initIndex][assertionIndex]!=null) 
				return true;
		}
		return false;
	}

}