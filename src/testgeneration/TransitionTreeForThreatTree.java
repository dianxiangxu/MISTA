/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;


import kernel.CancellationException;
import kernel.SystemOptions;
import mid.ThreatTreeNode;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;

public class TransitionTreeForThreatTree extends TransitionTree{
	private static final long serialVersionUID = 1L;

	public TransitionTreeForThreatTree(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
	}
	
	protected void createRootNode(){
		root = new TransitionTreeNode(null, null, null);
	}

	public void generateTransitionTree() throws CancellationException{
		createRootNode();
		TransitionTreeNode start = new TransitionTreeNode(new Transition(MID.ConstructorEvent), null, new Marking());
		start.setOutlineNumber("1");
		root.add(start);
		ArrayList<ThreatTreeNode> threatTreeRoots = mid.getThreatTreeRoots();
		for (ThreatTreeNode threatTreeRoot: threatTreeRoots)
			for (ArrayList<String> attackPath: generateAttackPaths(threatTreeRoot))
				mergeThreatPathIntoTree(start, attackPath);
		start.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}

	private void mergeThreatPathIntoTree(TransitionTreeNode start, ArrayList<String> attackPath){
		TransitionTreeNode currentNode = start;
		for (String event: attackPath){
			TransitionTreeNode nodeForCurrentEvent = findEquivalentNodeFromChildren(currentNode, event);
			if (nodeForCurrentEvent ==null){
				nodeForCurrentEvent = new TransitionTreeNode(mid.createTransitionForEvent(event), new Substitution(), new Marking());
				currentNode.add(nodeForCurrentEvent);
			}
			currentNode = nodeForCurrentEvent;
		}
	}

	private TransitionTreeNode findEquivalentNodeFromChildren(TransitionTreeNode currentNode, String event){
		for (TransitionTreeNode child: currentNode.children())
			if (child.getEvent().equalsIgnoreCase(event))
				return child;
		return null;
	}
	
	private ArrayList<ArrayList<String>> generateAttackPaths(ThreatTreeNode threatTreeNode) throws CancellationException{
		checkForCancellation();
		ArrayList<ArrayList<String>> attackPaths = new ArrayList<ArrayList<String>>();
		if (threatTreeNode.isLeaf()){
			ArrayList<String> path = new ArrayList<String>();
			path.add(threatTreeNode.getEvent());
			attackPaths.add(path);
			return attackPaths;
		} 
		ArrayList<ThreatTreeNode> childNodes = threatTreeNode.getChildren();
		attackPaths = generateAttackPaths(childNodes.get(0));
		if (childNodes.size()==1)
			return attackPaths;
		if (threatTreeNode.isRelationAND()){
			for (int i=1; i<childNodes.size(); i++)
				attackPaths = concatSequences(attackPaths, generateAttackPaths(childNodes.get(i)));
		} else {
			for (int i=1; i<childNodes.size(); i++)
				attackPaths.addAll(generateAttackPaths(childNodes.get(i)));			
		}
		return attackPaths;
	}
	
	private ArrayList<ArrayList<String>> concatSequences(ArrayList<ArrayList<String>> set1, ArrayList<ArrayList<String>> set2){
		ArrayList<ArrayList<String>> set3 = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> sequenceInSet1: set1)
			for (ArrayList<String> sequenceInSet2: set2) {
				ArrayList<String> concatSequence = new ArrayList<String>();
				concatSequence.addAll(sequenceInSet1);
				concatSequence.addAll(sequenceInSet2);
				set3.add(concatSequence);
			}
		return set3;
	}
}
