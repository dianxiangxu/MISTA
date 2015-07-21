package planninggraph;

/**
 *
 * @version 1.0, 12/26/2001
 * @author Dianxiang Xu
 */

import java.util.*;
import java.io.*;

public class TransitionFiring extends FiringSubstitution{

    private int transition;             // transition
    private Vector preCondFacts;        // facts of unification(preCond+biCond) in prior marking level
    private Vector delCondFacts;        // facts of precondition(deletion) in current marking level
    private Vector postCondFacts;       // facts of postcondition in current marking level
    private Vector mutexFirings;        // mutual exclusive firings (interference or competing needs/conflict)
                                        // to save memory, interference firings may not be recorded

    /**
     * construct a noop firing, which carried facts from level to level
     */
    TransitionFiring(Fact preFact, Fact postFact){             // noop
        transition = -1;
        preCondFacts = new Vector();
        preCondFacts.addElement(preFact);
        postCondFacts = new Vector();
        postCondFacts.addElement(postFact);
        delCondFacts = new Vector();
    }

    /**
     * construct a regular firing
     */
    TransitionFiring(int transition, Environment env){
        super(env);
        this.transition = transition;
    }

    /**
     * get transition
     */
    public int getTransition(){
        return transition;
    }

    /**
     * get precondition facts in the previous marking level
     */
    public Vector getPreCondFacts(){
        return preCondFacts;
    }

    /**
     * set precondition facts in the previous marking level
     */
    public void setPreCondFacts(Vector preCondFacts){
        this.preCondFacts = preCondFacts;
    }

    /**
     * get precondition/delete facts
     */
    public Vector getDelCondFacts(){
        return delCondFacts;
    }

    /**
     * set precondition facts in the previous marking level
     */
    public void setDelCondFacts(Vector delCondFacts){
        this.delCondFacts = delCondFacts;
    }

    /**
     * get postcondition facts
     */
    public Vector getPostCondFacts(){
        return postCondFacts;
    }

    /**
     * set postcondition facts
     */
    public void setPostCondFacts(Vector postCondFacts){
        this.postCondFacts = postCondFacts;
    }

    /**
     * get mutually exclusive firings
     */
    public Vector getMutexFirings(){
        return mutexFirings;
    }

    /**
     * add a mutually exclusive firing
     */
    public void addMutexFiring(TransitionFiring firing){
        if (mutexFirings==null)
            mutexFirings = new Vector();
        mutexFirings.addElement(firing);
    }

    /**
     * reason about interference between this firing and another firing
     */
    public boolean interference(TransitionFiring firing){
        if (transition==-1 && firing.isNOOP())
            return false;

        for (int i=0; i<firing.postCondFacts.size(); i++) {
            if (delCondFacts.contains((Fact)firing.postCondFacts.elementAt(i)))
               return true;
        }
        for (int i=0; i<firing.delCondFacts.size(); i++){
            if (delCondFacts.contains((Fact)firing.delCondFacts.elementAt(i)))
                return true;
        }
        for (int i=0; i<postCondFacts.size(); i++) {
            if (firing.delCondFacts.contains((Fact)postCondFacts.elementAt(i)))
                return true;
        }
        return false;
    }

    /**
     * reason about conflict between this firing and another firing
     */
    public boolean conflict(TransitionFiring firing){
        for (int i=0; i<preCondFacts.size(); i++) {
            Fact p = (Fact)preCondFacts.elementAt(i);
            for (int j=0; j<firing.preCondFacts.size(); j++){
                Fact q = (Fact)firing.preCondFacts.elementAt(j);
                if (p.isMutuallyExclusiveOf(q))
                    return true;
            }
        }
        return false;
    }

    /**
     * true if this firing is mutually exclusive with another firing
     */
    public boolean isMutuallyExclusiveOf(TransitionFiring firing){
        if (firing==this)
            return false;
        else
            return interference(firing) || (mutexFirings!=null && mutexFirings.contains(firing));
// use following statement if interference is recorded.
//        return (mutexFirings!=null && mutexFirings.contains(firing));
    }

    /**
     * is NOOP
     */
    public boolean isNOOP(){
        return (transition==-1);
    }

    /**
     * String representation of a firing
     */
    public String toString(){
        if (transition<0)
            return "  NOOP";
        return "  "+PetriNet.transitions[transition]+super.toString();
    }

    /**
     * convert conditions to a string (mostly for the purpose of debugging)
     */
    public String conditionString(){
        StringBuffer str = new StringBuffer();
        str.append("\n==Unification condition");
        if (preCondFacts!=null)
            for (int i=0; i<preCondFacts.size(); i++)
                str.append((Fact)preCondFacts.elementAt(i));
        str.append("\n==Precondition/delete condition");
        if (delCondFacts!=null)
            for (int i=0; i<delCondFacts.size(); i++)
                str.append((Fact)delCondFacts.elementAt(i));
        str.append("\n==PostCondition/add condition");
        if (postCondFacts!=null)
            for (int i=0; i<postCondFacts.size(); i++)
                str.append((Fact)postCondFacts.elementAt(i));

        return new String(str);
    }

    /**
     * convert mutually exclusive firings into a string (mostly for the purpose of debugging)
     */
    public String mutexFiringString(){
        if (mutexFirings==null)
            return "";
        StringBuffer str = new StringBuffer("\n  ------------------------  mutually exclusive with following firings due to competing needs");
        for (int i=0; i<mutexFirings.size(); i++)
            str.append((TransitionFiring)mutexFirings.elementAt(i));
        return new String(str);
    }

}
