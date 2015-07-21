/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package mid;
import java.io.Serializable;
import java.util.ArrayList;

import parser.ParseException;

import kernel.Kernel;
import locales.LocaleBundle;


public class Transition implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String AttackTransition = "Attack";
	
	private String id ="";		// unique ID, for animation
	private String event;
    private ArrayList<String> arguments;
    private ArrayList<Predicate> precondition;    // for unification/enabledness
    private ArrayList<Predicate> postcondition;   // for oracle values
    private ArrayList<Predicate> when;     		  // when condition       
    private ArrayList<Predicate> effect;		  // operations (calls or checks) 
    private String  guard;            				// for Petri nets only

    private ArrayList<String> allVariables;
    private ArrayList<Predicate> deletePrecondition;   // for firing - remove tokens 	
    private ArrayList<Predicate> addPostcondition;  	// for firing - add tokens to places or clear all tokens from places (RESET)
    // Places to be RESET may not be in the precondition
    // Rationale for not using a separate reset list: we can reset a place and then add a token in the same firing
    // for example: reset(p), p(x) in a postcondition will first reset p and then add token x to p
    
    public Transition(String event) {
		this.event = event;
		precondition = new ArrayList<Predicate>();
		postcondition = new ArrayList<Predicate>();
    }

	public String getEvent(){
		return event;
	}

	public String getId(){
		return id;
	}
	
	public void setId(String newId){
		this.id = newId;
	}
	
	public boolean isAttackTransition(){
		return event.toUpperCase().startsWith(AttackTransition.toUpperCase());
	}
	
	public ArrayList<String> getArguments() {
    	return arguments;
    }

    public void setArguments(ArrayList<String> variables) {
    	this.arguments = variables;
    }

    public void collectAllVariables(){
    	allVariables= new ArrayList<String>();
    	if (arguments!=null) {
    		for (String variable: arguments)
    			allVariables.add(variable);
    	}
    	for (Predicate predicate: precondition) {
    		for (String label: predicate.getArguments()) {
    			if (MID.isVariable(label) && !allVariables.contains(label))
    				allVariables.add(label);
    		}
    	}
    	if (when!=null){
        	for (Predicate whenPredicate: when) {
        		if (Functions.isArithmeticFunction(whenPredicate)){
					ArrayList<String> arguments = whenPredicate.getArguments();
					String newVariable = arguments.get(arguments.size()-1);
					if (!allVariables.contains(newVariable))
							allVariables.add(newVariable);
        		}
        	}   		
    	}
    }

    public ArrayList<String> getAllVariables(){
    	return allVariables;
    }

    public void setAllVariables(ArrayList<String> allVariables){
    	this.allVariables = allVariables;
    }

    public int getNumberOfArguments(){
    	if (arguments!=null)
    		return arguments.size();
    	else
    		return allVariables.size();
    }
    
	public ArrayList<Predicate> getPrecondition(){
		return precondition;
	}
	
	public void setPrecondition(ArrayList<Predicate> precondition){
		this.precondition = precondition;
	}
	
	public int numberOfInputs(){
		return precondition.size();
	}

	public ArrayList<Predicate> getDeletePrecondition(){
		return deletePrecondition;
	}
	
	public void setDeletePrecondition(){
		deletePrecondition = new ArrayList<Predicate>();
		addPostcondition = new ArrayList<Predicate>();
		for (Predicate predicate: postcondition)
			if (!predicate.getNegation())
				addPostcondition.add(predicate);
		for (Predicate predicate: precondition) {
			if (!predicate.getNegation()){
				int i=0;
				while (i<addPostcondition.size() && !predicate.equals(addPostcondition.get(i)))
					i++;
				if (addPostcondition.size()==0 || i>=addPostcondition.size())
					deletePrecondition.add(predicate);
				else 
					addPostcondition.remove(i);
			}
		}
	}

	public ArrayList<Predicate> getAddPostcondition(){
		return addPostcondition;
	}

	public ArrayList<Predicate> getPostcondition(){
		return postcondition;
	}
	
	public void setPostcondition(ArrayList<Predicate> postcondition){
		this.postcondition = postcondition;
	}

	public boolean preconditionContainsFunction(){
		return containsFunction(precondition);
	}
	
	public boolean postconditionContainsFunction(){
		return containsFunction(postcondition);
	}

	private boolean containsFunction(ArrayList<Predicate> condition){
		for (Predicate predicate: condition)
			if (Functions.isFunction(predicate))
				return true;
		return false;
	}
	
	public ArrayList<Predicate> getWhenCondition(){
		return when;
	}

	public void setWhenCondition(ArrayList<Predicate> when){
		this.when = when;
	}

	public String getGuard(){
		return guard;
	}

	public void setGuard(String guard){
		this.guard = guard;
	}

	public boolean hasGuard(){
		return guard!=null && !guard.equals("");
	}
	
	public ArrayList<Predicate> getEffect(){
		return effect;
	}

	public void setEffect(ArrayList<Predicate> effect){
		this.effect = effect;
	}

	public boolean isStateChangingTransition(){
		return deletePrecondition.size()>0 || addPostcondition.size()>0;
	}
	
	public String printPredicateList(ArrayList<Predicate> predicates) {
		if (predicates==null || predicates.size()==0)
			return "";
		StringBuffer str = new StringBuffer();
		str.append(predicates.get(0));
        for (int i=1; i<predicates.size(); i++) {
        	str.append(", ");
            str.append(predicates.get(i));
        }
		return str.toString();	
	}

	public String printVariableList() {
		return printVariableList(arguments);
	}

	public String printVariableList(ArrayList<String> list){
		if (list==null)
			return "";
		if (list.size()==0)
			return "()";
		StringBuffer str = new StringBuffer();
		str.append("(");
		str.append(list.get(0));
        for (int i=1; i<list.size(); i++) {
        	str.append(", ");
            str.append(list.get(i));
        }
		str.append(")");
		return str.toString();	
		
	}
	
	public boolean hasVariable(String var){
		if (allVariables!=null) {
			for (String variable: allVariables)
				if (variable.equals(var))
					return true;
		}
		return false;
//		return allVariables.contains(var);
	}
	
	// combinatorial testing is applicable only when:
	// guard (when) is empty, 
	// there are more than two input predicates with no negation, 
	// all input predicates have different variables
	public boolean isCombinatorialTestingApplicable(){
		if (when!=null && when.size()>0)
			return false;
		if (precondition==null || precondition.size()<=2)
			return false;
		ArrayList<String> variables = new ArrayList<String>();
		for (Predicate predicate: precondition) {
			if (predicate.getNegation())
				return false;
			for (String argument: predicate.getArguments()){
				if (!MID.isVariable(argument))
					return false;
				for (String var: variables) {
					if (var.equals(argument))
						return false;
				}
				variables.add(argument);
			}
		}
		return true;
	}
	
	// the pre- and post-conditions as a contract are formulas in first order logic 
	// translate it into transition pre- and postconditions in Petri net
	public void transformFromContract() throws ParseException {
		ArrayList<Predicate> origPrecondition = precondition;
		ArrayList<Predicate> origPostcondition = postcondition;
		precondition = new ArrayList<Predicate>();
		postcondition = new ArrayList<Predicate>();
		for (Predicate pre: origPrecondition){
			if (Functions.isFunction(pre)){
				if (!Functions.hasCorrectArguments(pre))
					throw new ParseException(pre+": "+LocaleBundle.bundleString("incorrect function arguments"));
				if (when==null)
					when = new ArrayList<Predicate>();
				when.add(pre);
			}
			else {
				precondition.add(pre);
				int i=0;
				while (i<origPostcondition.size() && !origPostcondition.get(i).equalsIgnoreNegation(pre)) 
					i++;
				if (i>=origPostcondition.size())
					postcondition.add(pre);
				else if (origPostcondition.get(i).getNegation())
					origPostcondition.remove(i);
			}
		}
		for (Predicate post: origPostcondition)
			postcondition.add(post);
	}

	public ArrayList<String> getFormalParameters() {
		ArrayList<String> formalParameters = arguments;
		if (formalParameters==null) {
			if (allVariables==null) {
				collectAllVariables();
			}
			formalParameters = allVariables;
		}
		return formalParameters!=null? formalParameters: new ArrayList<String>();
	}

	// START V0.3
	public String getTransitionIdentityString(){
		StringBuffer str = new StringBuffer();
		str.append("TRANSITION ");
		str.append(event);
		str.append(printVariableList());
		if (precondition!=null && precondition.size()>0){
			str.append("\nPRECOND ");
			str.append(printPredicateList(precondition));
		}
		if (postcondition!=null && postcondition.size()>0){
			str.append("\nPOSTCOND ");
			str.append(printPredicateList(postcondition));
		}
		if (when!=null && when.size()>0){
			str.append("\nWHEN ");
			str.append(printPredicateList(when));
		}
		return str.toString();
	}	
	// END V0.3
	
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append(LocaleBundle.bundleString("TRANSITION")+" ");
		str.append(event);
		str.append(printVariableList());
		if (hasGuard())
			str.append(" ["+guard+"]");
		if (precondition!=null && precondition.size()>0){
			str.append("\n"+LocaleBundle.bundleString("PRECONDITION")+" ");
			str.append(printPredicateList(precondition));
		}
		if (Kernel.IS_DEBUGGING_MODE) {
			if (deletePrecondition!=null && deletePrecondition.size()>0){
				str.append("\nPRECOND(DELETE) ");
				str.append(printPredicateList(deletePrecondition));
			}
		}
		if (postcondition!=null && postcondition.size()>0){
			str.append("\n"+LocaleBundle.bundleString("POSTCONDITION")+" ");
			str.append(printPredicateList(postcondition));
		}
		if (Kernel.IS_DEBUGGING_MODE) {
			if (addPostcondition!=null && addPostcondition.size()>0){
				str.append("\nPOSTCOND(ADD) ");
				str.append(printPredicateList(addPostcondition));
			}
		}
		if (when!=null && when.size()>0){
			str.append("\n"+LocaleBundle.bundleString("WHEN")+" ");
			str.append(printPredicateList(when));
		}
		if (effect!=null && effect.size()>0){
			for (Predicate e: effect)
				str.append("\n"+LocaleBundle.bundleString("EFFECT")+" "+e);
		}
		
		return str.toString();
	}
}
