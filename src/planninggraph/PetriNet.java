package planninggraph;

/**
 * ________________________________________________________________________________________________
 *
 * Reachability analysis of predicate/transition nets through planning graph.
 *
 * @Version: 1.0
 * @Date:   12/2001
 * @Author: Dianxiang Xu
 * @        Department of Computer Science
 * @        Texas A&M University
 * ________________________________________________________________________________________________
 *
 * The internal representation of timed predicate/transition nets
 *         <Pred, Tran, PreCond, PostCond, DelCond, InCond, Ins, EFT, LFT, Marking>
 *
 * Pred: a set of places (predicates), indexed by integers
 * Tran: a set of transitions, indexed by integers
 * PreCond: a mapping from transitions to places/predicates with
 *      arc labels/arguments of predicates (preconditions)
 * PostCond: a mapping from transitions to places/predicates with
 *      arc labels/arguments of predicates (postconditions)
 * DelCond: a mapping from transitions to places/predicates with
 *      arc labels/arguments of predicates (delete conditions)
 * InCond: inhibitor conditions
 * Ins: a mapping from transitions to inscriptions
 * EFT: earliest firing time, relative to the time point when the transition is enabled
 * LFT: latest firing time
 * Marking: initial/current marking
 *
 * ________________________________________________________________________________________________
 *
 * inhibitor conditions and timing constraints are not considered yet
 *
 * ________________________________________________________________________________________________
 *
 * COMMENTS on marking representation
 * A marking level contains facts of all predicates
 * The facts in the initial marking should be sorted in terms of predicates
 * ________________________________________________________________________________________________
 */

import java.util.*;


import kernel.CancellationException;
import kernel.ProgressDialog;
import kernel.VerificationManager;
import locales.LocaleBundle;



public class PetriNet {

    public static String[]    places;         // place/predicate names; places are referenced by internal numbers
    public static String[]    transitions;    // tranistion names; transitions are referenced by internal numbers
//    private String[]    places;         // place/predicate names; places are referenced by internal numbers
//    private String[]    transitions;    // tranistion names; transitions are referenced by internal numbers
    private NetPredicate[][] preConds;     // preConds[i] is the list of preconditions of transition i
    private NetPredicate[][] delConds;     // preConds[i] is the list of preconditions(delete conditions) of transition i (become untrue after firing)
    private NetPredicate[][] postConds;    // postConds[i] is the list of postconditions(add conditions) of transition i (become true after firing)
    private NetPredicate[][] inConds;      // inConds[i] is the list of inhibitors for transition i(prevent the transition from firing)
    private Formula[]   ins;            // ins[i] is the inscription formula(may contain non-place predicate) for transition i
//    private double[]    efts;           // efts[i] is the earliest firing time of transition i
//    private double[]    lfts;           // lfts[i] is the latest firing time of transition i
    private NetMarking     marking;       // current marking level: marking[i] is the initial/current marking for place i
    private NetMarking     newMarking;    // new facts for next marking level: newMarking[i] contains new facts of possible firings
    private FiringSet   currentFiringLevel; // current firing level

    private Stack<FiringSet>       firingStack;                        // for plan execution
    private Vector      unsolvableGoals = new Vector();     // goals that are already proven unsolvable
    private NetMarking     currentGoals;                       // current goals

    // for unification
    private int[]       start;      // start[i]: the index of first fact of place i
    private int[]       total;      // total[i]: number of facts in place i in current marking

    private Vector[]    preCondVariables;   // preCondVariables[i] contains variables for transition i

    private int searchDepth = 100;
    
    /**
     * construct a predicate/transition net
     */
    public PetriNet(    Vector<String> places, Vector<String> transitions,
                        NetPredicate[][] preConds, NetPredicate[][] delConds,
                        NetPredicate[][] postConds, NetPredicate[][] inConds,
                        Formula[] ins){
        this.places = new String[places.size()];
        for (int i=0; i<places.size(); i++)
            this.places[i] = (String)places.elementAt(i);
        this.transitions = new String[transitions.size()];
        for (int i=0; i<transitions.size(); i++)
            this.transitions[i] = (String)transitions.elementAt(i);
        this.preConds = preConds;
        this.delConds = delConds;
        this.postConds = postConds;
        this.inConds = inConds;
        this.ins = ins;

    }

    public void setSearchDepth(int depth){
    	this.searchDepth = depth;
    }
    
    /**
     * Reachability analysis based on planning graph
     */
    public boolean graphAnalysis(NetMarking init, NetMarking goals, ProgressDialog progressDialog) throws CancellationException{
        long begin = System.currentTimeMillis();
        // initialization
        Vector mLevels = new Vector();
        Vector fLevels = new Vector();
        marking = init.copy();
//        marking = init;
        newMarking = new NetMarking();
        start = new int[places.length];
        total = new int[places.length];
        marking.countFacts(start, total);
//printStartIndex();
        setPreCondVariables();
        int[] goalFactIndices = resetGoalFactIndices(goals);
        unsolvableGoals.addElement(new Hashtable());
        mLevels.addElement(marking);

        int graphLevelNo=1;
        while (!findSolution(mLevels, fLevels, goals, goalFactIndices, progressDialog)){
            // initialize marking for current graph level
//            System.out.println("Graph level "+graphLevelNo);
            currentFiringLevel = new FiringSet();
            // copy prior marking level into next level
            marking = marking.carryForward(currentFiringLevel);
            // reset new marking to record facts to be created
            newMarking = new NetMarking();
            extendGraphLevel(mLevels, fLevels, progressDialog);

            //System.out.println("--------------------------------------");
            //System.out.println("Marking Level ");
            //System.out.println(marking);
if (graphLevelNo>searchDepth){
//    System.out.println("\nThe goal is unsolvable within "+graphLevelNo+" steps");
    return false;
}
            unsolvableGoals.addElement(new Hashtable());
            graphLevelNo++;
        }
//        printUnsolvableGoals();
//        executePlan(init, goals);
//        System.out.println("\n\nTime span for planning graph analysis: "+((System.currentTimeMillis()-begin)/1000.0)+" seconds.");
//        constructParallelPlan();
//        System.out.println("\n\nTotal time span including parallelizing plan: "+((System.currentTimeMillis()-begin)/1000.0)+" seconds.");
        return true;
    }

    /**
     * create a new graph(firing and marking) level
     */
    private void extendGraphLevel(Vector mLevels, Vector fLevels, ProgressDialog progressDialog) throws CancellationException{
        // process all possile firings of all transitions
        for (int tran=0; tran<transitions.length; tran++){
            doVirtualFirings(tran);
        }
        // reason about mutual exclusion relationships among firings in current firing level
        currentFiringLevel.inferMutexFirings();
        // save current firing level
        fLevels.addElement(currentFiringLevel);
        // combine marking and newMarking and adjust the fact indices
        if (newMarking!=null){
            marking.mergeMarkings(newMarking);
            marking.countFacts(start, total);
//            printStartIndex();
        }
        // reason about mutual exclusion relationships among facts in current marking level
        marking.inferMutexFacts(progressDialog);
        // save current marking level
        mLevels.addElement(marking);

        //System.out.println("--------------------------------------");
        //System.out.println("Firing Level "+currentFiringLevel.debugString());

    }

    /**
     * Conduct all possible firings of a given transition under current marking
     * Note that facts of preconditions are not removed
     */
    private void doVirtualFirings(int transition){
        int preIndex, factIndex;
        Vector facts;
        int place;
	boolean unified;
        //get preconditions
	NetPredicate[] preCond = preConds[transition];
	if (preCond.length == 0) { // there are no input places
            System.out.println("no precondition");
            return;
        }
        //initialize substitution
        Environment env = new Environment(preCondVariables[transition]);

        //set indices for unification
        int[] pointer = new int[preCond.length];
        for (preIndex=0; preIndex<preCond.length; preIndex++) {
            if (total[preCond[preIndex].getName()]<1 || start[preCond[preIndex].getName()]<0)
                return;
            pointer[preIndex]=0;     // pointer(relative to start) to facts for preIndex-th precondition
        }

        preIndex=0;
        while (preIndex>=0 && preIndex<preCond.length)  {
            env.undoUnification(preIndex);
            place = preCond[preIndex].getName();
            if (pointer[preIndex]>total[place]-1) {
                if (preIndex==0) { return; }
                else {pointer[preIndex] = 0; preIndex--; }
            }
            else {
                    do {
                        Token token = marking.factAt(start[place]+pointer[preIndex]).getToken();
                        pointer[preIndex] = pointer[preIndex]+1;
                        unified = env.unify(preIndex, preCond[preIndex], token);
                        if (unified && preIndex == preCond.length-1){
                            if (ins[transition] == null || ins[transition].truth(env)){
                                TransitionFiring firing = processVirtualFiring(transition, pointer, env);
                                if (firing!=null)
                                    currentFiringLevel.addFiring(firing);
                            }
                            env.undoUnification(preIndex);
                            unified = false;
                        }
                    } while (pointer[preIndex]<total[place] && !unified);
                    if (!unified) {pointer[preIndex]=0; preIndex--;}
                    else { preIndex++;}
            }   // end if-else
        }       // end while
    }


    /**
     * process a virtual firing:
     *    set pointers to preconditions in current marking level
     *    add new facts to the new fact list next marking levels
     *    add the firing to the new firing list
     *    set pointers to new facts for the firing
     */
    private TransitionFiring processVirtualFiring(int transition, int[] pointer, Environment env) {
        NetPredicate[] preCond = preConds[transition];
        // preCondition facts in the prior marking level
        // the firing is not possible if there exist two mutually exclusive facts in preCondFacts
        Vector preCondFacts = new Vector();
        for (int preIndex=0; preIndex<preCond.length; preIndex++){
            int place = preCond[preIndex].getName();
            Fact currentFact = marking.factAt(start[place]+pointer[preIndex]-1);
            TransitionFiring noop = (TransitionFiring)currentFact.getFirings().elementAt(0);  // NOOP is the first firing
            Fact priorFact = (Fact)noop.getPreCondFacts().elementAt(0);   // priorFact and newFact are the same fact connected by NOOP between two levels
            for (int i=0; i<preCondFacts.size(); i++)
                if (priorFact.isMutuallyExclusiveOf((Fact)preCondFacts.elementAt(i)))              // impossible firing
                    return null;
            preCondFacts.addElement(priorFact);
        }

        // create the firing
        TransitionFiring firing = new TransitionFiring(transition, env);

        // set pointers to unification(preCondition+biCondition) facts in the prior marking level
        firing.setPreCondFacts(preCondFacts);

        // set pointers to precondition(delete) facts in the current marking level
        Vector delCondFacts = new Vector();
        for (int delIndex=0; delIndex<delConds[transition].length; delIndex++){
            int place = preCond[delIndex].getName();
            delCondFacts.addElement(marking.factAt(start[place]+pointer[delIndex]-1));
        }
        firing.setDelCondFacts(delCondFacts);

        // set the pointers to newly created facts in current marking level
        Vector postCondFacts = new Vector();
        for ( int postIndex=0; postIndex<postConds[transition].length; postIndex++) {
            NetPredicate p = postConds[transition][postIndex];
            Fact newFact = createNewFact(p.getName(),env.substitute(p.getArguments()));
            newFact.addFiring(firing);
            postCondFacts.addElement(newFact);
        }
        firing.setPostCondFacts(postCondFacts);
        //System.out.println("Firing: "+firing);
        return firing;
    }

    /**
     * create a new fact of the firing
     */
    private Fact createNewFact(int pred, String[] args){
        Fact fact = marking.searchFact(pred, args);
        if (fact!=null)
            return fact;
        fact = newMarking.searchFact(pred, args);
        if (fact!=null)
            return fact;
        fact = new Fact(pred, args);
        newMarking.addFact(fact);
        return fact;
    }

    /**
     * extract the solution if the goal is satisfied and the goal facts are not mutual exclusive
     */
    private boolean findSolution(Vector mLevels, Vector fLevels, NetMarking goals, int[] goalFactIndices, ProgressDialog progressDialog) throws CancellationException{

        // goal facts are already reached?
        if (!marking.includes(goals, start, total, goalFactIndices))
            return false;
        // goals are satisfied at initial state
        if (mLevels.size()==1){
//            System.out.println("The goal is reached at the initial state");
            return true;
        }
        // translate goalFactIndices to facts
        currentGoals = new NetMarking();
        for (int tIndex=0; tIndex<goalFactIndices.length; tIndex++){
            int place = goals.factAt(tIndex).getPlace();
            currentGoals.addFact(marking.factAt(start[place]+goalFactIndices[tIndex]));
        }

        // goal facts are not mutual exclusive?
        if (!currentGoals.checkGoalConsistence())
            return false;

        // goals are satisfied
        return extractSolution(mLevels, fLevels, progressDialog);
    }

    /**
     * extract solution
     */
    private boolean extractSolution(Vector mLevels, Vector fLevels, ProgressDialog progressDialog) throws CancellationException{

        //initialization for solution extraction
        Stack goalStack = new Stack();
        goalStack.push(currentGoals);
//        Stack firingStack = new Stack();
        firingStack = new Stack();
        int currentLevelNo = mLevels.size()-1;

        // reset pointers of select firings
        for (int levelNo=mLevels.size()-1; levelNo>0; levelNo--)
            ((NetMarking)mLevels.elementAt(levelNo)).unSelectFirings();

        //System.out.println("______________________________________");
        //System.out.println("Finding the solution for following goal:"+currentGoals.size());
        //System.out.println(currentGoals);

        while (currentLevelNo>=0){
    		if (progressDialog!=null && progressDialog.isCancelled())
    			throw new CancellationException(LocaleBundle.bundleString("Verification cancelled"));
            Hashtable unsolvable = (Hashtable)unsolvableGoals.elementAt(currentLevelNo);
            FiringSet firingSet = new FiringSet();
            if (unsolvable.containsKey(currentGoals.hashKey())){
//                System.out.println("Current goal is proven unsolvable at level: "+currentLevelNo);
            }
            if (!unsolvable.containsKey(currentGoals.hashKey()) && goalResolution(firingSet)){
//            System.out.println("Extraction succeeds at level "+currentLevelNo);
                // goals are solvable at this level
                goalStack.push(currentGoals);
                firingStack.push(firingSet);
                if (currentLevelNo==1)   {   // final solution found
                    outputSolution(firingStack);
                    return true;
                }
                // else
                currentLevelNo--;
            }
            else { // goals are not solvable at this level
//                System.out.println("Extraction fails at level "+currentLevelNo);
                if (!unsolvable.contains(currentGoals))
                    unsolvable.put(currentGoals.hashKey(), currentGoals);
                if (currentLevelNo==mLevels.size()-1){
                    return false;           // no solution found
                }
                // else: go back to last level
                firingStack.pop();
                goalStack.pop();
                currentGoals = (NetMarking) goalStack.peek();
                currentLevelNo++;
            }
        }
        return false;
    }

    /**
     * solve current goals
     */
     private boolean goalResolution(FiringSet firingSet){

        // start with the first fact for the first time of firing selection
        int goalIndex=0;
        // start with the last fact when backtracking
        for (int index=0; index<currentGoals.size(); index++)
            if (currentGoals.factAt(goalIndex).getPointer()>0){
                goalIndex = currentGoals.size()-1;
                break;
            }
        // select firings and regress goals
        while (goalIndex>=0 && goalIndex<currentGoals.size()){
            Fact fact = currentGoals.factAt(goalIndex);
            if (fact.hasMoreFirings()){
                boolean success = false;
                TransitionFiring selectedFiring;
                do {
                    selectedFiring = fact.nextFiring();
                    if (selectedFiring.isNOOP()) // noop is consistent with any other firings
                        success = true;
                    else // check the legality of selected firings
                        success = checkSelectedFirings(goalIndex);
                    // if firing is selected for the last goal, deduce new goals
                    if (success && goalIndex==currentGoals.size()-1){
                        NetMarking newGoals = goalRegression(firingSet);
                        if (newGoals!=null){
                            currentGoals = newGoals;
                            return true;
                        }
                        else
                            success = false;
                    }
                } while (fact.hasMoreFirings() && !success);
                if (!success){ // no firing can be selected for current subgoal, go back to prior subgoal
                    fact.resetPointer();
                    goalIndex--;
                }
                else {         // a firing is selected for current subgoal
                    goalIndex++;
                }
            }
            else{ // no firing for current subgoal
                fact.resetPointer();
                goalIndex--;
            }
        }
        // goalIndex<0
        // unselect (reset the pointers of) firings for all facts in a goal marking
        for (goalIndex=0; goalIndex<currentGoals.size(); goalIndex++)
            currentGoals.factAt(goalIndex).resetPointer();
        return false;
    }

    /**
     * deduce new goals according to selected firings
     */
    private NetMarking goalRegression(FiringSet firingSet){
        // get selected firings
        if (!getSelectedFirings(firingSet)){
            return null;
        }
        // deduce new subgoals
        NetMarking subGoals = new NetMarking();
        for (int firingIndex=0; firingIndex<firingSet.numOfFirings(); firingIndex++){
            Vector preCondFacts = firingSet.firingAt(firingIndex).getPreCondFacts();
            for (int preIndex=0; preIndex<preCondFacts.size(); preIndex++){
                if (!subGoals.containsFact((Fact)preCondFacts.elementAt(preIndex)))
                        subGoals.addFact((Fact)preCondFacts.elementAt(preIndex));
            }
        }
        // check goal consistence
        if (!subGoals.checkGoalConsistence()){
            return null;
        }

//System.out.println("----------------------------------------");
//printGoalPointers("Pointers of selected firings");
//System.out.println("\nSelected firings:"+firingSet.numOfFirings());
//System.out.println(firingSet.conditionString());
//System.out.println(firingSet.debugString());
//System.out.println("----------------------------------------");
//System.out.println("\nNew subgoals");
//System.out.println(currentGoals);
// System.out.println("Current goal is deduced.");

        return subGoals;
    }

    /**
     * get selected firings
     * return false if they are illegal
     */
    private boolean getSelectedFirings(FiringSet firingSet){
        //empty the firingSet
        firingSet.removeAllFirings();
        // get all legal firings at this level
        for (int goalIndex=0; goalIndex<currentGoals.size(); goalIndex++){
            TransitionFiring selectedFiring = currentGoals.factAt(goalIndex).getSelectedFiring();
            if (!firingSet.containsFiring(selectedFiring))
                firingSet.addFiring(selectedFiring);
        }
       // the firing set is not feasible if it contains NOOPs only
        if (!firingSet.containsNonNOOP()){
            return false;
        }
        // remove NOOP which fact is already contained in the postcondition of another firing
        for (int goalIndex=0; goalIndex<currentGoals.size(); goalIndex++){
            TransitionFiring selectedFiring = currentGoals.factAt(goalIndex).getSelectedFiring();
            if (selectedFiring.isNOOP()){
                Vector firings = currentGoals.factAt(goalIndex).getFirings();
                boolean found = false;
                for (int firingIndex=0; firingIndex<firingSet.numOfFirings(); firingIndex++){
                    TransitionFiring f = firingSet.firingAt(firingIndex);
                    if (!f.isNOOP() && firings.contains(f)){
                        found = true;
                        break;
                    }
                 }
                 if (found)
                    firingSet.removeFiring(selectedFiring);
            }
        }
        return true;
    }

    /**
     * check conflict of selected firings
     */
    private boolean checkSelectedFirings(int goalIndex){
        // check currently selected firing against previously selected firings
        Fact currentGoalFact = currentGoals.factAt(goalIndex);
        for (int i=0; i<goalIndex; i++){
            if (!currentGoalFact.consistentFiringSelection(currentGoals.factAt(i))){
                return false;

            }
        }
        // the following code seems unncessary because implied by mutex inference

        // selected firing is not feasible if it deletes any fact of current goal
/*
        Vector delCondFacts = currentGoalFact.getSelectedFiring().getDelCondFacts();
        for (int dIndex=0; dIndex<delCondFacts.size(); dIndex++){
            if (currentGoals.containsFact((Fact)delCondFacts.elementAt(dIndex)))
                    return false;
        }
*/
        return true;
    }

    /**
     * output solution
     */
    private boolean outputSolution(Stack firingStack){
        int step = 1;
//        System.out.println("\nThe goal may be achieved by following steps");
        for (int i=firingStack.size()-1; i>=0; i--){
//            System.out.println("\nStep "+step);
//            System.out.println((FiringSet)firingStack.elementAt(i));
            step++;
        }
        /* keep the stack for plan execution
        while (!firingStack.empty()){
            System.out.println("\nStep "+step);
            System.out.println((FiringSet)firingStack.pop());
            step++;
        }
        */
        return true;
    }

    /**
     * report solution
     */
    public String reportSolution(){
    	String result = "";
    	if (firingStack==null)
    		return result;
        for (int i=firingStack.size()-1; i>=0; i--){
            result +=(FiringSet)firingStack.elementAt(i);
        }
        return result;
    }

    public Stack<FiringSet> getFiringStack(){
    	return firingStack;
    }

    /**
     * return true if the fixpoint is reached
     * 1) same number of facts in two adjacent levels, and
     * 2) two equivalent facts in adjacent levels have the same number of firings
     */
    private boolean isFixPoint(Vector mLevels) {
        if (mLevels.size()<2)
           return false;
        NetMarking currentLevel = (NetMarking)mLevels.lastElement();
        NetMarking priorLevel = (NetMarking)mLevels.elementAt(mLevels.size()-2);
        if (currentLevel.size()!=priorLevel.size())
                return false;
        for (int i=0; i<currentLevel.size(); i++){
            if (((Fact)currentLevel.factAt(i)).numOfFirings() != ((Fact)currentLevel.factAt(i)).numOfFirings())
                return false;
        }
        System.out.println("Fixpoint reached.");
        return true;
    }

    /**
     * play back: execute a plan
     */
    private void executePlan(NetMarking init, NetMarking goals){
        marking = init;
        System.out.println("\nExecuting the plan ...");
        System.out.println("\nInitial state");
        System.out.println(init);

        int step = 1;
        for (int index=firingStack.size()-1; index>=0; index--){
            System.out.println("\nStep "+(firingStack.size()-index)+": firing(s)");
            Vector firings = ((FiringSet)firingStack.elementAt(index)).getFirings();

            for (int i=0; i<firings.size(); i++){
                if (((TransitionFiring)firings.elementAt(i)).isNOOP())
                    continue;
                System.out.println((TransitionFiring)firings.elementAt(i));
                if (!fireTransition((TransitionFiring)firings.elementAt(i))){
                    System.out.println("\nThe plan is not feasible at step "+(firingStack.size()-index));
                    return;
                }
            }
//            System.out.println("\nCurrent state");
//            System.out.println(marking);
        }
        System.out.println("\nGoal state");
        System.out.println(goals);
        int[] goalFactIndices = resetGoalFactIndices(goals);
        // goal facts are already reached?
        if (!marking.satisfied(goals)){
            System.out.println("\nGoal is not reached");
            return;
        }
        System.out.println("\nGoal is reached");
    }

    /**
     * Fire a transition/execution a plan step
     */
    public boolean fireTransition(TransitionFiring firing){
        //get preconditions
        if (firing.isNOOP())
            return true;
	NetPredicate[] preCond = preConds[firing.getTransition()];
        for (int preIndex=0; preIndex<preCond.length; preIndex++){
            int place = preCond[preIndex].getName();
            String[] token = firing.substitute(preCond[preIndex].getArguments());
            if (!marking.unifyFact(place, token)){
                System.out.println("Preconditions are not satisfied.");
                return false;
            }
        }
	NetPredicate[] delCond = delConds[firing.getTransition()];
        for (int delIndex=0; delIndex<delCond.length; delIndex++){
            int place = delCond[delIndex].getName();
            String[] token = firing.substitute(delCond[delIndex].getArguments());
            if (!marking.removeUnifiedFact(place, token)){
                System.out.println("No precondition is deleted.");
                return false;
            }
        }
	NetPredicate[] postCond = postConds[firing.getTransition()];
        for (int postIndex=0; postIndex<postCond.length; postIndex++){
            int place = postCond[postIndex].getName();
            String[] token = firing.substitute(postCond[postIndex].getArguments());
            if (marking.unifyFact(place, token)){
                System.out.println("Postconditions already exist.");
                return false;
            }
            marking.addFact(new Fact(place, token));
        }
        return true;
    }

    private void constructParallelPlan(){
        SeqPlan plan = new SeqPlan();
        for (int index=firingStack.size()-1; index>=0; index--){
            Vector firings = ((FiringSet)firingStack.elementAt(index)).getFiringsExceptNOOP();
            // a single firing is a separate step (perhaps need more consideration)
            if (firings.size()==1){
                plan.addStep((TransitionFiring)firings.elementAt(0));
                continue;
            }
            // more than one firing
            // if first step or last step is a single firing, create a step containing parallel branches
            if ((plan.size()==0 || plan.lastStepIsFiring()) && firings.size()>1){    // first step, more than one firing(parallel)
                plan.addStep(new ParPlan(firings));
                continue;
            }
            // more than one firing and last step has parallel branches
            if (plan.size()>0 && plan.lastStepIsParPlan() && firings.size()>1){
                if (!((ParPlan)plan.lastStep()).mergeWithFirings(firings))
                    plan.addStep(new ParPlan(firings));
            }
        }
        System.out.println("\n\nParallelized plan");
        System.out.println(plan);
    }

    /**
     * get all labeling variables of arcs(arguments of predicates).
     */
    private void setPreCondVariables(){
        preCondVariables = new Vector[transitions.length];
        String [] arguments;
        for (int tran=0; tran<transitions.length; tran++){
            Vector vars = new Vector();
            for (int i=0; i<preConds[tran].length; i++) {
	        arguments = preConds[tran][i].getArguments();
	        for (int j=0; j<arguments.length; j++) {
	            if (arguments[j].startsWith("?") && !vars.contains(arguments[j])) {
		        vars.addElement(arguments[j]);
		    }
                }
            }
            preCondVariables[tran] = vars;
        }
    }

    /**
     * set goal fact indices
     */
    private int[] resetGoalFactIndices(NetMarking goals){
        int[] goalFactIndices = new int[goals.size()];
        for (int index=0; index<goals.size(); index++)
            goalFactIndices[index] = -1; // not found yet
        return goalFactIndices;
    }

    /**
     * print all goals proven unsolvable at all levels.
     */
    private void printUnsolvableGoals(){
	System.out.println("\n\n");
	System.out.println("The following goals are proven unsolvable");
        for (int levelNo=1; levelNo<unsolvableGoals.size()-1; levelNo++){
            Hashtable unsolvable = (Hashtable)unsolvableGoals.elementAt(levelNo);
            Enumeration en = unsolvable.elements();
            while (en.hasMoreElements()) {
                System.out.println("\nunsolvable goal at level "+levelNo);
                System.out.println((NetMarking)en.nextElement());
            }
	}
    }

    /**
     * print the positions of goal facts in current state (for debugging).
     */
    private void printGoalPointers(String str){
        System.out.println("\n\n---------------------------\nGOAL pointer  "+str);
        for (int index=0; index<currentGoals.size(); index++){
	    System.out.print(currentGoals.factAt(index));
            System.out.print(" ----- pointer"+currentGoals.factAt(index).getPointer());
	}
	System.out.println("");
    }

    /**
     * print the start positions of facts for predicates in current state (for debugging).
     */
    private void printStartIndex(){
        System.out.println("\n\n---------------------------\nFact index  ");
        for (int index=0; index<places.length; index++){
	    System.out.println(places[index]+"\n\tstarts at: "+start[index]+"   total number: "+total[index]);
	}
    }

}
