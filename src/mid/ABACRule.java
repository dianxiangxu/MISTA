package mid;

import java.util.ArrayList;

public class ABACRule {
	public static enum EffectType {PERMIT, DENY};

	private EffectType effect;
	private ArrayList<Predicate> subjectCondition;    
	private ArrayList<Predicate> actionCondition;
	private ArrayList<Predicate> resourceCondition;      
	private ArrayList<Predicate> environmentCondition;      
	private ArrayList<Predicate> obligations;

	public ABACRule(String effectString, ArrayList<Predicate> subjectCondition, ArrayList<Predicate> actionCondition, 
			ArrayList<Predicate> resourceCondition, ArrayList<Predicate> environmentCondition, ArrayList<Predicate> obligations){
		effect = effectString.equalsIgnoreCase("permit")? EffectType.PERMIT: EffectType.DENY;
		this.subjectCondition = subjectCondition;
		this.actionCondition = actionCondition;
		this.resourceCondition = resourceCondition;
		this.environmentCondition = environmentCondition;
		this.obligations = obligations;
	}
	
	public static boolean isLegalEffect(String effectString){
		return effectString.equalsIgnoreCase("permit") || effectString.equalsIgnoreCase("deny");
	}
	
	public EffectType getEffect(){
		return effect;
	}

	public ArrayList<Predicate> getSubjectCondition(){
		return subjectCondition;
	}

	public ArrayList<Predicate> getActionCondition(){
		return actionCondition;
	}

	public ArrayList<Predicate> getResourceCondition(){
		return resourceCondition;
	}

	public ArrayList<Predicate> getEnvironmentCondition(){
		return environmentCondition;
	}

	public ArrayList<Predicate> getObligtions(){
		return obligations;
	}
}
