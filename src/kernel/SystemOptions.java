/* 	
	Author Dianxiang Xu
*/
package kernel;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import java.util.ArrayList;

import locales.LocaleBundle;
import mid.MID;

import testcode.GoalTagCode;
import testcode.TargetLanguage;
import testcode.TargetLanguageOO;
import testgeneration.TransitionTreeForDeadlockStateCoverage;
import testgeneration.TransitionTreeForDepthCoverageBFS;
import testgeneration.TransitionTreeForDepthCoverageDFS;
import testgeneration.TransitionTreeForRoundTripBFS;
import testgeneration.TransitionTreeForRoundTripDFS;
import testgeneration.CoverageCriterion;
import testgeneration.TransitionTreeForStateCoverageBFS;
import testgeneration.TransitionTreeForStateCoverageDFS;
import testgeneration.TransitionTreeForThreatNetBFS;
import testgeneration.TransitionTreeForThreatNetDFS;
import testgeneration.TransitionTreeForTransitionCoverage;
import testgeneration.TransitionTree;
import testgeneration.TransitionTreeForThreatTree;
import testgeneration.TransitionTreeFromVerificationResults;
import testgeneration.TransitionTreeRandomGenerator;
import utilities.ReadWriteObj;
import verification.AssertionVerifier;
import verification.AssertionVerifierBFS;
import verification.AssertionVerifierDFS;
import verification.GoalVerifier;

public class SystemOptions implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String SystemOptionsFileName = "options.dat";
	public static enum ModelType {FUNCTIONNET, ABAC, STATEMACHINE, CONTRACT, THREATNET, THREATTREE};

	public static final ModelType DEFAULT_MODEL_TYPE = ModelType.FUNCTIONNET;
	
	public static final String FUNCTIONNET_KEYWORD = "Function net";
	public static final String STATEMACHINE_KEYWORD = "State machine";
	public static final String CONTRACT_KEYWORD = "Contracts";
	public static final String THREATNET_KEYWORD = "Threat net";
	public static final String THREATTREE_KEYWORD = "Threat tree";
	public static final String ABAC_KEYWORD = "ABAC model";
	
//	public static final String[] ALL_MODEL_TYPES={FUNCTIONNET_KEYWORD};
//	public static final String[] ALL_MODEL_TYPES={FUNCTIONNET_KEYWORD, STATEMACHINE_KEYWORD, CONTRACT_KEYWORD};
//	public static final String[] ALL_MODEL_TYPES={FUNCTIONNET_KEYWORD, STATEMACHINE_KEYWORD, CONTRACT_KEYWORD, THREATNET_KEYWORD, THREATTREE_KEYWORD};
	public static final String[] ALL_MODEL_TYPES={FUNCTIONNET_KEYWORD, ABAC_KEYWORD, STATEMACHINE_KEYWORD, CONTRACT_KEYWORD, THREATNET_KEYWORD, THREATTREE_KEYWORD};
	
	public static final CoverageCriterion ReachabilityTreeCoverage =new CoverageCriterion("Reachability Tree", "RT");  
	public static final CoverageCriterion ReachabilityTreeWithDirtyTests = 	new CoverageCriterion("Reachability Tree with Dirty Tests", "RTD");
	public static final CoverageCriterion TransitionCoverage = 		new CoverageCriterion("Transition Coverage", "TC");
	public static final CoverageCriterion StateCoverage = 			new CoverageCriterion("State Coverage", "SC");
	public static final CoverageCriterion DepthCoverage = 			new CoverageCriterion("Depth Coverage", "DP");  
	public static final CoverageCriterion RandomGeneration = 		new CoverageCriterion("Random Generation", "RAN");  
	public static final CoverageCriterion GoalCoverage = 			new CoverageCriterion("Goal Coverage", "GC");
	public static final CoverageCriterion CounterExampleCoverage= 	new CoverageCriterion("ASSERTION COUNTEREXAMPLES", "ACE");
	public static final CoverageCriterion DeadlockStateCoverage = 	new CoverageCriterion("Deadlock States", "DSC");
	public static final CoverageCriterion SequenceCoverage = 		new CoverageCriterion("Given Sequences", "SQ");  

	public static final CoverageCriterion ThreatNetPathCoverage = 	new CoverageCriterion("Attack Paths", "TNP");  
	public static final CoverageCriterion ThreatNetStateCoverage = 	new CoverageCriterion("Attack States", "TNS");  

	public static final CoverageCriterion ThreatTreePathCoverage = 	new CoverageCriterion("Threat Paths", "TT");  

	public static final CoverageCriterion[] coverageListForFunctionNets 	= 
		{ReachabilityTreeCoverage, 
		ReachabilityTreeWithDirtyTests, 
		TransitionCoverage, 
		StateCoverage, 
		DepthCoverage, 
		RandomGeneration, 
		GoalCoverage, 
		CounterExampleCoverage, 
		DeadlockStateCoverage, 
		SequenceCoverage};
	public static final CoverageCriterion[] coverageListForABAC = coverageListForFunctionNets;
	public static final CoverageCriterion[] coverageListForStateMachines 	= 
		{ReachabilityTreeCoverage, 
		ReachabilityTreeWithDirtyTests, 
		TransitionCoverage, 
		StateCoverage, 
		DepthCoverage, 
		RandomGeneration, 
		GoalCoverage};
	public static final CoverageCriterion[] coverageListForThreatNets 		= 
		{ThreatNetPathCoverage, 
		ThreatNetStateCoverage};
	public static final CoverageCriterion[] coverageListForThreatTrees 		= 
		{ThreatTreePathCoverage};
	public static final CoverageCriterion[] coverageListForInvalidModel		= 
		{ReachabilityTreeCoverage};

	// editing preferences
	private boolean useGraphicalNetEditor = true;
	private boolean useGraphicalStateMachineEditor = true;
	private boolean useGraphicalThreatTreeEditor = true;
	
	private boolean netHierarchyEnabled = true;
	
	public static String DefaultFontName = getDefaultFontName();
	private Font textFont = new Font(DefaultFontName, Font.PLAIN, 13);

	// search preferences - for verification and test generation
	private boolean breadthFirstSearch = true;		
	private int		searchDepth = 100;		 
	private boolean searchForHomeStates = true;
	
	private boolean totalOrdering = true;
	private boolean pairwiseTesting = false;
	
	// random tests
	private int		maxRandomTests = 20;		 
	
	// options for test generation only
	private CoverageCriterion coverageCriterion = ReachabilityTreeCoverage; 

	public static final TargetLanguage[] AllLanguages = {
		TargetLanguage.JAVA, 
		TargetLanguage.PYTHON, 
		TargetLanguage.CPP, 
		TargetLanguage.CSHARP, 
		TargetLanguage.PHP, 
		TargetLanguage.HTML, 
		TargetLanguage.C, 
		TargetLanguage.VB, 
		TargetLanguage.KBT, 
		TargetLanguage.SELENIUMDRIVER, 
		TargetLanguage.RPC, 
//		TargetLanguage.UFT, 
		};
	
	private TargetLanguage language = TargetLanguage.JAVA;

	private int testFrameworkIndex = 0;						// default: no unit test framework  

	private ArrayList<GoalTagCode> goalTagsForAllTestFrameworks = new ArrayList<GoalTagCode>();
	
	private boolean includeSeqIndicesInTestID = false;
	private int		maxIdDepth = 5;		 // <=searchDepth; generate node ID number only within this depth

	private int 	maxLevelOfNodeExpansion = 30;
	private boolean showStatesInNodes = false;
	
	private boolean viewTestCode = true;
	private boolean generateSeparateTestFiles = false;
		
	// OO language only
	private boolean createObjectReference = true;	// OO style for class testing if true. 		
	// enabled only when class testing 
	private boolean referenceForMethodCall = true;	// attach object reference to method invocation code
	private boolean referenceForAccessorCall = true; // attach object reference to accessor code
	private boolean referenceForMutatorCall = true;   // attach object reference to mutator code
	
	private boolean generateTestParameters = true; // generate actual parameters from substitutions
	
	private boolean includeAssertDefinitionForC = true;
	
	private boolean verifyMarkings = true;		// verify all tokens of each resultant marking
	private boolean verifyPostconditions = false; // verify immediate postcondition, not the resultant marking
	private boolean verifyNegatedConditions = false; // verify immediate postcondition, not the resultant marking
	private boolean verifyEffects = true;		// verify transition effects
	private boolean verifyFirstOccurrence = true;		// verify transition effects

	private boolean createGoalTags = false;
	private boolean goalTagsAtBeginningOfTests = true;
	
	private boolean verifyDirtyTestState = true;	// verify that dirty tests preserve states
	private boolean verifyDirtyTestException = true;	// verify that dirty tests throw exceptions

	// test analysis options
	private boolean listFailureTests = true;
	private boolean findShortestPaths = true;
	private boolean evaluateTransitionCoverage = true;
	private boolean evaluateStateCoverage = true;
	private boolean listCoveredStates = true;
	private boolean listUncoveredStates = true;
	
	public SystemOptions(){
	}

	/////////////////////////////////////////////////////////
	//Editing preferences
	/////////////////////////////////////////////////////////	
	public boolean useGraphicalNetEditor(){
		return useGraphicalNetEditor;
	} 

	public void setUseGraphicalNetEditor(boolean flag){
		this.useGraphicalNetEditor = flag;
	} 

	public boolean useGraphicalStateMachineEditor(){
		return useGraphicalStateMachineEditor;
	} 

	public void setUseGraphicalStateMachineEditor(boolean flag){
		this.useGraphicalStateMachineEditor = flag;
	} 

	public boolean useGraphicalThreatTreeEditor(){
		return useGraphicalThreatTreeEditor;
	} 

	public void setUseGraphicalThreatTreeEditor(boolean flag){
		this.useGraphicalThreatTreeEditor = flag;
	} 

	public boolean useGraphicalEditor(ModelType modelType){
		switch (modelType){
			case FUNCTIONNET:
			case ABAC:
			case THREATNET:
				return useGraphicalNetEditor;
			case STATEMACHINE:
				return useGraphicalStateMachineEditor;
			case THREATTREE: 
				return useGraphicalThreatTreeEditor;
			case CONTRACT:
				return false;
		}
		return false;
	}
	
	public boolean isNetHierarchyEnabled(){
		return netHierarchyEnabled;
	}
	
	public void setNetHierarchyEnabled(boolean enabled){
		netHierarchyEnabled = enabled;
	}
	
	public Font getTextFont(){
		return textFont;
	}
	
	public void setTextFont(Font newFont){
		textFont = newFont;
	}
	
	public static String getDefaultFontName(){
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] faceNames =  e.getAvailableFontFamilyNames();
		// first choice
		for (String name: faceNames)
			if (name.equalsIgnoreCase("Arial Unicode MS"))
				return name;
		// second choice
		// there was a problem with the default font <Monospaced, plain, 13> on the Chinese Windows
		for (String name: faceNames)
			if (name.equalsIgnoreCase("Monospaced"))
				return name;
		return "Arial"; 
	}

	/////////////////////////////////////////////////////////
	//Search options
	/////////////////////////////////////////////////////////	
	public boolean isBreadthFirstSearch(){
		return breadthFirstSearch;
	} 

	public void setBreadthFirstSearch(boolean bfs){
		this.breadthFirstSearch = bfs;
	} 

	public int getSearchDepth(){
		return searchDepth;
	}

	public void setSearchDepth(int depth){
		this.searchDepth = depth;
	}
	
	public void setSearchForHomeStates(boolean search){
		this.searchForHomeStates = search;
	}
	
	public boolean searchForHomeStates(){
		return searchForHomeStates;
	}
	
	public boolean isTotalOrdering(){
		return totalOrdering;
	}
	
	public void setTotalOrdering(boolean ordering){
		this.totalOrdering = ordering;	
	}
	
	public boolean isPairwiseTesting(){
		return pairwiseTesting;
	}
	
	public void setPairwiseTesting(boolean pairwise){
		this.pairwiseTesting = pairwise;
	}
	
	public void setMaxRandomTests(int max){
		maxRandomTests = max;
	}
	
	public int getMaxRandomTests(){
		return maxRandomTests;
	}
	
	public void setMaxIdDepth(int max){
		maxIdDepth = max;
	}
	
	public int getMaxIdDepth(){
		return maxIdDepth;
	}
	
	
	public int getMaxLevelOfNodeExpansion(){
		return maxLevelOfNodeExpansion;
	}
	
	public void setMaxLevelOfNodeExpansion(int level){
		this.maxLevelOfNodeExpansion = level;
	}
	
	public boolean showStatesInNodes(){
		return showStatesInNodes;
	}
	
	public void setShowsStatesInNodes(boolean showStates){
		this.showStatesInNodes = showStates;
	}
	
	/////////////////////////////////////////////////////////
	//Model type
	/////////////////////////////////////////////////////////	
	public static boolean isModelTypeKeyword(String modelTypeString){
		for (String modString: ALL_MODEL_TYPES)
			if (modString.equalsIgnoreCase(modelTypeString))
				return true;
		return false;	
	}
	
	public static ModelType getModelType(String modelTypeString){
		if (modelTypeString.equalsIgnoreCase(FUNCTIONNET_KEYWORD))
			return ModelType.FUNCTIONNET;
		else if (modelTypeString.equalsIgnoreCase(ABAC_KEYWORD))
			return ModelType.ABAC;
		else if (modelTypeString.equalsIgnoreCase(CONTRACT_KEYWORD))
			return ModelType.CONTRACT;
		else if (modelTypeString.equalsIgnoreCase(STATEMACHINE_KEYWORD))
			return ModelType.STATEMACHINE;
		else if (modelTypeString.equalsIgnoreCase(THREATNET_KEYWORD))
			return ModelType.THREATNET;
		else if (modelTypeString.equalsIgnoreCase(THREATTREE_KEYWORD))
			return ModelType.THREATTREE;
		return DEFAULT_MODEL_TYPE;
	}
	
	public static String getModelTypeString(ModelType modelType){
		switch (modelType){
			case FUNCTIONNET: return FUNCTIONNET_KEYWORD;
			case ABAC: return ABAC_KEYWORD;
			case CONTRACT: return CONTRACT_KEYWORD;
			case STATEMACHINE: return STATEMACHINE_KEYWORD;
			case THREATNET: return THREATNET_KEYWORD;
			case THREATTREE: return THREATTREE_KEYWORD;
		default: return "";
		}
	}

	public static boolean isLegalModelType(ModelType modelType){
		String modelTypeString = getModelTypeString(modelType);
		for (String legalModelString: ALL_MODEL_TYPES)
			if (legalModelString.equalsIgnoreCase(modelTypeString))
				return true;
		return false;
	}
	/////////////////////////////////////////////////////////
	//Coverage criterion
	/////////////////////////////////////////////////////////	
	private CoverageCriterion[] getCoverageList(ModelType modelType){
		if (modelType==null)
			return coverageListForInvalidModel;
		switch (modelType){
			case FUNCTIONNET: return coverageListForFunctionNets;
			case ABAC: return coverageListForABAC;
			case STATEMACHINE: return coverageListForStateMachines; 
			case CONTRACT: return coverageListForFunctionNets;
			case THREATNET: return coverageListForThreatNets; 
			case THREATTREE: return coverageListForThreatTrees; 
			default: return coverageListForFunctionNets;
		}
	}

	public TransitionTree createTransitionTree(MID mid, ModelType modelType){
		switch (modelType){
			case FUNCTIONNET: 
			case ABAC: 
			case CONTRACT: 
			case STATEMACHINE: 
				if (coverageCriterion==ReachabilityTreeCoverage || coverageCriterion==ReachabilityTreeWithDirtyTests) {
					return breadthFirstSearch? new TransitionTreeForRoundTripBFS(mid, this): 
						new TransitionTreeForRoundTripDFS(mid, this);
				} else 
				if (coverageCriterion==TransitionCoverage) {
					return new TransitionTreeForTransitionCoverage(mid, this);
				} else 
				if (coverageCriterion==StateCoverage) {
					return breadthFirstSearch? new TransitionTreeForStateCoverageBFS(mid, this): new TransitionTreeForStateCoverageDFS(mid, this);
				} else 
				if (coverageCriterion==GoalCoverage) {
					ProgressDialog progressDialog = new ProgressDialog(null, LocaleBundle.bundleString("Reachability Analysis"), LocaleBundle.bundleString("Checking for goal reachability"));
					GoalReachabilityChecker goalReachabilityChecker = new GoalReachabilityChecker(mid, searchForHomeStates, progressDialog);
					Thread gaolReachabilityThread = new Thread(goalReachabilityChecker);
					gaolReachabilityThread.start();
					progressDialog.setVisible(true);
					GoalVerifier goalVerifier= goalReachabilityChecker.getGoalVerifier();
					return goalVerifier!=null? new TransitionTreeFromVerificationResults(mid, this, goalVerifier): null;
				} else 
				if (coverageCriterion==CounterExampleCoverage) {
					ProgressDialog progressDialog = new ProgressDialog(null, LocaleBundle.bundleString("VERIFY_ASSERTIONS"), LocaleBundle.bundleString("VERIFYING_ASSERTIONS"));
					AssertionChecker assertionChecker = new AssertionChecker(mid, progressDialog);
					Thread assertionCheckingThread = new Thread(assertionChecker);
					assertionCheckingThread.start();
					progressDialog.setVisible(true);
					AssertionVerifier assertionVerifier= assertionChecker.getAssertionVerifier();
					return assertionVerifier!=null? new TransitionTreeFromVerificationResults(mid, this, assertionVerifier): null;
				} else 
				if(coverageCriterion==DepthCoverage)
					return breadthFirstSearch? new TransitionTreeForDepthCoverageBFS(mid, this): 
						new TransitionTreeForDepthCoverageDFS(mid, this);
				else
				if(coverageCriterion==RandomGeneration)
					return new TransitionTreeRandomGenerator(mid, this);
				else
				if(coverageCriterion==DeadlockStateCoverage)
					return new TransitionTreeForDeadlockStateCoverage(mid, this);
			case THREATNET: 
				return breadthFirstSearch? new TransitionTreeForThreatNetBFS(mid,this): new TransitionTreeForThreatNetDFS(mid,this);
			case THREATTREE: 
				return new TransitionTreeForThreatTree(mid, this);
			default: 
				return new TransitionTreeForRoundTripBFS(mid, this);
		}
	}
	
	class GoalReachabilityChecker implements Runnable {
		private ProgressDialog progressDialog;
		private MID mid;
		private boolean searchForHomeStates;
		private GoalVerifier goalVerifier= null;
		
		GoalReachabilityChecker(MID mid, boolean searchForHomeStates, ProgressDialog progressDialog) {
			this.mid = mid;
			this.searchForHomeStates = searchForHomeStates;
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				goalVerifier = VerificationManager.getPropertyVerifier(mid, breadthFirstSearch, searchDepth, searchForHomeStates, progressDialog);
			}
			catch (CancellationException e){}			
			progressDialog.dispose();				
		}
		
		// return null if reachability analysis is cancelled
		public GoalVerifier getGoalVerifier(){ 
			return goalVerifier;
		}
	}

	class AssertionChecker implements Runnable {
		private MID mid;
		private ProgressDialog progressDialog;
		private AssertionVerifier verifier = null;
		
		AssertionChecker(MID mid, ProgressDialog progressDialog) {
			this.mid = mid;
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				verifier = breadthFirstSearch? new AssertionVerifierBFS(mid, searchDepth, progressDialog):
							new AssertionVerifierDFS(mid, searchDepth, progressDialog);
			}
			catch (CancellationException e){
			}			
			progressDialog.dispose();				
		}
		
		public AssertionVerifier getAssertionVerifier(){ 
			return verifier;
		}
	}

	public String[] getCoverageStringList(ModelType modelType){
		return getCoverageStrings(getCoverageList(modelType));
	}
	
	private String[] getCoverageStrings(CoverageCriterion[] coverages){
		String[] coverageStrings = new String[coverages.length];
		for (int i=0; i<coverages.length; i++)
			coverageStrings[i]=coverages[i].getName();
		return coverageStrings;
	}

	public int getCoverageIndex(ModelType givenModelType, CoverageCriterion givenCoverage){
		CoverageCriterion[] coverageList = getCoverageList(givenModelType); 
		for (int index=0; index<coverageList.length; index++)
			if (givenCoverage == coverageList[index])
				return index;
		return 0;
	}

	public int getCoverageIndex(ModelType givenModelType){
		return getCoverageIndex(givenModelType, coverageCriterion);
	}

	public void setCoverageCriterion(int selectedCoverageIndex, ModelType modelType){
		if (selectedCoverageIndex>=0) {
			CoverageCriterion[] coverageList = getCoverageList(modelType); 
			if (selectedCoverageIndex<coverageList.length){
				coverageCriterion = coverageList[selectedCoverageIndex];
			}
		}
	}
	
	public void setCoverageCriterion(String coverageString){
		coverageCriterion = getCoverageObject(coverageString);
	}

	public CoverageCriterion getCoverageCriterion(){
		return coverageCriterion;
	}

	public boolean areDirtyTestsNeeded(){
		return coverageCriterion == ReachabilityTreeWithDirtyTests;
	}
	
	public static CoverageCriterion getCoverageObject(String coverageString){
		CoverageCriterion coverage = getCoverageObject(coverageListForFunctionNets, coverageString);
		if (coverage!=null)
			return coverage;
		coverage = getCoverageObject(coverageListForThreatTrees, coverageString);
		if (coverage!=null)
			return coverage;
		else
			return ReachabilityTreeCoverage;
	}
	
	private static CoverageCriterion getCoverageObject(CoverageCriterion[] coverageList, String coverage){
		for (int index=0; index<coverageList.length; index++) 
			if (coverage.equalsIgnoreCase(coverageList[index].getID()) ||
				coverage.equalsIgnoreCase(coverageList[index].getName())	
				)
				return coverageList[index];
		return null;
	}

	/////////////////////////////////////////////////////////
	//language
	/////////////////////////////////////////////////////////	
	public String[] getAllLanguageNamesForSelection(){
		String[] names = new String[AllLanguages.length];
		for (int i=0; i<names.length; i++)
			names[i] = AllLanguages[i].getName();
		return names;
	}
	
	public void setLanguage(int index) {
		if (index<AllLanguages.length)
			language = AllLanguages[index];
		else
			language = AllLanguages[0];
		if (testFrameworkIndex>=language.getNumberOfTestFrameworks())
			testFrameworkIndex = 0;
	}

	public void setLanguage(TargetLanguage language){
		this.language = language;
		if (testFrameworkIndex>=language.getNumberOfTestFrameworks())
			testFrameworkIndex = 0;
	}
	
	public TargetLanguage getLanguage() {
		return language;
	}

	public static TargetLanguage getLanguageAt(int index) {
		assert index<AllLanguages.length;
		return AllLanguages[index];
	}

	public boolean isOOLanguage(){
		return language instanceof TargetLanguageOO;
	}

	public int getLanguageIndex() {
		for (int i=0; i<AllLanguages.length; i++)
			if (AllLanguages[i].equals(language))
				return i;
		return 0;
	}

	/////////////////////////////////////////////////////////
	//Test framework
	/////////////////////////////////////////////////////////	
	public void setTestFrameworkIndex(int unit){
		if (unit>=0) {
//			this.testFrameworkIndex = (testFrameworkIndex <language.getNumberOfTestFrameworks())? unit: 0; 
			this.testFrameworkIndex = (unit <language.getNumberOfTestFrameworks())? unit: 0; 
		}
	}

	public boolean useTestFramework(){
		return testFrameworkIndex>0 || language==TargetLanguage.PHP || language==TargetLanguage.PYTHON;
	} 
	
	public int getTestFrameworkIndex(){
		return testFrameworkIndex;
	}

	/////////////////////////////////////////////////////////
	//test code view and style
	/////////////////////////////////////////////////////////	
	public boolean viewTestCode(){
		return viewTestCode;
	}
	
	public void setViewTestCode(boolean toView){
		this.viewTestCode = toView;
	} 
	
	public boolean generateSeparateTestFiles(){
		return generateSeparateTestFiles;
	}

	public void setGenerateSeparateTestFile(boolean separateFiles){
		this.generateSeparateTestFiles = separateFiles;
	}

	public boolean includeSeqIndicesInTestID(){
		return includeSeqIndicesInTestID;
	}
	
	public void setIncludeSeqIndicesInTestID(boolean toIncludeSeqIndices){
		this.includeSeqIndicesInTestID = toIncludeSeqIndices;
	} 

	// abuse HTML header and testFrameworkPackage
	public String getHeader(){	// for HTML
		return getTestFrameworkPackage();
	}
	public String getTestFrameworkPackage(){
		return language.getTestFrameworkPackage(testFrameworkIndex);
	}
	
	//abuse HTML initialization code with the test class name of test framework
	public String getInitializatioCode(){
		return getTestFrameworkTestClass();
	}
	public String getTestFrameworkTestClass(){
		return language.getTestFrameworkTestClass(testFrameworkIndex);
	}
	
	/////////////////////////////////////////////////////////
	//Object references
	/////////////////////////////////////////////////////////	
	public boolean createObjectReference(){
		return createObjectReference;
	}
	
	public void setCreateObjectReference(boolean isClassTesting){
		this.createObjectReference = isClassTesting;
	} 

	public boolean generateReferenceForMethodCall() {
		return referenceForMethodCall; 
	}

	public void setGenerateReferenceForMethodCall(boolean generateReference) {
		this.referenceForMethodCall = generateReference; 
	}

	public boolean generateReferenceForAccessorCall() {
		return referenceForAccessorCall; 
	}

	public void setGenerateReferenceForAccessorCall(boolean generateReference) {
		this.referenceForAccessorCall = generateReference; 
	}

	public boolean generateReferenceForMutatorCall() {
		return referenceForMutatorCall; 
	}

	public void setGenerateReferenceForMutatorCall(boolean generateReference) {
		this.referenceForMutatorCall = generateReference; 
	}

	/////////////////////////////////////////////////////////
	//Test parameters
	/////////////////////////////////////////////////////////	
	
	public boolean generateTestParameters() {
		return generateTestParameters; 
	}

	public void setGenerateTestParameters(boolean flag) {
		generateTestParameters = flag; 
	}
	
	public void enabeIncludeAssertDefintionForC(boolean enabled){
		includeAssertDefinitionForC = enabled;
	}

	public boolean includeAssertDefintionForC(){
		return includeAssertDefinitionForC;
	}
	
	/////////////////////////////////////////////////////////
	//Test oracles
	/////////////////////////////////////////////////////////	

	public void setVerifyMarkings(boolean flag) {
		verifyMarkings = flag; 
	}
	
	public boolean verifyMarkings() {
		return verifyMarkings; 
	}

	public void setVerifyPostconditions(boolean flag) {
		verifyPostconditions = flag; 
	}
	
	public boolean verifyPostconditions() {
		return verifyPostconditions; 
	}

	public void setVerifyNegatedConditions(boolean flag) {
		verifyNegatedConditions = flag; 
	}
	
	public boolean verifyNegatedConditions() {
		return verifyNegatedConditions; 
	}

	public void setVerifyEffects(boolean flag) {
		verifyEffects = flag; 
	}
	
	public boolean verifyEffects() {
		return verifyEffects; 
	}

	public void setVerifyFirstOccurrence(boolean flag) {
		verifyFirstOccurrence = flag; 
	}
	
	public boolean verifyFirstOccurrence() {
		return verifyFirstOccurrence; 
	}

	public void setCreateGoalTags(boolean attachGoalTags) {
		this.createGoalTags = attachGoalTags; 
	}
	
	public boolean createGoalTags() {
		return createGoalTags; 
	}
	
	public void setGoalTagsAtBeginningOfTests(boolean beginning){
		this.goalTagsAtBeginningOfTests = beginning;
	}

	public boolean areGoalTagsAtBeginningOfTests(){
		return goalTagsAtBeginningOfTests;
	}
	
	public void setVerifyDirtyTestState(boolean flag) {
		verifyDirtyTestState = flag; 
	}
	
	public boolean verifyDirtyTestState() {
		return verifyDirtyTestState; 
	}

	public void setVerifyDirtyTestException(boolean flag) {
		verifyDirtyTestException = flag; 
	}
	
	public boolean verifyDirtyTestException() {
		return verifyDirtyTestException; 
	}

	public GoalTagCode getGoalTag(){
		for (GoalTagCode goalTag: goalTagsForAllTestFrameworks)
			if (goalTag.isForTestFramework(language, language.getTestFrameworkName(testFrameworkIndex)))
				return goalTag;
		return  null;
	}

	public String getTagCodeForTestFramework(){
		for (GoalTagCode goalTag: goalTagsForAllTestFrameworks)
			if (goalTag.isForTestFramework(language, language.getTestFrameworkName(testFrameworkIndex)))
				return goalTag.getTagCode();
		return "";
	}

	public boolean hasTagCodeForTestFramework(){
		for (GoalTagCode goalTag: goalTagsForAllTestFrameworks)
			if (goalTag.isForTestFramework(language, language.getTestFrameworkName(testFrameworkIndex)))
				return true;
		return false;
	}
	
	private void setDefaultTageCodeForAllTestFrameworks(){
		if (goalTagsForAllTestFrameworks==null)
			goalTagsForAllTestFrameworks = new ArrayList<GoalTagCode>();
		if (goalTagsForAllTestFrameworks.size()==0)
			for (int i=0; i<GoalTagCode.DEFAULT_GOAL_TAGS.length; i++)
				goalTagsForAllTestFrameworks.add(GoalTagCode.DEFAULT_GOAL_TAGS[i]);
				
	}


	public SystemOptions getSystemOptionForStateGeneration() {
		SystemOptions newOptions = new SystemOptions();
		newOptions.searchDepth = this.searchDepth;
		newOptions.breadthFirstSearch = true;
		newOptions.pairwiseTesting = false;
		newOptions.totalOrdering = true;
		newOptions.searchForHomeStates = true;
		return  newOptions;
	}

	
	public boolean listFailureTests(){
		return listFailureTests;
	};

	public void setListFailureTests(boolean listFailureTests){
		this.listFailureTests = listFailureTests;
	}

	public boolean findShortestPaths(){
		return findShortestPaths;
	};

	public void setFindShortestPaths(boolean findShortestPaths){
		this.findShortestPaths = findShortestPaths;
	}

	public boolean evaluateTransitionCoverage(){
		return evaluateTransitionCoverage;
	};

	public void setEvaluateTransitionCoverage(boolean evaluateTransitionCoverage){
		this.evaluateTransitionCoverage = evaluateTransitionCoverage;
	}

	public boolean evaluateStateCoverage(){
		return evaluateStateCoverage;
	};

	public void setEvaluateStateCoverage(boolean evaluateStateCoverage){
		this.evaluateStateCoverage = evaluateStateCoverage;
	}

	public boolean listCoveredStates(){
		return listCoveredStates;
	};

	public void setListCoveredStates(boolean toList){
		this.listCoveredStates = toList;
	}

	public boolean listUncoveredStates(){
		return listUncoveredStates;
	};

	public void setListUncoveredStates(boolean toList){
		this.listUncoveredStates = toList;
	}

	/////////////////////////////////////////////////////////
	//Persistence
	/////////////////////////////////////////////////////////	
	public void saveSystemOptionsToFile() {
		try {
			ReadWriteObj.write(this, SystemOptionsFileName);
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	public static SystemOptions readSystemOptionsFromFile(){
		SystemOptions systemOptions = null;
		try {
			systemOptions = (SystemOptions) (ReadWriteObj.read(SystemOptionsFileName));
			systemOptions.setLanguage(systemOptions.getLanguageIndex());
		}
		catch (Exception e) {
//			e.printStackTrace();
			systemOptions = new SystemOptions();
		}
		if (Kernel.IS_LIMITATION_SET){
			if (systemOptions.searchDepth > Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION)
				systemOptions.searchDepth = Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION;
			if (systemOptions.maxLevelOfNodeExpansion > Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION)
				systemOptions.maxLevelOfNodeExpansion = Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION;
			if (systemOptions.maxIdDepth>Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION)
				systemOptions.maxIdDepth=Kernel.MAX_SEARCH_DEPTH_FOR_LIMITATION;
			if (systemOptions.maxRandomTests > Kernel.MAX_RANDOM_TESTS_FOR_LIMITATION)
				systemOptions.maxRandomTests = Kernel.MAX_RANDOM_TESTS_FOR_LIMITATION;				
		}
		systemOptions.setDefaultTageCodeForAllTestFrameworks();
		return systemOptions;
	}
	
}
