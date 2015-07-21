/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.Random;

import kernel.CancellationException;
import kernel.SystemOptions;
import locales.LocaleBundle;

import mid.Firing;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import mid.Unifier;

public class TransitionTreeRandomGenerator extends TransitionTreeFromSequences {
	
	private static final long serialVersionUID = 1L;

	public TransitionTreeRandomGenerator(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
		root = new TransitionTreeNode(null, null, null);
	}

	public void generateTransitionTree() throws CancellationException {
		int testCount=1;
//		progressDialog.setCancelText(LocaleBundle.bundleString("Stop Test Generation"));
		while (testCount<=systemOptions.getMaxRandomTests() && generateRandomSequence(testCount)){
			testCount++;
		}
		if (systemOptions.getMaxIdDepth()>0 || mid.getInitialMarkings().size()>1)
			root.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
//		progressDialog.setCancelText(LocaleBundle.bundleString("Cancel"));
	}
	
	private Random randomIndexGenerator = new Random(); 

	private TransitionTreeNode currentNode=null;
	private Marking currentMarking = null;
	private ArrayList<Firing> possibleFirings = new ArrayList<Firing>();
	
	private void computePossibleFirings(){
		possibleFirings.clear();
		for (Transition transition: mid.getTransitions()){
			Unifier unifier = new Unifier(transition, currentMarking);
			for (Substitution substitution: unifier.getSubstitutions())
				possibleFirings.add(new Firing(transition, substitution));
		}
	}

	private void setRandomInitialMarking(){
		currentNode =null;
		ArrayList<Marking> initMarkings =  mid.getInitialMarkings();
		int randomInitMarkingIndex =0 ;
		if (initMarkings.size()>1){
			randomInitMarkingIndex = randomIndexGenerator.nextInt(initMarkings.size());
		}
		currentMarking=initMarkings.get(randomInitMarkingIndex);
		boolean initMarkingNodeExists = false;
		for (TransitionTreeNode initMarkingNode: root.children())
			if (initMarkingNode.getMarking()==currentMarking){
				initMarkingNodeExists = true;
				currentNode = initMarkingNode;
			}
		if (!initMarkingNodeExists){
			currentNode = createNewPositiveNode(root, new Transition(MID.ConstructorEvent), null, currentMarking);
		}
		if (systemOptions.getMaxIdDepth()==0 && mid.getInitialMarkings().size()==1)
			currentNode.setOutlineNumber("");
	}

	private Firing getRandomFiring(){
		if (possibleFirings.size()==0)
			return null;
		else if (possibleFirings.size()==1)
			return possibleFirings.get(0);
		int randomFiringIndex = randomIndexGenerator.nextInt(possibleFirings.size());
		return possibleFirings.get(randomFiringIndex);
	}
	
	private boolean generateRandomSequence(int testCount) throws CancellationException {
		progressDialog.setMessage(LocaleBundle.bundleString("Generating random test")+": "+testCount+"/"+systemOptions.getMaxRandomTests()+"...");
		setRandomInitialMarking();
		int depth =0;
		Firing randomFiring = null;
		do {
			checkForCancellation();
			computePossibleFirings();
			randomFiring = getRandomFiring();
			if (randomFiring!=null) { 
				currentMarking = mid.fireTransition(currentMarking, randomFiring.getTransition(), randomFiring.getSubstitution());
				currentNode = createNewPositiveNode(currentNode, randomFiring.getTransition(), randomFiring.getSubstitution(), currentMarking);
				depth++;
			}
		}
		while (randomFiring!=null && depth<systemOptions.getSearchDepth());
		return true;
	}
		
}
