package planninggraph;

/**
 * All transition firings at a firing level
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 *
 */

import java.util.*;

public class FiringSet {

    private Vector<TransitionFiring> firings;	// a list of firings for all transitions

    /**
     * default constructor
     */
    public FiringSet(){
        firings = new Vector<TransitionFiring>();
    }

    /**
     * construct a firing level.
     */
    public FiringSet(Vector<TransitionFiring> firings){
        this.firings = firings;
    }

    /**
     * add a firing
     */
    public void addFiring(TransitionFiring firing){
        if (!firings.contains(firing))
            firings.addElement(firing);
    }

    /**
     * return true if contains a given firing
     */
    public boolean containsFiring(TransitionFiring firing){
        return firings.contains(firing);
    }

    /**
     * get a firing at specific position
     */
    public TransitionFiring firingAt(int index){
        return (TransitionFiring)firings.elementAt(index);
    }

    /**
     * remove all firings
     */
    public void removeAllFirings(){
        firings.removeAllElements();
    }

    /**
     * remove a firing
     */
    public void removeFiring(TransitionFiring firing){
        firings.removeElement(firing);
    }

    /**
     * get all firings
     */
    public Vector<TransitionFiring> getFirings(){
        return firings;
    }
    /**
     * get all firings except NOOPs
     */
    public Vector<TransitionFiring> getFiringsExceptNOOP(){
        Vector<TransitionFiring> f = new Vector<TransitionFiring>();
        for (int i=0; i<firings.size(); i++){
            if (!((TransitionFiring)firings.elementAt(i)).isNOOP()){
                f.addElement((TransitionFiring)firings.elementAt(i));
            }
        }
        return f;
    }

    /**
     * get the number of firings
     */
    public int numOfFirings(){
        return firings.size();
    }

   /**
     * return true if contains at least one non-NOOP firing
     */
    public boolean containsNonNOOP(){
        for (int i=0; i<firings.size(); i++)
            if (!((TransitionFiring)firings.elementAt(i)).isNOOP())
                return true;
        return false;
    }

    /**
     * Reason about mutual exclusion relationships among firings
     * Two NOOP may be mutual exclusive of each other if the facts they are carrying are mutual exclusive
     * A NOOP may also be mutual exclusive of a normal firing if:
     *     1) their preconditions are mutual exclusive, or
     *     2) the normal firing deletes the fact carried forward by the NOOP
     */
    public void inferMutexFirings(){
        TransitionFiring firing1, firing2;
        for (int index1=0; index1<firings.size(); index1++){
            firing1 = (TransitionFiring)firings.elementAt(index1);
            for (int index2=index1+1; index2<firings.size();index2++){
                firing2 = (TransitionFiring)firings.elementAt(index2);
                if (firing1.conflict(firing2)){
// use following statement to record interference (need more memory)
//                if (firing1.interference(firing2)||firing1.conflict(firing2)){
                    firing1.addMutexFiring(firing2);
                    firing2.addMutexFiring(firing1);
                }
            }
        }
    }

    /**
     * convert the list of firings to a string
     */
    public String toString(){
        StringBuffer str = new StringBuffer();
        for (int i=0; i<firings.size(); i++){
            if (!((TransitionFiring)firings.elementAt(i)).isNOOP()){  // NOOPs are not displayed
                str.append((TransitionFiring)firings.elementAt(i)+"\n");
            }
        }
        return new String(str);
    }

    /**
     * convert the list of firings to a string for the prupose of debugging
     * (including NOOP firings and mutually exclusive firings)
     */
    public String debugString(){
        StringBuffer str = new StringBuffer();
        for (int i=0; i<firings.size(); i++){
                str.append("\n"+(TransitionFiring)firings.elementAt(i));
                str.append(((TransitionFiring)firings.elementAt(i)).mutexFiringString());
        }
        return new String(str);
    }

        /**
     * convert the list of firings to a string
     */
    public String conditionString(){
        StringBuffer str = new StringBuffer();
        for (int i=0; i<firings.size(); i++){
            str.append("\n-------------------");
            str.append((TransitionFiring)firings.elementAt(i));
            str.append(((TransitionFiring)firings.elementAt(i)).conditionString());
        }
        return new String(str);
    }
}
