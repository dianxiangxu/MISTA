package mid;

import java.io.Serializable;
import java.util.ArrayList;


public class FiringSequence implements Serializable {
	private static final long serialVersionUID = 1L;

	private ArrayList<Firing> sequence;
	
	public FiringSequence(ArrayList<Firing> sequence) {
		this.sequence = sequence;
	}
	
	public ArrayList<Firing> getSequence(){
		return sequence;
	}
		
	public String toString(){
		String result = "";
		if (sequence == null)
			return result;
		for (Firing firing: sequence) {
			result += "\t"+(sequence.indexOf(firing)+1)+". "+firing.getFiringString() +"\n";
		}
		return result;
	}
	
	public String toCompactString(){
		String result = "";
		if (sequence == null || sequence.size()==0)
			return result;
		result += sequence.get(0).getFiringString();
		for (int i=1; i<sequence.size(); i++) 
			result += ", "+sequence.get(i).getFiringString();
		return result;
	}

}
