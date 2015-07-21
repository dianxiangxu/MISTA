package verification;

import java.util.ArrayList;

import kernel.CancellationException;
import kernel.ProgressDialog;
import locales.LocaleBundle;

import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.GoalProperty;

import testgeneration.TransitionTreeNode;

public abstract class GoalVerifier extends AbstractPropertyVerifier {
	protected ArrayList<GoalProperty> goals = new ArrayList<GoalProperty>();

	protected boolean searchForHomeStates;
	
	public GoalVerifier(MID mid, int searchDepth, ProgressDialog progressDialog, boolean searchForHomeStates) {
		super(mid, searchDepth, progressDialog);
		this.searchForHomeStates = searchForHomeStates;
		firingSequences = new FiringSequence[mid.getInitialMarkings().size()][mid.getGoalProperties().size()];
	}

	protected void setGoals(){
		goals.clear();
		for (GoalProperty propertyTransition: mid.getGoalProperties())
			goals.add(propertyTransition);		
	}
	
	abstract protected void verify() throws CancellationException;
	
	protected void verifyGoals(TransitionTreeNode node) throws CancellationException{
		for (int i=goals.size()-1; i>=0; i--) {
			GoalProperty goalTransition = goals.get(i);
			if (node.getMarking().isFirable(goalTransition)) {
				int goalIndex = mid.getGoalProperties().indexOf(goalTransition);
				firingSequences[node.getInitMarkingIndex()-1][goalIndex] = node.createFiringSequence();
				goals.remove(goalTransition);
			}
			checkForCancellation();
		}
	} 
	
	public int removeInvalidFiringSequences(){
		int numberOfInvalidFiringSequences = 0;
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			Marking initMarking = mid.getInitialMarkings().get(initIndex); 
			for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
				GoalProperty goalProperty = mid.getGoalProperties().get(goalIndex);
				FiringSequence firingSequence = firingSequences[initIndex][goalIndex];
				if (firingSequence!=null)
					if (!replayFiringSequence(firingSequence, initMarking.clone(), goalProperty)) {
//System.out.println("Invalid firing sequence: "+firingSequences[initIndex][goalIndex]);						
						firingSequences[initIndex][goalIndex] = null;
						numberOfInvalidFiringSequences++;
					}
			}
		}
		return numberOfInvalidFiringSequences;
	}

	boolean replayFiringSequence(FiringSequence firingSequence, Marking initialMarking, GoalProperty goalProperty){
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
		if (!currentMarking.isFirable(goalProperty)){
//			System.out.println("\nThe firing sequence cannot reach the goal state: "+goalProperty);
//			System.out.println("\nCurrent marking: "+currentMarking);						
		}
		return currentMarking.isFirable(goalProperty);
	}

	public String reportResult(){
		String result = "";
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			Marking initMarking = mid.getInitialMarkings().get(initIndex);
			for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
				if (firingSequences[initIndex][goalIndex]!=null) {
					result += LocaleBundle.bundleString("Goal state")+": "+ mid.getGoalProperties().get(goalIndex).getPropertyString() + " "+LocaleBundle.bundleString("is reachable")
							+ "\n"+LocaleBundle.bundleString("Initial state")+": " + initMarking
							+ "\n"+LocaleBundle.bundleString("Resultant state")+": " + getResultantMarking(firingSequences[initIndex][goalIndex], initMarking)
							+ "\n"+LocaleBundle.bundleString("Firing sequence")+": "
							+ "\n"+firingSequences[initIndex][goalIndex]
							+ "\n";
				}
			}
		}
		for (int goalIndex=0; goalIndex<firingSequences[0].length; goalIndex++) {
			if (!isGoalReached(goalIndex))
				result += LocaleBundle.bundleString("NO_PATH_IS_FOUND_TO_REACH_GOAL")+": "+ mid.getGoalProperties().get(goalIndex).getPropertyString()+ ".\n";
		}
		return result;
	}
	
	private boolean isGoalReached(int goalIndex){
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			if (firingSequences[initIndex][goalIndex]!=null) 
				return true;
		}
		return false;
	}

}