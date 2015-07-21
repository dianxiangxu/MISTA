package mid;
import java.io.Serializable;
import java.util.*;

public class Predicate implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean negation;  
	private String name;			
	private ArrayList<String> arguments;		

	public Predicate(String name, ArrayList<String> arguments){
		this(name, arguments, false);
	}

	public Predicate(String name, ArrayList<String> arguments, boolean negation){
		this.name = name;
        this.arguments = arguments;
        this.negation = negation;
	}

	public String getName() {
         return name;
	}

	public ArrayList<String> getArguments() {
         return arguments;
	}
	
	public boolean hasVariables(){
		for (String argument: arguments) {
			if (MID.isVariable(argument))
				return true;
		}
		return false;
	}
	
	public Substitution unify(Tuple tuple){
		if (arguments.size()!=tuple.arity())
			return null;
		Hashtable <String, String> bindings = new Hashtable <String, String>();
		ArrayList<String> tupleArguments = tuple.getArguments();
		for (int i=0; i<arguments.size(); i++){
			String argument = arguments.get(i);
			String tupleArgument = tupleArguments.get(i);
			if (MID.isVariable(argument))
				bindings.put(argument, tupleArgument);
			else if (!argument.equals(tupleArgument))
				return null;
		}
		return new Substitution(bindings);
	}
	
	public boolean getNegation(){
		return negation;
	}
	
	public int arity() {
		return arguments.size();
    }

	public boolean equals(Predicate other) {
		if (negation!=other.negation || !name.equals(other.name) || arguments.size()!=other.arguments.size())
			return false;
		for (int i=0; i<arguments.size(); i++)
			if (!arguments.get(i).equals(other.arguments.get(i)))
				return false;
		return true;
	}

	public boolean equalsIgnoreNegation(Predicate other) {
		if (!name.equals(other.name) || arguments.size()!=other.arguments.size())
			return false;
		for (int i=0; i<arguments.size(); i++)
			if (!arguments.get(i).equals(other.arguments.get(i)))
				return false;
		return true;
	}

	public String toString(){
		if (Functions.isArithmeticFunction(this) || Functions.isComparisonFunction(this))
			return Functions.printArithmeticOperation(this);
		StringBuffer str = new StringBuffer();
		if (negation)
			str.append("not ");
		str.append(name);
		
		if (arguments==null || arguments.size()==0)
			return str.toString();
		
        str.append("(");
        str.append(arguments.get(0));
        for (int i=1; i<arguments.size(); i++) {
        	str.append(",");
            str.append(arguments.get(i));
        }
        str.append(")");
		return str.toString();
	}
}
