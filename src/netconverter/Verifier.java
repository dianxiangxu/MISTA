package netconverter;

import java.util.ArrayList;

import locales.LocaleBundle;
import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;

public class Verifier {

	protected MID mid;
	
	protected FiringSequence[][] firingSequences; 
	protected ArrayList<Marking> goalMarkings;

	public Verifier(MID mid, ArrayList<Marking> goalMarkings, FiringSequence[][] firingSequences) {
		this.mid = mid;
		this.goalMarkings = goalMarkings;
		this.firingSequences = firingSequences;
	}
	
	public MID getMID() {
		return mid;
	}

	public void setNet(MID mid) {
		this.mid = mid;
	}
	
	public FiringSequence[][] getFiringSequences(){
		return firingSequences;	
	}
	
	public int removeInvalidFiringSequences(){
		int numberOfInvalidFiringSequences = 0;
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			Marking initMarking = mid.getInitialMarkings().get(initIndex); 
			for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
				Marking goalMarking = goalMarkings.get(goalIndex);
				FiringSequence firingSequence = firingSequences[initIndex][goalIndex];
				if (firingSequence!=null)
					if (!replayFiringSequence(firingSequence, initMarking.clone(), goalMarking)) {
//System.out.println("Invalid firing sequence: "+firingSequences[initIndex][goalIndex]);						
						firingSequences[initIndex][goalIndex] = null;
						numberOfInvalidFiringSequences++;
					}
			}
		}
		return numberOfInvalidFiringSequences;
	}

	boolean replayFiringSequence(FiringSequence firingSequence, Marking currentMarking, Marking goalMarking){
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
		if (!currentMarking.includes(goalMarking)){
//			System.out.println("\nThe firing sequence cannot reach the goal state: "+goalMarking);
//			System.out.println("\nCurrent marking: "+currentMarking);						
		}
		return currentMarking.includes(goalMarking);
	}
	
	public String reportResult(){
		String result = "";
		for (int initIndex=0; initIndex<firingSequences.length; initIndex++) {
			Marking initMarking = mid.getInitialMarkings().get(initIndex);
			for (int goalIndex=0; goalIndex<firingSequences[initIndex].length; goalIndex++) {
				if (firingSequences[initIndex][goalIndex]!=null) {
					result += LocaleBundle.bundleString("Goal state")+": "+ goalMarkings.get(goalIndex) + " "+LocaleBundle.bundleString("is reachable")
							+ "\n"+LocaleBundle.bundleString("Initial state")+": " + initMarking
							+ "\n"+LocaleBundle.bundleString("Firing sequence")+": "
							+ "\n"+firingSequences[initIndex][goalIndex]
							+ "\n\n";
				}
			}
		}
		for (int goalIndex=0; goalIndex<firingSequences[0].length; goalIndex++) {
			if (!isGoalReached(goalIndex))
				result += LocaleBundle.bundleString("NO_PATH_IS_FOUND_TO_REACH_GOAL")+": "+ goalMarkings.get(goalIndex)+ ".\n\n";
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