package mid;

import kernel.Kernel;

public class AssertionProperty {
	
	// ASSERTION ifClause => thenClause: thenClause must hold whenever ifClause holds; ifClause => is optional.
	// Example1: ASSERTION safety1 tokenCount(p, x), x<2
	// Example2: ASSERTION safety2 p(x) => q(x) 
	
	private String assertionString;
	private String assertionName;
	private Transition ifClause;
	private Transition thenClause;	
	
    public AssertionProperty(String assertionString, String assertionName, Transition ifClause, Transition thenClause) {
    	this.assertionString = assertionString;
		this.assertionName = assertionName;
		this.ifClause = ifClause;
		this.thenClause = thenClause;
    }

    public String getAssertionString(){
    	return assertionString;
    }

    public String getAssertionName(){
    	return assertionName;
    }
    
    public boolean hasIfClause(){
    	return ifClause.getPrecondition()!=null && ifClause.getPrecondition().size()>0 
    			|| ifClause.getWhenCondition()!=null && ifClause.getWhenCondition().size()>0;
    }

    public Transition getIfClause(){
    	return ifClause;
    }

    public Transition getThenClause(){
    	return thenClause;
    }

    public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("ASSERTION " + assertionString);
		if (Kernel.IS_DEBUGGING_MODE){
			str.append("\n");
			if (hasIfClause()){
				str.append(ifClause.printPredicateList(ifClause.getPrecondition()));
				str.append(" "+ifClause.printPredicateList(ifClause.getWhenCondition()));
				str.append(" => ");
			}
			str.append(thenClause.printPredicateList(thenClause.getPrecondition()));
			str.append(" "+thenClause.printPredicateList(thenClause.getWhenCondition()));
		}
		return str.toString();
    }
}

