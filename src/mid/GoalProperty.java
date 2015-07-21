package mid;

@SuppressWarnings("serial")
public class GoalProperty extends Transition {
	
	private String propertyString;
	
    public GoalProperty(String event, String propertyString) {
		super(event);
		this.propertyString = propertyString;
    }

    public String getPropertyString(){
    	return propertyString;
    }
    
}

