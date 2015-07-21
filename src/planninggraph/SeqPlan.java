package planninggraph;
import java.util.*;
import java.io.*;

/**
 * A sequence of plans
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 */
public class SeqPlan {

    Vector steps;           // a sequence of steps: ParPlans or firings

    /**
     * Default construct
     */
    public SeqPlan(){
        steps = new Vector();
    }

    /**
     * construct a seqPlan
     */
    public SeqPlan(TransitionFiring firing){
        steps = new Vector();
        steps.addElement(firing);
    }

    /**
     * add a firing as a step in the sequence
     */
    public void addStep(TransitionFiring firing){
        steps.addElement(firing);
    }

    /**
     * add a ParPlan as a step in the sequence
     */
    public void addStep(ParPlan parPlan){
        steps.addElement(parPlan);
    }

    /**
     * get a step
     */
    public Object stepAt(int index){
        return (Object)steps.elementAt(index);
    }

    /**
     * last step is a firing
     */
    public boolean lastStepIsFiring(){
        return (steps.lastElement() instanceof TransitionFiring);
    }

    /**
     * last step is a parPlan
     */
    public boolean lastStepIsParPlan(){
        return (steps.lastElement() instanceof ParPlan);
    }

    /**
     * get the last step
     */
    public Object lastStep(){
        return (Object)steps.lastElement();
    }

    /**
     * number of steps
     */
    public int size(){
        return steps.size();
    }

    /**
     * get all postcondition facts of all firings in the sequence
     */
    public Vector getPostCondFacts(){
        Vector allPostConds = new Vector();
        for (int i=0; i<steps.size(); i++){
            Object step = steps.elementAt(i);
            if (step instanceof ParPlan){
                System.out.println("Currently, multiple levels of parallelism are not considered yet.");
                return null;
            }
            if (step instanceof TransitionFiring){
                Vector postConds = ((TransitionFiring)step).getPostCondFacts();
                for (int j=0; j<postConds.size(); j++)
                    allPostConds.addElement((Fact)postConds.elementAt(j));
            }
        }
        return allPostConds;
    }

    /**
     * toString
     */
    public String toString(){
        if (steps.size()<1)
            return "";
        StringBuffer str = new StringBuffer("\n");
        if (steps.size()>1)
            str.append("(seq ");
        for (int i=0; i<steps.size(); i++){
            if ( steps.elementAt(i) instanceof TransitionFiring)
                str.append((TransitionFiring)steps.elementAt(i));
            else
                str.append((ParPlan)steps.elementAt(i));
        }
        if (steps.size()>1)
            return str.toString()+"\n)";
        else
            return str.toString()+"\n";
    }
}
