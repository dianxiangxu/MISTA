package planninggraph;
import java.util.*;
import java.io.*;

/**
 * A paruence of plans
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 */
public class ParPlan {

    Vector branches;           // a list of parallel plans(seqPlans)

    /**
     * Default construct
     */
    public ParPlan(){
        branches = new Vector();
    }

    /**
     * construct a seqPlan in terms of firings
     * each one of the firings forms one branch
     */
    public ParPlan(Vector firings){
        branches = new Vector();
        for (int fIndex=0; fIndex<firings.size(); fIndex++){
            branches.addElement(new SeqPlan((TransitionFiring)firings.elementAt(fIndex)));
        }
     }

    /**
     * add a ParPlan as a step in the sequence
     */
    public void addBranch(SeqPlan seqPlan){
        branches.addElement(seqPlan);
    }

    /**
     * get a branch
     */
    public SeqPlan branchAt(int index){
        return (SeqPlan)branches.elementAt(index);
    }

    /**
     * number of branches
     */
    public int size(){
        return branches.size();
    }

    // impossible to merge if the number of branches is less than the number of firings
    // the firings could then form a single step containing parallel branches
    // Rationale: graphplan always find minimum set of firings
    // Example: impossible to merge firings f1, f2, f3 into branches b1|b2
    // if mergable, (b1,f1)|(b2,f2)|f3, then f3 is not supposed to be selected at its level
    // but rather selected at the same level as b1 and b2
    // maybe need more consideration
    // for any branch, at most one firing can be added
    /**
     * add firings to branches
     */
    public boolean mergeWithFirings(Vector firings){
        if (branches.size()<firings.size())
            return false;
        // pointer[i]: the number of branch that i-th firing is appended
        int pointer[] = new int[firings.size()];
        for (int i=0; i<firings.size(); i++)
            pointer[i]=-1;
        TransitionFiring firing;
        boolean appendable;
        int fIndex = 0;
        while (fIndex>=0 & fIndex<firings.size()){
            firing = (TransitionFiring)firings.elementAt(fIndex);
            do {
                appendable = true;
                pointer[fIndex] = pointer[fIndex]+1;
                for (int i=0; i<fIndex; i++)
                    if (pointer[fIndex] == pointer[i]){
                        appendable = false;
                        break;
                    }
                if (appendable){
                    appendable = appendableFiring(pointer[fIndex], firing);
                }
            } while (!appendable && pointer[fIndex]<branches.size());
            if (!appendable){
                pointer[fIndex] = -1;
                fIndex--;
            }
            else{
                fIndex++;
            }
        }
        if (fIndex<0)
            return false;
        for (fIndex=0; fIndex<firings.size(); fIndex++){
            if (pointer[fIndex]<0 || pointer[fIndex]>firings.size()-1){
                return false;
            }
        }
        for (fIndex=0; fIndex<firings.size(); fIndex++){
            SeqPlan branch = (SeqPlan)branches.elementAt(pointer[fIndex]);
            branch.addStep((TransitionFiring)firings.elementAt(fIndex));
        }
        return true;
    }

    /**
     * a firing is appendable to a branch
     * if f's precondition is not contained in the postconditions of other branches
     */
    public boolean appendableFiring(int index, TransitionFiring f){
        SeqPlan branch;
        // get all postconditions of the branches except the branch to which the firing will be added
        Vector allPostConds = new Vector();
        for (int i=0; i<branches.size(); i++){
            if (i==index) continue;
            Vector branchPostConds = ((SeqPlan)branches.elementAt(i)).getPostCondFacts();
            for (int j=0; j<branchPostConds.size();j++){
                allPostConds.addElement((Fact)branchPostConds.elementAt(j));
            }
        }
/*
System.out.println("\n=================================");
System.out.println("\nPrecond of  "+f);
System.out.println(new Marking(f.getPreCondFacts()));
System.out.println("\nPostcond of other branches except "+index);
System.out.println(new Marking(allPostConds));
*/
        //not appendable if a precondition of the firing is contained in the postconditions
        for (int j=0; j<f.getPreCondFacts().size(); j++){
            boolean found = false;
            Fact preFact = (Fact)f.getPreCondFacts().elementAt(j);
            for (int i=0; i<allPostConds.size(); i++){
                if (preFact.getToken()==((Fact)allPostConds.elementAt(i)).getToken()){
                    found = true;
                    break;
                }
            }
            if (found)
                return false;
        }
        return true;
    }

    /**
     * toString
     */
    public String toString(){
        if (branches.size()<1)
            return "";
        StringBuffer str = new StringBuffer("\n(par ");
        for (int i=0; i<branches.size(); i++){
            str.append((SeqPlan)branches.elementAt(i));
        }
        return str.toString()+"\n)";
    }
}