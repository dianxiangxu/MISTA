package mid;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.Serializable;

public class Substitution implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Hashtable <String, String> bindings;

	public Substitution() {
		 bindings = new Hashtable <String, String>();
	}

	public Substitution(Hashtable <String, String> bindings) {
		this.bindings = bindings;
	}
	
	public String getBinding(String variable) {
		return bindings.get(variable);
	}

	public Hashtable <String, String> getBindings() {
		return bindings;
	}
	
	public boolean hasBindings() {
		return bindings.size()>0;
	}

	public boolean isBound(String variable){
		return bindings.get(variable)!=null;
	}

	public Tuple substitute(Predicate predicate){
		ArrayList<String> actualArguments = new ArrayList<String>();
		for (String formalArgument: predicate.getArguments()) {
			if (MID.isVariable(formalArgument) && bindings.get(formalArgument)!=null)
				actualArguments.add(bindings.get(formalArgument));
			else
				actualArguments.add(formalArgument);
		}
		return TupleFactory.createTuple(actualArguments);
	}

	// replace a variable in an expression with the binding
	public String substitute(String expression){
		String exp = expression;
		Enumeration<String> keys = bindings.keys();
		while (keys.hasMoreElements()) {
			String variable = (String)keys.nextElement();
			String value = bindings.get(variable);
			exp = exp.replace(variable, value);
		}
		return exp;
	}

	public boolean equals(Substitution other){
		if (bindings.size()!=other.bindings.size())
			return false;
		Enumeration<String> keys = bindings.keys();
		while (keys.hasMoreElements()) {
			String variable = (String)keys.nextElement();
			String value = bindings.get(variable);
			String otherValue = other.bindings.get(variable);
			if (!value.equals(otherValue))
				return false;
		}
		return true;
	}
	
	public String toString(ArrayList<String> variables){
		String str = "";
		if (variables==null || bindings.size()==0)
			return str;
		str= "[";
		for (String variable: variables){
			// bindings.get(variable) can be null
			str += variable +"/" + bindings.get(variable)+", ";
		}
		return str.substring(0, str.length()-2)+"]";
	}

	public String printAllBindings(){
		return toString(getAllVariables());
	}
	
	public ArrayList<String> getAllVariables(){
		ArrayList<String> arguments = new ArrayList<String>();
		Enumeration<String> keys = bindings.keys();
		while (keys.hasMoreElements())
			arguments.add((String)keys.nextElement());
		return arguments;
	} 
	
/*
	public String toString(){
		ArrayList<String> variables = getVariables();
		String str = "";
		if (variables==null || bindings.size()==0)
			return str;
		str= "[";
		for (String variable: variables){
			str += variable +"/" + bindings.get(variable)+", ";
		}
		return str.substring(0, str.length()-2)+"]";
	}
	*/
}