package testcode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import kernel.SystemOptions;
import locales.LocaleBundle;

import parser.MIDParser;
import parser.ParseException;

import edit.GeneralEditor;

import mid.MID;
import mid.Mapping;
import mid.Marking;
import mid.Predicate;
import mid.Substitution;
import mid.Transition;
import mid.Tuple;

public class TestCodeGeneratorOnline {
	
	private GeneralEditor editor;
	private MID mid;
	private OnlineEngineInterface engine;
	
	public TestCodeGeneratorOnline(GeneralEditor editor, MID mid){
		this.editor=editor;
		this.mid = mid;
		try {
			URL serverURL = new URL(mid.getSystemName());
			if (editor.getKernel().getSystemOptions().getLanguage() == TargetLanguage.RPC){
				switch (editor.getKernel().getSystemOptions().getTestFrameworkIndex()){
					case TargetLanguage.JSONRPC: engine = new OnlineEngineRPCJSON(serverURL); break;
					case TargetLanguage.XMLRPC: engine = new OnlineEngineRPCApache(serverURL); break;
				}
			}
			else {
				engine = new OnlineEngineSelenium(mid.getSystemName(), editor);
			}
		} catch (MalformedURLException e) {
		}
	}
	
	public boolean hasTestEngine(){
		return engine.hasEngine();
	}
	
	public void executeSetUp(Marking marking) throws Exception {
		if (mid.hasSetUpCode()){
			ArrayList<Predicate> setUpRPCs = MIDParser.parseRPCString(mid.getSetUpCode());
			for (Predicate rpc: setUpRPCs){
//			editor.printInConsoleArea(rpc.toString(), false);
				engine.executeMethod(rpc);
			}
		} else { // generate setup code
			for (String place: marking.getPlaces())
				if (!mid.isHidden(place)) {
					for (Tuple tuple: marking.getTuples(place)) 
						executeMutator(place, tuple);
				}
		}
	}

	public void executeTearDown() throws Exception {
		if (mid.hasTearDownCode()){
			ArrayList<Predicate> setUpRPCs = MIDParser.parseRPCString(mid.getTearDownCode());
			for (Predicate rpc: setUpRPCs){
				engine.executeMethod(rpc);
			}
		}
	}

	public void executeTestInput(Transition transition, Substitution substitution) throws Exception{
		executeOptionSetup(transition, substitution);
		for (Predicate rpc: getTestInputRPCs(transition, substitution)){
			engine.executeMethod(rpc);
		}
	}
	
	public Transition executeNondeterministicTestOracles(Marking sourceMarking, Transition transition, Substitution substitution, Marking destMarking, ArrayList<Transition> nonDeterministicTransitions) throws Exception {
		// deterministic oracles
		if (checkTestOracles(transition, substitution, destMarking))
			return transition;
		// non-deterministic oracles
		for (Transition nonDeterministicTransition: nonDeterministicTransitions) {
			Marking nonDeterministicMarking = mid.fireTransition(sourceMarking, nonDeterministicTransition, substitution);
			if (checkTestOracles(transition, substitution, nonDeterministicMarking)) {
				editor.printInConsoleArea("Nondeterminism found.", false);
				return nonDeterministicTransition;
			}
		}
		return null;
//		throw new Exception(PrTEngine.getActualParameterList(transition, substitution)+": "+LocaleBundle.bundleString("ON_THE_FLY_TESTING_FAILED"));
	}

	private void executeOptionSetup(Transition transition, Substitution substitution) throws Exception {
		ArrayList<Predicate> precondition = transition.getPrecondition();
		if (precondition!=null) {
			for (Predicate input: precondition){
				String place = input.getName();
				if (mid.isOption(place)) {
					Tuple tuple = substitution.substitute(input);
					executeMutator(place, tuple);
				}
			}
		}
	}

	private ArrayList<Predicate> getTestInputRPCs(Transition transition, Substitution substitution) throws ParseException{
		String event = transition.getEvent();
		if (substitution!=null && substitution.hasBindings()){
			ArrayList<String> actualParameters = getActualParameters(transition, substitution);
			if (mid.getMethod(event)==null) { // action is not specified
				ArrayList<Predicate> rpcs = new ArrayList<Predicate>();
				rpcs.add(new Predicate(event, actualParameters));
				return rpcs;
			}
			else 
				return getTestInputWithSubstitution(event, new Tuple(actualParameters));
		}		
		else {
			Mapping method = mid.getMethod(event);
			if (method != null)
				return MIDParser.parseRPCString(method.getOperator().trim());
			else {
				ArrayList<Predicate> rpcs = new ArrayList<Predicate>();
				rpcs.add(new Predicate(event, new ArrayList<String>()));
				return rpcs;
			}
		}
	}

	private ArrayList<Predicate> getTestInputWithSubstitution(String event, Tuple tuple) throws ParseException{
		Mapping method = mid.getMethod(event, tuple);
		if (method!=null){
			return MIDParser.parseRPCString(method.getOperator());
		}
		if (tuple.arity()>0){
			method = mid.getMethodWithVariables(event);
			if (method!=null){
				Substitution substitution = method.getPredicate().unify(tuple);
				if (substitution!=null) 
					return MIDParser.parseRPCString(substitution.substitute(method.getOperator()));
			}
		}
		ArrayList<Predicate> rpcs = new ArrayList<Predicate>();
		rpcs.add(new Predicate(event, tuple.getArguments()));
		return rpcs;
	}
	
	private ArrayList<String> getActualParameters(Transition transition, Substitution substitution) {
		ArrayList<String> actualParameters = new ArrayList<String>();
		ArrayList<String> formalParameters = transition.getArguments();
		if (formalParameters==null)
			formalParameters = transition.getAllVariables();
		for (String variable: formalParameters) {
			String value = substitution.getBinding(variable);
			String object = variable;
			if (value!=null) { 
				object = mid.getObject(value);
				if (object==null)
					object = value;
			}
			actualParameters.add(object); 
		}
		return actualParameters; 
	}
	
	// test oracle part
	private final boolean needNegation = true;
	
	public boolean checkTestOracles(Transition transition, Substitution substitution, Marking marking) throws Exception{
		boolean pass = true;
		SystemOptions systemOptions = editor.getKernel().getSystemOptions();
		if (systemOptions.verifyPostconditions())
			pass = verifyConditions(substitution, transition.getPostcondition(), !needNegation);
		else if (systemOptions.verifyMarkings())
			pass = verifyMarking(marking);
		if (!pass)
			return false;
		if (systemOptions.verifyNegatedConditions())
			pass = verifyConditions(substitution, transition.getDeletePrecondition(), needNegation);
		if (systemOptions.verifyEffects())
			pass = verifyConditions(substitution, transition.getEffect(), !needNegation);
		return pass;
	}

	private boolean verifyMarking(Marking marking) throws Exception {
		for (String place: mid.getPlaces()) {
			if (!mid.isHidden(place) && marking.hasTuples(place)) {
				ArrayList<Tuple> tuples = marking.getTuples(place);
				for (Tuple tuple: tuples) {
					if (!executeAccessor(place, tuple, !needNegation))
						return false;
				}
			}
		}
		return true;
	}
	
	private boolean verifyConditions(Substitution substitution, ArrayList<Predicate> conditions, boolean needNegation) throws Exception {
		if (conditions==null || substitution==null)
			return true;
		for (Predicate predicate: conditions) {
			String place = predicate.getName();
			if (!mid.isHidden(place)){
				Tuple tuple = substitution.substitute(predicate);
				if (!executeAccessor(place, tuple, needNegation))
					return false;
			}	
		}
		return true;
	}

	private boolean executeAccessor(String place, Tuple tuple, boolean needNegation) throws Exception{
		String accessorCode = "";  
		Mapping accessor = mid.getAccessor(place, tuple);
		if (accessor!=null)
			accessorCode = accessor.getOperator(); 
		else if (tuple.arity()>0){
			accessor = mid.getAccessorWithVariables(place);
			if (accessor!=null){
				Substitution substitution = accessor.getPredicate().unify(tuple);
				if (substitution!=null) {
					substitution = mid.substituteForObjects(substitution);
					accessorCode = substitution.substitute(accessor.getOperator());
				}
			}
		}
		// No accessor code found
		if (accessorCode.equals(""))
			accessorCode = tuple.arity()!=0? place+tuple: place;		
		ArrayList<Predicate> rpcs = MIDParser.parseRPCString(accessorCode);
		for (Predicate rpc: rpcs) {
			boolean pass = needNegation? !engine.executeQuery(rpc): engine.executeQuery(rpc); 
			if (!pass) {
				editor.printInConsoleArea(rpc+" "+LocaleBundle.bundleString("is false"), false);		
				return false;
			}
		}
		return true;
	}

	private void executeMutator(String place, Tuple tuple) throws Exception{
		String mutatorCode = "";
		Mapping mutator = mid.getMutator(place, tuple);
		if (mutator!=null)
			mutatorCode = mutator.getOperator(); 
		else if (tuple.arity()>0){
			mutator = mid.getMutatorWithVariables(place);
			if (mutator!=null){
				Substitution substitution = mutator.getPredicate().unify(tuple);
				if (substitution!=null) {
					substitution = mid.substituteForObjects(substitution);
					mutatorCode = substitution.substitute(mutator.getOperator());
				}
			}
		}
		if (!mutatorCode.equals("")){
			ArrayList<Predicate> rpcs = MIDParser.parseRPCString(mutatorCode);
			for (Predicate rpc: rpcs) {
				engine.executeQuery(rpc); 
			}
		}
	}	
	
	public void terminate(){
		engine.terminate();
	}
}
