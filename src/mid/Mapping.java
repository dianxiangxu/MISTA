/* 	
	
	All Rights Reserved
*/
package mid;

import java.io.Serializable;

// Map a model-level element(transitions and predicates) to 
// implementation-level methods and accessors/mutators
public class Mapping implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Predicate predicate;	// transition or place, with parameters
	private String operator;		// method, accessor, mutator for the predicate

	public Mapping(Predicate predicate, String operator) {
		this.predicate = predicate;
		this.operator = operator;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String toString(){
		return "("+predicate+", "+operator+")";
	}
}
