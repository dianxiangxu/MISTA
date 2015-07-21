package simulation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import kernel.CancellationException;
import kernel.ProgressDialog;
import locales.LocaleBundle;

import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;
import testgeneration.TransitionTreeNode;

public class BFSPathFinder {
	private TransitionTreeNode root;
	
	protected ProgressDialog progressDialog;
	private MID mid;
	private int searchDepth; 
	private Marking initMarking;
	private ArrayList<Marking> givenGoalMarkings;
	private ArrayList<FiringSequence> firingSequences;

	
	private ArrayList<Marking> currentGoals;

	public BFSPathFinder(ProgressDialog progressDialog, MID mid, int searchDepth,Marking initMarking, ArrayList<Marking> givenGoalMarkings, ArrayList<FiringSequence> existingFiringSequences) throws CancellationException {
		this.progressDialog = progressDialog;
		this.mid = mid;
		this.searchDepth = searchDepth;
		this.initMarking = initMarking;
		this.givenGoalMarkings = givenGoalMarkings;
		this.firingSequences = existingFiringSequences;
		setCurrentGoals();
		createRootNode();
	}
	
 	protected ArrayList<FiringSequence> findPaths() throws CancellationException{
		TransitionTreeNode initNode = root.children().get(0);
 	   	Hashtable <String, Marking> expandedMarkings = new Hashtable <String, Marking>();  
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		queue.addLast(initNode);
		while (!queue.isEmpty() && currentGoals.size()!=0) {
				checkForCancellation();
				TransitionTreeNode node = queue.poll();
				if (node!=initNode)
					verifyCurrentGoals(node);
				if (node.getLevel()<=searchDepth) {
					createChildren(node);
					expandedMarkings.put(node.getMarking().getKeyString(mid.getPlaces()), node.getMarking());
					for (TransitionTreeNode child: node.children()) {
						if (expandedMarkings.get(child.getMarking().getKeyString(mid.getPlaces()))==null)
							queue.addLast(child);
					}
				}
		}
		return firingSequences;
	}

	protected void setCurrentGoals(){
		currentGoals = new ArrayList<Marking>();
		for (int goalIndex=0; goalIndex<givenGoalMarkings.size(); goalIndex++) {
			if (firingSequences.get(goalIndex)==null)
				currentGoals.add(givenGoalMarkings.get(goalIndex));
		}
	}
	
	protected void verifyCurrentGoals(TransitionTreeNode node) throws CancellationException{
		for (int i=currentGoals.size()-1; i>=0; i--) {
			Marking goal = currentGoals.get(i);
			if (node.getMarking().includes(goal)) {
				int goalIndex = currentGoals.indexOf(goal);
				firingSequences.set(goalIndex, node.createFiringSequence());
				currentGoals.remove(goal);
			}
			checkForCancellation();
		}
	} 
	
	protected void createChildren(TransitionTreeNode node) throws CancellationException{
		Marking currentMarking = node.getMarking();
    	for (Transition transition: mid.getTransitions()) {
    			checkForCancellation();
	    		Unifier unifier = new Unifier(transition, currentMarking);
	    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
	    		for (Substitution substitution: substitutions) {
	    				Marking newMarking = mid.fireTransition(currentMarking, transition, substitution);
	    				TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);
	    				node.add(newNode);
	    		}
    	}
 	}

	private void createRootNode(){
		root = new TransitionTreeNode(null, null, null);
		Transition constructor = new Transition(MID.ConstructorEvent);
		root.add(new TransitionTreeNode(constructor, null, initMarking));
    	root.resetChildrenOutlineNumbers(searchDepth);
	}

	public void setProgressDialog(ProgressDialog progressDialog){
		this.progressDialog = progressDialog;
	}

	public ProgressDialog getProgressDialog(){
		return progressDialog;
	}
	
	public void checkForCancellation() throws CancellationException {
		if (progressDialog!=null && progressDialog.isCancelled())
			throw new CancellationException(LocaleBundle.bundleString("Test analysis cancelled"));
	}


}