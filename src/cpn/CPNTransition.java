package cpn;

public class CPNTransition {

	private String id;
	private String event;
	private String precondition;
	private String postcondition;
	private String inscription;
	
	public CPNTransition(String ID, String event, String inscription){
		this.id = ID;
		this.event = event;
		this.inscription = inscription;
	}
	
	public String getID(){
		return id;
	}
	
	public String getEvent(){
		return event;
	}

	public void setEvent(String event){
		this.event = event;
	}

	public String getPrecondition(){
		return precondition!=null? precondition: "";
	}

	public void setPrecondition(String precondition){
		this.precondition = precondition;
	}

	public String getPostcondition(){
		return postcondition!=null? postcondition: "";
	}

	public void setPostcondition(String postcondition){
		this.postcondition = postcondition;
	}

	public String getInscription(){
		return inscription!=null? inscription: "";
	}
	
	public void setInscription(String inscription){
		this.inscription = inscription;
	}

	public String toString(){
		return id+" "+event+" "+getInscription()+"\nPrecond: "+getPrecondition()+"\nPostcond: "+getPostcondition();
	}
}
