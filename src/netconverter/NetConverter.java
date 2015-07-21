package netconverter;
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

import kernel.CancellationException;
import kernel.ProgressDialog;

import mid.Firing;
import mid.FiringSequence;
import mid.MID;
import mid.Marking;
import mid.Predicate;
import mid.Substitution;
import mid.Transition;
import mid.Tuple;

import planninggraph.Fact;
import planninggraph.FiringSet;
import planninggraph.Formula;
import planninggraph.Literal;
import planninggraph.NetMarking;
import planninggraph.NetPredicate;
import planninggraph.PetriNet;
import planninggraph.Token;
import planninggraph.TransitionFiring;
import planninggraph.VarValue;

public class NetConverter
{
    private MID pn;

    private Vector<String>    predicates = new Vector<String>();         // place/predicate names; places are referenced by internal numbers
    private Vector<String>    transitions = new Vector<String>();    // tranistion names; transitions are referenced by internal numbers

    // correspondent to the above Vectors, used for creating a Petri net
    private NetPredicate[][] preConds= new NetPredicate[transitions.size()][]; // preconditions
    private NetPredicate[][] delConds= new NetPredicate[transitions.size()][]; // delete conditions
    private NetPredicate[][] postConds= new NetPredicate[transitions.size()][];// postconditions
    private NetPredicate[][] inhibitorConds= new NetPredicate[transitions.size()][];  // inhinitors
    private Formula[] inscriptionConds;     // inscription condition

    public NetConverter(MID pn){
    	this.pn = pn; 
    	initilization();
    	convertTransitions();
    }

    private void initilization(){
    	for (String place: pn.getPlaces()){
    		predicates.add(place);
    	}
    	int numOfTransitions = pn.getTransitions().size();
        preConds= new NetPredicate[numOfTransitions][]; 	// preconditions
        delConds= new NetPredicate[numOfTransitions][]; 	// delete conditions
        postConds= new NetPredicate[numOfTransitions][];	// postconditions
        inhibitorConds= new NetPredicate[numOfTransitions][];  // inhibitors
        inscriptionConds = new Formula[numOfTransitions];     // inscription condition
    }

    public Verifier analyzePlanningGraph(ArrayList<Marking> goalMarkings, int depth, ProgressDialog progressDialog) throws CancellationException{
		PetriNet petri = new PetriNet(predicates, transitions,
                preConds, delConds, postConds, inhibitorConds, inscriptionConds);
		petri.setSearchDepth(depth);
		FiringSequence[][] firingSequences = new FiringSequence[pn.getInitialMarkings().size()][goalMarkings.size()];; 
		for (int i=0; i<pn.getInitialMarkings().size(); i++){
			for (int j=0; j<goalMarkings.size(); j++){
				if (petri.graphAnalysis(convertMarking(pn.getInitialMarkings().get(i)), convertMarking(goalMarkings.get(j)), progressDialog)){
					if (petri.getFiringStack()!=null)
						firingSequences[i][j] = convertFiringStackToFiringSequence(petri.getFiringStack());
				}	
			}
		}	
		return new Verifier(pn, goalMarkings, firingSequences);
    }

    public ArrayList<FiringSequence> analyzePlanningGraph(Marking initMarking, ArrayList<Marking> goalMarkings, int depth, ProgressDialog progressDialog) throws CancellationException{
		PetriNet petri = new PetriNet(predicates, transitions,
                preConds, delConds, postConds, inhibitorConds, inscriptionConds);
		petri.setSearchDepth(depth);
		ArrayList<FiringSequence> firings = new ArrayList<FiringSequence>();
		for (Marking goalMarking: goalMarkings) {
			if (petri.graphAnalysis(convertMarking(initMarking), convertMarking(goalMarking), progressDialog))
				firings.add(convertFiringStackToFiringSequence(petri.getFiringStack()));
			else
				firings.add(null);
		}
		return firings;
    }

    private FiringSequence convertFiringStackToFiringSequence(Stack<FiringSet> firingStack){
    	ArrayList<Firing> firings = new ArrayList<Firing>();
        for (int i=firingStack.size()-1; i>=0; i--){
            Vector<TransitionFiring> transitionFirings = firingStack.elementAt(i).getFirings();
            for (int j=0; j<transitionFirings.size(); j++){
                if (!transitionFirings.elementAt(j).isNOOP()){  // NOOPs are not displayed
                    TransitionFiring firing = transitionFirings.elementAt(j);
                    Transition t = pn.getTransitions().get(firing.getTransition());
                    Substitution s = convertBindingsToSubstitution(t, firing.getVariableBindings());
                    firings.add(new Firing(t, s));
                }
            }
        }    	
        return new FiringSequence(firings);
    }
    
    private String findOriginalVariable(ArrayList<String> originalVariables, String var){
    	assert originalVariables!=null;
    	for (String original: originalVariables)
    		if (var.equals(original))
    			return original;
    	for (String original: originalVariables)
    		if (var.equals("?"+original)){
//    			System.out.println("Variable converted back: "+var);
    			return original;
    		}	
    	return null;
    }
    
    private Substitution convertBindingsToSubstitution(Transition t, VarValue[] bindings){
    	ArrayList<String> originalVariables = t.getAllVariables(); 
    	Hashtable <String, String> newBindings = new Hashtable <String, String>();
    	for (VarValue pair: bindings) {
    		String originalVariable = findOriginalVariable(originalVariables, pair.getName());
    		assert originalVariable !=null;
    		newBindings.put(originalVariable, pair.getValue());
    	}
    	return new Substitution(newBindings);
    }
    
    public NetMarking convertMarking(Marking marking){
    	Vector<Fact> tokens = new Vector<Fact>();
    	for (int i=0; i<predicates.size(); i++) {
    		ArrayList<Tuple> tuples = marking.getTuples(predicates.get(i));
    		if (tuples == null)
    			continue;
    		for (Tuple tuple: tuples) {
    			Vector<String> args = new Vector<String>();
    			for (String arg: tuple.getArguments())
    				args.add(arg);
    			if (args.size()==0)
                    args.addElement(Token.DEFAULTARGUMENTS[0]);
    			tokens.add(new Fact(i, args));
    		}
    	}
    	return new NetMarking(tokens);
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
     * new delcond: p1(?x), p3(?x), p4(?x)
     * new postcond: p3(?x), p5(?x)
     * new inhibitor: p2(?x)
     *
     * Notes:
     * (1) A precondition is a delete condition (will not hold after firing), e.g. p4(?x)
     *     unless explicitly listed in postcondition, e.g. p3(?x)
     *
     * (2) p(?x) must occur in the precondition if not p(?x) occurs in the postcondition, e.g. p1(?x)
     *     so p1(?x) can be omitted
     *
     * (3) 
     * if not p(?x) only occurs in the precondition, it is an inhibitor
     * if not p(?x) occur in both precondition and postcondition, it is also an inhibitor
     */
    
    private boolean convertTransitions(){
        for (int tindex=0; tindex<pn.getTransitions().size(); tindex++) {      	
        	Transition transition = pn.getTransitions().get(tindex);
        	
            Vector<NetPredicate> newPreConds = new Vector<NetPredicate>();
            Vector<NetPredicate> newDelConds = new Vector<NetPredicate>();
            Vector<NetPredicate> newPostConds = new Vector<NetPredicate>();
            Vector<NetPredicate> newInhibitors = new Vector<NetPredicate>();
           
        	transitions.add(transition.getEvent());
            for (Predicate prePredicate: transition.getPrecondition()){
            	NetPredicate pred = convertPredicateToPredicate(prePredicate);
                if (prePredicate.getNegation())
                	newInhibitors.addElement(pred);
                else {
                	newPreConds.addElement(pred);
                	if (!isBidirectionalPredicate(prePredicate, transition.getPostcondition()))
                		newDelConds.addElement(pred);
                }
            }

            for (Predicate postPredicate: transition.getPostcondition()){
            	if (!isBidirectionalPredicate(postPredicate, transition.getPrecondition())){
            		NetPredicate pred = convertPredicateToPredicate(postPredicate);
            		newPostConds.addElement(pred);
            	}
            }

            convertInscription(tindex);
            
            preConds[tindex] = vector2array(newPreConds);
            postConds[tindex] = vector2array(newPostConds);
            delConds[tindex] = vector2array(newDelConds);
            inhibitorConds[tindex] = vector2array(newInhibitors);

/*            System.out.println((String)transitions.elementAt(tindex));
            printList("\tprecondition:", preConds[tindex]);
            printList("\tdelcondition:", delConds[tindex]);
            printList("\tpostcondition:", postConds[tindex]);
            if (inscriptionConds[tindex]!=null)
                System.out.println("\tinscription:"+"\n\t\t"+inscriptionConds[tindex]);
            if (inhibitorConds[tindex].length>0)
                printList("\tinhibitor:", inhibitorConds[tindex]);
*/
        }
        return true;
    }

    private boolean isBidirectionalPredicate(Predicate predicate, ArrayList<Predicate> conditions){
    	for (Predicate condition: conditions)
    		if (predicate.equals(condition))
    			return true;
    	return false;
    }
    
    private String convertArgument(String argument){
    	return MID.isVariable(argument) && !argument.startsWith("?")? "?"+argument: argument;
    }

    private NetPredicate convertPredicateToPredicate(Predicate predicate){
    	int placeIndex = getPlaceIndex(predicate.getName());
    	if (placeIndex<0){
//    		System.out.println("NetConverter: place "+predicate.getName()+" not found!");
    		return null;
    	}	
    	Vector<String> arguments = new Vector<String>();
    	for (String arg: predicate.getArguments())
    		arguments.add(convertArgument(arg));
        if (arguments.size()==0)
            arguments.addElement(Token.DEFAULTARGUMENTS[0]);
        return new NetPredicate(placeIndex, arguments);
    }

    private Literal convertPredicateToLiteral(Predicate predicate){
    	Vector<String> arguments = new Vector<String>();
    	if (predicate.getArguments()!=null) {
    		for (String arg: predicate.getArguments())
    			arguments.add(convertArgument(arg));
    	}
        return new Literal(predicate.getNegation(), NetPredicate.predicateNumber(predicate.getName()), arguments);
    }
    
    private NetPredicate[] vector2array(Vector<NetPredicate> conjunction) {
        NetPredicate[] preds = new NetPredicate[conjunction.size()];
        for (int i=0; i<conjunction.size(); i++) {
            preds[i] = (NetPredicate) conjunction.elementAt(i);
        }
        return preds;
    }

    private void printList(String title, NetPredicate[] conjunction) {
        System.out.println(title);
        for (int i=0; i<conjunction.length; i++) {
            System.out.print("\t\t"+predicates.elementAt(conjunction[i].getName()));
            System.out.println(conjunction[i]);
        }
    }

    private void convertInscription(int i) {
        	Transition transition = pn.getTransitions().get(i);
        	ArrayList<Predicate> inscription = transition.getWhenCondition();
        	if (inscription==null)
        		return;
        	Vector<Literal> v = new Vector<Literal>();
        	for (Predicate predicate: inscription) {
        		v.add(convertPredicateToLiteral(predicate));
        	}
            if (v.size()>0)
                inscriptionConds[i] = new Formula(v);
   }

    private int getPlaceIndex(String place){
    	for (int i=0; i<predicates.size(); i++)
    		if (place.equalsIgnoreCase(predicates.get(i)))
        			return i;
    	return -1;
    }
    
 }
