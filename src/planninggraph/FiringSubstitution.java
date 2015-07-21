package planninggraph;
import java.util.*;
import java.io.*;

/**
 * @Substitution: a list of variable/value pairs. It's simplified version of environment.
 *
 * @version 1.0, 12/26/2001
 * @author Dianxiang Xu
 *
 */
class FiringSubstitution {

    protected VarValue[] variables;

    /**
     * Construct an empty substitution.
     */
    public FiringSubstitution(){
        variables = new VarValue[0];
    }

    /**
     * construct a substitution from an environment
     */
    public FiringSubstitution(Environment env){
		variables = new VarValue[env.size()];
//                System.out.println("Environment: "+env);
                for (int i=0; i<variables.length; i++){
                        Variable source = env.getVariables()[i];
                        if (!source.history.empty()) {
                            UnificationRecord record = (UnificationRecord)source.history.peek();
	                    variables[i] = new VarValue(new String(source.name), new String(record.value));
			}
                        else
                            variables[i] = new VarValue(new String(source.name));
		}
//                System.out.println("Substitution: "+toString());
    }

    /**
     * substitute the variables in arguments with bound values
     */
    public String[] substitute(Vector arguments){
        String[] value = new String[arguments.size()];
        int i=0;
        Enumeration en = arguments.elements();
        while (en.hasMoreElements()) {
            value[i++] = replace((String)en.nextElement());
        }
        return value;
    }

    /**
     * substitute the variables in arguments for bound values.
     */
    public String[] substitute(String[] arguments){
        String value[] = new String[arguments.length];
        for (int i=0; i<arguments.length; i++)
            value[i] = replace(arguments[i]);
        return value;
    }

    private String replace(String v) {
        if (v.startsWith("?")) {
        for (int i=0; i<variables.length; i++)
            if (variables[i].getName().equals(v))
                return variables[i].getValue();
            return v;
        }
        return v;
    }

    public VarValue[] getVariableBindings(){
    	return variables;
    	
    }
    public String toString(){
        if (variables.length<1)
            return "";
        StringBuffer str = new StringBuffer("("+variables[0]);
        for (int i=1; i<variables.length; i++) {
            str.append(","+variables[i]);
        }
        return str.toString()+")";
    }
}



