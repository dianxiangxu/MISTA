/* 	
	Author Dianxiang Xu
*/
package testcode;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import kernel.CancellationException;

import testgeneration.ParaTableModel;
import testgeneration.TransitionTree;
import testgeneration.TransitionTreeNode;
import utilities.FileUtil;

import locales.LocaleBundle;
import mid.MID;
import mid.Marking;
import mid.Tuple;

public class TestCodeGeneratorOO extends TestCodeGenerator{
	
	protected TargetLanguageOO language;
	
	public TestCodeGeneratorOO(TransitionTree transitionTree) {
		super(transitionTree);
		assert systemOptions.isOOLanguage();
		tab = "\t";
		language = systemOptions.isOOLanguage()? 
				(TargetLanguageOO)systemOptions.getLanguage():
				(TargetLanguageOO)TargetLanguage.JAVA;
		if (language==TargetLanguage.JAVA)
				((TargetLanguageJava)language).setJUnit4(systemOptions.getTestFrameworkIndex()==1); // junit4?

	}
	

	public void saveTestsToSingleFile(ArrayList<TransitionTreeNode> allTests, PrintWriter testSuiteWriter) throws CancellationException {
		try {
			testSuiteWriter.print(getGreetingCode());
			testSuiteWriter.print(getPackageAndImportCode());
			if (systemOptions.useTestFramework())
				testSuiteWriter.print(language.getTestFixtureAttribute());
			testSuiteWriter.print(getClassSignature(FileUtil.getTestClassName(transitionTree))); 
			testSuiteWriter.print(createObjectVariableDeclaration());
			testSuiteWriter.print(getSetupCode()); 
			testSuiteWriter.print(getTearDownCode());
			testSuiteWriter.print(language.getAssertDefinition()); 
			if (mid.hasHelperCode())
				testSuiteWriter.print("\n"+newLine+mid.getHelperCode().replaceAll("\n", newLine));
			getTestCaseSegment(allTests, testSuiteWriter);
			getTestSuiteSegment(allTests, testSuiteWriter); 	// include alpha/omega code
			testSuiteWriter.print(language.createMainAndClassEnding(newLine, tab, FileUtil.getTestClassName(transitionTree), !systemOptions.useTestFramework())); 
			if (mid.hasPackageBlock()) 
				testSuiteWriter.print(language.getEndOfNameSpace());
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
	}

	public void saveTestsToSeparateFiles(ArrayList<TransitionTreeNode> allTests, PrintWriter testSuiteWriter, File testSuiteFile) throws CancellationException{
		
		String filePrefix = FileUtil.getPrefix(testSuiteFile.getName());
		String fileExtension = FileUtil.getExtension(testSuiteFile.getName());
		String fileDir = testSuiteFile.getParent();		
		String basicClassName = FileUtil.getTestClassName(transitionTree);
		String packageAndImportCode = getPackageAndImportCode();
		String[] setupDefinitions = getAllSetupDefinitions();
		String tearDownCode = getTearDownCode();
		String assertDefinition = language.getAssertDefinition();
		String helperCode = mid.hasHelperCode()?
			"\n"+newLine+mid.getHelperCode().replaceAll("\n", newLine): "";
		String endOfNameSpace = mid.hasPackageBlock()? language.getEndOfNameSpace():"";
		
		testSuiteWriter.print(language.getLineCommentToken()+TestCodeMessage+"\n\n");
		testSuiteWriter.print(packageAndImportCode);
		testSuiteWriter.print(getClassSignature(FileUtil.getTestClassName(transitionTree)));
		
		String callsInTestSuite = getAlphaCode();
		int testNo = 1;
		try {
		for (TransitionTreeNode leaf : allTests) {
			if (transitionTree.getProgressDialog()!=null){
				transitionTree.getProgressDialog().setMessage(LocaleBundle.bundleString("Generating test code")+testNo+"/"+allTests.size());
				transitionTree.checkForCancellation();
			}
			ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
			String testClassName = basicClassName+testNo;
			String testID = getTestId(testNo, leaf);
			StringBuffer testCodeBuffer = new StringBuffer();
			testCodeBuffer.append(language.getLineCommentToken()+TestCodeMessage+"\n\n");
			testCodeBuffer.append(packageAndImportCode);
			if (systemOptions.useTestFramework())
				testCodeBuffer.append(language.getTestFixtureAttribute());
			testCodeBuffer.append(getClassSignature(testClassName));
			testCodeBuffer.append(createObjectVariableDeclaration());
			int initStateIndex = Integer.parseInt(testSequence.get(0).getOutlineNumber())-1;
			testCodeBuffer.append(setupDefinitions[initStateIndex]);
			testCodeBuffer.append(tearDownCode);
			testCodeBuffer.append(assertDefinition);		
			String testAttribute = systemOptions.useTestFramework()? language.getTestAttribute(): "";
			testCodeBuffer.append(testAttribute+generateTestMethod(testNo, testSequence));
			testCodeBuffer.append(newLine); 
			testCodeBuffer.append(helperCode);
			String testCall = newLine+tab+language.getTestMethodCall(testClassName, ""+testNo, testID).replace("\n", newLine+tab);
			if (systemOptions.useTestFramework())
				testCodeBuffer.append(language.endClassWithoutMainMathod());
			else
				testCodeBuffer.append(language.endClassWithMainMathod(newLine, tab, testCall)); 
			testCodeBuffer.append(endOfNameSpace);
			String testFileName = filePrefix+testID+"."+fileExtension;
			String testFilePath = fileDir+File.separator+testFileName;
			FileUtil.saveStringToTextFile(testCodeBuffer.toString(), testFilePath);

			callsInTestSuite += testCall;
			
			testNo++;
		}
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
		if (mid.hasOmegaBlock())
			callsInTestSuite += newLine+ tab + mid.getOmegaBlock().replace("\n", newLine+tab);
		testSuiteWriter.print(language.endClassWithMainMathod(newLine, tab, callsInTestSuite));
		testSuiteWriter.print(endOfNameSpace);
	}

	// called when user clicks on a leaf of the tree
	// test sequence must start with an initial state node
	public String generateSequenceCodeForReview(ArrayList<TransitionTreeNode> testSequence) {
		newLine = "\n";
		String methodBody = newLine + tab +language.printLine("\"Test case " + getTestId(testSequence) + "\"");
		if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
				systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
			methodBody += getGoalTagsAtBeginningOfTest(testSequence);
		methodBody += generateSetupCallCode(testSequence.get(0));
		for (int i=1; i<testSequence.size(); i++) {
			TransitionTreeNode currentNode = testSequence.get(i);
			methodBody += generateTestInputCodeForNode(currentNode)
						+ generateTestOracleCodeForNode(currentNode);
		}
		if (mid.hasTearDownCode() && !systemOptions.useTestFramework())
			methodBody += newLine + tab +language.getTearDownMethodName()+"()"+language.getEndOfStatement();
		return newLine + language.getTestMethodSignature(getTestId(testSequence))
			+ language.getMethodBodyStart() 
			+ methodBody + newLine 
			+ language.getMethodBodyEnd();
	}

	private String getGreetingCode(){
		String message = language.getLineCommentToken()+ TestCodeMessage+"\n\n";
		if (systemOptions.useTestFramework() && transitionTree.getRoot().children().size()>1 && !mid.hasSetUpCode())
			message = language.getLineCommentToken()+"Warning: The same setUp method is used for multiple initial states.\n\n";
		return message;
	}
	
	private String getPackageAndImportCode(){
		String packageCode = "";
		if (mid.hasPackageBlock())
			packageCode += mid.getPackageBlock();
		
		String importCode = mid.hasImportBlock()? mid.getImportBlock()+"\n": "";
		if (systemOptions.useTestFramework())
			importCode += systemOptions.getTestFrameworkPackage()+"\n";

		if (!importCode.equals(""))
			importCode += "\n";
		
		return language.getHeader(packageCode, importCode);
	}

	private String getClassSignature(String testClassName){
		String inheritance = systemOptions.useTestFramework()? " "+systemOptions.getTestFrameworkTestClass(): "";
		return language.getTestClassSignature(testClassName, inheritance)+"\n";
	}

	protected String createObjectVariableDeclaration(){
		return newLine + language.getVariableDeclaration(mid.getSystemName())+newLine;
	}
	
	protected String getAlphaCode(){
		return mid.hasAlphaBlock()? newLine +tab+ mid.getAlphaBlock().replace("\n", newLine+tab):"";
	} 

	private void getTestCaseSegment(ArrayList<TransitionTreeNode> allLeaves, PrintWriter out) throws CancellationException {
		String testAttribute = systemOptions.useTestFramework()?
				language.getTestAttribute(): "";
		int testNo = 1;		
		for (TransitionTreeNode leaf : allLeaves) {
			if (transitionTree.getProgressDialog()!=null) {
				transitionTree.getProgressDialog().setMessage(LocaleBundle.bundleString("Generating test code")+testNo+"/"+allLeaves.size());
				transitionTree.checkForCancellation();
			}
			out.print(testAttribute);
			ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
			out.print(generateTestMethod(testNo, testSequence) + "\n");
			testNo++;
		}
	}

	private void getTestSuiteSegment(ArrayList<TransitionTreeNode> leaves, PrintWriter out) throws CancellationException {
		if (!systemOptions.useTestFramework() || systemOptions.getLanguage()==TargetLanguage.PYTHON){ 
			out.print(newLine+ language.getTestSuiteMethodSignature() +
					language.getMethodThrowException() + language.getMethodBodyStart());
			out.print(getAlphaCode());
			String testClassName = FileUtil.getTestClassName(transitionTree);
			int testNo = 1;
			if (systemOptions.getLanguage()==TargetLanguage.PYTHON){
				out.print("\n\tsuite = unittest.TestSuite()");
			}					
			for (TransitionTreeNode leafNode : leaves){
				transitionTree.checkForCancellation();
				if (systemOptions.getLanguage()==TargetLanguage.PYTHON)
					out.print(newLine + language.getTestMethodCall(testClassName, ""+testNo, getTestId(testNo, leafNode)));					
				else
					out.print(newLine + tab + language.getTestMethodCall(getTestId(testNo, leafNode)));
				testNo++;
			}
			if (systemOptions.getLanguage()==TargetLanguage.PYTHON)
				out.print("\n\tunittest.TextTestRunner(verbosity=2).run(suite)");			
			if (mid.hasOmegaBlock())
				out.print(newLine+ tab + mid.getOmegaBlock().replace("\n", newLine+tab));
			out.print(newLine+language.getMethodBodyEnd()+"\n");
		}
	}

	/*
	private String getTestCaseDriver(String testID) {
		if (!systemOptions.useTestFramework()){ 
			StringBuffer codeBuf = new StringBuffer();
			codeBuf.append(newLine+ language.getTestSuiteMethodSignature() +
					language.getMethodThrowException() + language.getMethodBodyStart());
			codeBuf.append(newLine + tab + language.getTestMethodNamePrefix() + testID + "()"+language.getEndOfStatement());
			if (mid.hasOmegaBlock())
				codeBuf.append(newLine+ newLine+ tab + mid.getOmegaBlock().replace("\n", newLine));
			codeBuf.append(newLine+language.getMethodBodyEnd()+"\n");
			return codeBuf.toString();
		}
		return "";
	}

*/
	private String getTearDownCode(){
		String tearDownAttribute = systemOptions.useTestFramework()?
				formatAttribute(language.getTearDownAttribute()): "";
		return mid.hasTearDownCode()? tearDownAttribute+newLine+mid.getTearDownCode().replaceAll("\n", newLine)+"\n":"";
	}
	
	private String formatAttribute(String attribute){
		return attribute.equals("")? "": "\n\t"+attribute;
	}
	
	private String getSetupCode(){
		String setUpAttribute = systemOptions.useTestFramework()? formatAttribute(language.getSetUpAttribute()):"";
		// user-defined setUp method
		if (mid.hasSetUpCode())
			return setUpAttribute+newLine+mid.getSetUpCode().replaceAll("\n", newLine)+"\n";
		// if no user-defined setup  
		// generate setUp method from each of the initial states
		Vector<TransitionTreeNode> initNodes = transitionTree.getRoot().children(); 
		String setupCode = "";
		for (int i=0; i<initNodes.size(); i++){
			String index= i==0? "": ""+(i+1);
			setupCode += setUpAttribute+getSetupCode(initNodes.get(i), index);
		}
		return setupCode;
	}
	
	private String[] getAllSetupDefinitions(){
		String setUpAttribute = systemOptions.useTestFramework()?
				formatAttribute(language.getSetUpAttribute()): "";
		Vector<TransitionTreeNode> initNodes = transitionTree.getRoot().children(); 
		String[] setUpMethods = new String[initNodes.size()];
		if (mid.hasSetUpCode()){
			// user-defined setUp method
			String setUpCode = setUpAttribute+newLine+mid.getSetUpCode().replaceAll("\n", newLine)+"\n";
			for (int i=0; i<setUpMethods.length; i++)
				setUpMethods[i] = setUpCode;
		} else {
			// if no user-defined setup  
			// generate setUp method from each of the initial states
			for (int i=0; i<initNodes.size(); i++)
				setUpMethods[i] = setUpAttribute+getSetupCode(initNodes.get(i), "");
		}
		return setUpMethods;
	}

	protected String getSetupCode(TransitionTreeNode initNode, String index){
		Marking marking = initNode.getMarking(); 
		String setupCode = newLine+language.getSetUpSignature() 
			+ index	+ "() " + language.getMethodThrowException()
			+ language.getMethodBodyStart();
		setupCode += generateObjectConstructionCode(initNode);
		for (String place: marking.getPlaces()) {
			ArrayList<Tuple> tuples = marking.getTuples(place);
			for (Tuple tuple: tuples) {
				if (!mid.isHidden(place)) {
					String code = getInitTupleCode(place, tuple);
					if (!code.equals(""))
						setupCode += newLine + tab + code;
				}
			}
		}
		setupCode += newLine+language.getMethodBodyEnd()+newLine;
		return setupCode;
	} 

	// for setUp method definition
	protected String generateObjectConstructionCode(TransitionTreeNode currentNode) {
		String className = mid.getSystemName();
		ParaTableModel paraTable = currentNode.getParaTable();
		String statements = getUserProvidedStatements(currentNode.getParaTable().getNonParaStrings());
		String paraString = paraTable.getParaString();
		if (paraString.length()==0) // parameters defined in the input file
			paraString = mid.getParameterString(currentNode.getEvent());;
		return statements + newLine + tab + language.getDefaultObjectVariableInitDeclaration(className) 
				+ " = "+language.getNewOperator()+" " + className + language.objectConstructionParaString(paraString)+language.getEndOfStatement();
	}

		
	// test sequence must start with an initial state node
	private String generateTestMethod(int testNo, ArrayList<TransitionTreeNode> testSequence) throws CancellationException {
		String methodBody = newLine + tab +language.printLine("\"Test case " + getTestId(testNo, testSequence) + "\"");
		if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
				systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
			methodBody += getGoalTagsAtBeginningOfTest(testSequence);
		methodBody += generateSetupCallCode(testSequence.get(0));
		for (int i=1; i<testSequence.size(); i++) {
			transitionTree.checkForCancellation();
			TransitionTreeNode currentNode = testSequence.get(i);
			methodBody += preferSpeed? currentNode.getTestInputCode(): generateTestInputCodeForNode(currentNode);
			if (!systemOptions.verifyFirstOccurrence() || !currentNode.isTraversed())
				methodBody += preferSpeed? currentNode.getTestOracleCode(): generateTestOracleCodeForNode(currentNode);
			currentNode.setTraversed(true);
		}
		if (mid.hasTearDownCode() && !systemOptions.useTestFramework())
			methodBody += newLine +tab+language.getTearDownMethodName()+"()"+language.getEndOfStatement();
		return newLine+language.getTestMethodSignature(getTestId(testNo, testSequence)) + language.getMethodBodyStart() + methodBody + newLine + language.getMethodBodyEnd();
	}
	
	protected String getInputActionCode(TransitionTreeNode currentNode) {
		String inputActionCode = getInputActionExpression(currentNode);
		inputActionCode = newLine + tab + inputActionCode+language.getEndOfStatement();
		String guard = currentNode.getTransition().getGuard(); 
		if ( guard!= null && guard.length()>0)
			inputActionCode += tab+language.getLineCommentToken()+"constraint: " + guard;
		return inputActionCode;
	}

	private String generateSetupCallCode(TransitionTreeNode currentNode){
		assert currentNode.getEvent().equalsIgnoreCase(MID.ConstructorEvent); 
		String	setupCode = "";
		if (!systemOptions.useTestFramework()){
			String outlineNumber = currentNode.getOutlineNumber();
			String setupIndex = systemOptions.generateSeparateTestFiles() || outlineNumber.length()==0 || outlineNumber.charAt(0)=='1'? "": outlineNumber;
			setupCode += newLine +tab+language.getSetUpMethodName()+setupIndex+"()"+language.getEndOfStatement();
		}
		return setupCode;
	}
	
	// overriding
	protected String getExceptionHandlingCode(TransitionTreeNode currentNode, String testInputCode) {
		return currentNode.isNegative() && systemOptions.verifyDirtyTestException()?
			language.getExceptionHandlingCode(newLine, tab, testInputCode, currentNode.getTestCaseId()):
			testInputCode;
	} 
	
	// overriding 
	protected String normalizeSetupCode(String code) {
		String normalizedCode = code;
		if (systemOptions.generateReferenceForMutatorCall())
			normalizedCode = getInputActionObjectReference(code);
		String endOfStatement = language.getEndOfStatement();
		if (!endOfStatement.equals("") && !normalizedCode.endsWith(endOfStatement))
			normalizedCode += endOfStatement;
		return normalizedCode;
	}

	// overriding
	protected String assertPredicate(String testID, String condition, boolean needNegation){
		String message = "\""+ testID+"\""; 
		String normalizedCondition = normalizeOracleCode(condition);
		if (needNegation)
			normalizedCondition = language.getNegationToken()+" (" + normalizedCondition +")";
		return tab + language.generateAssertStatement(systemOptions.useTestFramework(), normalizedCondition, message);
	} 
	
	//overriding
	protected String normalizeEffectCode(String code){
		String effect = code.trim();
		String endOfStatement = language.getEndOfStatement();
		if (!endOfStatement.equals("") && !effect.endsWith(endOfStatement)) 
			effect += endOfStatement;
		return tab + effect;
	}

	private String normalizeOracleCode(String code) {
		if (!systemOptions.generateReferenceForAccessorCall())
			return code;
		String normalizedCode = code; 
		String negationToken = language.getNegationToken();
		String not = "";
		if (code.startsWith(negationToken)) {
			not = negationToken;
			normalizedCode = code.substring(negationToken.length(), code.length());
		}  
		normalizedCode = getInputActionObjectReference(normalizedCode);
		return not +  normalizedCode;
	}

}
