package mid;

import java.io.Serializable;
import java.util.*;

//Tuple is the terminology used for token 
// because token is already used by the generated parser. 

public class Tuple implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String DUMMY_VARIABLE="_";
	
    private ArrayList<String> arguments;

    public Tuple(ArrayList<String> arguments){
    	this.arguments = arguments;
    }

	public ArrayList<String> getArguments(){
        return arguments;
    }

    public int arity(){
        return arguments.size();
    }

    public boolean equals(Tuple other) {
        if (arguments.size()!=other.arguments.size())
            return false;
        for (int i=0; i<arguments.size(); i++)
            if (!arguments.get(i).equals(other.arguments.get(i)))
                return false;
        return true;
    }
    
    public String hashKey(){
//        return String.valueOf(super.hashCode());
      return toString();
    }

    public String toString(){
    	if (arguments.size()==0)
    		return "";
        StringBuffer str = new StringBuffer("(");
        str.append(arguments.get(0));
        for (int i=1; i<arguments.size(); i++) {
            str.append(", ");
            str.append(arguments.get(i));
        }
        return str.toString()+")";
    }

}

