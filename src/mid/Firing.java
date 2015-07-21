package mid;

import java.io.Serializable;
import java.util.ArrayList;


public class Firing implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Transition transition;
	private Substitution substitution;
	
	public Firing(Transition transition, Substitution substitution){
		this.transition = transition;
		this.substitution = substitution;
	}
	
	public Transition getTransition(){
		return transition;
	}
	
	public Substitution getSubstitution(){
		return substitution;
	}
	
	public String toString(){
		return transition.getEvent()+substitution.toString(transition.getAllVariables());
	}
	
	public String getFiringString() {
		ArrayList<String> formalParameters = transition.getArguments();
		if (formalParameters==null)
			formalParameters = transition.getAllVariables();
		if (formalParameters==null || formalParameters.size()==0)
			return transition.getEvent();
		StringBuffer buffer = new StringBuffer(transition.getEvent()+"(");
		buffer.append(substitution.getBinding(formalParameters.get(0)));
		for (int i=1; i<formalParameters.size(); i++){
			String value = substitution.getBinding(formalParameters.get(i));
			buffer.append(", ");
			buffer.append(value); 
		}
		buffer.append(")"); 
		return buffer.toString(); 
	}

}
