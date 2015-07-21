package mid;

import java.util.ArrayList;
import java.util.Hashtable;

public abstract class CombinatorialUnifier implements GeneralUnifier {
	
	private Transition transition;
	private Marking marking;
	
	public CombinatorialUnifier(Transition transition, Marking marking) {
		this.transition = transition;
		this.marking = marking;
	}
	
	public ArrayList<Substitution> getSubstitutions(){
		ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
		ArrayList<Predicate> preconditions = transition.getPrecondition();
		int[] lengths = new int[preconditions.size()];
		for (int i=0; i<preconditions.size(); i++){
			String place = preconditions.get(i).getName();
			ArrayList<Tuple> tokens = marking.getTuples(place);
			if (tokens==null)
				return substitutions;
			lengths[i] = tokens.size();
		}
		ArrayList<int[]> twayTests = computeCombinatorialTests(lengths);
		for (int[] twayTest: twayTests)
			substitutions.add(getSubstitution(twayTest, preconditions));
		return substitutions;
	}

	public abstract ArrayList<int[]> computeCombinatorialTests(int[] choices);
	
	private Substitution getSubstitution(int[] twayTest, ArrayList<Predicate> preconditions){
		Hashtable <String, String> bindings = new Hashtable <String, String>();
		for (int i=0; i<preconditions.size(); i++) {
			Predicate predicate = preconditions.get(i);
			ArrayList<Tuple> tuples = marking.getTuples(predicate.getName());
			Tuple tuple = tuples.get(twayTest[i]);
			unifyPredicate(predicate, tuple, bindings);
		}
		return new Substitution(bindings);
	}
		
	private boolean unifyPredicate(Predicate predicate, Tuple tuple, Hashtable <String, String> bindings){
		ArrayList<String> arguments = predicate.getArguments();
		for (int i=0; i<arguments.size(); i++) {
			String tupleElement = tuple.getArguments().get(i);
			String arugment = arguments.get(i);
			if (!MID.isVariable(arugment) || bindings.get(arugment)!=null)
				return false;
			bindings.put(arugment, tupleElement);
		}
		return true;
	}

}
