/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;


import kernel.CancellationException;
import kernel.SystemOptions;
import mid.MID;
import mid.Marking;
import mid.PairwiseUnifier;
import mid.Predicate;
import mid.Substitution;
import mid.Transition;
import mid.Tuple;
import mid.Unifier;
import mid.GeneralUnifier;


public abstract class TransitionTreeForStructureCoverage extends TransitionTree {
	private static final long serialVersionUID = 1L;

	private static final String GuardIsAlwaysFalse = "0";
	
	private boolean areDirtyTestsAllowed = true;
	
	public static boolean SINK_EVENTS_DISABLED = false;
	
	protected boolean areSinkEventsEnabled = true;
	
	public TransitionTreeForStructureCoverage(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions);
		this.areSinkEventsEnabled = true;
	}

	// for deadlock state coverage, sinks are disabled
	public TransitionTreeForStructureCoverage(MID mid, SystemOptions systemOptions, boolean areSinkEventsEnabled) {
		super(mid, systemOptions);
		this.areSinkEventsEnabled = areSinkEventsEnabled;;
	}
	
	public void generateTransitionTree() throws CancellationException {
		createRootNode();
		expand();
		if (areDirtyTestsAllowed && systemOptions.areDirtyTestsNeeded()) {
			findSubstitutionsForDirtyTests();
			if (systemOptions.getMaxIdDepth()>0 || mid.getInitialMarkings().size()>1)
				root.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
		} 
		else	// do not handle sink events when dirty tests are generated
			if (areSinkEventsEnabled && mid.hasSinkEvents()) {
				expandNonSinkLeafNodes();			
				removeNonSinkPaths();
			}

	}

	public void setDirtyTestsAllowd(boolean allowed){
		areDirtyTestsAllowed = allowed;
	}
	
	abstract protected void expand() throws CancellationException;

	protected void createChildren(TransitionTreeNode node)  throws CancellationException{
		for (String event: mid.getEvents()){
			ArrayList<Transition> transitions = mid.getTransitionsForEvent(event);
			if (transitions!=null && transitions.size()>0)
				createTestNodes(transitions, node);
			else if (systemOptions.areDirtyTestsNeeded()) 
				createDirtyTestNodes(new Transition(event), node);
		}
    	node.setExpanded(true);
   		node.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
	}

	// begin- interleaving of independent firings
	private static ArrayList<Predicate> getTokenSetForCondition(ArrayList<Predicate> condition, Substitution substitution){
		ArrayList<Predicate> tokens = new ArrayList<Predicate>();
    	for (Predicate predicate: condition){
    		Tuple tuple = substitution.substitute(predicate);
    		tokens.add(new Predicate(predicate.getName(), tuple.getArguments()));
    	}
    	return tokens;
	}
	
	private static boolean areDisjointTokenSets(ArrayList<Predicate> set1, ArrayList<Predicate> set2){
		for (Predicate token1: set1) {
			for (Predicate token2: set2)
				if (token1.equals(token2))
					return false;
		}
/*		
System.out.println("Disjoint token sets");
for (Predicate token1: set1) 
	System.out.println(token1);
System.out.println("--------------------");
for (Predicate token2: set2) 
	System.out.println(token2);
*/
		return true;
	}
	
	private static boolean hasNoResetConflict(ArrayList<Predicate> currentPrecondTokens, ArrayList<Predicate> addPostcondition){
		for (Predicate postPredicate: addPostcondition){
	   		if (postPredicate.getName().equalsIgnoreCase(MID.RESET)){
    			String resetPlace = postPredicate.getArguments().get(0);
    			for (Predicate precondToken: currentPrecondTokens)
    				if (precondToken.getName().equals(resetPlace))
    					return false;		// there is a conflict
    		}
		}
		return true;
	}
	
	public static boolean isIndependentFiring(Transition transition, Substitution substitution, Vector<TransitionTreeNode> existingChildren){
		ArrayList<Predicate> currentPrecondTokens = getTokenSetForCondition(transition.getPrecondition(), substitution);
		for (TransitionTreeNode existingChild: existingChildren){
			if (existingChild.getTransition().getDeletePrecondition()!=null){
				ArrayList<Predicate> childDelCondTokens = getTokenSetForCondition(existingChild.getTransition().getDeletePrecondition(), existingChild.getSubstitution());
				if (!existingChild.isNegative() && 
					(childDelCondTokens.size()>0 || existingChild.getTransition().getAddPostcondition().size()>0) &&   // otherwise states may be missing because a child node without state change may not be expanded
					areDisjointTokenSets(currentPrecondTokens, childDelCondTokens)
					&& 
					hasNoResetConflict(currentPrecondTokens, existingChild.getTransition().getAddPostcondition())
					) // <transition,substitution> will be fired under this child. NO need to create a new node 
				return true;
			}
		}
		return false;
	}
	// end - interleaving of independent firings
	
	private void createTestNodes(ArrayList<Transition> transitions, TransitionTreeNode node) throws CancellationException{
		String compoundGuard = "";
		boolean needDirtyTest = true;
		Marking currentMarking = node.getMarking();
		for (Transition transition: transitions) {
    		GeneralUnifier unifier = systemOptions.isPairwiseTesting() && mid.isCombinatorialTestingApplicable(transition)? new PairwiseUnifier(transition, currentMarking): new Unifier(transition, currentMarking);
    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
    		for (Substitution substitution: substitutions) {
    			checkForCancellation();
    			if (systemOptions.isTotalOrdering() || !isIndependentFiring(transition, substitution, node.children())){
//System.out.println(transition.getEvent()+": "+substitution.toString(transition.getAllVariables()));
    			Marking newMarking = mid.fireTransition(currentMarking, transition, substitution);
    			TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, newMarking);
    			node.add(newNode);
//    			System.out.println("New marking: "+newNode.getMarking());
    			} 
/*    			else {
    				System.out.println("No node for "+transition.getEvent()+": "+substitution.toString(transition.getAllVariables()));
    			}
*/    		}
    		if (areDirtyTestsAllowed && systemOptions.areDirtyTestsNeeded() && substitutions.size()>0) {
    			if (transition.hasGuard())
    				compoundGuard += "!("+transition.getGuard() + ") && ";
    			else
    				needDirtyTest = false;
    		}
			checkForCancellation();
		}
		// 
		// Notes: 	IF dirty tests are wanted when there are firings for event e
		// 			THEN associate a guard condition with the transition of event e  
		// Reason: if there is no guard and there is one or more firings, then needDirtyTests == false
		//
//		needDirtyTest = needDirtyTest || AlwaysNeedDirtyTest;
			
		if (needDirtyTest && !mid.isNonNegativeTransition(transitions.get(0)) && areDirtyTestsAllowed && systemOptions.areDirtyTestsNeeded()) {
			createDirtyTestNodes(transitions.get(0).getEvent(), compoundGuard, node);
		}
	}
	
	private void createDirtyTestNodes(String event, String compoundGuard, TransitionTreeNode node){
		if (compoundGuard.length()>0) {
			compoundGuard = compoundGuard.substring(0, compoundGuard.length()-4);
			if (compoundGuard.equals(GuardIsAlwaysFalse))
				return;
		}
		Transition newTransition = new Transition(event);
		newTransition.setGuard(compoundGuard);
		newTransition.setArguments(new ArrayList<String>());
		createDirtyTestNodes(newTransition, node);
	}

	private void createDirtyTestNodes(Transition transition, TransitionTreeNode node){
		node.add(new TransitionTreeNode(transition, new Substitution(), node.getMarking(), true));
	}

	// Start of searching a substitution for each of the dirty test nodes 
	// Choice 1: one from a dirty unit test
	// Choice 2: one from a clean unit test, but not used by siblings
	// Choice 3: one from all substitutions for the event in the transition tree, but not used by the siblings 
	private void findSubstitutionsForDirtyTests()  throws CancellationException {
		SubstitutionsForEvents substitutionsForEvents = new SubstitutionsForEvents(root);
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (node.isNegative()) {
				if (!findDirtySubstitutionFromDirtyUnitTests(node) && !findDirtySubstitutionFromCleanUnitTests(node))
					findDirtySubstitutionFromTree(substitutionsForEvents, node);
			}
			for (TransitionTreeNode child: node.children())
				queue.addLast(child);					
		}
	}

	private boolean findDirtySubstitutionFromDirtyUnitTests(TransitionTreeNode dirtyNode) {
		Substitution substitution = mid.getDirtySubstitutionFromUnitTests(dirtyNode.getEvent());
		if (substitution!=null){
			setDirtyTestSubstitutionFromUnitTests(dirtyNode, substitution);
			return true;
		}
		return false;
	}
		
	private boolean findDirtySubstitutionFromCleanUnitTests(TransitionTreeNode dirtyNode)  throws CancellationException{
		ArrayList<Substitution> substitutions = mid.getCleanSubstitutionsFromUnitTests(dirtyNode.getEvent());
		if (substitutions.size()==0)
			return false;
		Vector<TransitionTreeNode> siblings = getSiblingsForEvent(dirtyNode); 
		for (Substitution substitution: substitutions){
			checkForCancellation();
			boolean feasible = true;
			for (int i=0; i<siblings.size() && feasible; i++)
				if (substitution.equals(siblings.get(i).getSubstitution())){
					feasible = false;
				}
			if (feasible) {
				setDirtyTestSubstitutionFromUnitTests(dirtyNode, substitution);
				return true;
			}
		}
		return false;
	}
	
	// Find the first feasible substitution for the event in a negative test node
	// Only if the firings of the event involve variables  
	private void findDirtySubstitutionFromTree(SubstitutionsForEvents substitutionsForEvents, TransitionTreeNode node)  throws CancellationException{
		ArrayList<TransitionTreeNode> substitutionNodes = substitutionsForEvents.findSubstitutionsForEvent(node.getEvent());
		if (substitutionNodes.size()==0) {
			return;
		}
		Vector<TransitionTreeNode> siblings = getSiblingsForEvent(node); 
		for (TransitionTreeNode substitutionNode: substitutionNodes){
			checkForCancellation();
			boolean feasible = true;
			for (int i=0; i<siblings.size() && feasible; i++)
				if (substitutionNode.getSubstitution().equals(siblings.get(i).getSubstitution())){
//System.out.println("\n equal substitutions " + substitutionNode + "->"+siblings.get(i).getSubstitution());				
					feasible = false;
				}
			if (feasible) {
				setDirtyTestSubstitutionFromTransitionTree(node, substitutionNode);
				//System.out.println("\n set substitutions " + substitutionNode);
				return;
			}
		}
		// no feasible substitution - should delete node 
		TransitionTreeNode parent = node.getParent();
		parent.children().remove(node);
	}

	private void setDirtyTestSubstitutionFromUnitTests(TransitionTreeNode dirtyNode, Substitution substitution){
		dirtyNode.setSubstitution(substitution);
		Transition transition = mid.findTransition(dirtyNode.getEvent());
		dirtyNode.getTransition().setArguments(transition.getArguments());
		dirtyNode.getTransition().setAllVariables(transition.getAllVariables());
	} 

	private void setDirtyTestSubstitutionFromTransitionTree(TransitionTreeNode dirtyNode, TransitionTreeNode substitutionNode){
		dirtyNode.setSubstitution(substitutionNode.getSubstitution());
		Transition transition = dirtyNode.getTransition();
		transition.setArguments(substitutionNode.getTransition().getArguments());
		transition.setAllVariables(substitutionNode.getTransition().getAllVariables());
	} 
	
	private Vector<TransitionTreeNode> getSiblingsForEvent(TransitionTreeNode node){
		Vector<TransitionTreeNode> siblings = new Vector<TransitionTreeNode>(); 
		for (TransitionTreeNode sibling: node.getParent().children())
			if (sibling.getEvent().equals(node.getEvent()) && !sibling.isNegative() && sibling!=node) {
				siblings.add(sibling);
//System.out.println("\n sibling " + node +"->" +sibling);
			}
		return siblings;
	}
	// End of searching substitutions for negative test nodes
	
	
	
	//_______________________________________________________________________________________________________________________________
	// START: Sink events
	private void removeNonSinkPaths() throws CancellationException{
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf() && !mid.isSinkTransition(node.getTransition()))
				removeNonSinkLeafNode(node);
			else
				for (int i=node.children().size()-1; i>=0; i--)
					stack.push(node.children().get(i));
		}
	}
	
	private void removeNonSinkLeafNode(TransitionTreeNode node) throws CancellationException{
		TransitionTreeNode toBeRemoved = node;
		do {
			checkForCancellation();
			TransitionTreeNode parent = toBeRemoved.getParent();
			parent.children().remove(toBeRemoved);
			toBeRemoved.setParent(null);
			toBeRemoved = !parent.isRoot() && parent.isLeaf()? parent: null;
		} while (toBeRemoved!=null);		
	}
	
	
	// find one sink path for each non-sink leaf
	private void expandNonSinkLeafNodes() throws CancellationException{
		ArrayList<TransitionTreeNode> sinkLeaves = new ArrayList<TransitionTreeNode>();
		ArrayList<TransitionTreeNode> nonSinkLeaves = new ArrayList<TransitionTreeNode>(); 
		if (systemOptions.isBreadthFirstSearch()) 
			regroupLeafNodesBFS(sinkLeaves, nonSinkLeaves);
		else
			regroupLeafNodesDFS(sinkLeaves, nonSinkLeaves);			
		boolean done = true;
		do {
			done = true;
//System.out.println("Sink size: "+sinkLeaves.size()+"  Non-sink leaves: "+ nonSinkLeaves.size());					
			for (int index=nonSinkLeaves.size()-1; index>=0; index--){
				checkForCancellation();
				TransitionTreeNode nonSinkLeaf = nonSinkLeaves.get(index);
				ArrayList<TransitionTreeNode> sinkPath = findSinkPath(sinkLeaves, nonSinkLeaf);				
				if (sinkPath!=null && sinkPath.size()>0){
					TransitionTreeNode currentNonsinkNode = nonSinkLeaf;
					for (TransitionTreeNode child: sinkPath){
						checkForCancellation();
						TransitionTreeNode newChild = new TransitionTreeNode(child.getTransition(), child.getSubstitution(), child.getMarking());
						currentNonsinkNode.add(newChild);
						currentNonsinkNode = newChild;
					}
			    	nonSinkLeaf.resetChildrenOutlineNumbers(systemOptions.getMaxIdDepth());
			    	nonSinkLeaves.remove(nonSinkLeaf);
			    	sinkLeaves.add(currentNonsinkNode);
			    	done =false;
				}
			}
		}
		while (!done);
	}

	protected void regroupLeafNodesBFS(ArrayList<TransitionTreeNode> sinkLeaves, ArrayList<TransitionTreeNode> nonSinkLeaves)  throws CancellationException{
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (node.isLeaf() && !node.isNegative()){
				if (mid.isSinkTransition(node.getTransition()))
					sinkLeaves.add(node);
				else if (hasEnabledTransitions(mid.getTransitions(), node))
						nonSinkLeaves.add(0, node);
			}
			for (TransitionTreeNode child: node.children())
				queue.addLast(child);
		}
	}

	
	protected void regroupLeafNodesDFS(ArrayList<TransitionTreeNode> sinkLeaves, ArrayList<TransitionTreeNode> nonSinkLeaves)  throws CancellationException{
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (node.isLeaf() && !node.isNegative()){
				if (mid.isSinkTransition(node.getTransition()))
					sinkLeaves.add(node);
				else if (hasEnabledTransitions(mid.getTransitions(),node))
						nonSinkLeaves.add(0, node);
			}
			for (int i=node.children().size()-1; i>=0; i--)
				stack.push(node.children().get(i));
		}
	}

	private boolean hasEnabledTransitions(ArrayList<Transition> transitions, TransitionTreeNode node){
		Marking currentMarking = node.getMarking();
		for (Transition transition: transitions) {
    		Unifier unifier = new Unifier(transition, currentMarking);
    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
    		if (substitutions.size()>0)
    			return true;
		}
		return false;
	}


	private ArrayList<TransitionTreeNode> findSinkPath(ArrayList<TransitionTreeNode> sinkLeaves, TransitionTreeNode nonSinkLeaf) throws CancellationException{
		ArrayList<TransitionTreeNode> sinkPath = new ArrayList<TransitionTreeNode>();					
		for (TransitionTreeNode sinkLeaf: sinkLeaves) {
			checkForCancellation();
			sinkPath.clear();
			TransitionTreeNode currentNode = sinkLeaf;
			while (!currentNode.getParent().isRoot() && !currentNode.getParent().getMarking().equals(nonSinkLeaf.getMarking())){
					sinkPath.add(0, currentNode);
					currentNode = currentNode.getParent();
					checkForCancellation();
			}
			if (!currentNode.getParent().isRoot()){
				sinkPath.add(0, currentNode);
//System.out.println("Found sink path: "+sinkPath.size());					
				return sinkPath;
			} 
		}
//		System.out.println("Found no sink path: "+nonSinkLeaf);					
		return null;
	}

	// END: Sink Events
}
