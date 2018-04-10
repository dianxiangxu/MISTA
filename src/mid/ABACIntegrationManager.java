package mid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mid.ABACRule.EffectType;
import parser.MIDParser;
import parser.ParseException;

/**
 * @author Samer Khamaiseh
 * 
 * This class integrates the ABAC rules with Functional model by updating the MID object. 
 * For each transition we do the following
 * 1- collect all the rules that related to the transition.
 * 2- Define the rule effect 
 * 3- if the rule effect == permit >> we add places to the functional model transition that represent the ABAC attribute and add all the 
 * ABAC attribute to the guard condition and update the initial marking.
 * 4- if the rule effect == deny >> we add a new transition that share all precondition with related functional model transition, and add the ABAC attribute to the 
 * new created transition guard condition. 
 * 5- Obligation post user obligation is handled too
 *
 */
public class ABACIntegrationManager {

	private MID mid;
	//private HashMap<String, String> initialMarkingValues;
	private ArrayList<Transition> ABACDenyTransitions;
    private ArrayList<Predicate> ABACNewPredicates ;
    public static final String ABAC_TRANSITION_PREFIX = "ABACTRANSITION";
    private final String ABAC_PREDICATE_PREFIX = "ABAC";
    private  static boolean isABACModelType = false;

	public ABACIntegrationManager(MID mid) {
		this.mid = mid;
		//initialMarkingValues = new HashMap<String, String>();
		ABACDenyTransitions = new ArrayList<Transition>();
		ABACNewPredicates =  new ArrayList<Predicate>();
	}

	
	/**
	 * @throws ParseException
	 * Integrate the ABAC model
	 */
	public void integrateABACRulesWithFunctionalModel() throws ParseException {

		if (mid.getRules() == null || mid.getRules().isEmpty()
				|| mid.getAttributes() == null || mid.getAttributes().isEmpty()) {
			return;
		}

		ArrayList<Transition> transitions = new ArrayList<Transition>();
		transitions.addAll(mid.getTransitions());
		for (Iterator<Transition> iterator = transitions.iterator(); iterator.hasNext();) {
			Transition transition = iterator.next();
			integrateABACRuleWithTranstion(transition);
			UpdateABACDenyTransitionsInputPlacesAfterPermitRulesEffect(transition);
			ABACNewPredicates.clear();
		}
		
	}

	/**
	 * @author Samer Khamaiseh
	 * @param fModeltransition
	 * @throws ParseException
	 * 
	 *             This method created to update the input places of the
	 *             transitions created by ABAC rules. Because Permit effect
	 *             update the input places for functional model and these input
	 *             places should be added to the already deny transitions
	 *             created before the permit rule.
	 */
	private void UpdateABACDenyTransitionsInputPlacesAfterPermitRulesEffect(
			Transition fModeltransition) throws ParseException {

		if (ABACDenyTransitions.isEmpty()) {
			return;
		}

		ArrayList<Predicate> fModelTransitionPrecondition = new ArrayList<Predicate>();
		fModelTransitionPrecondition.addAll(fModeltransition.getPrecondition());

		for (int i = 0; i < ABACDenyTransitions.size(); i++) {

			Transition ABACTransition = ABACDenyTransitions.get(i);
			if (ABACDenyTransitions.get(i).getPrecondition().size() == fModelTransitionPrecondition
					.size()) {
				continue;
			}

			ABACTransition.setPrecondition(fModelTransitionPrecondition);
			ABACTransition.setPostcondition(fModelTransitionPrecondition);
			ABACTransition.collectAllVariables();
			MIDParser.checkPostconditionVariables(ABACTransition);
			updateTransitionParametersInMIM(ABACTransition);
		}
		
		ABACDenyTransitions.clear();

	}

	
	/**
	 * Integrate the ABAC rules for each transition
	 * @param fModeltransition
	 * @throws ParseException
	 */
	private void integrateABACRuleWithTranstion(Transition fModeltransition)
			throws ParseException {

		String fModelTransitionName = fModeltransition.getEvent();
		for (Iterator<ABACRule> iterator = mid.getRules().iterator(); iterator.hasNext();) {
			ABACRule rule = iterator.next();
			for (Iterator<Predicate> iter = rule.getActionCondition()
					.iterator(); iter.hasNext();) {
				Predicate actionCondition = iter.next();

				if (!fModelTransitionName.equalsIgnoreCase(actionCondition
						.getName().trim())) {
					continue;
				}

				if (EffectType.PERMIT.equals(rule.getEffect())) {
					addPremitEffectToFunctionalModel(fModeltransition, rule);
				}

				if (EffectType.DENY.equals(rule.getEffect())) {
					addDenyRuleEffectToTheFunctionalModel(fModeltransition,
							rule);

				}
			}
		}
	}


	/**
	 * 
	 * @param fModelTransition
	 * @param rule
	 * @throws ParseException
	 */
	private void addDenyRuleEffectToTheFunctionalModel(Transition fModelTransition, ABACRule rule) throws ParseException {

		String transitionName = ABAC_TRANSITION_PREFIX + fModelTransition.getEvent();
		Transition ABACtransition = new Transition(transitionName);
		AddBidirectionalArc(fModelTransition.getPrecondition(), ABACtransition);
		AddTransitionWhenCondition(rule.getSubjectCondition(), ABACtransition);
		AddTransitionWhenCondition(rule.getResourceCondition(), ABACtransition);
		AddTransitionWhenCondition(rule.getEnvironmentCondition(), ABACtransition);
		addObligationConditionToTransition(ABACtransition, rule);
		mid.addTransition(ABACtransition);
		ABACDenyTransitions.add(ABACtransition);
		updateTransitionParametersInMIM(ABACtransition);
	}


	/**
	 * 
	 * @param transitions
	 */
	private void updateTransitionParametersInMIM(Transition transitions) {

		ArrayList<String> aurgments = new ArrayList<String>();

		for (Iterator<Predicate> iterator = transitions.getPrecondition().iterator(); iterator.hasNext();) {
			Predicate predicate = iterator.next();
			aurgments.addAll(predicate.getArguments());
		}

		Mapping transitionMethod = mid.getMethod(transitions.getEvent());
		if (transitionMethod == null && transitions.getEvent().contains(ABAC_TRANSITION_PREFIX)) {
			Predicate ABAC_MIM_Transition = new Predicate(transitions.getEvent(), aurgments);
			String ABACTransitionCMD = new String(" ");
			mid.addMethod(new Mapping(ABAC_MIM_Transition, ABACTransitionCMD));
		}

		if (transitionMethod != null) {
			Predicate predicate = transitionMethod.getPredicate();
			
			if(aurgments.size() == predicate.getArguments().size()){
				return;
			}
			predicate.setAurgment(aurgments);
//			String operator = transitionMethod.getOperator();
		}

	}


	/**
	 * @author Samer Khamaiseh
	 * @param transition
	 * @param rule
	 * @throws ParseException
	 * 
	 * Reflect the Permit rule effect to the Functional model
	 */
	private void addPremitEffectToFunctionalModel(Transition transition,ABACRule rule) throws ParseException {

		setRuleConditionsAttributesPredicates(rule.getSubjectCondition(), rule.getRuleNumber());
		setRuleConditionsAttributesPredicates(rule.getEnvironmentCondition(),rule.getRuleNumber());
		setRuleConditionsAttributesPredicates(rule.getResourceCondition(),rule.getRuleNumber());
         
		AddBidirectionalArc(this.ABACNewPredicates, transition);

		AddTransitionWhenCondition(rule.getSubjectCondition(), transition);
		AddTransitionWhenCondition(rule.getEnvironmentCondition(), transition);
		AddTransitionWhenCondition(rule.getResourceCondition(), transition);
        addObligationConditionToTransition(transition,rule);
		addInitialMarking(this.ABACNewPredicates);
		updateTransitionParametersInMIM(transition);
		
		
	}

	

	/**
	 * Add the rule obligation to the transition effect
	 * @param transition
	 */
	private void addObligationConditionToTransition(Transition transition,ABACRule rule) {
		
		if(rule.getObligtions() == null || rule.getObligtions().isEmpty()){
			return;
		}
		ArrayList<Predicate> alreadyExistEffect = new ArrayList<Predicate>();
		if(transition.getEffect() != null){
			 alreadyExistEffect.addAll(transition.getEffect());
		}
		
		Predicate obligationPredicate = rule.getObligtions().get(0);
		alreadyExistEffect.add(obligationPredicate);
		transition.setEffect(alreadyExistEffect);
		Mapping accessorMapping = new Mapping(obligationPredicate, "ABACRule"+rule.getRuleNumber()+"_"+obligationPredicate.getName().trim()+"_ObligationEffect()");
		mid.addAccessor(accessorMapping);
		
	}


	/**
	 * @param newPredicates
	 * 
	 *            Update the initial marking M0
	 */
	private void addInitialMarking(ArrayList<Predicate> newPredicates) {

		for (Iterator<Predicate> iterator = newPredicates.iterator(); iterator
				.hasNext();) {
			Predicate predicate = iterator.next();
			List<String> initialMarkingAttaributeValue = getABACInitialMarkingPredicates(predicate);

			for (Iterator<String> iter = initialMarkingAttaributeValue
					.iterator(); iter.hasNext();) {
				String value = iter.next();
				Marking marking = new Marking();
				ArrayList<String> arg = new ArrayList<String>();
				arg.add(value);
				// arg.add(initialMarkingValues.get(predicate.getName()));
				marking.addTuple(predicate.getName(), TupleFactory.createTuple(arg));
				mid.addPlace(predicate.getName());
				mid.getInitialMarkings().get(0).merge(marking);
			}
		}
	}


	/**
	 * 
	 * @param predicate
	 * @return
	 */
	private List<String> getABACInitialMarkingPredicates(Predicate predicate) {
		
		String attributeName = predicate.getArguments().get(0);
		ABACAttribute conditionAttribut = mid.getABACAttributeByName(attributeName);
		List<String> initialMarkingValuesMap = new ArrayList<String>();
		String[] attributeValues = conditionAttribut.getValues();
		
		if (conditionAttribut.getType().equalsIgnoreCase("String") || conditionAttribut.getType().equalsIgnoreCase("boolean")) {
			ArrayList<String> updatedAttributeValues = new ArrayList<String>();
			 for (String value : attributeValues) {
				 updatedAttributeValues.add("\"" + value + "\"");
			}
			 initialMarkingValuesMap.addAll(updatedAttributeValues);
		}
		
		if(conditionAttribut.getType().equalsIgnoreCase("integer")){
			//ArrayList<Integer> attributeIntegerValues = new ArrayList<Integer>();
		/*	for (String value : attributeValues) {
				attributeIntegerValues.add(Integer.valueOf(value));
			}*/
			initialMarkingValuesMap.addAll(Arrays.asList(attributeValues));
		}
		return initialMarkingValuesMap;
	}

	/**
	 * @param newPredicates
	 * @param transition
	 * @throws ParseException
	 * 
	 * Add bi-directional between the transition and predicates
	 */
	private void AddBidirectionalArc(ArrayList<Predicate> newPredicates,
			Transition transition) throws ParseException {

		ArrayList<Predicate> alreadyExistPrecondition = new ArrayList<Predicate>();
		if (transition.getPrecondition() != null) {
			alreadyExistPrecondition.addAll(transition.getPrecondition());
		}
		alreadyExistPrecondition.addAll(newPredicates);
		transition.setPrecondition(alreadyExistPrecondition);

		ArrayList<Predicate> alreadyExistPostCondition = new ArrayList<Predicate>();
		if (transition.getPostcondition() != null) {
			alreadyExistPostCondition.addAll(transition.getPostcondition());
		}
		alreadyExistPostCondition.addAll(newPredicates);
		transition.setPostcondition(alreadyExistPostCondition);

		transition.collectAllVariables();
		// to validate the post condition variable
		MIDParser.checkPostconditionVariables(transition);
	}

	
	/**
	 * @param rule
	 * @param transition
	 * 
	 * Add guard condition to the transition
	 */
	private void AddTransitionWhenCondition(ArrayList<Predicate> whenConditionsAttributes, Transition transition) {

		ArrayList<Predicate> alredayExistWhenCondtion = new ArrayList<Predicate>();
		if (transition.getWhenCondition() != null) {
			alredayExistWhenCondtion.addAll(transition.getWhenCondition());
		}
		
		for (Iterator<Predicate> iterator = whenConditionsAttributes
				.iterator(); iterator.hasNext();) {
			Predicate condition = iterator.next();
            
			// get the attribute type 
			String attributeName = condition.getArguments().get(0);
			ABACAttribute conditionAttribut = mid.getABACAttributeByName(attributeName);
		//	ArrayList<String> whenPredicatAurgment = new ArrayList<String>();
			
			if(conditionAttribut.getType().equalsIgnoreCase("String")|| conditionAttribut.getType().equalsIgnoreCase("boolean")){
				String attributeValue = condition.getArguments().get(1);
				condition.getArguments().remove(1);
				condition.getArguments().add(1, "\""+attributeValue+"\"");
			}
			
			Predicate whenPredicate = null;
			
			 if(condition.getNegation()){
				 whenPredicate = new Predicate(Functions.EQUALS,condition.getArguments(),true);
			 }else{
				 whenPredicate = new Predicate(Functions.EQUALS,condition.getArguments());
			 }
			alredayExistWhenCondtion.add(whenPredicate);
		}
		transition.setWhenCondition(alredayExistWhenCondtion);

	}

	/**
	 * 
	 * @param transition
	 * @param ruleAttribute
	 * @return
	 */
	private void setRuleConditionsAttributesPredicates(ArrayList<Predicate> ruleAttribute,Integer ruleNumber) {

		//ArrayList<Predicate> ruleAttributesConditionsAsPredicates = new ArrayList<Predicate>();
		for (Iterator<Predicate> iterator = ruleAttribute.iterator(); iterator.hasNext();) {
			Predicate ruleCondition = iterator.next();
			ArrayList<String> predicateAurgment = new ArrayList<String>();
		
			String attributeConditionName = ruleCondition.getArguments().get(0).trim();
			predicateAurgment.add(attributeConditionName);
			String PredicateName = ABAC_PREDICATE_PREFIX +ruleNumber.intValue()+ attributeConditionName;
         
		//	initialMarkingValues.put(PredicateName, ruleCondition.getArguments().get(1).trim());
			Predicate predicate = new Predicate(PredicateName,predicateAurgment);
			this.ABACNewPredicates.add(predicate);
		}
		
	
	}


	/**
	 * 
	 * @param b
	 */
	public  void setABACModelType(boolean modelType) {
		 isABACModelType = modelType;
		
	}
	
	public static boolean isABACModelType(){
		return isABACModelType;
	}


	/**
	 * 
	 * @param ruleCondition
	 * @param PredicateName
	 *//*
	private void prepareInitialMarkingTokens(Predicate ruleCondition,
			String PredicateName) {
		String attributeName = ruleCondition.getArguments().get(0);
		ABACAttribute conditionAttribut = mid.getABACAttributeByName(attributeName);
		if(conditionAttribut.getType().equalsIgnoreCase("String")){
			String attributeValue = ruleCondition.getArguments().get(1);
			ruleCondition.getArguments().remove(1);
			ruleCondition.getArguments().add(1, "\""+attributeValue+"\"");
		}
		
		initialMarkingValues.put(PredicateName, ruleCondition.getArguments().get(1).trim());
	}*/

	
}
