package mid;

public class ABACAttribute {
	private String name;
	private String type;
	private String[] values;
	
	public ABACAttribute(String name, String type, String[] values){
		this.name = name;
		this.type = type;
		this.values = values;
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}
	
	public String[] getValues(){
		return values;
	}
	
}
