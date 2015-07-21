/* 	
	Author Dianxiang Xu
*/
package mid;
import java.io.Serializable;
import java.util.*;

import mid.Tuple;

// A marking is a distribution of tokens in places. 
// To avoid confusion due to the generated parser, we use the terminology tuple, rather than token. 
 
public class Marking implements Serializable {
	private static final long serialVersionUID = 1L;

    private Hashtable<String, ArrayList<Tuple>> states = new Hashtable<String, ArrayList<Tuple>>();

    public Marking(){
    }

    public Marking(Hashtable<String, ArrayList<Tuple>> states){
    	this.states = states;
    }
    
    public void addTuples(String place, ArrayList<Tuple> tuples){
        states.put(place, sort(tuples));
    }

    private ArrayList<Tuple> sort(ArrayList<Tuple> tuples){
    	ArrayList<Tuple> newTuples = new ArrayList<Tuple>();
    	for (Tuple tuple: tuples) {
    		insertTuple(newTuples, tuple);
    	}
    	return newTuples;
    }
    
/*
	// Sort tuples in terms of their hashcode 
    // Different runs may result in different results
    private void insertTuple(ArrayList<Tuple> tuples, Tuple tuple){
		int index=0;
		while (index<tuples.size() && tuples.get(index).hashCode()>tuple.hashCode())
			index++;
if (index>=tuples.size() || tuples.get(index).hashCode()!=tuple.hashCode())
        tuples.add(index, tuple);   	
else
	System.out.println("\nThe net is not safe. A duplicate token "+tuple+" is produced!");
    }
*/

    // sort tuples in terms of the arguments
    private void insertTuple(ArrayList<Tuple> tuples, Tuple tuple){
		int index=0;
		while (index<tuples.size() && tuples.get(index).hashKey().compareTo(tuple.hashKey())<0)
			index++;
//assure safe nets
if (index>=tuples.size() || tuples.get(index).hashKey().compareTo(tuple.hashKey())!=0)
//	
        tuples.add(index, tuple);   	
else
	System.out.println("\nThe net is not safe. A duplicate token "+tuple+" is produced!");
    }
    
    public void addTuple(String place, Tuple tuple){
    	ArrayList<Tuple> tuples = states.get(place);
    	if (tuples == null) {
    		tuples = new ArrayList<Tuple>();
    		tuples.add(tuple);
    		states.put(place, tuples);
    	}
    	else {
            insertTuple(tuples, tuple);
    	}
    }

    public void removeTuple(String place, Tuple tuple){
		ArrayList<Tuple> tuples = states.get(place);
		if (tuples!=null){
			tuples.remove(tuple);
			if (tuples.size()==0)
				states.remove(place);
		}
    }
    
    // remove all tokens from the given place (for RESET) 
    public void resetPlace(String place) {
    	states.remove(place);
    }
    
	public ArrayList<Tuple> getTuples(String place){
    	return states.get(place);
    }

    public int numberOfTuples(String place){
    	ArrayList<Tuple> tuples = states.get(place);
    	if (tuples == null)
    		return -1;
    	else
    		return tuples.size();
    }
    
    public boolean hasTuples(String place){
    	return numberOfTuples(place)>0;
    }

    public ArrayList<String> getPlaces(){
		ArrayList<String> places = new ArrayList<String>();
		Enumeration<String> keys = states.keys();
		while (keys.hasMoreElements()) {
			places.add(keys.nextElement());
		}
    	return places;
    }
    
    public void merge(Marking otherMarking){
    	ArrayList<String> placesInOtherMarking = otherMarking.getPlaces();
    	for (String place: placesInOtherMarking){
    		ArrayList<Tuple> tuplesInOtherMarking = otherMarking.getTuples(place);
    		if (tuplesInOtherMarking!=null)
    			for (Tuple tuple: tuplesInOtherMarking)
    				addTuple(place, tuple);
    	}
    }

  
    public boolean isAssertionTrue(AssertionProperty assertion){
    	ArrayList<Substitution> substitionsForIfClause;
    	if (assertion.hasIfClause()){
    		substitionsForIfClause = new Unifier(assertion.getIfClause(), this).getSubstitutions();
//System.out.println(this+"\n"+assertion+" - " + substitionsForIfClause.size());	   	
    		if (substitionsForIfClause.size()==0)
    			return true;
    	} else {
    		substitionsForIfClause = new ArrayList<Substitution>();
    		substitionsForIfClause.add(new Substitution());
    	}
    	for (Substitution substitutionFromIfClause: substitionsForIfClause) {
    		Unifier unifier = new Unifier(assertion.getThenClause(), this, substitutionFromIfClause);
//System.out.println(this+"\n"+assertion+" - " + unifier.isFirable());	   	
    		if (!unifier.isFirable())
    			return false;
    	} 
 		return true;
     }
    
/*    
   public boolean isFirable(Transition transition){
	   ArrayList<Substitution> substitutions = new Unifier(transition, this).getSubstitutions();
System.out.println((substitutions.size()>0) +" - " +new Unifier(transition, this).isFirable());	   
		return new Unifier(transition, this).getSubstitutions().size()>0;
    }
*/
    public boolean isFirable(Transition transition){
		return new Unifier(transition, this).isFirable();
    }
    
	public boolean includes(Marking goal){
		if (states.size()<goal.states.size())
			return false;
		Enumeration<String> keys = goal.states.keys();
		while (keys.hasMoreElements()) {
			String place = (String)keys.nextElement();
			ArrayList<Tuple> tuples = states.get(place);
			ArrayList<Tuple> goalTuples = goal.states.get(place);
			if (tuples==null || (goalTuples !=null && ! includes(tuples, goalTuples)))
				return false;
		}
		return true;
	}
	
	private boolean includes(ArrayList<Tuple> tuples, ArrayList<Tuple> goalTuples){
		for (Tuple goalTuple: goalTuples){
			if (!tuples.contains(goalTuple))
				return false;
		}
		return true;
	}

	public boolean equals(Marking goal){
		if (states.size()!=goal.states.size())
			return false;
		Enumeration<String> keys = states.keys();
		while (keys.hasMoreElements()) {
			String place = (String)keys.nextElement();
			ArrayList<Tuple> tuples = states.get(place);
			ArrayList<Tuple> goalTuples = goal.states.get(place);
			if (goalTuples ==null || tuples.size()!=goalTuples.size() || !haveSameTuples(tuples, goalTuples))
				return false;
		}
		return true;
	}
	
	private boolean haveSameTuples(ArrayList<Tuple> tuples, ArrayList<Tuple> goalTuples){
		for (int i=0; i<goalTuples.size(); i++){
			if (goalTuples.get(i)!=tuples.get(i))
				return false;
		}
		return true;
	}
	
	// two markings have the same key string if and only if they are equal
	public String getKeyString(ArrayList<String> places){
		StringBuffer buffer = new StringBuffer();
		for (int index=0;index<places.size(); index++){
			buffer.append(index);
			ArrayList<Tuple> tuples = states.get(places.get(index));
			if (tuples!=null)
				for (Tuple tuple: tuples)
					if (tuple.getArguments().size()==0)
						buffer.append("()");
					else
						buffer.append(tuple);
		}	
		return buffer.toString();
	}

	public String toString(){
		String str = ""; 
		Enumeration<String> keys = states.keys();
		while (keys.hasMoreElements()) {
			String place = (String)keys.nextElement();
			str += toString(place) +", ";
		}
		if (str.length()>0) {
			return str.substring(0, str.length()-2);
		}
		else
			return str;
	}

	public String toString(String place){
		StringBuffer str = new StringBuffer();
		ArrayList<Tuple> tuples = getTuples(place);
		if (tuples!=null) {
			for (Tuple tuple: tuples){
				str.append(place);
				str.append(tuple);
				str.append(", ");
			}	
		}
		if (str.length()>0) {
			return str.substring(0, str.length()-2);
		}
		else
			return "";
	}

	public String getStringForSimulation(){
		String str = ""; 
		Enumeration<String> keys = states.keys();
		while (keys.hasMoreElements()) {
			String place = (String)keys.nextElement();
			str += getStringForSimulation(place) +"\n";
		}
		return str;
	}

	private String getStringForSimulation(String place){
		StringBuffer str = new StringBuffer(place+": ");
		ArrayList<Tuple> tuples = getTuples(place);
		if (tuples!=null) {
			for (Tuple tuple: tuples){
				str.append(getTupleStringForSimulation(tuple));
				str.append(", ");
			}	
		}
		return str.length()>0? str.substring(0, str.length()-2): "";
	}
	
	private String getTupleStringForSimulation(Tuple tuple){
		ArrayList<String> arguments = tuple.getArguments();
	    if (arguments.size()==0)
	    	return "()";
//	    else 
//	    if (arguments.size()==1)
//	    	return arguments.get(0);
	    else {
	    	StringBuffer str = new StringBuffer("(");
	    	str.append(arguments.get(0));
	    	for (int i=1; i<arguments.size(); i++) {
	    		str.append(", ");
	    		str.append(arguments.get(i));
	    	}
	    	return str.toString()+")";
	    }
	}

	public Marking clone(){
		Hashtable<String, ArrayList<Tuple>> statesCopy = new Hashtable<String, ArrayList<Tuple>>();
		Enumeration<String> en = states.keys();
		while (en.hasMoreElements()) {
			String place = (String)en.nextElement();
			ArrayList<Tuple> tuples = states.get(place);
			if (tuples!=null) {
				ArrayList<Tuple> tuplesCopy = new ArrayList<Tuple>();
				for (Tuple tuple: tuples)
					tuplesCopy.add(tuple);
				statesCopy.put(place, tuplesCopy);
			}
		}
		return new Marking(statesCopy);
	} 
}
