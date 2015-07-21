package edit;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import edit.GeneralEditor.SimulatorType;

import locales.LocaleBundle;
import mid.AssertionProperty;
import mid.ExcelTestDataLoader;
import mid.MID;
import mid.Marking;
import mid.Predicate;
import mid.GoalProperty;
import mid.Transition;
import mid.Tuple;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;
import pipeprt.dataLayer.AnnotationNote;
import pipeprt.dataLayer.Arc;
import pipeprt.dataLayer.BidirectionalArc;
import pipeprt.dataLayer.DataLayerInterface;
import pipeprt.dataLayer.InhibitorArc;
import pipeprt.dataLayer.NormalArc;
import pipeprt.dataLayer.PipePlace;
import pipeprt.dataLayer.PipeTransition;
import pipeprt.gui.CreateGui;
import pipeprt.gui.PrTPanel;
import simulation.PrTEngine;
import simulation.PrTOnlineTester;
import simulation.PrTSimulator;
import utilities.FileUtil;

public class ModelPanelVisualNet extends ModelPanel implements VisualModelInterface {
	private static final long serialVersionUID = 1L;
	
	private PrTPanel mainNet;
	
	public ModelPanelVisualNet(XMIDEditor editor, File modelFile) {
		super(editor);
		createModelPanel(modelFile);
	}
	
	private void createModelPanel(File modelFile){
		removeAll();
		mainNet = CreateGui.createPrTPanel(editor.kernel.getParentFrame(), modelFile, editor.isEditable);
	    setLayout(new BorderLayout());
	    add(mainNet, BorderLayout.CENTER);
	}

	@Override
	public JMenu getModelMenu() {
		return editor.createModelMenu(mainNet.getPrTMenu());
	}

	@Override
	public void parse(MID mid) throws ParseException {
		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled() &&
			editor.hasForErrorsInSubModelFileHierarchy())
			throw new ParseException(LocaleBundle.bundleString("Incorrect hierarchy"));
		parseConstantsAnnotations();			// named integers which may appear in guard conditions of transitions
		parsePlaces(mid);						// including the default initial marking
		parseTransitions(mid, mainNet);			// including arcs
		parseMarkingAnnotations(mid);			// initial states
		parseDataAnnotations(mid);				// initial states from data files
		parseGoalPropertyAnnotations(mid);		// goal properties (existential)	
		parseAssertionPropertyAnnotations(mid);	// assertion properties (universal)
		parseOtherAnnotations(mid);				// sink events, test parameters, sequence file
		checkForDuplicateEvents();
	}
	
	public boolean isModelChanged(){
		return mainNet.isNetChanged() || editor.areSubModelsChanged();
	}
	
	public void saveModel(File xmidFile, Sheet sheet, CellStyle lineWrapStyle){
		int rowIndex = saveModelHeader(sheet, lineWrapStyle);
		String separateModelFileName = FileUtil.getDefaultSeparateModelFileName(xmidFile);
		File separateModelFile = new File(xmidFile.getParent()+File.separator+separateModelFileName);
		rowIndex = XMIDProcessor.createTableModelTypeRow(editor.getModelType(), separateModelFileName, sheet, rowIndex);
		mainNet.setFile(separateModelFile);
	    mainNet.saveNet();
	}

	public PrTPanel getMainNet(){
		return mainNet;
	}
	
	@Override
	public PrTEngine createSimulator(MID mid, SimulatorType simulatorType){
		try {
			if (simulatorType==SimulatorType.ONLINE_TEST_EXECUTION)
				return new PrTOnlineTester(editor, mainNet, editor.getTransitionTree());	   	
			else
				return new PrTSimulator(editor, mainNet, mid, simulatorType);	 
			}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public JToolBar getAdditionalToolBar(){
		return mainNet.getPaletteToolBar();
	}
	
	@Override
	public void updateFont() {

	}

	private void checkDuplicateTokens(ArrayList<Tuple> tokenList, String placeName, String tokenString) throws ParseException{
		for (int i=0; i<tokenList.size(); i++)
			for (int j=i+1; j<tokenList.size(); j++)
				if (tokenList.get(i)==tokenList.get(j))
					throw new ParseException(placeName+" - "+tokenString+": "+LocaleBundle.bundleString("Duplicate tokens"));
				
	}
	
	private void parseTokens(Marking marking, PipePlace place) throws ParseException{
        String activeTokenClassID = CreateGui.getModel().getActiveTokenClassID();
        DataLayerInterface net = mainNet.getModel();
        int pos = net.getPosInList(activeTokenClassID, place.getCurrentMarking());
        if (pos >= 0) {
      		String tokenString = place.getCurrentMarking().get(pos).getCurrentMarking().trim();
      		if (tokenString!=null && !tokenString.equals("")){
      			ArrayList<Tuple> tokenList = new ArrayList<Tuple>();
      			try {
      				tokenList = MIDParser.parseTokenString(tokenString);
      			}
      			catch (ParseException e){
      				throw new ParseException(place.getName()+": "+tokenString+". "+e.toString());
      			}
      			if (tokenList.size()>0){
      				// check arity consistency
      				int firstArity = tokenList.get(0).arity();
      				for (int index=1; index<tokenList.size(); index++)
      					if (tokenList.get(0).arity()!=firstArity)
      						throw new ParseException(place.getName()+" "+LocaleBundle.bundleString("has inconsistent token length"));
      				// check duplication
      				checkDuplicateTokens(tokenList, place.getName(), tokenString);
      			}
       			marking.addTuples(place.getName(), tokenList);
      		}
        }
	}
	
	private String modelNameMessage(PrTPanel netModel){
		return editor.getSubModels().size()>0? "["+netModel.getFile().getName()+"] ": "";
	}

	private Hashtable <PrTPanel, Marking> subModelNonAnnotationInitialMarkings = new Hashtable <PrTPanel, Marking>();  
	private void parsePlaces(MID mid) throws ParseException{
		Marking initialMarking = parsePlaces(mid, mainNet);
        if (initialMarking.getPlaces().size()>0)
        	mid.addInitialMarking(initialMarking);
        if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
        	for (JPanel subModel: editor.getSubModels())
        		if (subModel instanceof PrTPanel){
        			Marking subInitMarking = parsePlaces(mid, (PrTPanel)subModel);
        			if (subInitMarking.getPlaces().size()>0)
        				subModelNonAnnotationInitialMarkings.put((PrTPanel)subModel, subInitMarking);
        		}
        }
	}
	
	private Marking parsePlaces(MID mid, PrTPanel netModel) throws ParseException{
        DataLayerInterface net = netModel.getModel();
 		Marking initialMarking = new Marking();
		PipePlace[] places = net.getPlaces();
        for (int i = 0; i < places.length; i++){
//System.out.println("Place ID: "+places[i].getId()+" Name: "+places[i].getName());        	
        	String placeName = places[i].getName();
        	if (!MIDParser.isIdentifier(placeName))
        		throw new ParseException(modelNameMessage(netModel)+placeName+" - "+LocaleBundle.bundleString("should start with a letter"));
        	if (!places[i].getConnectFromIterator().hasNext() &&
        			!places[i].getConnectToIterator().hasNext())
        		throw new ParseException(modelNameMessage(netModel)+placeName+" - "+LocaleBundle.bundleString("is not connected"));
        	parseTokens(initialMarking, places[i]);
        	mid.addPlace(placeName);
        }
        return initialMarking;
	}
	
	private ArrayList<Predicate> getPrecondition(PipeTransition pipeTransition) throws ParseException {
		ArrayList<Predicate> precondition = new ArrayList<Predicate>();
		//bidirectional arcs 
    	@SuppressWarnings("rawtypes")
		Iterator outputArcIterator = pipeTransition.getConnectFromIterator();
		while (outputArcIterator.hasNext()) {
			Arc outputArc = (Arc) (outputArcIterator.next());
			if (outputArc instanceof BidirectionalArc){
				String placeName = outputArc.getTarget().getName();
				for (Predicate predicate: getPredicateListFromArc(placeName, outputArc))
					precondition.add(predicate);
			}
		}
		// directed arcs 
    	@SuppressWarnings("rawtypes")
		Iterator inputArcIterator = pipeTransition.getConnectToIterator();
		while (inputArcIterator.hasNext()) {
			Arc inputArc = (Arc) (inputArcIterator.next());
			if (!(inputArc instanceof InhibitorArc)){
				String placeName = inputArc.getSource().getName();
				for (Predicate predicate: getPredicateListFromArc(placeName, inputArc))
					precondition.add(predicate);
			}
		}
		// inhibitor arcs 
		@SuppressWarnings("rawtypes")
		Iterator inhibitorArcIterator = pipeTransition.getConnectToIterator();
		while (inhibitorArcIterator.hasNext()) {
			Arc inhibitorArc = (Arc) (inhibitorArcIterator.next());
			if (inhibitorArc instanceof InhibitorArc){
				String placeName = inhibitorArc.getSource().getName();
				for (Predicate predicate: getPredicateListFromArc(placeName, inhibitorArc))
					precondition.add(predicate);
			}
		}		
		return precondition;
	}

	private ArrayList<Predicate> getPostcondition(PipeTransition pipeTransition) throws ParseException {
		ArrayList<Predicate> postcondition = new ArrayList<Predicate>();
		// bidirectional arcs
		@SuppressWarnings("rawtypes")
		Iterator inputArcIterator = pipeTransition.getConnectToIterator();
		while (inputArcIterator.hasNext()) {
			Arc inputArc = (Arc) (inputArcIterator.next());
			if (inputArc instanceof BidirectionalArc){
				String placeName = inputArc.getSource().getName();
				for (Predicate predicate: getPredicateListFromArc(placeName, inputArc))
					postcondition.add(predicate);
			}
		}
		// directed arcs
    	@SuppressWarnings("rawtypes")
		Iterator outputArcIterator = pipeTransition.getConnectFromIterator();
		while (outputArcIterator.hasNext()) {
			Arc outputArc = (Arc) (outputArcIterator.next());
			String placeName = outputArc.getTarget().getName();
			if (isRESETArcLabel(outputArc)){
				ArrayList<String> arguments = new ArrayList<String>();
				arguments.add(placeName);
				postcondition.add(new Predicate(MID.RESET, arguments));
			}
			else{
				for (Predicate predicate: getPredicateListFromArc(placeName, outputArc))
					postcondition.add(predicate);
			}
		}
		return postcondition;
	}
	
	private boolean isRESETArcLabel(Arc outputArc){
		String activeTokenClassID = CreateGui.getModel().getActiveTokenClassID();
		int pos = CreateGui.getModel().getPosInList(activeTokenClassID, outputArc.getWeight());
		if (pos >= 0) {
			String arcLabel = outputArc.getWeight().get(pos).getCurrentMarking().trim();
			try {
				ArrayList<ArrayList<String>> arcLabelList = MIDParser.parseArcLabelString(arcLabel);
				if (arcLabelList.size()>1)
					return false;
				ArrayList<String> labelArguments = arcLabelList.get(0);
				return labelArguments.size()==1 && labelArguments.get(0).equalsIgnoreCase(MID.RESET);
			}
			catch (ParseException e){
				return false;
			}
		}
		return false;
	} 

	// <label1>&<label2>&...  => p(label1),p(label2),...
	private ArrayList<Predicate> getPredicateListFromArc(String placeName, Arc arc) throws ParseException{
		if (!MIDParser.isIdentifier(placeName))
    		throw new ParseException(placeName+" "+LocaleBundle.bundleString("should start with a letter"));
		String activeTokenClassID = CreateGui.getModel().getActiveTokenClassID();
		int pos = CreateGui.getModel().getPosInList(activeTokenClassID, arc.getWeight());
		if (pos >= 0) {
			String arcLabel = arc.getWeight().get(pos).getCurrentMarking().trim();
			ArrayList<Predicate> predicates = new ArrayList<Predicate>();
			try {
				ArrayList<ArrayList<String>> arcLabelList = MIDParser.parseArcLabelString(arcLabel);
				for (ArrayList<String> labelArguments: arcLabelList)
					predicates.add(new Predicate(placeName, labelArguments, arc instanceof InhibitorArc));
				return predicates;
			}
			catch (ParseException e){
				throw new ParseException(LocaleBundle.bundleString("Arc label")+" "+arcLabel+" ("+arc.getSource().getName()+" , "+arc.getTarget().getName()+"). "+e.toString());
			}
		}
		return new ArrayList<Predicate>();
	}

	private ArrayList<Predicate> parseGlobalPredicates(MID mid, PrTPanel prtNet) throws ParseException{
		String GLOBAL_KEYWORD = "GLOBAL";
		ArrayList<Predicate> globalPredicates = new ArrayList<Predicate>();
        DataLayerInterface net = prtNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(GLOBAL_KEYWORD)) {
				String globalPredicateString =  text.substring(GLOBAL_KEYWORD.length()).trim();
				try{ // abuse transition declaration for global predicate - same syntax
					Predicate signaturePredicate = MIDParser.parseTransitionSignatureString(globalPredicateString); 
					globalPredicates.add(signaturePredicate);
					mid.addPlace(signaturePredicate.getName());

				} catch (ParseException e){
					throw new ParseException(modelNameMessage(prtNet)+text+": "+LocaleBundle.bundleString("Incorrect GLOBAL annotation"));
				}
			}
		}
		return globalPredicates;
	}

	
	private void parseTransitions(MID mid, PrTPanel prtNet) throws ParseException{
		ArrayList<Predicate> globalPredicates = parseGlobalPredicates(mid, prtNet);
        DataLayerInterface net = prtNet.getModel();
        PipeTransition[] pipeTransitions = net.getTransitions();
        for (int i = 0; i < pipeTransitions.length; i++) {
        	PipeTransition pipeTransition =  pipeTransitions[i];
        	if (pipeTransition.hasValidSubnetFile() && editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
        		PrTPanel subModel = editor.findPrTPanelForFile(pipeTransition.getSubnetFileHandler());
        		if (subModel!=null) {
        			// check composition rules, e.g., place names
        			checkForCompositionErrors(prtNet, pipeTransition, subModel);
        			parseTransitions(mid, subModel);
        		}
        	} else
        		parsePipeTransition(mid, prtNet, pipeTransition, globalPredicates);
//System.out.println("Transition ID: "+pipeTransitions[i].getId()+" Name: "+pipeTransitions[i].getName());        	
       }
		
	}

	private void parsePipeTransition(MID mid, PrTPanel prtNet, PipeTransition pipeTransition, ArrayList<Predicate> globalPredicates) throws ParseException{
    	String transitionSignature = pipeTransition.getName();
    	Transition istaTransition = null;
    	Predicate signaturePredicate = null;
    	try {
    		signaturePredicate = MIDParser.parseTransitionSignatureString(transitionSignature);
    		istaTransition = new Transition(signaturePredicate.getName());  
    		istaTransition.setArguments(signaturePredicate.getArguments());
    	}
    	catch (Exception e) {
        	throw new ParseException(e.toString());	
        }
    	ArrayList<Predicate> precondition = getPrecondition(pipeTransition);
    	ArrayList<Predicate> postcondition = getPostcondition(pipeTransition);
    	if (globalPredicates.size()>0){
    		for (int index=globalPredicates.size()-1; index>=0; index--){
    			Predicate global = globalPredicates.get(index);
    			precondition.add(0, global);
       			postcondition.add(0, global);
    		}
    	}
    	istaTransition.setPrecondition(precondition);
    	istaTransition.setPostcondition(postcondition);
    	try {
    		String whenString = pipeTransition.getGuard().trim();
    		if (whenString!=null && !whenString.equals("")) {
    			MIDParser.parseWhenCondition(istaTransition, whenString);
    		}
    	}
    	catch (Exception e) {
        	throw new ParseException(modelNameMessage(prtNet)+transitionSignature+" "+LocaleBundle.bundleString("Guard condition")+":"+e.toString());	
        }
    	MIDParser.checkTransitionArguments(transitionSignature, signaturePredicate.getArguments(), precondition, istaTransition.getWhenCondition());
    	MIDParser.checkWhenConditionVariables(precondition, istaTransition.getWhenCondition());

    	try {
    		String effectString = pipeTransition.getEffect().trim();	
    		ArrayList<Predicate> effect = MIDParser.parseConditionString(effectString); 
    		istaTransition.setEffect(effect);
    	}
    	catch (Exception e) {
        	throw new ParseException(modelNameMessage(prtNet)+transitionSignature+" "+LocaleBundle.bundleString("Effect")+": "+e.toString());	
        }
    	
   		istaTransition.collectAllVariables(); 
		MIDParser.checkPostconditionVariables(istaTransition);
		istaTransition.setId(pipeTransition.getId());
   		mid.addTransition(istaTransition); 
   		mid.putPipeTransition(istaTransition, pipeTransition);
	}
	
	private void checkForCompositionErrors(PrTPanel parentNet, PipeTransition pipeTransition, PrTPanel childNet) throws ParseException{
		checkForInputPlaces(parentNet, pipeTransition, childNet);
		checkForInhibitorPlaces(parentNet, pipeTransition, childNet);
		checkForOutputPlaces(parentNet, pipeTransition, childNet);
		checkForDuplicatePlacesInSubModel(parentNet, pipeTransition, childNet);
//		checkForDuplicateEventsInSubModel(parentNet, childNet);
	}

	private void checkForDuplicatePlacesInSubModel(PrTPanel parentNet, PipeTransition pipeTransition, PrTPanel childNet){
		// composition places
		ArrayList<String> compositionPlaceNames = getInputPlaces(pipeTransition);
		for (String inhibitorPlaceName: getInhibitorPlaces(pipeTransition))
			if (!compositionPlaceNames.contains(inhibitorPlaceName))
				compositionPlaceNames.add(inhibitorPlaceName);
		for (String outputPlaceName: getOutputPlaces(pipeTransition))
			if (!compositionPlaceNames.contains(outputPlaceName))
				compositionPlaceNames.add(outputPlaceName);
		
		ArrayList<String> placeNamesInParentNet = new ArrayList<String>();
		PipePlace[] placesInParentNet = parentNet.getModel().getPlaces();
        for (int i = 0; i < placesInParentNet.length; i++)
        	placeNamesInParentNet.add(placesInParentNet[i].getName());
				
		// check non-composition places
		PipePlace[] placesInChildNet = childNet.getModel().getPlaces();
        for (int i = 0; i < placesInChildNet.length; i++){
        	String placeName = placesInChildNet[i].getName();
        	if (!compositionPlaceNames.contains(placeName) && placeNamesInParentNet.contains(placeName))
    			editor.printInConsoleArea(LocaleBundle.bundleString("Warning")+": "+modelNameMessage(childNet)+placeName+": "+LocaleBundle.bundleString("PLACE_OCCURRED_IN_BOTH_LEVELS"));        		
         }
	}

	/*
	private void checkForDuplicateEventsInSubModel(PrTPanel parentNet, PrTPanel childNet){
		ArrayList<String> parentEvents = new ArrayList<String>();
        PipeTransition[] parentTransitions = parentNet.getModel().getTransitions();
        for (int i = 0; i < parentTransitions.length; i++) 
        	parentEvents.add(parentTransitions[i].getName());
		
       PipeTransition[] childTransitions = childNet.getModel().getTransitions();
        for (int i = 0; i < childTransitions.length; i++) {
        	String childEventName =  childTransitions[i].getName();
        	if (parentEvents.contains(childEventName))
    			editor.printInConsoleArea(LocaleBundle.bundleString("Warning")+": "+modelNameMessage(childNet)+childEventName+": "+LocaleBundle.bundleString("TRANSITION_OCCURRED_IN_BOTH_LEVELS"));        		       		
        }
 	}
*/
	
	private void checkForDuplicateEvents(){
	    if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
	    	ArrayList<String> events = new ArrayList<String>();
	    	PipeTransition[] mainTransitions = mainNet.getModel().getTransitions();
	    	for (int i = 0; i < mainTransitions.length; i++) 
	    		events.add(mainTransitions[i].getName());
        	for (JPanel subModel: editor.getSubModels())
        		if (subModel instanceof PrTPanel){
        	    	PipeTransition[] subTransitions = ((PrTPanel)subModel).getModel().getTransitions();
        	    	for (int i = 0; i < subTransitions.length; i++){
        	    		if (events.contains(subTransitions[i].getName())){
        	    			editor.printInConsoleArea(LocaleBundle.bundleString("Warning")+": "+modelNameMessage((PrTPanel)subModel)+subTransitions[i].getName()+": "+LocaleBundle.bundleString("TRANSITION_OCCURRED_IN_MULTIPLE_MODELS"));        		       		        	    			
        	    		}
        	    	}
        	    	for (int i = 0; i < subTransitions.length; i++){
         	    		events.add(subTransitions[i].getName());
        	    	}
        		}
        }
 	}

	private ArrayList<String> getBiArcPlaces(PipeTransition pipeTransition){
		ArrayList<String> placeNames = new ArrayList<String>();
		//bidirectional arcs 
    	@SuppressWarnings("rawtypes")
		Iterator inputArcIterator = pipeTransition.getConnectToIterator();
		while (inputArcIterator.hasNext()) {
			Arc inputArc = (Arc) (inputArcIterator.next());
			if (inputArc instanceof BidirectionalArc){
				placeNames.add(inputArc.getSource().getName());
			}
		}
    	@SuppressWarnings("rawtypes")
		Iterator outputArcIterator = pipeTransition.getConnectFromIterator();
		while (outputArcIterator.hasNext()) {
			Arc outputArc = (Arc) (outputArcIterator.next());
			if (outputArc instanceof BidirectionalArc){
				placeNames.add(outputArc.getTarget().getName());
			}
		}
/*System.out.println(pipeTransition.getName()+" biarc places: ");
for (String place: placeNames)
	System.out.print(place+" ");
*/
		return placeNames;
	}

	private ArrayList<String> getInputPlaces(PipeTransition pipeTransition){
		ArrayList<String> placeNames = getBiArcPlaces(pipeTransition);
		// directed arcs 
    	@SuppressWarnings("rawtypes")
		Iterator inputArcIterator = pipeTransition.getConnectToIterator();
		while (inputArcIterator.hasNext()) {
			Arc inputArc = (Arc) (inputArcIterator.next());
			if (inputArc instanceof NormalArc){
				placeNames.add(inputArc.getSource().getName());
			}
		}
/*System.out.println("\n"+pipeTransition.getName()+" input places: ");
for (String place: placeNames)
	System.out.print(place+" ");
*/
		return placeNames;
	}

	private ArrayList<String> getInhibitorPlaces(PipeTransition pipeTransition){
		ArrayList<String> placeNames = new ArrayList<String>();
		// inhibitor arcs 
		@SuppressWarnings("rawtypes")
		Iterator inhibitorArcIterator = pipeTransition.getConnectToIterator();
		while (inhibitorArcIterator.hasNext()) {
			Arc inhibitorArc = (Arc) (inhibitorArcIterator.next());
			if (inhibitorArc instanceof InhibitorArc){
				placeNames.add(inhibitorArc.getSource().getName());
			}
		}		
/*System.out.println("\n"+pipeTransition.getName()+" inhibitor places: ");
for (String place: placeNames)
	System.out.print(place+" ");
*/		return placeNames;
	}

	private ArrayList<String> getOutputPlaces(PipeTransition pipeTransition){
		ArrayList<String> placeNames = getBiArcPlaces(pipeTransition);
		// directed arcs 
    	@SuppressWarnings("rawtypes")
		Iterator outputArcIterator = pipeTransition.getConnectFromIterator();
		while (outputArcIterator.hasNext()) {
			Arc outputArc = (Arc) (outputArcIterator.next());
			if (outputArc instanceof NormalArc){
				placeNames.add(outputArc.getTarget().getName());
			}
		}
/*System.out.println("\n"+pipeTransition.getName()+" output places: ");
	for (String place: placeNames)
		System.out.print(place+" ");
*/
		return placeNames;
	}

	private void checkForInputPlaces(PrTPanel parentNet,PipeTransition pipeTransition, PrTPanel childNet) throws ParseException{
		for (String placeNameInParentNet: getInputPlaces(pipeTransition)){
			PipePlace placeInSubNet =  pipePlaceInSubNet(placeNameInParentNet, childNet);
			if (placeInSubNet==null)
		        	throw new ParseException(modelNameMessage(parentNet)+LocaleBundle.bundleString("transition") + " "+pipeTransition.getName()
		        			+": "+LocaleBundle.bundleString("place")+" "+placeNameInParentNet + " "+LocaleBundle.bundleString("DOES_NOT_EXIST_IN_THE_SUBMODEL"));	
			boolean found = false;
			@SuppressWarnings("rawtypes")
			Iterator arcIterator = placeInSubNet.getConnectFromIterator();
			while (arcIterator.hasNext() && !found) {
				Arc arc = (Arc) (arcIterator.next());
				if (!(arc instanceof InhibitorArc))
					found = true;
			}	
			if (!found) {
				@SuppressWarnings("rawtypes")
				Iterator biarcIterator = placeInSubNet.getConnectToIterator();
				while (biarcIterator.hasNext() && !found) {
					Arc arc = (Arc) (biarcIterator.next());
					if (arc instanceof BidirectionalArc)
						found = true;
				}		
			}
			if (!found)
	        	throw new ParseException(modelNameMessage(parentNet)+LocaleBundle.bundleString("transition") + " "+pipeTransition.getName()
	        			+": "+LocaleBundle.bundleString("place")+" "+placeNameInParentNet + " "+LocaleBundle.bundleString("IS_NOT_AN_INPUT_PLACE_IN_THE_SUBMODEL")+modelNameMessage(childNet)+".");	
		}
	}
	
	private void checkForInhibitorPlaces(PrTPanel parentNet,PipeTransition pipeTransition, PrTPanel childNet) throws ParseException{
		for (String placeNameInParentNet: getInhibitorPlaces(pipeTransition)){
			PipePlace placeInSubNet =  pipePlaceInSubNet(placeNameInParentNet, childNet);
			if (placeInSubNet==null)
		        	throw new ParseException(modelNameMessage(parentNet)+LocaleBundle.bundleString("transition") + " "+pipeTransition.getName()
		        			+": "+LocaleBundle.bundleString("place")+" "+placeNameInParentNet + " "+LocaleBundle.bundleString("DOES_NOT_EXIST_IN_THE_SUBMODEL")+" "+modelNameMessage(childNet)+".");	
			boolean found = false;
			@SuppressWarnings("rawtypes")
			Iterator arcIterator = placeInSubNet.getConnectFromIterator();
			while (arcIterator.hasNext() && !found) {
				Arc arc = (Arc) (arcIterator.next());
				if (arc instanceof InhibitorArc)
					found = true;
			}	
			if (!found)
	        	throw new ParseException(modelNameMessage(parentNet)+LocaleBundle.bundleString("transition") + " "+pipeTransition.getName()
	        			+": "+LocaleBundle.bundleString("place")+" "+placeNameInParentNet + " "+LocaleBundle.bundleString("IS_NOT_AN_INHIBITOR_PLACE_IN_THE_SUBMODEL")+" "+modelNameMessage(childNet)+".");	
		}		
	}
	
	private void checkForOutputPlaces(PrTPanel parentNet,PipeTransition pipeTransition, PrTPanel childNet) throws ParseException{
		for (String placeNameInParentNet: getOutputPlaces(pipeTransition)){
			PipePlace placeInSubNet =  pipePlaceInSubNet(placeNameInParentNet, childNet);
			if (placeInSubNet==null)
		        	throw new ParseException(modelNameMessage(parentNet)+LocaleBundle.bundleString("transition") + " "+pipeTransition.getName()
		        			+": "+LocaleBundle.bundleString("place")+" "+placeNameInParentNet + " "+LocaleBundle.bundleString("DOES_NOT_EXIST_IN_THE_SUBMODEL")+" "+modelNameMessage(childNet)+".");	
			boolean found = false;
			@SuppressWarnings("rawtypes")
			Iterator arcIterator = placeInSubNet.getConnectToIterator();
			while (arcIterator.hasNext() && !found) {
				Arc arc = (Arc) (arcIterator.next());
				if (!(arc instanceof InhibitorArc))
					found = true;
			}	
			if (!found) {
				@SuppressWarnings("rawtypes")
				Iterator biarcIterator = placeInSubNet.getConnectFromIterator();
				while (biarcIterator.hasNext() && !found) {
					Arc arc = (Arc) (biarcIterator.next());
					if (arc instanceof BidirectionalArc)
						found = true;
				}		
			}
			if (!found)
	        	throw new ParseException(modelNameMessage(parentNet)+LocaleBundle.bundleString("transition") + " "+pipeTransition.getName()
	        			+": "+LocaleBundle.bundleString("place")+" "+placeNameInParentNet + " "+LocaleBundle.bundleString("IS_NOT_AN_OUTPUT_PLACE_IN_THE_SUBMODEL")+" "+modelNameMessage(childNet)+".");	
		}
	}

	private PipePlace pipePlaceInSubNet(String placeName, PrTPanel childNetPanel){
        DataLayerInterface net = childNetPanel.getModel();
		for (PipePlace pipePlace: net.getPlaces())
			if (pipePlace.getName().equals(placeName))
				return pipePlace;
		return null;	
	}

	private void checkSubModelEnumerationAnnotations(){
		int totalEnumerations = numberOfNumerationAnnotations(mainNet);
		for (JPanel subModel: editor.getSubModels())
			if (subModel instanceof PrTPanel)
				totalEnumerations += numberOfNumerationAnnotations((PrTPanel)subModel);
		if (totalEnumerations>1)
			editor.printInConsoleArea(LocaleBundle.bundleString("Warning")+": "+LocaleBundle.bundleString("ONLY_ONE_ENUMERATION_SHOULD_BE_USED"));
	}
	
	private int numberOfNumerationAnnotations(PrTPanel prtNet){
		int numberOfEnumerations =0;
        DataLayerInterface net = prtNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.ENUM_KEYWORD))
				numberOfEnumerations++;
		}
		return numberOfEnumerations;
	}
	
	private void parseConstantsAnnotations() throws ParseException{
		parseConstantsAnnotations(mainNet);
		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
			checkSubModelEnumerationAnnotations();
			for (JPanel subModel: editor.getSubModels())
				if (subModel instanceof PrTPanel)
					parseConstantsAnnotations((PrTPanel)subModel);
		}

	}
	
	private void parseConstantsAnnotations(PrTPanel prtNet) throws ParseException{
        DataLayerInterface net = prtNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.CONSTANTS_KEYWORD))
				parseConstants(text, XMIDProcessor.CONSTANTS_KEYWORD);
			else
			if (text.startsWith(XMIDProcessor.ENUM_KEYWORD))
				parseEnumeration(text, XMIDProcessor.ENUM_KEYWORD);
		}
	}

	private void parseGoalPropertyAnnotations(MID mid) throws ParseException{
        DataLayerInterface net = mainNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.GOAL_KEYWORD)){
				String propertyString = text.substring(XMIDProcessor.GOAL_KEYWORD.length());
				if (!propertyString.trim().equals("")) {
					try {
						GoalProperty propertyTransition = MIDParser.parseGoalPropertyString(propertyString);
						mid.addGoalProperty(propertyTransition);
					}
					catch (ParseException e) {
						throw new ParseException(text+". "+e.toString());
					}
				}
			}
		}
	}

	private void parseAssertionPropertyAnnotations(MID mid) throws ParseException{
        DataLayerInterface net = mainNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.ASSERTION_KEYWORD)){
				String assertionString = text.substring(XMIDProcessor.ASSERTION_KEYWORD.length());
				if (!assertionString.trim().equals("")) {
					try {
						AssertionProperty assertion = MIDParser.parseAssertionPropertyString(assertionString);
						mid.addAssertionProperty(assertion);
					}
					catch (ParseException e) {
						throw new ParseException(text+". "+e.toString());
					}
				}
			}
		}
	}

	private void parseDataAnnotations(MID mid) throws ParseException{
        DataLayerInterface net = mainNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.DATA_KEYWORD)) {
				String dataPath = text.substring(XMIDProcessor.DATA_KEYWORD.length()).trim();
				try {
					ExcelTestDataLoader excelTestDataLoader = new ExcelTestDataLoader(editor.midFile.getParent()+File.separator+dataPath, mid.getPlaces());
					for (Marking initMarking: excelTestDataLoader.getInitMarkings()){
						mid.addInitialMarking(initMarking);
					}
				}
				catch (Exception e){
					throw new ParseException(text+" -> "+e.getMessage());					
				}
			}
		}
	}

	private void parseMarkingAnnotations(MID mid) throws ParseException{
        DataLayerInterface net = mainNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.INIT_KEYWORD))
				parseMainModelAnnotationMarking(mid, text);
		}
		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled())
			parseSubModelMarkingAnnotations(mid);
	}

	private void parseSubModelMarkingAnnotations(MID mid) throws ParseException{
		for (JPanel subModel: editor.getSubModels())
			if (subModel instanceof PrTPanel){
				// deal with initial markings
				ArrayList<Marking> initMarkings = parseSubModelMarkingAnnotations((PrTPanel)subModel, XMIDProcessor.INIT_KEYWORD);
				Marking nonAnnotationInitMarking = subModelNonAnnotationInitialMarkings.get((PrTPanel)subModel);
				if (nonAnnotationInitMarking!=null)
					initMarkings.add(0,nonAnnotationInitMarking);
				if (initMarkings.size()>1)
					throw new ParseException(LocaleBundle.bundleString("Warning")+": "+modelNameMessage((PrTPanel)subModel)+LocaleBundle.bundleString("SUBMODEL_HAS_MORE_THAN_ONE_INITIAL_MARKINGS"));
				if (initMarkings.size()>0){
					if (mid.getInitialMarkings().size()==0)
						mid.addInitialMarking(initMarkings.get(0));
					else 
						mid.getInitialMarkings().get(0).merge(initMarkings.get(0));
				}
			}
	}
	
	protected void parseMainModelAnnotationMarking(MID mid, String text) throws ParseException {
		String markingString = text.substring(XMIDProcessor.INIT_KEYWORD.length());
		if (markingString.trim().equals(""))
			return;
		Marking marking = null;
		try {
			marking = MIDParser.parseMarkingString(markingString);
			mid.addInitialMarking(marking);
		}
		catch (ParseException e) {
			throw new ParseException(XMIDProcessor.INIT_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+". "+e.toString());
		}
	}

	
	protected ArrayList<Marking> parseSubModelMarkingAnnotations(PrTPanel subPrtPanel, String markingKeyword) throws ParseException {
		ArrayList<Marking> markings = new ArrayList<Marking>();
        DataLayerInterface net = subPrtPanel.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(markingKeyword)){
				Marking marking = parseSubModelAnnotationMarking(text, markingKeyword);
				if (marking!=null)
					markings.add(marking);
			}
		}
		return markings;
	}

	protected Marking parseSubModelAnnotationMarking(String text, String markingKeyword) throws ParseException {
		String markingString = text.substring(markingKeyword.length());
		if (markingString.trim().equals(""))
			return null;
		Marking marking = null;
		try {
			marking = MIDParser.parseMarkingString(markingString);
		}
		catch (ParseException e) {
			throw new ParseException(markingKeyword+" "+LocaleBundle.bundleString("annotation")+" "+text+". "+e.toString());
		}
		return marking;
	}

	private void parseOtherAnnotations(MID mid) throws ParseException{
		parseOtherAnnotations(mid, mainNet);
		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
			for (JPanel subModel: editor.getSubModels())
				if (subModel instanceof PrTPanel)
					parseOtherAnnotations(mid, (PrTPanel)subModel);
		}
	}
	
	private void parseOtherAnnotations(MID mid, PrTPanel prtNet) throws ParseException{
        DataLayerInterface net = prtNet.getModel();
		AnnotationNote[] annotationNotes = net.getLabels();
		for (AnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.UNITTESTS_KEYWORD))
				parseUnitTests(mid, text, XMIDProcessor.UNITTESTS_KEYWORD);
			else
			if (text.startsWith(XMIDProcessor.SINKS_KEYWORD))
				parseSinkEvents(mid, text, XMIDProcessor.SINKS_KEYWORD);
			else
			if (text.startsWith(XMIDProcessor.SINK_KEYWORD))
				parseSinkEvents(mid, text, XMIDProcessor.SINK_KEYWORD);
			else
			if (text.startsWith(XMIDProcessor.NONNEGATIVE_KEYWORD))
				parseNonNegativeEvents(mid, text, XMIDProcessor.NONNEGATIVE_KEYWORD);
			else
			if (text.startsWith(XMIDProcessor.SEQUENCESFILE_KEYWORD))
				parseSequencesFile(mid, text, XMIDProcessor.SEQUENCESFILE_KEYWORD);
			else
			if (text.startsWith(XMIDProcessor.SEQUENCES_KEYWORD))
				parseSequencesFile(mid, text, XMIDProcessor.SEQUENCES_KEYWORD);
		}
	}
	
	private void parseConstants(String text, String keyword) throws ParseException {
		String constantsString =  text.substring(keyword.length());
		if (constantsString.equals(""))
			return;
		try {
			MIDParser.parseNamedIntegersString(constantsString);
		}
		catch (ParseException e) {
			throw new ParseException(XMIDProcessor.CONSTANTS_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n"+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(XMIDProcessor.CONSTANTS_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+": "+LocaleBundle.bundleString("Lexical error"));				
		}
	}

	private void parseEnumeration(String text, String keyword) throws ParseException {
		String enumerationString =  text.substring(keyword.length());
		if (enumerationString.equals(""))
			return;
		try {
			MIDParser.parseEnumerationString(enumerationString);
		}
		catch (ParseException e) {
			throw new ParseException(XMIDProcessor.ENUM_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n"+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(XMIDProcessor.ENUM_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+": "+LocaleBundle.bundleString("Lexical error"));				
		}
	}

	private void parseSinkEvents(MID mid, String text, String keyword) throws ParseException {
		String sinkEventsString =  text.substring(keyword.length());
		if (sinkEventsString.equals(""))
			return;
		try {
			ArrayList<String> sinkEvents = MIDParser.parseIdentifierListString(sinkEventsString);
			mid.setSinkEvents(sinkEvents);
		}
		catch (ParseException e) {
			throw new ParseException(XMIDProcessor.SINKS_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n"+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(XMIDProcessor.SINKS_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+": "+LocaleBundle.bundleString("Lexical error"));				
		}
	}
	
	private void parseNonNegativeEvents(MID mid, String text, String keyword) throws ParseException {
		String nonNegativeEventsString =  text.substring(keyword.length());
		if (nonNegativeEventsString.equals(""))
			return;
		try {
			ArrayList<String> nonNegativeEvents = MIDParser.parseIdentifierListString(nonNegativeEventsString);
			mid.setNonNegativeEvents(nonNegativeEvents);
		}
		catch (ParseException e) {
			throw new ParseException(XMIDProcessor.NONNEGATIVE_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n"+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(XMIDProcessor.NONNEGATIVE_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+": "+LocaleBundle.bundleString("Lexical error"));				
		}
	}

	
	private void parseUnitTests(MID mid, String text, String keyword) throws ParseException {
		String unitTestString = text.substring(keyword.length());
		if (unitTestString.trim().equals(""))
			return;
		ArrayList<Predicate> unitTests = null;
		try {
			unitTests = MIDParser.parseConditionString(unitTestString);
		}
		catch (ParseException e) {
			throw new ParseException(keyword+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n"+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(keyword+" "+LocaleBundle.bundleString("annotation")+" "+text+": "+LocaleBundle.bundleString("Lexical error"));				
		}
		for (Predicate test: unitTests)
			mid.addUnitTest(test);
	}

	private void parseSequencesFile(MID mid, String text, String keyword) throws ParseException{
		String fileString = text.substring(keyword.length());
		if (!fileString.trim().equals(""))
			mid.setSequencesFile(text.substring(keyword.length()).trim());
	}

}
