package planninggraph;

public class VarValue {
	/**
	 * Variable-value pair for substitution
	 */

	    private String name;		// starts with '?'
	    private String value;

	    public VarValue (String name){
	        this.name = name;
	    }

	    public VarValue (String name, String value){
	        this.name = name;
	        this.value = value;
	    }

	    public String getName(){
	    	return name;
	    }
	    
	    public String getValue(){
	    	return value;
	    }
	    
	    public String toString(){
	        return name+"/"+value;
	    }
}
