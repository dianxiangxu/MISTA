/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package mid;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

public class Unifier implements GeneralUnifier{

		private Transition transition;
		private Marking marking;

		private int[] pointers;		// index of the tokens in each input place
		private int inputIndex = 0;	// index of the input place for unification
		private Hashtable <String, String> bindings = new Hashtable <String, String>();
		private Stack<BindingRecord> bindingHistory = new Stack<BindingRecord>();

		public Unifier(Transition transition, Marking marking) {
			this.transition = transition;
			this.marking = marking;
		}

		public Unifier(Transition transition, Marking marking, Substitution existingSubstitution) {
			this(transition, marking);
			bindings = existingSubstitution.getBindings();
		}

		// Find ALL possible substitutions for firing the transition under current marking
		public ArrayList<Substitution> getSubstitutions() {
			ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
			if (transition.numberOfInputs()==0) {
				if (isWhenConditionTrue())
					substitutions.add(new Substitution(bindings));
				return substitutions;
			}
			if (!hasOneTupleInEachInputPlace())
				return substitutions;

			initializePointers();
			inputIndex = 0;		
			// start from the first input place
			// Note that the first input can be a negation. This is considered in the loop body, not while
			ArrayList<Predicate> precondition = transition.getPrecondition();
			String firstInputPlace = transition.getPrecondition().get(0).getName();
			do {
				assert inputIndex<transition.numberOfInputs(): "InputIndex is out of bound"; 
				// start from the next tuple of the current input place(predicate) for unification
				pointers[inputIndex]++;
				Predicate predicate = precondition.get(inputIndex);
				ArrayList<Tuple> tuples = marking.getTuples(predicate.getName());
				if (!predicate.getNegation() && unify(predicate, tuples) || predicate.getNegation() && unifyForNegation(predicate, tuples)) {
					handleSuccessfulUnification(predicate, substitutions);
				} else { // unsuccessful unification
					if (inputIndex == 0 && predicate.getNegation()) 
						return substitutions; // done if the first input place is a negation
					else if (inputIndex>0) {  // the current input is not the first input place
						do { // keep backtrack until there is no negation	
							undoBindings();  
							backtrack();
							predicate = precondition.get(inputIndex);
						} while (inputIndex>0 && predicate.getNegation());
					} 
				}
			} while (inputIndex!=0 || pointers[0]<marking.numberOfTuples(firstInputPlace));
			
			return substitutions;
		}

		public boolean isFirable() {
			if (transition.numberOfInputs()==0) {
				return isWhenConditionTrue();
			}
			if (!hasOneTupleInEachInputPlace())
				return false;
			initializePointers();
			inputIndex = 0;		
			// start from the first input place
			// Note that the first input can be a negation. This is considered in the loop body, not while
			ArrayList<Predicate> precondition = transition.getPrecondition();
			String firstInputPlace = transition.getPrecondition().get(0).getName();
			do {
				assert inputIndex<transition.numberOfInputs(): "InputIndex is out of bound"; 
				// start from the next tuple of the current input place(predicate) for unification
				pointers[inputIndex]++;
				Predicate predicate = precondition.get(inputIndex);
				ArrayList<Tuple> tuples = marking.getTuples(predicate.getName());
				if (!predicate.getNegation() && unify(predicate, tuples) || predicate.getNegation() && unifyForNegation(predicate, tuples)) {
					if (inputIndex != transition.numberOfInputs()-1) {
						inputIndex++;
					} else {
					if (isWhenConditionTrue())
						return true;
					}
				} else { // unsuccessful unification
					if (inputIndex == 0 && predicate.getNegation()) 
						return false; // done if the first input place is a negation
					else if (inputIndex>0) {  // the current input is not the first input place
						do { // keep backtrack until there is no negation	
							undoBindings();  
							backtrack();
							predicate = precondition.get(inputIndex);
						} while (inputIndex>0 && predicate.getNegation());
					} 
				}
			} while (inputIndex!=0 || pointers[0]<marking.numberOfTuples(firstInputPlace));
			
			return false;
		}

		private void handleSuccessfulUnification(Predicate predicate, ArrayList<Substitution> substitutions){
			// successful unification at inputIndex
			if (inputIndex != transition.numberOfInputs()-1) {
				inputIndex++;
				return;
			}
			// A successful substitution is found if the when condition evaluates true 
			if (isWhenConditionTrue())
				substitutions.add(new Substitution(copyBindings()));
			// prepare for next round of unification
			 undoBindings();
			 // For not p, need to backtrack, because no other possible bindings at p 
			Predicate currentPredicate = transition.getPrecondition().get(inputIndex);
			while (currentPredicate.getNegation() && inputIndex>0){
				 backtrack();
				 currentPredicate = transition.getPrecondition().get(inputIndex);
			}
		}
		
		private void initializePointers(){
			pointers = new int[transition.numberOfInputs()];
			for (int index =0; index<pointers.length; index++)
				pointers[index] = -1;
		}

		private void backtrack() {
			pointers[inputIndex] = -1;
			inputIndex--;
			undoBindings();  
		}

		// each input place except for inhibitor (negation) inputs has at least one token 
		private boolean hasOneTupleInEachInputPlace() {
			for (Predicate predicate: transition.getPrecondition())
				if (!predicate.getNegation() && !marking.hasTuples(predicate.getName()))
					return false;
			return true;
		}

		// find next tuple (token) that unifies with the predicate
		private boolean unify(Predicate predicate, ArrayList<Tuple> tuples){
			ArrayList<String> labels = predicate.getArguments();
			while (pointers[inputIndex]<tuples.size()) {
				Tuple tuple = tuples.get(pointers[inputIndex]);
				assert labels.size()==tuple.arity(): "Unifer.java: cannot unify "+ predicate +" with tuple "+tuple;
				if (unifyLabels(labels, tuple))
					return true;
			}
			return false;
		}

		// Unify predicate arguments (arc labels) with a tuple
		// e.g., p(?x, ?y) vs. ("a", "b")
		private boolean unifyLabels(ArrayList<String> labels, Tuple tuple){
			for (int i=0; i<labels.size(); i++) {
				String tupleElement = tuple.getArguments().get(i);
				String label = labels.get(i);
				if (MID.isVariable(label)) {
					String boundValue = bindings.get(label);
					if (boundValue==null) {
//System.out.println(label+" is bound to "+tupleElement);							
						bindings.put(label, tupleElement);
						bindingHistory.push(new BindingRecord(label, inputIndex));
						continue;
					}
					else {
//System.out.println(label+" is "+boundValue);							
						label=boundValue;
					}
				}
				if (!label.equals(tupleElement)) {
//System.out.println(label+" != "+tupleElement);							
					undoBindings();
					pointers[inputIndex]++;
					return false;
				}
			}
			return true;
		}

		// Unify a negated predicate with tuples (tokens)
		// not p(?x) is true only if no tuple (token) can be unified 
		private boolean unifyForNegation(Predicate predicate, ArrayList<Tuple> tuples){
			if (tuples==null || tuples.size()==0) {
				return true;
			}
			ArrayList<String> labels = predicate.getArguments();
			while (pointers[inputIndex]<tuples.size()) {
				Tuple tuple = tuples.get(pointers[inputIndex]);
				assert labels.size()==tuple.arity(): "Unifer.java: cannot unify "+ predicate +" with tuple "+tuple;
				if (unifyLabels(labels, tuple))
					return false;
			}
//System.out.println(predicate +" satisfied ");			
			return true;
		}

		// undo the bindings for the variables that are bound at inputIndex
		private void undoBindings(){
			while (bindingHistory.size()>0 && bindingHistory.peek().isIndex(inputIndex)) {
				BindingRecord bindingRecord = bindingHistory.pop();
				bindings.remove(bindingRecord.getVariable());
			}
		}

		// evaluate the truth value of the when condition according to the bindings
		private boolean isWhenConditionTrue(){
			if (transition.getWhenCondition()==null || transition.getWhenCondition().size()==0)
				return true;
			for (Predicate predicate: transition.getWhenCondition()) {
				if (Functions.isAssertFunction(predicate)){
					if (!Functions.assertTrue(predicate, marking))
						return false;
				} else
				if (Functions.isTokenCountFunction(predicate)){
					if (!Functions.tokenCount(predicate, marking, bindings, bindingHistory, inputIndex))
						return false;					
				} else
				if (!Functions.isTrue(predicate, bindings, bindingHistory, inputIndex))
					return false;
			}	
			return true;
		}
		
		private Hashtable <String, String> copyBindings(){
			Hashtable <String, String> bindingsCopy = new Hashtable <String, String>();
			Enumeration<String> en = bindings.keys();
			while (en.hasMoreElements()) {
				String variable = (String)en.nextElement();
				String boundValue = bindings.get(variable);
				if (boundValue!=null)
					bindingsCopy.put(variable, boundValue);
			}
			return bindingsCopy;
		}
}

class BindingRecord {
	// "variable" is bound when the "index" input place is unified
	private String variable;
	private int index;

	public BindingRecord(String variable, int index){
		this.variable = variable;
		this.index = index;
	}

	public String getVariable(){
		return variable;
	}
	
	public boolean isIndex(int index){
		return this.index == index;
	}
}