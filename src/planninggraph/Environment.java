package planninggraph;
import java.util.*;

/**
 * @Environment for unification(bindings of a list of variables) between tokens and arguments of predicates
 *
 * @version 1.0, 12/26/2001
 * @author Dianxiang Xu
 *
 */
class Environment {

	private Variable[] variables;

        /**
	* Construct an empty environment.
	*/
	public Environment()
	{
		variables = new Variable[0];
	}

	/**
	* Construct an environment in terms of a list(an array) of variable names.
	*/
	public Environment(String vars[])
	{
		variables = new Variable[vars.length];
		for (int i=0; i<vars.length; i++) {
			variables[i] = new Variable(vars[i]);
		}
	}

	/**
	* Construct an environment in terms of a list(vector) of variable names.
	*/
	public Environment(Vector vars)
	{
		variables = new Variable[vars.size()];
                int i=0;
		Enumeration en = vars.elements();
		while (en.hasMoreElements()) {
			variables[i++] = new Variable((String)en.nextElement());
		}
	}

	/**
	* unify variables in an arc arguments with values in a token.
	*/
	public boolean unify(int preIndex, NetPredicate p, Token token)
	{
                if (p.arity()!=token.arity())
                    return false;
		int i=0;
		boolean loop, unified = true;
		Variable tmpvar;
		String [] value = token.getArguments();
		String [] vars = p.getArguments();

		while (i <value.length && unified) {

// System.out.println("\nvalue:"+value[i]);
// System.out.println("var:"+vars[i]);

			if (vars[i].startsWith("?")) {
				loop=true;
                int j=0;
				while (j<variables.length && loop) {
					tmpvar = variables[j++];
					if (tmpvar.name.equals(vars[i])) {
					    if (tmpvar.bound) {
					    	unified = (value[i].equals(tmpvar.getValue()));
                        }
                        else  { tmpvar.setValue(preIndex, value[i]); }
					    loop = false;
					}
				}
			}
//			else { unified= (vars[i].equalsIgnoreCase(value[i]));}
			else { unified= (vars[i].equals(value[i]));}
			i++;
		}
		if (!unified) { 
			undoUnification(preIndex); 
		}
// System.out.println("\nUnified: "+unified);

		return unified;
	}

	/**
	* substitute the variables in arguments for bound values.
	*/
	String[] substitute(String[] arguments)
	{
		String value[] = new String[arguments.length];
		for (int i=0; i<arguments.length; i++)
                    value[i] = replace(arguments[i]);
/*
System.out.print("output token: ");
for (int j=0; j<value.length; j++) {
 	System.out.print(value[j]+", ");
}
System.out.print(" ");
*/
		return value;
	}

    /**
	* substitute the variables in arguments for bound values.
	*/
	String[] substitute(Vector arguments)
	{
		String[] value = new String[arguments.size()];
                int i=0;
		Enumeration en = arguments.elements();
                while (en.hasMoreElements()) {
                        value[i] = replace((String)en.nextElement());
                        i++;
		}
		return value;
	}

    private String replace(String v) {
        if (v.startsWith("?")) {
                for (int j=0; j<variables.length; j++)
                    	if (variables[j].name.equals(v))
                            return variables[j].getValue();
                    return v;
                }
        else
        	return v;
    }

	/**
	* undo variable assignments of last unification.
	*/
	void undoUnification(int preIndex)
	{
		for (int i=0; i<variables.length; i++)
			variables[i].unsetValue(preIndex);
	}

    public int size() {
    	return variables.length;
    }

    public Variable[] getVariables() {
    	return variables;
    }

    public String toString(){
            StringBuffer str = new StringBuffer();
            for (int i=0; i<variables.length; i++) {
                str.append(" "+variables[i].name+"/"+variables[i].getValue());
            }
            return str.toString();
    }
}

/**
 * Variable and its binding history for unification
 */
class Variable {

	String name;		// starts with '?'
	boolean bound; 		// whether the variable has already had a value
	Stack history;		// all bindings during one unification

	public Variable (String name)
	{
		this.name = name;
		bound = false;
		history = new Stack();
	}

	public void setValue (int preIndex, String value)
	{
		UnificationRecord record;
		record = new UnificationRecord(preIndex, value);
		history.push(record);
		bound = true;
	}

	public void unsetValue (int preIndex)
	{
		if ( history.empty() || ((UnificationRecord)history.peek()).index != preIndex) {
			return;
		}
		history.pop();
		if (history.empty()) {
			bound = false;
		}
	}

	public String getValue ()
	{
		if (history.empty()) {
			return "";
		}
		return (((UnificationRecord)history.peek()).value);
	}
}


class UnificationRecord {

	int index;      // the index number of the predicate in precondition
	String value;

	public UnificationRecord(int preIndex, String value)
	{
		this.index = preIndex;
		this.value = value;
	}
}

