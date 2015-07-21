package mid;

import java.io.Serializable;
import java.util.ArrayList;



public class UserDefinedSequences implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private ArrayList<FiringSequence> sequences =null;
	
	public UserDefinedSequences(){
		sequences = new ArrayList<FiringSequence>();
	}
	
	public void addSequence(FiringSequence sequence){
		sequences.add(sequence);
	}
	
	public ArrayList<FiringSequence> getSequences(){
		return sequences;
	}
	
	public String toString(){
		String result = "\nFirings: ";
		for (FiringSequence sequence: sequences)
			result +="\n"+sequence.toString();
		return result;
	}

	public boolean hasSequences(){
		return sequences.size()>0;
	}
	
	public static ArrayList<UserDefinedSequences> initializeALLSequences(MID mid){
		ArrayList<UserDefinedSequences> allSequences = new ArrayList<UserDefinedSequences>();
		for (int i=0; i< mid.getInitialMarkings().size(); i++)
			allSequences.add(new UserDefinedSequences());
		return allSequences;
	}

}
