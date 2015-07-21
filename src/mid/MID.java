/* 	
	Author Dianxiang Xu
*/
package mid;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import kernel.Kernel;

import locales.LocaleBundle;

import parser.MIDParser;
import pipeprt.dataLayer.PipeTransition;

import testcode.TargetLanguage;
import testcode.TargetLanguageOO;
import utilities.FileUtil;

// Model-Implementation Description (MID) Language
// PART I: 	Model (PrT net, contracts, finite state machine, threat net, threat tree)
// PART II: Model-Implementation Mapping
// PART III: Helper Code

public class MID implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String ConstructorEvent = "new";
	public static final String DEFAULT_GOAL_TAG = "GOAL";
	public static final String RESET = "reset";		// reset a predicate/place (remove all tokens), can be used only in postcondition
	private static final String VariablePrefix = "?";
	
	// Part 0: name of the file from which the mid object is created
	private String fileName; 
	
	// Part I: Model - PrT net
    private ArrayList<Transition>	transitions = new ArrayList<Transition>(); 

    private ArrayList<String> events = new ArrayList<String>(); 
	private ArrayList<String> places = new ArrayList<String>();         

    private Hashtable <String, ArrayList<Transition>> transitionsForEvents = new Hashtable <String, ArrayList<Transition>>();  
    
	private ArrayList<Marking> initialMarkings = new ArrayList<Marking>();     

    private ArrayList<GoalProperty> goalProperties = new ArrayList<GoalProperty>();
    private ArrayList<AssertionProperty> assertionProperties = new ArrayList<AssertionProperty>();
    
	private static Hashtable<String, String> symbolsToNumbers = new Hashtable<String, String>();   
	private static Hashtable<String, String> numbersToSymbols = new Hashtable<String, String>();   

	private ArrayList<String> sinkEvents = new ArrayList<String>();		// sink (termination) events

	private ArrayList<String> nonNegativeEvents = new ArrayList<String> (); // non-negative events - will not be used to generate negative/dirty tests
	
	private ArrayList<Predicate> unitTests = new ArrayList<Predicate>();

	private String sequencesFile = null;		// user provided sequences for test generation  
	
	// Threat Tree
	private ArrayList<ThreatTreeNode> threatTreeNodes = new ArrayList<ThreatTreeNode>();
	
    // Part II: Model-Implementation Mapping
	private String systemName;	
	private Hashtable<String, String> objects = new Hashtable<String, String>(); // mapping from model-level individuals to implementation objects
	private ArrayList<Mapping> methods = new ArrayList<Mapping>(); // method invocations
	private ArrayList<Mapping> accessors = new ArrayList<Mapping>(); // accessors for verifying test oracles
	private ArrayList<Mapping> mutators = new ArrayList<Mapping>(); // mutators for setting things up
	private ArrayList<String> options = new ArrayList<String>();	// these places represent options (all items in the same option are mutually exclusive) 
	private ArrayList<String> hidden = new ArrayList<String>();		// these places and/or transitions are excluded from code generation

	private Hashtable<String, ArrayList<String>> parameters = new Hashtable<String, ArrayList<String>>();   

	private Hashtable<String, ArrayList<String>> regions = new Hashtable<String, ArrayList<String>>();   

	// Part III: Test Code to be included in the test class
	
	//  no need to specify language in MID file because a particular user would almost use the same language
	//  this only needs to be set up once
	//	private TargetLanguage language = TargetLanguage.JAVA;
	
	private String packageBlock = null; 
	private String importBlock = null;
	
	private String alphaBlock = null;	// code before any test starts
	private String omegaBlock = null;	// code after all tests are done (not effective when junit/nunit used)
	
	private String setUpCode = null;	// first step within a test
	private String tearDownCode = null;	// last step within a test
	
	private ArrayList<String> helperCode = new ArrayList<String>();


    public MID(){
    	symbolsToNumbers.clear();
    	numbersToSymbols.clear();
    }

    // Part 0: file name
    public void setFileName(String fileName){
    	this.fileName = fileName;
    }
    
    public String getFileName(){
    	return fileName;
    }
    
	// Part I: Methods for the Model
    public void collectPlaces(){
    	for (Transition transition: transitions){
   			for (Predicate predicate: transition.getPrecondition())
   				addPlace(predicate.getName());
   			for (Predicate predicate: transition.getPostcondition())
   				addPlace(predicate.getName());
    	}
    }

    public void addPlace(String place){
    	if (!places.contains(place) && !place.equalsIgnoreCase(RESET))
    		places.add(place);
    }

    public boolean hasPlace(String placeName){
    	return places.contains(placeName);
    }

    public ArrayList<String> getPlaces(){
    	return places;
    }

    public void setPlaces(ArrayList<String> places){
    	this.places = places;
    }
    
    public void collectEvents(){
    	for (Transition transition: transitions){
    		addEvent(transition.getEvent());
    	}	
    }

    private void addEvent(String event){
    	if (!events.contains(event))
    		events.add(event);
    }

    public boolean hasEvent(String event){
    	return events.contains(event);
    }
    
    public ArrayList<String> getEvents(){
    	return events;
    } 

    public void setEvents(ArrayList<String> events){
    	this.events = events;
    } 

    public boolean hasAttackTransition(){
    	for (Transition transition: transitions)
    		if (transition.isAttackTransition())
    			return true;
    	return false;
    }
    
    public void addTransition(Transition transition){
    	transitions.add(transition);
    }

    public int getTransitionIndex(Transition transition){
    	for (int index=0; index<transitions.size(); index++)
    		if (transitions.get(index) == transition)
    			return index;
    	return -1; 		// constructor/new
    }
    
    public Transition getTransitionAtIndex(int index){
    	if (index>=0 && index<transitions.size())
    		return transitions.get(index);
    	else
    		return null;
    } 
    
    public ArrayList<Transition> getTransitions(){
    	return transitions;
    }

    public void setTransitions(ArrayList<Transition> transitions){
    	this.transitions = transitions;
    }

    public ArrayList<Transition> getTransitionsForEvent(String event){
    	return transitionsForEvents.get(event);
    }

    public Hashtable<String, ArrayList<Transition>> getTransitionsForEvents(){
    	 return transitionsForEvents;
    }

    public void setTransitionsForEvents(Hashtable<String, ArrayList<Transition>> transitionsForEvents){
    	this.transitionsForEvents = transitionsForEvents;
    }

    public void setTransitionsForEvents(){
    	for (Transition transition: transitions) {
    		ArrayList<Transition> ts = transitionsForEvents.get(transition.getEvent()); 
    		if (ts==null) {
    			ts = new ArrayList<Transition>();
    			transitionsForEvents.put(transition.getEvent(), ts);
    		}
    		ts.add(transition);
    	}
    }

    public void setTransitionDeletePreconditions(){
    	for (Transition transition: transitions)
    		transition.setDeletePrecondition();
    }
    
	public static boolean isVariable(String name) {
		if (name.length()==0)
			return false;
		return name.startsWith(VariablePrefix)
				|| Character.isLowerCase(name.charAt(0));
	}
    
    public void fireAll(Marking marking){
    	for (Transition transition: transitions) {
    		Unifier unifier = new Unifier(transition, marking);
    		ArrayList<Substitution> substitutions = unifier.getSubstitutions();
    		for (Substitution substitution: substitutions) {
    			System.out.println(transition.getEvent()+": "+substitution.toString(transition.getAllVariables()));
    			Marking newMarking = fireTransition(marking, transition, substitution);
    			System.out.println("New marking: "+newMarking);
    		}
    	}
    }

    public Marking fireTransition(Marking marking, Transition transition, Substitution substitution){
    	Marking newMarking = marking.clone();
    	// remove tokens from input places
//    	for (Predicate input: transition.getPrecondition()){
    	for (Predicate input: transition.getDeletePrecondition()){
    		Tuple tuple = substitution.substitute(input);
//System.out.println("Input tuple: "+tuple);
    		newMarking.removeTuple(input.getName(), tuple);
    	}
    	// add new tokens to output places or reset given places
//    	for (Predicate output: transition.getPostcondition()){
    	for (Predicate output: transition.getAddPostcondition()){
    		if (output.getName().equalsIgnoreCase(RESET)){
    			newMarking.resetPlace(output.getArguments().get(0));
    		}
    		else {
    			Tuple tuple = substitution.substitute(output);
    			newMarking.addTuple(output.getName(), tuple);
    		}
    	}
    	return newMarking;
    }

    public boolean isFirable(Marking marking, Transition transition, Substitution substitution){
		for (Predicate input: transition.getPrecondition()){
    		Tuple tuple = substitution.substitute(input);
    		ArrayList<Tuple> tokens = marking.getTuples(input.getName());
			if (!input.getNegation() && (tokens==null || !tokens.contains(tuple)))
    			return false;
			if (input.getNegation() && tokens!=null && tokens.size()>0){
				if (!hasFreeVariable(input, substitution)){
					if (tokens.contains(tuple))
						return false;
				}
				else if (matchToken(input.getArguments(), tokens, substitution))
					return false;
			}
    	}
		if (transition.getWhenCondition()!=null){
			for (Predicate guard: transition.getWhenCondition())
				if (!Functions.isTrue(guard, substitution.getBindings(), new Stack<BindingRecord>(), 0)) {
					return false;
				}
		}
    	return true;
    }
    
    private boolean hasFreeVariable(Predicate input, Substitution substitution){
    	for (String argument: input.getArguments())
    		if (MID.isVariable(argument)) {
    			String value = substitution.getBinding(argument);
    			if (value==null || value.equals("null"))
    				return true;
    		}
    	return false;
    }
    
    private boolean matchToken(ArrayList<String> arguments, ArrayList<Tuple> tokens, Substitution substitution){
    	for (Tuple token: tokens) {
    		boolean match = true;
    		int index = 0;
    		while (index<arguments.size() && match) {
    			String argument = arguments.get(index);
    			String tokenArg = token.getArguments().get(index);
    			if (MID.isVariable(argument)) {
    				String value = substitution.getBinding(argument);
    				if (value!=null && !value.equals("null") && !value.equals(tokenArg))
    					match = false;
    			} else 
    			if (!argument.equals(tokenArg))
    				match = false;
    			index++;
    		}
    		if (match)
    			return true;
    	}
    	return false; 
    }
    
    private ArrayList<Transition> combinatorialApplicableTransitions = null;
    public boolean isCombinatorialTestingApplicable(Transition transition){
    	if (combinatorialApplicableTransitions==null){
    		combinatorialApplicableTransitions = new ArrayList<Transition>();
    		for (Transition t: transitions)
    			if (t.isCombinatorialTestingApplicable())
    				combinatorialApplicableTransitions.add(t);
    	}
    	return combinatorialApplicableTransitions.contains(transition);		
    }
    
    // BEGIN threat tree
    public ArrayList<ThreatTreeNode> getThreatTreeNodes(){
    	return threatTreeNodes;
    }
    
    public Transition createTransitionForEvent(String event){
    	for (Transition transition: transitions)
    		if (transition.getEvent().equalsIgnoreCase(event))
    			return transition;
		Transition newTransition = new Transition(event);
   		transitions.add(newTransition);
   	   	addEvent(event);
    	return newTransition;
    }
    
    public void addThreatTreeNode(String event, ArrayList<String> childEvents, String relationString){
    	ThreatTreeNode newThreatTreeNode = new ThreatTreeNode(createTransitionForEvent(event));
    	if (childEvents!=null && childEvents.size()>0){
    		for (String childEvent: childEvents){
    			newThreatTreeNode.addChildNode(new ThreatTreeNode(createTransitionForEvent(childEvent)));
    		}
    		if (relationString!=null)
    			newThreatTreeNode.setRelation(relationString);
    	}
    	threatTreeNodes.add(newThreatTreeNode);
    }
    
    public ArrayList<ThreatTreeNode> getThreatTreeRoots(){
    	ArrayList<ThreatTreeNode> roots = new ArrayList<ThreatTreeNode>();
    	for (ThreatTreeNode node: threatTreeNodes)
    		if (isThreatTreeRoot(node))
    			roots.add(node);
    	return roots;
    	
    }
    
    private boolean isThreatTreeRoot(ThreatTreeNode node){
    	Transition transition = node.getTransition();
    	for (ThreatTreeNode tmpNode: threatTreeNodes){
    		if (tmpNode.containsTransition(transition))
    			return false;
    	}
    	return true;	
    }
    
    public boolean isThreatTreeModel(){
    	return threatTreeNodes.size()>0;
    }
    
    public void buildThreatTree(){
    	for (ThreatTreeNode threatTreeNode: threatTreeNodes){
    		ArrayList<ThreatTreeNode> children = threatTreeNode.getChildren();
    		for (int index =0; index<children.size(); index++){
    			ThreatTreeNode child = children.get(index);
    			ThreatTreeNode foundThreatTreeNode = findThreatTreeNode(child.getTransition());
    			if (foundThreatTreeNode!=null)
    				children.set(index, foundThreatTreeNode);
    		}	
    	}
    }
    
    private ThreatTreeNode findThreatTreeNode(Transition transition){
       	for (ThreatTreeNode threatTreeNode: threatTreeNodes)
       		if (threatTreeNode.getTransition()==transition)
       			return threatTreeNode;
       	return null;
    }
    
    public String toThreatTreeString(){
    	String result = "";
    	if (threatTreeNodes.size()>0){
    		result+="\nThread tree nodes:\n";
    		for (ThreatTreeNode node: threatTreeNodes)
    			result += node+"\n";
    	}
    	return result;
    }
    // END threat tree
    
	public void addInitialMarking(Marking initMarking) {
		initialMarkings.add(initMarking);
	}

	public ArrayList<Marking> getInitialMarkings() {
		return initialMarkings;
	}

	public void addGoalProperty(GoalProperty goalProperty) {
		goalProperties.add(goalProperty);
	}
	
	public ArrayList<GoalProperty> getGoalProperties() {
		return goalProperties;
	}

	public void addAssertionProperty(AssertionProperty assertionProperty) {
		assertionProperties.add(assertionProperty);
	}
	
	public ArrayList<AssertionProperty> getAssertionProperties() {
		return assertionProperties;
	}

	// begin Named Integer Constants: <name> <number>
	public static String getNumberForSymbol(String symbol){
		return symbolsToNumbers.get(symbol);
	}

	public static String getSymbolForNumber(String number){
		return numbersToSymbols.get(number);
	}
	
	public static void putSymbolNumber(String symbol, String number){
		symbolsToNumbers.put(symbol, number);
		numbersToSymbols.put(number, symbol);
	}
	
	public static boolean containsNumberKey(String number){
		return numbersToSymbols.containsKey(number);
	}

	public static boolean containsSymbolKey(String symbol){
		return symbolsToNumbers.containsKey(symbol);
	}
	// end Named Integer Constants: <name> <number>
	
	public boolean hasUnitTests(){
		return unitTests.size()!=0;
	}
	
	public ArrayList<Predicate> getUnitTests(){
		return unitTests;
	}

	public void addUnitTest(Predicate unitTest){
		unitTests.add(unitTest);
	}

	public void setSequencesFile(String fileName){
		sequencesFile = fileName;
	}
	
	public String getSequencesFile(){
		return sequencesFile;
	}

	// sink events
	public boolean hasSinkEvents(){
		return sinkEvents.size()>0;
	}
	
	public boolean isSinkTransition(Transition transition){
		return sinkEvents.contains(transition.getEvent());
	}

	public void setSinkEvents(ArrayList<String> sinks){
		sinkEvents = sinks;
	}
	
	public String findErrorsInSinkEvents(){
		for (String event: sinkEvents)
			if (!events.contains(event))
				return event+":"+LocaleBundle.bundleString("Sink event is not defined in the transitions");
		return null;  
	}
	
	// non-negative events, not used for generating negative tests
	public boolean hasNonNegativEvents(){
		return nonNegativeEvents.size()>0;
	}
	
	public boolean isNonNegativeTransition(Transition transition){
		return nonNegativeEvents.contains(transition.getEvent());
	}

	public void setNonNegativeEvents(ArrayList<String> nonNegatives){
		nonNegativeEvents = nonNegatives;
	}

	public String findErrorsInNonNegativeEvents(){
		for (String nonNegativeEvent: nonNegativeEvents)
			if (!events.contains(nonNegativeEvent))
				return nonNegativeEvent+":"+LocaleBundle.bundleString("NONNEGATIVE_EVENT_IS_NOT_DEFINED_INT_THE_TRANSITIONS");
		return null;  
	}

	// Access control
	private Hashtable<String, ABACAttribute> attributes = new Hashtable<String, ABACAttribute>();   
	private ArrayList<ABACRule> rules = new ArrayList<ABACRule>();		

	public Hashtable<String, ABACAttribute> getAttributes(){
		return attributes;
	}
	
	public void addAttribute(ABACAttribute attribute){
		attributes.put(attribute.getName(), attribute);
	}

	public boolean attributeExists(ABACAttribute attribute){
		return attributes.get(attribute.getName())!=null;
	}
	
	public ArrayList<ABACRule> getRules(){
		return rules;
	}
	
	public void addRule(ABACRule rule){
		rules.add(rule);
	}

    // Part II Model-Implementation Mapping
	public void setSystemName(String systemName){
   		this.systemName = systemName;
    }

    public String getDefaultSystemName(){
    	return fileName!=null? FileUtil.getPrefix(new File(fileName).getName()): "System";
    }

    public String getSystemName(){
    	return systemName ==null || systemName.equals("")? getDefaultSystemName(): systemName;
    }

    public String specifiedSystemName(){
    	return systemName;
    }

	public void addMethod(Mapping newMethodOperator) {
		Predicate predicate = newMethodOperator.getPredicate();
		Mapping existingMethodOperator = getMethod(predicate.getName(), new Tuple(predicate.getArguments()));
		if (existingMethodOperator==null)
			methods.add(newMethodOperator);
		else {
			String combinedOperator = existingMethodOperator.getOperator() +"\n" + newMethodOperator.getOperator();
			Mapping combinedMapping = new Mapping (existingMethodOperator.getPredicate(), combinedOperator);
			methods.set(methods.indexOf(existingMethodOperator), combinedMapping);
		}
	}

	public ArrayList<Mapping> getMethods(){
		return methods;
	}
	
	public Mapping getMethod(String event) {
		for (Mapping declaration: methods){
			Predicate predicate = declaration.getPredicate(); 
			if (predicate.getName().equals(event))
			return declaration;
		}
		return null;
	}
	
	public Mapping getMethod(String event, Tuple tuple) {
		for (Mapping declaration: methods){
			Predicate predicate = declaration.getPredicate(); 
			if (predicate.getName().equals(event)
					&& tuple.equals(new Tuple(predicate.getArguments()))
					)
			return declaration;
		}
		return null;
	}
	
	public Mapping getMethodWithVariables(String event) {
		for (Mapping declaration: methods){
			Predicate predicate = declaration.getPredicate(); 
			if (predicate.getName().equalsIgnoreCase(event) &&
					predicate.hasVariables()) 
				return declaration;
		}
		return null;
	}

	public String getObject(String individual) {
		return objects.get(individual);
	}

	public Hashtable<String, String> getObjects(){
		return objects;
	}
	
	public String addObject(String individual, String object) {
		return objects.put(individual, object);
	}

	public Substitution substituteForObjects(Substitution substitution){
		Hashtable <String, String> bindings = new Hashtable <String, String>();
		Enumeration<String> keys = substitution.getBindings().keys();
		while (keys.hasMoreElements()) {
			String variable = (String)keys.nextElement();
			String value = substitution.getBinding(variable);
			String object = getObject(value);
			if (object!=null)
				bindings.put(variable, object);
			else 
				bindings.put(variable, value);
		}
		return new Substitution(bindings);
	} 
	
	static public String getSeleniumCommandHTML(String command, String target, String value){
		return 
		"<tr>"+
		"\n\t<td>"+command+"</td>"+ 
		"\n\t<td>"+target+"</td>"+
		"\n\t<td>"+value+"</td>" +
		"\n</tr>";
	} 

	static public String getSeleniumCommandCall(String command, String target, String value){
		if (target.trim().equalsIgnoreCase("") && value.trim().equalsIgnoreCase(""))
			return command.trim();
		else if (target.trim().equalsIgnoreCase(""))
			return command.trim() +"("+addQuotesToString(value)+")";
		else if (value.trim().equalsIgnoreCase(""))
			return command.trim() +"("+addQuotesToString(target)+")";
		return command.trim() + "("+addQuotesToString(target)+", "+addQuotesToString(value)+")";
	} 

	static public String addQuotesToString(String str){
		String quote="\"";
		String result = str.trim();
		if (!result.startsWith(quote)) 
			result = quote + result;
		if (!result.endsWith(quote)) 
			result = result+quote;
		return result;
	}
	
	public void addAccessor(Mapping newAccessor) {
		Predicate predicate = newAccessor.getPredicate();
		Mapping existingAccessor = getAccessor(predicate.getName(), new Tuple(predicate.getArguments()));
		if (existingAccessor==null)
			accessors.add(newAccessor);
		else {
			String combinedOperator = existingAccessor.getOperator() +"\n" + newAccessor.getOperator();
			Mapping combinedAccessor = new Mapping (existingAccessor.getPredicate(), combinedOperator);
			accessors.set(accessors.indexOf(existingAccessor), combinedAccessor);
		}
	}
	
	public ArrayList<Mapping> getAccessors(){
		return accessors;
	}
	
	public Mapping getAccessor(String place, Tuple tuple) {
		for (Mapping declaration: accessors){
			Predicate predicate = declaration.getPredicate(); 
			if (predicate.getName().equals(place)
					&& tuple.equals(new Tuple(predicate.getArguments()))
					)
			return declaration;
		}
		return null;
	}
	
	public Mapping getAccessorWithVariables(String place) {
		for (Mapping declaration: accessors){
			Predicate predicate = declaration.getPredicate(); 
			if (predicate.getName().equalsIgnoreCase(place) &&
					predicate.hasVariables()) 
				return declaration;
		}
		return null;
	}


	public void addMutator(Mapping newMutator) {
		Predicate predicate = newMutator.getPredicate();
		Mapping existingMutator = getMutator(predicate.getName(), new Tuple(predicate.getArguments()));
		if (existingMutator==null)
			mutators.add(newMutator);
		else {
			String combinedOperator = existingMutator.getOperator() +"\n" + newMutator.getOperator();
			Mapping combinedMutator = new Mapping (existingMutator.getPredicate(), combinedOperator);
			mutators.set(mutators.indexOf(existingMutator), combinedMutator);
		}
	}
	
	public ArrayList<Mapping> getMutators(){
		return mutators;
	}

	public Mapping getMutator(String place, Tuple tuple) {
		for (Mapping mutator: mutators){
			Predicate predicate = mutator.getPredicate(); 
			if (predicate.getName().equals(place)
					&& tuple.equals(new Tuple(predicate.getArguments()))
					)
			return mutator;
		}
		return null;
	}
	
	public Mapping getMutatorWithVariables(String place) {
		for (Mapping mutator: mutators){
			Predicate predicate = mutator.getPredicate(); 
			if (predicate.getName().equalsIgnoreCase(place) &&
					predicate.hasVariables()) 
				return mutator;
		}
		return null;
	}

	private boolean hasMutatorForOption(String place){
		for (Mapping mutator: mutators){
			Predicate predicate = mutator.getPredicate(); 
			if (predicate.getName().equalsIgnoreCase(place)) 
				return true;
		}
		return false;
	}
	
	public ArrayList<String> getOptions(){
		return options;
	}
	
	public void addOptions(ArrayList<String> places){
		for (String place: places) {
			if (!options.contains(place))
				options.add(place);
			// an option place, when used in postcondition, is excluded from generation of oracle code
			// when used in precondition, it is used to generate of set up code  
			if (!hidden.contains(place))
				hidden.add(place);
		}
	}
	
	public boolean isOption(String place) {
		return options.contains(place);
	}
	
	public ArrayList<String> getHidden(){
		return hidden;
	}
	
	public void addHiddenPlacesAndEvents(ArrayList<String> hidden){
		for (String element: hidden)
			if (!this.hidden.contains(element))
				this.hidden.add(element);
	}

	public void addHiddenPlaceOrEvent(String element){
		if (!this.hidden.contains(element))
			this.hidden.add(element);
	}
	
	public boolean isHidden(String element) {
		return hidden.contains(element);
	}
	
	// Part III: Methods for Test Code
	public boolean hasPackageBlock(){
		return packageBlock!=null && packageBlock.trim().length()>0;
	}

	public String getPackageBlock(){
		return packageBlock;
	}
	public void setPackageBlock(String packageBlock){
		this.packageBlock = packageBlock;
	} 
	
	public boolean hasImportBlock(){
		return importBlock!=null && importBlock.trim().length()>0;
	}

	public String getImportBlock(){
		return importBlock;
	}

	public void setImportBlock(String importCode){
		this.importBlock = importCode;
	}

	public boolean hasAlphaBlock(){
		return alphaBlock!=null && alphaBlock.trim().length()>0;
	}

	public String getAlphaBlock(){
		return alphaBlock.trim();
	}

	public void setAlphaBlock(String alphaCode){
		this.alphaBlock = alphaCode;
	}
	
	public boolean hasOmegaBlock(){
		return omegaBlock!=null && omegaBlock.trim().length()>0;
	}

	public String getOmegaBlock(){
		return omegaBlock.trim();
	}

	public void setOmegaBlock(String code){
		this.omegaBlock = code;
	}

	public boolean hasSetUpCode(){
		return setUpCode!=null && setUpCode.trim().length()>0;
	}

	public String getSetUpCode(){
		return hasSetUpCode()? setUpCode: "";
	}	
	
	public String getSetUpCode(TargetLanguage language){
		if (hasSetUpCode())
			return "\n\t" 
			+ ((TargetLanguageOO)language).getSetUpSignature() + "() "
			+ ((TargetLanguageOO)language).getMethodThrowException() + "{\n"
			+ setUpCode
			+ "\t}";
		return "";
	}

	public void setSetUpCode(String code){
		setUpCode = code;
	} 
	
	public boolean hasTearDownCode(){
		return tearDownCode!=null && tearDownCode.trim().length()>0;
	}

	public String getTearDownCode(){
		return hasTearDownCode()? tearDownCode: "";
	}
	
	public String getTearDownCode(TargetLanguage language){
		if (hasTearDownCode())
			return "\n\n\t" 
			+ ((TargetLanguageOO)language).getTearDownSignature() 
			+"() " + ((TargetLanguageOO)language).getMethodThrowException()+" {\n"
			+ tearDownCode
			+ "\t}\n";
		return "";
	}
	
	public void setTearDownCode(String code){
		tearDownCode = code;
	} 
	
	public boolean hasHelperCode(){
		return helperCode.size()>0;
	}

	public boolean addHelperCode(String statement){
		return helperCode.add(statement);
	}
	
	public ArrayList<String> getHelperCodeList(){
		return helperCode;
	}
	
	public String getHelperCode(){
		String result = "";
		for (String code: helperCode) {
			result += code + "\n\n";
		}
		return result;
	}

	public Hashtable<String, ArrayList<String>> getParameters(){
		return parameters;
	}
	
	public void addParameters(String event, ArrayList<String> arguments) {
		parameters.put(event, arguments);
	}

	public boolean hasParameters(String event) {
		return parameters.get(event)!=null;
	}
	
	public String getParameterString(String event) {
		String result = "";
		ArrayList<String> arguments = parameters.get(event);
		if (arguments == null || arguments.size()==0)
			return result;
		result += arguments.get(0);
		for (int i=1; i<arguments.size(); i++)
			result += ", "+arguments.get(i);
		return result;	
	}

	public ArrayList<String> getParameters(String event) {
		return parameters.get(event);
	}
	
	private String toParametersString(){
		String result ="\n\nPARAMETERS\n";
		Enumeration<String> keys = parameters.keys();
		while (keys.hasMoreElements()) {
			String event = (String)keys.nextElement();
			result += event + "(" + getParameterString(event) +")\n";
		}
		return result;
	}

	public boolean hasRegions(){
		return regions.size()>0;
	}

	public Hashtable<String, ArrayList<String>> getRegions(){
		return regions;
	}
	
	public void addRegion(String regionName, ArrayList<String> events) {
		regions.put(regionName, events);
	}

	public ArrayList<String> getEventsOfRegion(String regionName) {
		return regions.get(regionName);
	}

	public String getRegionNameOfEvent(String event){
		Enumeration<String> keys = regions.keys();
		while (keys.hasMoreElements()) {
			String regionName = (String)keys.nextElement();
			if (regions.get(regionName).contains(event))
				return regionName;
		}		
		return null;
	}
	
	public boolean hasMultipleIndependentRegions(boolean setRegion){
		int regionNumber = 0;
		Hashtable<String, ArrayList<String>> regions = new Hashtable<String, ArrayList<String>>();   
		ArrayList<String> uncoveredEvents = new ArrayList<String>();
		for (String event: events)
			uncoveredEvents.add(event);
		while (uncoveredEvents.size()>0){
//			System.out.println("Uncovered "+listString(uncoveredEvents));
			regionNumber++;
			String event = uncoveredEvents.get(0);
			uncoveredEvents.remove(event);
			ArrayList<String> eventSet = new ArrayList<String>();
			eventSet.add(event);
			ArrayList<String> placeSet = new ArrayList<String>();
			for (Transition transition: getTransitionsForEvent(event))
				collectPlaces(placeSet, transition);
			boolean changed;
			do {
				changed = false;
				for (int i=uncoveredEvents.size()-1; i>=0; i--){
					String currentEvent = uncoveredEvents.get(i);
					boolean connected = false;
					for (Transition transition: getTransitionsForEvent(currentEvent))
						if (isConnected(placeSet, transition)){
							connected = true;
							break;
						}
					if (connected){	
						for (Transition transition: getTransitionsForEvent(currentEvent))
							collectPlaces(placeSet, transition);
						changed = true;
						uncoveredEvents.remove(currentEvent);
						eventSet.add(currentEvent);
					}
				}
			} while (changed);
			regions.put("region"+regionNumber, eventSet);
		}
		if (setRegion)
			this.regions = regions;
		return regionNumber>1;
	}
	
	private boolean isConnected(ArrayList<String> placeSet, Transition transition){
		for (Predicate predicate: transition.getPrecondition())
			if (placeSet.contains(predicate.getName()))
					return true;
		for (Predicate predicate: transition.getPostcondition())
			if (placeSet.contains(predicate.getName()))
					return true;
		return false;
	}
	
	private void collectPlaces(ArrayList<String> placeSet, Transition transition){
		for (Predicate predicate: transition.getPrecondition())
			if (!placeSet.contains(predicate.getName()))
					placeSet.add(predicate.getName());
		for (Predicate predicate: transition.getPostcondition())
			if (!placeSet.contains(predicate.getName()))
					placeSet.add(predicate.getName());
	}
	
	private String toRegionsString(){
		String result ="";
		Enumeration<String> keys = regions.keys();
		while (keys.hasMoreElements()) {
			String regionName = (String)keys.nextElement();
			result += "\nREGION "+ regionName + "(" + listString(regions.get(regionName)) +")";
		}
		return result;
	}

	private String listString(ArrayList<String> list){
		String str="";
		if (list.size()==0)
			return str;
		for (String element: list) 
			str += element + ", ";
		return str.substring(0, str.length()-2);
	}
	
	// each unit test is of the form: [not] event(x1,..., xn)
	// not: dirty test
	// a place eventTest is added as a precondition and postcondition for each transition of event
	// eventTest will be execluded (i.e., hidden) from test code generation
	// a tuple eventTest(x1,..., xn) is added into each initial marking
	private String processUnitTestDataAndFindErrors(){
		String errorMessage = checkUnitTestDataForConsistence(); 
		if (errorMessage!=null)
			return errorMessage;	
		ArrayList<String> events = getEventNamesFromUnitTests();
		for (String event: events){
			String placeName = getPlaceNameForUnitTest(event);
			if (!places.contains(placeName))
				places.add(placeName);
			hidden.add(placeName);
			addUnitTestsToTransitionConditions(placeName, event);
		}
		for (Predicate unitTest: unitTests){
			String placeName = getPlaceNameForUnitTest(unitTest.getName());
			addUnitTestsToInitialState(placeName, unitTest.getArguments());
		}
		return null;
	}
	
	// Collect event names from unit tests 
	// There could be multiple unit tests for the same event
	private ArrayList<String> getEventNamesFromUnitTests(){
		ArrayList<String> events = new ArrayList<String>(); 
		for (Predicate unitTest: unitTests){
			String event = unitTest.getName();
			if (!events.contains(event))
				events.add(event);
		}
		return events;
	}
	
	// each unit test is of the form: [not] event(x1, ..., xn)
	// not represents dirty tests
	// event must be involved in transitions and have the same number of arguments 
	// xi cannot be variables
	private String checkUnitTestDataForConsistence(){
		for (Predicate unitTest: unitTests){
			String event = unitTest.getName();
			if (!events.contains(event))
				return event+": "+LocaleBundle.bundleString("Unit test name is not found"); 	// wrong name
			for (String argument: unitTest.getArguments())
				if (isVariable(argument))
					return "\""+unitTest+"\": "+LocaleBundle.bundleString("Unit test specification should not use variables");
			ArrayList<Transition> transitions = transitionsForEvents.get(event);
			for (Transition transition: transitions)
				if (unitTest.arity()!=transition.getNumberOfArguments())
					return "\""+unitTest+"\": "+LocaleBundle.bundleString("Incorrect number of arguments in unit test specification");
		}
		return null;
	}

	private void addUnitTestsToTransitionConditions(String placeName, String event){
		ArrayList<Transition> transitions = transitionsForEvents.get(event);
		for (Transition transition: transitions){
			ArrayList<String> arguments = transition.getArguments();
	    	if (transition.getArguments()==null)
	    		arguments = transition.getAllVariables();
			Predicate predicate = new Predicate(placeName, arguments);
			transition.getPrecondition().add(0, predicate);
			transition.getPostcondition().add(0, predicate);
		}
	}
	
	private void addUnitTestsToInitialState(String placeName, ArrayList<String> arguments){
		for (Marking marking: initialMarkings){
			Tuple tuple = TupleFactory.createTuple(arguments);
			marking.addTuple(placeName, tuple);
		} 
	}
	
	private String getPlaceNameForUnitTest(String eventName){
		return eventName + "Test";
	}

	private ArrayList<Predicate> getDirtyUnitTests(String event){
		ArrayList<Predicate> dirtyUnitTests = new ArrayList<Predicate>(); 
		for (Predicate unitTest: unitTests)
			if (unitTest.getName().equals(event) && unitTest.getNegation())
				dirtyUnitTests.add(unitTest);
		return dirtyUnitTests;
	}

	private Predicate selectUnitTest(ArrayList<Predicate> unitTests){
		if (unitTests.size()==1)
			return unitTests.get(0);
		Random generator = new Random();
		int randomIndex = generator.nextInt(unitTests.size());
		return unitTests.get(randomIndex);
	}
	
	public Substitution getDirtySubstitutionFromUnitTests(String event){
		ArrayList<Predicate> dirtyUnitTests = getDirtyUnitTests(event);
		if (dirtyUnitTests.size()!=0) {
			Predicate seletedDirtyUnitTest = selectUnitTest(dirtyUnitTests);
			return unifyUnitTest(findTransition(event), seletedDirtyUnitTest);
		}
		return null;
	}

	public ArrayList<Substitution> getCleanSubstitutionsFromUnitTests(String event){
		ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
		Transition transition = findTransition(event);
		for (Predicate unitTest: unitTests)
			if (unitTest.getName().equals(transition.getEvent()) && !unitTest.getNegation())
				substitutions.add(unifyUnitTest(transition, unitTest));
		return substitutions;
	}

	public Transition findTransition(String event){
		return transitionsForEvents.get(event).get(0);
	}
	
	private Substitution unifyUnitTest(Transition transition, Predicate unitTest){
		Hashtable <String, String> bindings = new Hashtable <String, String>();
		ArrayList<String> arguments = transition.getArguments();
		if (arguments == null)
			arguments = transition.getAllVariables();
		for (int i=0; i<arguments.size(); i++)
			bindings.put(arguments.get(i), unitTest.getArguments().get(i));
		return new Substitution(bindings);
	}
	
	private String toUnitTestDataString(){
		if (!hasUnitTests())
			return "";
		String result = "\n\nUNIT TESTS\n";
		result+= unitTests.get(0);
		for (int i=1; i<unitTests.size(); i++)
			result += ", "+unitTests.get(i);
		return result +"\n";
	}
	
	// get model-level objects/events/predicates for user to select from when editing MIM objects, methods, accessors, mutators	
	
	// list of possible model-level objects for object definitions in MIM
	public ArrayList<String> getListOfObjects(){
		ArrayList<String> objects = new ArrayList<String>();
		for (Marking initMarking: initialMarkings) {
		    ArrayList<String> places = initMarking.getPlaces();
		    for (String place: places)
		    	for (Tuple tuple: initMarking.getTuples(place)) 
		    		for (String argument: tuple.getArguments())
		    			if (!isVariable(argument) && !objects.contains(argument))
		    				objects.add(argument);
		}
		for (GoalProperty goalProperty: goalProperties) {
			for (Predicate predicate: goalProperty.getPrecondition()) {
	    		for (String argument: predicate.getArguments())
	    			if (!isVariable(argument) && !objects.contains(argument))
	    				objects.add(argument);
			}
		}
		for (Transition transition: transitions) {
			for (Predicate predicate: transition.getPrecondition()) {
	    		for (String argument: predicate.getArguments())
	    			if (!isVariable(argument) && !objects.contains(argument))
	    				objects.add(argument);
			}
			for (Predicate predicate: transition.getPostcondition()) {
	    		for (String argument: predicate.getArguments())
	    			if (!isVariable(argument) && !objects.contains(argument))
	    				objects.add(argument);
			}
		}
		if (objects.size()>1)
			Collections.sort(objects);
		return objects;
	}

	// list of possible model-level event signatures for method definitions in MIM
	public ArrayList<String> getListOfMethods(){
		ArrayList<String> methods = new ArrayList<String>();
		for (String event: events) {
			ArrayList<Transition> transitions = transitionsForEvents.get(event);
			if (transitions!=null && transitions.size()>0){
				methods.add(transitions.get(0).getEvent()+getPreferredMIMVariableList(transitions.get(0).getFormalParameters()));
			}
		}
		if (methods.size()>1)
			Collections.sort(methods);
		return methods;
	}
	
	private String getPreferredMIMVariableList(ArrayList<String> formalParameters){
		if (formalParameters==null || formalParameters.size()==0)
			return "";
		StringBuffer buffer = new StringBuffer("(");
		buffer.append(getPreferredMIMVariableName(formalParameters.get(0)));
		for (int i=1; i<formalParameters.size(); i++){
			buffer.append(", ");
			buffer.append(getPreferredMIMVariableName(formalParameters.get(i))); 
		}
		buffer.append(")"); 
		return buffer.toString(); 
	}
	
	private String getPreferredMIMVariableName(String var){
		return var.startsWith("?")? var: "?"+var;
	}
	
	// list of possible model-level predicates for accessor/mutator definitions in MIM
	public ArrayList<String> getListOfPredicates(){
		ArrayList<String> predicates = new ArrayList<String>();
		for (int index=0; index<places.size(); index++){
			if (arities[index]<=0)
				predicates.add(places.get(index));
			else 
			if (arities[index]==1)
				predicates.add(places.get(index)+"(?x)");
			else
			if (arities[index]==2)
				predicates.add(places.get(index)+"(?x, ?y)");
			else
			if (arities[index]==3)
				predicates.add(places.get(index)+"(?x, ?y, ?z)");
			else
				predicates.add(places.get(index)+"("+arities[index]+" "+LocaleBundle.bundleString("arguments")+")");
		}
		Collections.sort(predicates);
		return predicates;
	}
	
	// Part V : Methods for Checking for syntactic and semantic violations
	
	public String findErrors(){
		if (isThreatTreeModel())
			return findErrorsInThreatTreeModel();
	   	collectPlaces();	
	   	collectEvents();
	   	setTransitionsForEvents();
	   	String errorMessage; 
	   	if ((errorMessage=findErrorsInPredicateArguments())!=null)		
		   	return errorMessage;
	   	if ((errorMessage=findErrorsInTransitionArguments())!=null)		
		   	return errorMessage;
	   	if ((errorMessage=findErrorsInPredicatesInInitialMarkings())!=null)		
		   	return errorMessage;
	   	if ((errorMessage=findErrorsInGoalProperties())!=null)		
		   	return errorMessage;
	   	if ((errorMessage=findErrorsInMethodSpecification())!=null)		
		   	return errorMessage;
		if ((errorMessage=findErrorsInSinkEvents())!=null)
			return errorMessage;
		if ((errorMessage=findErrorsInNonNegativeEvents())!=null)
			return errorMessage;
		if ((errorMessage=processUnitTestDataAndFindErrors())!=null)
		   	return errorMessage;
		if ((errorMessage=findErrorsInOptions())!=null)
		   	return errorMessage;
		if ((errorMessage=findErrorsInRegions())!=null)
		   	return errorMessage;
		if ((errorMessage=findErrorsInReset())!=null)
		   	return errorMessage;
		setTransitionDeletePreconditions();	
		return null;		// no error found
	}
	
	private String findErrorsInThreatTreeModel(){
		if (getThreatTreeRoots().size()==0){
			return "Threat tree should have a root";
		}
		return null;	// no error;
	}
	
	private int[] arities;
	
	private String findErrorsInPredicateArguments(){
		arities = new int[places.size()];
		if (transitions.size()==0)
			return LocaleBundle.bundleString("Model is expected");
		for (int i=0; i<places.size(); i++) {
//System.out.println("\nPlace: " + places.get(i));
			arities[i] = -1;	
			for (Transition transition: transitions) {
				if (!checkConditions(arities, i, transition.getPrecondition()))
					return LocaleBundle.bundleString("Transition")+" "+transition.getEvent()+": "+LocaleBundle.bundleString("Precondition")+" "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent arguments");
				if (!checkConditions(arities, i, transition.getPostcondition()))
					return LocaleBundle.bundleString("Transition")+" "+transition.getEvent()+": "+LocaleBundle.bundleString("Postcondition")+" "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent arguments");
			}
			if (!checkInitialMarkings(arities, i, initialMarkings))
				return LocaleBundle.bundleString("Initial state")+": "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent number of arguments");
			for (GoalProperty propertyTransition: goalProperties)
				if (!checkConditions(arities, i, propertyTransition.getPrecondition()))
					return LocaleBundle.bundleString("Goal state")+" "+propertyTransition.getPropertyString()+": "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent arguments");
//			if (!checkMarkings(arities, i, goalMarkings))
//				return LocaleBundle.bundleString("Goal states")+": "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent number of arguments");
			if (!checkStates(arities, i, accessors))
				return LocaleBundle.bundleString("Accessors")+": "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent number of arguments");
			if (!checkStates(arities, i, mutators))
				return LocaleBundle.bundleString("Mutators")+": "+places.get(i)+" "+LocaleBundle.bundleString("has inconsistent number of arguments");
		}	
		return null;
	}

	private boolean checkConditions(int[] arities, int index, ArrayList<Predicate> conditions) {
		String place= places.get(index);
		for (Predicate p: conditions) {
			if (!p.getName().equalsIgnoreCase(place))
				continue;
			if (arities[index] ==-1)
				arities[index] = p.arity();
			else if (arities[index]!=p.arity()) {
//					System.out.println("\"" + place+"\" "+LocaleBundle.bundleString("has inconsistent number of arguments"));
					return false; 
			}
		}
		return true;
	}

	private boolean checkInitialMarkings(int[] arities, int index, ArrayList<Marking> markings) {
		for (Marking marking: markings){
			ArrayList<Tuple> tuples = marking.getTuples(places.get(index));
			if (tuples==null)
				continue;
			for (Tuple tuple: tuples) {
				if (arities[index]!=-1 && arities[index]!=tuple.arity()) {
					System.out.println(LocaleBundle.bundleString("Init state")+": "+places.get(index) + " "+LocaleBundle.bundleString("has inconsistent arity"));
					return false; 
				}
			}
		}
		return true;
	}

	private String findErrorsInPredicatesInInitialMarkings(){
		String result;
		for (Marking marking: initialMarkings)
			if ((result=checkPredicatesInMarking(marking))!=null)
				return LocaleBundle.bundleString("Init state")+" "+marking+": "+result+" "+LocaleBundle.bundleString("is undefined");
		return null;	// no problem
	}
	
	private String checkPredicatesInMarking(Marking marking){
		ArrayList<String> predicates = marking.getPlaces();
		for (String predicate: predicates)
			if (!places.contains(predicate))
				return predicate;
		return null;	
		
	}

	private String findErrorsInGoalProperties(){
		for (GoalProperty goalProperty: goalProperties)
			for (Predicate predicate: goalProperty.getPrecondition())
				if (!places.contains(predicate.getName()))
					return LocaleBundle.bundleString("Goal state")+" "+goalProperty.getPropertyString()+": "+predicate.getName()+" "+LocaleBundle.bundleString("is undefined");
		return null;	// no problem
	}

	private boolean checkStates(int[] arities, int index, ArrayList<Mapping> expressions){
		String place = places.get(index);
		for (Mapping declaration: expressions){
			Predicate predicate = declaration.getPredicate();
			if (place.equalsIgnoreCase(predicate.getName()) && arities[index]!=predicate.arity()) {
				System.out.println(LocaleBundle.bundleString("Accessors or mutators")+": "+ places.get(index) + " "+LocaleBundle.bundleString("has inconsistent arity"));
				return false; 
			}	
		}
		return true;
	}
	
	// Transitions for the same event should have the same number of arguments
	private String findErrorsInTransitionArguments(){
		for (String event: getEvents()){
			ArrayList<Transition> transitions = getTransitionsForEvent(event);
			int numberOfArguments = transitions.get(0).getNumberOfArguments();
			for (int i=1; i<transitions.size(); i++)
				if (transitions.get(i).getNumberOfArguments()!=numberOfArguments)
					return event+" "+LocaleBundle.bundleString("has inconsistent number of arguments");
		}
		return null;
	}
	
	private String findErrorsInMethodSpecification(){
		for (Mapping method: methods) {
			Predicate predicate = method.getPredicate(); 
			ArrayList<Transition> transitions = getTransitionsForEvent(predicate.getName());
			if (transitions == null)
				return LocaleBundle.bundleString("Method specification")+": "+ predicate.getName()+" "+LocaleBundle.bundleString("is undefined");
			int numberOfArguments = transitions.get(0).getNumberOfArguments();
			if (predicate.arity()!=numberOfArguments)
				return LocaleBundle.bundleString("Method specification")+": "+predicate+" "+"has inconsistent number of arguments";
		}
		return null;
	}
	
	private String findErrorsInOptions(){
		for (String place: options) {
			int placeIndex = places.indexOf(place);
			if (placeIndex<0)
				return LocaleBundle.bundleString("Options")+": "+place +" "+LocaleBundle.bundleString("is undefined in the model");
			if (arities[placeIndex]<1)
				return LocaleBundle.bundleString("Options")+": "+place +" "+LocaleBundle.bundleString("should have at least one parameter");
			if (!hasMutatorForOption(place))
				return LocaleBundle.bundleString("Options")+": "+place +" "+LocaleBundle.bundleString("needs a muator");
		}
		return null; 
	}
	
	private String findErrorsInRegions(){
		if (!hasRegions())
			return null;
		ArrayList<String> regionNames = new ArrayList<String>();
		ArrayList<String> regionEvents = new ArrayList<String>();
		Enumeration<String> keys = regions.keys();
		while (keys.hasMoreElements()) {
			String regionName = (String)keys.nextElement();
			if (regionNames.contains(regionName))
				return LocaleBundle.bundleString("Duplicate region name")+": "+regionName;
			else {
				regionNames.add(regionName);
			}
			ArrayList<String> eventList = regions.get(regionName);
			for (String event: eventList){
				if (!this.events.contains(event))
					return event+" "+LocaleBundle.bundleString("is undefined in the model");
/*				if (regionEvents.contains(event))
					return "Event "+event+" in multiple regions.";
				else
*/					regionEvents.add(event);
			}
		}
		if (regionEvents.size()!=this.events.size()){
			for (String event: events)
				if (!regionEvents.contains(event))
					return event+": "+LocaleBundle.bundleString("Region name is not specified");
		}
		return null;
	}

	private String findErrorsInReset(){
		for (Transition transition: transitions) {
			if (resetAppearsIn(transition.getPrecondition()))
				return transition.getEvent()+transition.printVariableList()+": "+LocaleBundle.bundleString("Precondition of transition should not use RESET");
			if (resetAppearsIn(transition.getEffect()))
				return transition.getEvent()+transition.printVariableList()+": "+LocaleBundle.bundleString("Effect of transition should not use RESET");
			if (resetAppearsIn(transition.getWhenCondition()))
				return transition.getEvent()+transition.printVariableList()+": "+LocaleBundle.bundleString("When condition of transition should not use RESET");
			String errorMessage = checkResetArgument(transition);
			if (errorMessage!=null)
				return errorMessage;
		}
		return null;
	}
	
	private boolean resetAppearsIn(ArrayList<Predicate> list){
		if (list!=null){
			for (Predicate predicate: list)
				if (predicate.getName().equalsIgnoreCase(RESET))
					return true;
		}
		return false;
	}
	
	private String checkResetArgument(Transition transition){
		ArrayList<Predicate> list = transition.getPostcondition();
		if (list!=null){
			for (Predicate predicate: list)
				if (predicate.getName().equalsIgnoreCase(RESET)){
					if (predicate.getNegation())
						return transition.getEvent()+transition.printVariableList()+": "+LocaleBundle.bundleString("Negation does not apply to RESET");			
					ArrayList<String> arguments = predicate.getArguments();
					if (arguments.size()!=1 || !MIDParser.isIdentifier(arguments.get(0)))
						return transition.getEvent()+transition.printVariableList()+": "+LocaleBundle.bundleString("Incorrect RESET argument");			
					if (!places.contains(arguments.get(0)))
						return transition.getEvent()+transition.printVariableList()+": "+LocaleBundle.bundleString("RESET argument is undefined");			
				}
		}
		return null;
	}

	public String checkAttackTransition(){
		for (Transition transition: transitions)
			if (transition.isAttackTransition())
				return null;
		return LocaleBundle.bundleString("Attack transition is not specified");	
	}
		
	// hierarchical nets
	private Hashtable<Integer, PipeTransition> istaTransitionsToPipeTransitions = new Hashtable<Integer, PipeTransition>();   

	public void putPipeTransition(Transition transition, PipeTransition pipeTransition){
		istaTransitionsToPipeTransitions.put(Integer.valueOf(transitions.indexOf(transition)), pipeTransition);
	}

	public PipeTransition getPipeTransition(Transition transition){
		return istaTransitionsToPipeTransitions.get(Integer.valueOf(transitions.indexOf(transition)));
	}
	
	// Part VI: toString methods
	
	public String toSinksString() {
		String str = "SINKS "; 
		for (String element: sinkEvents) 
			str += element + ", ";
		return str.substring(0, str.length()-2) +"\n\n";
	}

	public String toNonNegativeString() {
		String str = "NONNEGATIVE "; 
		for (String element: nonNegativeEvents) 
			str += element + ", ";
		return str.substring(0, str.length()-2) +"\n\n";
	}

	public String toMethodString() {
		String str = "";
		for (Mapping method: methods) {
			str += "\n"+method;
		}
		return "METHODS" +str + "\n\n";
	}

	public String toObjectString() {
		String str = ""; 
		Enumeration<String> keys = objects.keys();
		while (keys.hasMoreElements()) {
			String individual = keys.nextElement();
			String object = objects.get(individual); 
			if (object.startsWith("\""))
				str += "\n(" + individual + ", \"\\"+object.substring(0, object.length()-1)+"\\\"\")";
			else 
				str += "\n(" + individual + ", "+object+")";
		}
		return "OBJECTS "+str + "\n\n";
	}

	public String toAccessorString() {
		String str = "";
		for (Mapping accessor: accessors) {
			if (accessor.getOperator()!=null)
				str += "\n"+accessor;
		}
		return "ACCESSORS" +str + "\n\n";
	}

	public String toMutatorString() {
		String str = " ";
		for (Mapping mutator: mutators) {
			if (mutator.getOperator()!=null)
				str += "\n"+mutator;
		}
		return "MUTATORS " +str + "\n\n";
	}

	public String toOptionsString() {
		String str = "OPTIONS "; 
		for (String element: options) 
			str += element + ", ";
		return str.substring(0, str.length()-2) +"\n\n";
	}

	public String toHiddenPlacesAndEventsString() {
		String str = "HIDDEN "; 
		for (String element: hidden) 
			str += element + ", ";
		return str.substring(0, str.length()-2)+"\n\n";
	}

	public String toString(){
		StringBuffer str = new StringBuffer();
		
		// MODEL
		str.append("//PART I. MODEL \n\n");
		if (isThreatTreeModel())
			for (ThreatTreeNode threatTreeNode: threatTreeNodes){
				str.append(threatTreeNode);
				str.append("\n");	
			}
		else
			for (Transition transition: transitions){
				str.append(transition);	
				str.append("\n");	
			}
		str.append("\n");			
		
		for (Marking initialMarking: initialMarkings) 
			str.append("INIT " + initialMarking + "\n\n");

		for (GoalProperty propertyTransition: goalProperties){ 
			str.append("GOAL " + propertyTransition.getPropertyString());
			if (Kernel.IS_DEBUGGING_MODE)
				str.append("\n"+propertyTransition.toString());
			str.append("\n\n");
		}

		for (AssertionProperty assertion: assertionProperties){ 
			str.append(assertion.toString());
			str.append("\n\n");
		}

		if (sequencesFile!=null && !sequencesFile.equals(""))
			str.append("SEQUENCES " + sequencesFile+"\n\n");

		if (hasUnitTests())
			str.append(toUnitTestDataString());

		if (hasSinkEvents())
			str.append(toSinksString());

		if (hasNonNegativEvents())
			str.append(toNonNegativeString());

		// MIM

		str.append("\n\n//PART II. MIM \n\n");
		
		if (systemName!=null)
			str.append("System " + systemName+"\n\n");
		
		if (objects.size()>0)
			str.append(toObjectString());

		if (methods.size()>0)
			str.append(toMethodString());

		if (accessors.size()>0)
			str.append(toAccessorString());
		if (mutators.size()>0)
			str.append(toMutatorString());
		if (options.size()>0)
			str.append(toOptionsString());
		if (hidden.size()>0)
			str.append(toHiddenPlacesAndEventsString());
		if (parameters.size()>0)
			str.append(toParametersString());
		if (regions.size()>0)
			str.append(toRegionsString());

		str.append("\n\n//PART III. HELPER CODE \n\n");

		// IMPLEMENTATION CODE
		if (hasPackageBlock())
			str.append("\n"+getPackageBlock());

		if (hasImportBlock())
			str.append("\n"+getImportBlock());

		if (hasAlphaBlock())
			str.append("\nALPHA/BEGIN:\n"+getAlphaBlock()+"\n");

		if (hasOmegaBlock())
			str.append("\nOMEGA/END:\n"+getOmegaBlock()+"\n");

		if (hasSetUpCode())
			str.append("\nSETUP:\n"+getSetUpCode());

		if (hasTearDownCode())
			str.append("\nTEARDOWN:\n"+getTearDownCode());
		
		if (hasHelperCode())
			str.append("\n"+getHelperCode());

		return str.toString();
	}
}
