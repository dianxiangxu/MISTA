package planninggraph;
import java.util.*;

import kernel.CancellationException;
import kernel.ProgressDialog;

import locales.LocaleBundle;

/**
 * Marking: a marking contains facts of all predicates
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 */
public class NetMarking {

    private Vector facts;			// a set of facts

    /**
     * default constructor
     */
    public NetMarking() {
        facts = new Vector();
    }

    /**
     * construct a marking in terms of an array of facts
     */
    public NetMarking(Fact[] tokens){
        facts = new Vector();
        for (int i=0; i<tokens.length; i++) {
            facts.addElement(tokens[i]);
        }
    }

    /**
     * construct a marking in terms of a vector of facts.
     */
    public NetMarking(Vector facts){
        this.facts = facts;
    }

    /**
     * add a fact
     */
    public void addFact(Fact fact){
            facts.addElement(fact);
    }

    /**
     * get the facts.
     */
    public Vector getFacts(){
        return facts;
    }

    /**
     * get a fact at specific position
     */
    public Fact factAt(int index){
        return (Fact)facts.elementAt(index);
    }

    /**
     * get the total number of facts.
     */
    public int size(){
        return facts.size();
    }

    /**
     * reason about mutual exclusion relationships among facts
     */
    public void inferMutexFacts(ProgressDialog progressDialog) throws CancellationException{
        for (int index1=0; index1<facts.size(); index1++){
    		if (progressDialog!=null && progressDialog.isCancelled())
    			throw new CancellationException(LocaleBundle.bundleString("Verification cancelled"));
           Fact p = (Fact)facts.elementAt(index1);
            for (int index2=index1+1; index2<facts.size(); index2++)
                p.inferMutualExclusion((Fact)facts.elementAt(index2));
        }
    }

    /**
     * unselect (reset the pointers of) firings for all facts in a marking
     */
    public void unSelectFirings(){
        for (int factIndex=0; factIndex<facts.size(); factIndex++)
            ((Fact)facts.elementAt(factIndex)).resetPointer();
    }

    /**
     * Return true (consistent goals) if there does not exist any pair of goals facts that are mutually exclusive
     */
    public boolean checkGoalConsistence(){
        for (int i=0; i<facts.size(); i++){
            Fact p = (Fact)facts.elementAt(i);
            for (int j=i+1; j<facts.size(); j++)
                if (p.isMutuallyExclusiveOf((Fact)facts.elementAt(j))){
//printGoalPointers("Goal facts are found exclusive:");
//System.out.println("\n---------------------------");
//System.out.println("Exclusive facts are found: ");
//System.out.println(p);
//System.out.println((Fact)facts.elementAt(j));
                    return false;
                }

        }
        return true;
    }

    /**
     * contains a fact
     */
    public boolean containsFact(Fact fact){
        return facts.contains(fact);
//      the problem with vector.contains() once occurred
    /*
        for (int i=0; i<facts.size(); i++)
            if ((Fact)facts.elementAt(i)==fact)
                return true;
        return false;
    */
    }

    /**
     * true if the facts in a goal are included in the current marking.
     * Note: facts must be sorted in terms of place indices and time steps
     */
   public boolean includes(NetMarking goal, int[] start, int[] total, int[] factIndices){
        for (int i=0; i<goal.size(); i++) {
            if (factIndices[i]==-1){
                int goalPlace = goal.factAt(i).getPlace();
                if (start[goalPlace]<0 || total[goalPlace]<1)
                    return false;
                Token goalToken = goal.factAt(i).getToken();
                boolean found = false;
                for (int index=0; index<total[goalPlace]; index++)
                    if (goalToken.equals(factAt(index+start[goalPlace]).getToken())) {
                        factIndices[i] = index;
                        found=true;
                        break;
                    }
                if (!found)
                    return false;
            }
        }
        return true;
    }

    /**
     * true if the facts in a goal are satisfied in the current marking.
     * for plan execution, facts in new markings are not properly sorted
     */
   public boolean satisfied(NetMarking goal){
//    System.out.println(goal);
//    System.out.println(facts);
        for (int i=0; i<goal.size(); i++) {
            boolean found = false;
            int index=0;
            Token token = goal.factAt(i).getToken();
            int place = goal.factAt(i).getPlace();
            while (!found && index<facts.size()){
                if (place==factAt(index).getPlace() && token.equals(factAt(index).getToken()))
                    found=true;
                else
                    index++;
            }
            if (!found)
                return false;
        }
        return true;
    }


    /**
     * search a fact
     */
    public Fact searchFact(int pred, String[] args){
        int i=0;
        while (i<facts.size()){
            Fact fact = (Fact)facts.elementAt(i);
            if (fact.getPlace()==pred && fact.getToken().equalsTo(args)){
                return fact;
            }
            i++;
        }
        return null;
    }

    /**
     * unify a fact/token (for plan execution)
     */
    public boolean unifyFact(int place, String[] args) {
        int i;
        for (i=0; i<facts.size(); i++){
            if (factAt(i).getPlace()== place && factAt(i).getToken().equalsTo(args))
                break;
        }
        if (i<facts.size()){
            return true;
        }
        return false;
    }

    /**
     * remove unified fact (for plan execution)
     */
    public boolean removeUnifiedFact(int place, String[] args) {
        int i;
        for (i=0; i<facts.size(); i++){
            if (factAt(i).getPlace()== place && factAt(i).getToken().equalsTo(args))
                break;
        }
        if (i<facts.size()){
            facts.removeElementAt(i);
            return true;
        }
        return false;
    }

    /**
     * carry forward a marking to next level, connected by noops
     */
    public NetMarking carryForward(FiringSet currentFiringLevel){
        Vector newFacts = new Vector();
        Enumeration en = facts.elements();
        while (en.hasMoreElements()) {
            Fact source = (Fact)en.nextElement();
            Fact dest = source.copy();
            TransitionFiring noop = new TransitionFiring(source, dest);
            dest.addFiring(noop);
            newFacts.addElement(dest);
            currentFiringLevel.addFiring(noop);
        }
        return new NetMarking(newFacts);
    }

    /**
     * copy a marking without considering noops
     */
    public NetMarking copy(){
        Vector newFacts = new Vector();
        Enumeration en = facts.elements();
        while (en.hasMoreElements()) {
            newFacts.addElement(((Fact)en.nextElement()).copy());
        }
        return new NetMarking(newFacts);
    }

    /**
     * merge sorted markings
     */
    public void mergeMarkings(NetMarking marking){
        int index, place;
        boolean inserted;
        Fact f;
        Enumeration en = marking.facts.elements();
        while (en.hasMoreElements()) {
            f = (Fact)en.nextElement();
            if (facts.size()==0){
                facts.addElement(f);
                continue;
            }
            inserted =false;
            index = 0;
            while (!inserted && index<facts.size()){
                if (f.getPlace()<((Fact)facts.elementAt(index)).getPlace()){
                    facts.insertElementAt(f, index);
                    inserted =true;
                }
                else
                    index++;
            }
            if (!inserted)
                facts.addElement(f);
        }
    }

    /**
     * count facts
     */
    public void countFacts(int[] start, int[] total){
        int i;
        for (i=0; i<start.length; i++){
            start[i] = -1;
            total[i] = 0;
        }
        for (i=0; i<facts.size(); i++){
            int place = ((Fact)facts.elementAt(i)).getPlace();
            if (start[place] == -1)
                start[place] = i;
            total[place] = total[place]+1;
        }
    }

    /**
     * hashKey (for goal markings)
     */
    public String hashKey(){
        StringBuffer str = new StringBuffer();
        Enumeration en = facts.elements();
        while (en.hasMoreElements()) {
            str.append(((Fact)en.nextElement()).getToken().hashKey());
        }
        return str.toString();
    }

    /**
     * convert a marking of facts as a string.
     */
    public String toString(){
        if (facts.size()==0)
//            return " ()";
            return "";
        StringBuffer str = new StringBuffer();
        Enumeration en = facts.elements();
        while (en.hasMoreElements()) {
            str.append(((Fact)en.nextElement()).toString());
        }
//                str.append("["+index+"]");
        return str.toString();
    }


}
