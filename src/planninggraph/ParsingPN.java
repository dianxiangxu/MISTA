package planninggraph;
/**
* @Author:  Dianxiang Xu
* @         Department of Computer Science
* @         Texas A&M University
* @Date:    12/2001
*
* Parsing the specification of a petri net
*
*   <pn-spec>::=
*       {<transition>}
*       {init <conjunction>}
*       {goal <conjunction>}
*
*       <transition> ::=  transition <name>
* 	                [precond <formula>]
*                       {postcond <formula> [when <formula>]}
*                       [inscription <formula>]
*                       [interval <number>[,<number>]]
*
*       <formula>::= <literal> {[,]<literal>}
*       <conjunction> :: = <predicate>{[,]<predicate>}
*       <literal> ::= [not] <predicate>
*       <predicate> ::= <predicate-name>[<parameter>{,<parameter>}]
*       <parameter> :: = <variable> | <symbol>
*       <variable>::= ?<symbol>
*       <predicate-name> ::= <built-in-predicate> | <place>
*
*       <built-in-predicate> list is contained in class Predicate
*
*
*/

import java.util.*;
import java.io.*;

public class ParsingPN
{
    private StreamTokenizer spec;

    private Vector transitions; // a list of transitions/actions to be translated into transitions
    private Vector predicates;  // a list of non-built-in predicates to be translated into places

    private Vector pres;        // preconditions(formulae) of transitions
    private Vector posts;       // postconditions(formulae) of transitions
    private Vector inscriptions;// inscription conditions (formulae) of transitions
    private Vector earliests;    // earliest firing time of transitions
    private Vector latests;      // latest firing time of transitions
    private Vector init;        // initial state(conjunctions)
    private Vector goal;        // goal state(conjunctions)

    private PetriNet pn;

    // correspondent to the above Vectors, used for creating a Petri net
    private NetPredicate[][] preConds; // preconditions
    private NetPredicate[][] delConds; // delete conditions
    private NetPredicate[][] postConds;// postconditions
    private NetPredicate[][] biConds;  // biconditions
    private NetPredicate[][] inConds;  // inhinitors
    private Formula[] ins;     // inscription condition
    private int[] efts;             // earliest firing time
    private int[] lfts;             // latest firing time
    private NetMarking[] initState;    // initial state
    private NetMarking[] goalState;    // goal state

    private boolean correct = true;

    static final String[] keywords = {"transition", "precond", "postcond", "inscription",
                    "when", "init", "goal"};

    static final boolean PRECONDITION = true;
    static final boolean POSTCONDITION = false;

    static final int MAXPRED = 50;                  // maximum of predicates

    /**
    * constructor
    */
    public ParsingPN(String fname){
    	init();
        correct = parse(fname);
    }
    
    public ParsingPN(File file){
    	init();
        correct = parse(file);
    }

    private void init(){
        spec = null;
        predicates = new Vector();
        transitions = new Vector();
        pres = new Vector();
        posts = new Vector();
        init = new Vector();
        goal = new Vector();
        inscriptions = new Vector();
        earliests = new Vector();
        latests = new Vector();    	
    }
    /**
    * whether the syntax is correct
    */
    public boolean isCorrect(){
            return correct;
    }

    /**
    * get the petri net
    */
    public PetriNet getPN(){
            return pn;
    }

    /**
    * get the init state
    */
    public NetMarking getInit(){
        return mergeMarkings(initState);
    }

    /**
    * get the goal state
    */
    public NetMarking getGoal(){
        return mergeMarkings(goalState);
    }

    /**
     * parse the specification and construct the petri net
     */
    public boolean parse(String fname){
        return parse(new File(fname));
    }
    
    public boolean parse(File fileName){
        FileReader file = null;
        boolean succeed = false;
        try {
            file = new FileReader(fileName);
            spec = new StreamTokenizer(file);
            spec.wordChars('?','?');
            spec.slashSlashComments(true);
            spec.commentChar(';');
            spec.nextToken();

            succeed = true;
            while (spec.ttype != StreamTokenizer.TT_EOF && succeed) {
                if (spec.ttype == StreamTokenizer.TT_WORD) {
                    if (spec.sval.equalsIgnoreCase("transition")) {
                        succeed = parseTransition();
                    }
                    else
                    if (spec.sval.equalsIgnoreCase("init")) {
                        succeed = parseInitState();
                    }
                    else
                    if (spec.sval.equalsIgnoreCase("goal")) {
                        succeed = parseGoalState();
                    }
                }
                else {succeed = false;}
            }
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
            succeed = false;
        }
        finally {
            try {
                if (file != null) file.close();
            }
            catch (IOException ex) {
                System.out.println(ex);
            }
        }
        if (!succeed)
            return false;
        translateInscription();
        if (!translatePredicates())
            return false;
        initState = translateState(init);
        goalState = translateState(goal);

/*       for (int i=0; i<predicates.size(); i++) {
            initState[i] = initState[i].sortMarking();
            goalState[i] = goalState[i].sortMarking();
        }
*/

/*        System.out.println(transitions);
        System.out.println(init);
        System.out.println(inscriptions);
        System.out.println("Create petri net...");
*/
        pn = new PetriNet(predicates, transitions, preConds, delConds, postConds, inConds, ins);

//        for (int i=0; i<predicates.size(); i++)
//            System.out.println((String)predicates.elementAt(i)+allTokens[i]);
//        for (int j=0; j<predicates.size(); j++)
//            System.out.println((String)predicates.elementAt(j)+allMarkings[j]);

//        System.out.println("Parsing done...");
//       MyInput.readInt();

        return succeed;
    }

    /**
     * get the description of a transition
     */
    private boolean parseTransition() throws java.io.IOException {

        // Transition name is expected
        spec.nextToken();
        if (spec.ttype != StreamTokenizer.TT_WORD) {
            System.out.println("Transition name is expected!");
            return false;
        }

        int op = addTransition(spec.sval);
//        System.out.println("\n>Transition="+spec.sval+"["+op+"]");

        // precondition and/or postcond is expected
        spec.nextToken();
        if (spec.ttype != StreamTokenizer.TT_WORD) {
            System.out.println("precond or postcond is expected!");
            return false;
        }

//        System.out.println(spec.sval);

        if (spec.sval.equalsIgnoreCase("precond"))
           if (!parsePrecondition())
              return false;

        // postcondition is expected
        if (spec.ttype != StreamTokenizer.TT_WORD)
            return false;
        if (spec.sval.equalsIgnoreCase("postcond"))
           if (!parsePostcondition())
              return false;

        // inscription condition is expected
        if (spec.ttype != StreamTokenizer.TT_WORD)
            return false;
        if (spec.sval.equalsIgnoreCase("inscription")) {
           if (!parseInscription())
              return false;
        }
        else
            inscriptions.addElement(new Vector());

        // interval expected
        if (spec.ttype != StreamTokenizer.TT_WORD)
            return false;
        if (spec.sval.equalsIgnoreCase("interval")) {
           if (!parseTiming())
              return false;
        }
        else{
            earliests.addElement(new Vector());
            latests.addElement(new Vector());
        }

        return true;
    }

    /**
     * parse the precondition
     */

    private boolean parsePrecondition() throws java.io.IOException {
        return parseFormula(pres, false);           // no built-in predicates
    }

    /**
     * parse the postcondition
     */
    private boolean parsePostcondition() throws java.io.IOException {
        if (!parseFormula(posts, false))            // no built-in predicates
            return false;
        if (spec.ttype == StreamTokenizer.TT_WORD && spec.sval.equalsIgnoreCase("when")) {
//            return parseFormula(whens);
        }
        return true;
    }

    /**
     * parse inscription conditions for transitions
     */
    private boolean parseInscription() throws IOException {
        return parseFormula(inscriptions, true);          // built-in predicates allowed
    }

    /**
     * parse timing interval
     */
    private boolean parseTiming() throws IOException {
        return true;
    }

    /**
     * parse the initial state
     */
    private boolean parseInitState() throws java.io.IOException {
        return parseState(init);
    }

    /**
     * parse the goal state
     */
    private boolean parseGoalState() throws java.io.IOException {
        return parseState(goal);
    }


    /**
     * a list of predicates (no built-in predicates)
     */
    private boolean parseFormula(Vector conds, boolean builtin) throws IOException {
        Vector formula = new Vector();
        spec.nextToken();
        while (spec.ttype == StreamTokenizer.TT_WORD && isKeyword(spec.sval)<0){
            boolean negation = false;
            if (spec.sval.equalsIgnoreCase("not")){
                negation = true;
                spec.nextToken();
                if (spec.ttype != StreamTokenizer.TT_WORD || isKeyword(spec.sval)>=0){
                    System.out.println("Syntax error!");
                    return false;
                }
            }
            int predicate = addPredicate(spec.sval.toLowerCase(), builtin);
//            System.out.println("predicate="+spec.sval);
            Vector arguments = new Vector();
            if (!getParameters(arguments))
                return false;
            if (arguments.size()==0)
                arguments.addElement(Token.DEFAULTARGUMENTS[0]);
            formula.addElement(new Literal(negation, predicate, arguments));
            if (spec.ttype==',')
                spec.nextToken();
        }
        conds.addElement(formula);
        return true;
    }

    /**
    * parse state (a list of predicates)
    */
    private boolean parseState(Vector state) throws java.io.IOException {
        spec.nextToken();
        while (spec.ttype == StreamTokenizer.TT_WORD && isKeyword(spec.sval)<0){
            int predicate = addPredicate(spec.sval.toLowerCase(), false);
            Vector arguments = new Vector();
            if (!getParameters(arguments))
                return false;
            ((NetMarking)state.elementAt(predicate)).addFact(new Fact(predicate, arguments));
            if (spec.ttype==',')
                spec.nextToken();
        }
        return true;
    }

    /**
     * parse the list of parameters
     */
    private boolean getParameters(Vector arguments) throws IOException{
       spec.nextToken();
       if (spec.ttype=='(') {    // get parameters
            spec.nextToken();
            switch (spec.ttype) {
                case StreamTokenizer.TT_WORD:   arguments.addElement(spec.sval.toLowerCase());
                                            if (!getMoreParameters(arguments))
                                                 return false;
                                            break;
                case StreamTokenizer.TT_NUMBER: arguments.addElement(String.valueOf((int)spec.nval));
                                            if (!getMoreParameters(arguments))
                                                 return false;
                                            break;
                case '"': arguments.addElement(spec.sval.toLowerCase());
                			if (!getMoreParameters(arguments))
                			return false;
                			break;
                case ')': break;
                default: return false;
            }
            spec.nextToken();
        }
        return true;
    }

    /**
     * parse more parameters when more than one
     */
    private boolean getMoreParameters(Vector arcLabel) throws IOException{
        spec.nextToken();
        while (spec.ttype==',') {
            spec.nextToken();
            switch (spec.ttype) {
                case StreamTokenizer.TT_WORD:   arcLabel.addElement(spec.sval.toLowerCase());
                                                break;
                case StreamTokenizer.TT_NUMBER: arcLabel.addElement(String.valueOf((int)spec.nval));
                                                break;
                case '"': arcLabel.addElement(spec.sval.toLowerCase());
                		break;
                default: return false;
            }
            spec.nextToken();
        }
        if (spec.ttype!=')')
            return false;
        else
            return true;
    }

    /**
    * Search a predicate: an existing non-built-in/built-in, or a new non-built-in
    * If a new non-built-in predicate, add it to current list
    */
    private int addPredicate(String predicate, boolean builtin){
        for (int i=0; i<predicates.size(); i++)
            if (((String)predicates.elementAt(i)).equalsIgnoreCase(predicate))
                return i;
        if (builtin) { // built-in predicates allowed
            int num = NetPredicate.predicateNumber(predicate);
            if (num>=0)
                return num;
        }
        // a new non-built-in predicate
        predicates.addElement(predicate);
        init.addElement(new NetMarking());
        goal.addElement(new NetMarking());
        return predicates.size()-1;
    }

    /**
    * Search an transition: already exists or new. If new, add it to current list
    */
    private int addTransition(String op)
    {
        for (int i=0; i<transitions.size(); i++)
            if (((String)transitions.elementAt(i)).equalsIgnoreCase(op))
                return i;
        transitions.addElement(op);
        return transitions.size()-1;
    }

    /**
     * Translate preconditions and postconditions into
     * preconditions/postconditions/biconditions/inhibitors
     *
     * Example:
     * Original precondition:  p1(?x), not p2(?x), p3(?x), p4(?x)
     * Original postcondition: not p1(?x), p3(?x), p5(?x)
     *
     * new precond: p1(?x), p4(?x)
     * new inhibitor: p2(?x)
     * new bi: p3(?x)
     * new postcond: p5(?x)
     *
     * Notes:
     * (1) by default, a precondtion will not hold after firing, e.g. p4(?x)
     * If a precondition still holds, it should be explicitly specified, e.g. p3(?x)
     *
     * (2) p(?x) must occur in the precondition if not p(?x) occurs in the postcondition, e.g. p1(?x)
     * so p1(?x) can be omitted
     *
     * (3) if not p(?x) occur in both precondition and postcondition, it is an inhibitor
     * if not p(?x) only occurs in the precondition, it is an inhibitor
     *
     */
    private boolean translatePredicates(){
        preConds = new NetPredicate[transitions.size()][];
        delConds = new NetPredicate[transitions.size()][];
        postConds = new NetPredicate[transitions.size()][];
        biConds = new NetPredicate[transitions.size()][];
        inConds = new NetPredicate[transitions.size()][];

        for (int i=0; i<transitions.size(); i++) {

            Vector pre = (Vector)pres.elementAt(i);
            Vector post = (Vector)posts.elementAt(i);
            Vector newPre = new Vector();
            Vector newPost = new Vector();
            Vector newBi = new Vector();
            Vector newIn = new Vector();

            // process precondition
            // if a literal also occurs in the postcondition, remove it
            for (int j=0; j<pre.size(); j++){
                Literal lit1 = (Literal) pre.elementAt(j);
                if (lit1.getName()>=NetPredicate.START){
                    System.out.println("Illegal predicate "+lit1+" in the precondition.");
                    return false;
                }
                NetPredicate pred = new NetPredicate(lit1.getName(), lit1.getArguments());
                boolean found = false;
                for (int k=0; k<post.size(); k++) {
                    Literal lit2 = (Literal) post.elementAt(k);
                    if (lit1.equals(lit2)){ //
                        found = true;
                        if (lit1.getNegation()){
                            newIn.addElement(pred);     // inhibitor
                            if (lit2.getNegation())
                                newPost.addElement(new NetPredicate(lit2.getName(), lit2.getArguments()));
                        }
                        else {
                            if (!lit2.getNegation())         // bicondition
                                newBi.addElement(pred);
                            else                             // regular precondition, as default case
                                newPre.addElement(pred);
                        }
                        post.removeElementAt(k);
                        break;
                    }
                }
                if (!found) {   // not occur in the postcondition
                    if (lit1.getNegation())
                        newIn.addElement(pred);
                    else
                        newPre.addElement(pred);
                }
            }

            // process the rest of postcondition
            for (int j=0; j<post.size(); j++) {
                Literal lit2 = (Literal) post.elementAt(j);
                if (lit2.getName()>=NetPredicate.START){
                    System.out.println("Illegal predicate name "+lit2+" in the precondition.");
                    return false;
                }
                if (lit2.getNegation()) {
                    System.out.println("Illegal negation in "+lit2);
                    return false;
                }
                else
                    newPost.addElement(new NetPredicate(lit2.getName(), lit2.getArguments()));
            }

            // from vectors to arrays
            preConds[i] = vector2array(newPre);
            postConds[i] = vector2array(newPost);
            biConds[i] = vector2array(newBi);
            inConds[i] = vector2array(newIn);

            // meanings of terminologies are somewhat changed for planning graph analysis
            // 1/2/2002, DX
            delConds[i] = preConds[i];
            preConds[i] = new NetPredicate[delConds[i].length+biConds[i].length];
            for (int index=0; index<delConds[i].length; index++)
                preConds[i][index] = delConds[i][index];
            for (int index=0; index<biConds[i].length; index++)
                preConds[i][delConds[i].length+index] = delConds[i][index];

            System.out.println((String)transitions.elementAt(i));
            printList("\tprecondition:", preConds[i]);
            printList("\tdelcondition:", delConds[i]);
            printList("\tpostcondition:", postConds[i]);
            if (biConds[i].length>0)
                printList("\tbicondition:", biConds[i]);
            if (ins[i]!=null)
                System.out.println("\tinscription:"+"\n\t\t"+ins[i]);
            if (inConds[i].length>0)
                printList("\tinhibitor:", inConds[i]);

        }
        return true;
    }

    /**
     * put the predicates in a vector into an array
     */
    private NetPredicate[] vector2array(Vector conjunction) {
        NetPredicate[] preds = new NetPredicate[conjunction.size()];
        for (int i=0; i<conjunction.size(); i++) {
            preds[i] = (NetPredicate) conjunction.elementAt(i);
        }
        return preds;
    }

    /**
     *
     */
    private void printList(String title, NetPredicate[] conjunction) {
        System.out.println(title);
        for (int i=0; i<conjunction.length; i++) {
            System.out.print("\t\t"+predicates.elementAt(conjunction[i].getName()));
            System.out.println(conjunction[i]);
        }
    }



    /**
     *  translate the initial/goal state in a vector into an array
     */
    NetMarking[] translateState(Vector source){
        NetMarking[] state = new NetMarking[source.size()];
        for (int i=0; i<source.size(); i++){
            state[i] = (NetMarking)source.elementAt(i);
        }
        return state;
    }

    /**
     *  merge a group of markings for individual places into one global marking
     *  Note that the facts are sorted in terms of places
     */
    private NetMarking mergeMarkings(NetMarking[] source){
        NetMarking dest = new NetMarking();
        for (int i=0; i<source.length; i++){
            for (int j=0; j<source[i].size();j++)
                dest.addFact(source[i].factAt(j));
        }
        return dest;
    }

    /**
     * associate inscription consitions with transitions
     */
    private void translateInscription() {
        ins = new Formula[inscriptions.size()];
        for (int i=0; i<inscriptions.size(); i++){
            Vector v = (Vector)inscriptions.elementAt(i);
            if (v.size()>0)
                ins[i] = new Formula(v);
        }
   }

   private int searchPredicate(String predicate, boolean builtin) {
        for (int i=0; i<predicates.size(); i++)
            if (predicate.equalsIgnoreCase((String)predicates.elementAt(i)))
                return i;
        if (builtin)
            return NetPredicate.predicateNumber(predicate);
        else
            return -1;
    }


    private int searchTransition(String tran) {
        for (int i=0; i<transitions.size(); i++)
            if (tran.equalsIgnoreCase((String)transitions.elementAt(i)))
                return i;
        return -1;
    }

    /**
     * check if a word is a keyword
     */
    private int isKeyword(String s) {
        for (int i=0; i<keywords.length; i++)
            if (keywords[i].equalsIgnoreCase(s))
                return i;
        return -1;
    }

}
