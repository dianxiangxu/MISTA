package planninggraph;
import java.util.*;
import java.io.*;

/**
 *
 * @version 1.0, 12/26/2001
 * @author Dianxiang Xu
 */
public class Fact{

    private Token  token;         // basic fact (predicate+arguments)
    private Vector firings;       // a list of firings creating the fact
    private Vector mutexFacts;    // facts that the fact is mutually exclusive
    private int pointer=0;        // pointer of firing selection

    /**
    * construct a fact
    */
    public Fact(int place, String[] arguments){
        token = new Token(place, arguments);
    }

    /**
    * construct a fact
    */
    public Fact(int place, Vector arguments){
        token = new Token(place, arguments);
    }

    /**
    * construct a fact which is carried forward by NOOP
    */
    public Fact(Token token){
        this.token = token;
    }

    /**
     * get the token
     */
    public Token getToken(){
        return token;
    }

    /**
     * get the index of place/predicate
     */
    public int getPlace(){
        return token.getPlace();
    }

    /**
    * add a firing that creates the fact
    */
    public void addFiring(TransitionFiring firing) {
        if (firings==null)
            firings = new Vector();
        firings.addElement(firing);
    }

    /**
     * get the firings that create the fact
     */
    public Vector getFirings(){
        return firings;
    }

    /**
     * get the number of firings that may create the fact
     */
    public int numOfFirings(){
        return firings.size();
    }

    /**
     * get the facts that the fact is mutually exclusive
     */
    public Vector getMutexFacts(){
        return mutexFacts;
    }

    /**
    * add a mutually exclusive fact
    */
    public void addMutexFact(Fact fact) {
        if (mutexFacts==null)
            mutexFacts = new Vector();
        if (!mutexFacts.contains(fact))
            mutexFacts.addElement(fact);
    }

    /**
     * get the pointer of firing selection
     */
    public int getPointer(){
        return pointer;
    }

    /**
     * set the pointer of firing selection
     */
    public void setPointer(int pointer){
        this.pointer = pointer;
    }

    /**
     * reset the pointer of firing selection to 0
     */
    public void resetPointer(){
        pointer = 0;
    }

    /**
     * has more firings that may create the fact
     */
    public boolean hasMoreFirings(){
        return (firings!=null && pointer<firings.size());
    }

    /**
     * get next firing that may create the fact
     */
    public TransitionFiring nextFiring(){
        return ((TransitionFiring)firings.elementAt(pointer++));
    }

    /**
     * get the currently selected firing
     */
    public TransitionFiring getSelectedFiring(){
        if (pointer>0)
            return (TransitionFiring)firings.elementAt(pointer-1);
        else
            return null;
    }

    /**
     * reason about mutual exclusion between this fact and another fact
     */
    public void inferMutualExclusion(Fact fact){
        for (int i=0; i<firings.size(); i++) {
            TransitionFiring firing = (TransitionFiring)firings.elementAt(i);
            for(int j=0; j<fact.firings.size(); j++)
                if (!firing.isMutuallyExclusiveOf((TransitionFiring)fact.firings.elementAt(j)))
                    return;
        }
        addMutexFact(fact);
        fact.addMutexFact(this);
    }

    /**
     * true if this fact is mutually exclusive with another fact
     */
    public boolean isMutuallyExclusiveOf(Fact fact){
        return (mutexFacts!=null && mutexFacts.contains(fact));
/*
        for (int i=0; i<mutexFacts.size(); i++)
            if ((Fact)mutexFacts.elementAt(i)==fact)
                return true;
        return false;
*/
    }

    /**
     * check consitence between the selected firing for this fact(goal) and
     * the previously selected firing for another fact(goal)
     */
    public boolean consistentFiringSelection(Fact fact){
        // the previously selected firing for another fact
        TransitionFiring anotherFiring = fact.getSelectedFiring();

        // not consistent if currently selected firing is exclusive with the selected firing of another fact
        if (getSelectedFiring().isMutuallyExclusiveOf(anotherFiring))
            return false;

        // consistent if the selected firing of another fact does not create this fact
         if (!firings.contains(anotherFiring))
            return true;

        // consistent if the selected firing of another fact also creates this fact and is the same as currently selected firing
        // this kind of firing should be preferred
        if (getSelectedFiring() == anotherFiring)
            return true;

        // select the same firing as anotherFiring if the two facts may be created by the same firing
        pointer = firings.indexOf(anotherFiring)+1;
        return true;

        // consider:
        // case1
        //       fact1: firing1, firing2
        //       fact2: firing2, firing3
        // case 2
        //       fact1: firing2, firing1
        //       fact2: firing3, firing2
        // reselect: to set   pointer = firings.indexOf(anotherFiring)+1;
    }

    /**
    * copy a fact from current marking level to next marking level.
    * The source fact and the dest fact are connected by noop
    */
    public Fact copy(){
        return new Fact(token);
    }

    /**
     * convert a fact into a string
     */
    public String toString(){
        StringBuffer str = new StringBuffer("\n"+PetriNet.places[token.getPlace()]+token);
        if (firings!=null){
            str.append("\n  ------------------------ created by following firings");
            for (int i=0; i<firings.size(); i++){
                str.append((TransitionFiring)firings.elementAt(i));
            }
        }
        str.append(mutexFactString());
        return str.toString();
    }

    /**
     * convert mutually exclusive facts into a string
     * mostly for the purpose of debugging
     */
    public String mutexFactString(){
        if (mutexFacts==null)
            return "";
        StringBuffer str = new StringBuffer("\n  ------------------------ mutually exclusive with following facts");
        for (int i=0; i<mutexFacts.size(); i++){
            str.append(((Fact)mutexFacts.elementAt(i)).basicFactString());
        }
        return str.toString();
    }

    /**
     * basic fact string
     */
    private String basicFactString(){
        return "\n  "+PetriNet.places[token.getPlace()]+token;
    }
}
