package cpn;

public class CPNArc {
	public static final boolean PLACETOTRANSITION = true;
	public static final boolean TRANSITIONTOPLACE = false;	
	
	private CPNPlace place;
	private CPNTransition transition;
	private boolean orientation; 
	private String label;
	
	public CPNArc(CPNPlace place, CPNTransition transition, boolean orientation, String label){
		this.place = place;
		this.transition = transition;
		this.orientation = orientation;
		this.label = label;
	}
	
	public CPNPlace getPlace(){
		return place;
	}
	
	public boolean isCondition(CPNTransition transition, boolean orientation){
		return this.transition == transition && this.orientation == orientation; 
	}
	
	public String getCondition(){
		if (label==null || label.equals("1"))
			return place.getName();
		String parameters = label.trim();
		if (!parameters.startsWith("("))
			parameters = "("+parameters;
		if (!parameters.endsWith(")"))
			parameters = parameters+")";
		return place.getName()+parameters;
	} 
	
	public boolean isDefaultLabel(){   // labeled "1" (traditional black token)
		return label!=null && label.equals("1");	
	}
}