/* 	
	Author Dianxiang Xu
*/
package testcode;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import kernel.CancellationException;

import testgeneration.TransitionTree;
import testgeneration.TransitionTreeForThreatNet;
import testgeneration.TransitionTreeNode;
import utilities.FileUtil;

import mid.MID;
import mid.Marking;
import mid.Tuple;

public class TestCodeGeneratorC extends TestCodeGenerator {
	
	public TestCodeGeneratorC(TransitionTree transitionTree) {
		super(transitionTree);
		tab = "\t";
	}

	public void saveTestsToSingleFile(ArrayList<TransitionTreeNode> allTests, PrintWriter testSuiteWriter) throws CancellationException {
		try {
			testSuiteWriter.print("/*"+TestCodeMessage+"*/\n\n");
			testSuiteWriter.print(getHeader());
			testSuiteWriter.print(getHelperCode());
			testSuiteWriter.print(getSetupCode()); 
			testSuiteWriter.print(getTearDownCode());
			testSuiteWriter.print(getAssertDefinition()); 
			getTestCaseSegment(allTests, testSuiteWriter);
			getTestSuiteSegment(allTests, testSuiteWriter); 	// include alpha/omega code
			testSuiteWriter.print(getTestSuiteDriverCode()); 		
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}

	}

	public void saveTestsToSeparateFiles(ArrayList<TransitionTreeNode> allTests, PrintWriter testSuiteWriter, File testSuiteFile) throws CancellationException{
		testSuiteWriter.print("/*Tests are saved in the following files*/\n");
		String filePrefix = FileUtil.getPrefix(testSuiteFile.getName());
		String fileExtension = FileUtil.getExtension(testSuiteFile.getName());
		String fileDir = testSuiteFile.getParent();		
		String header = getHeader();
		String helper = getHelperCode();
		String[] setupDefitnitions = getAllSetupDefinitions();
		String tearDown = getTearDownCode();
		String assertDefinition = getAssertDefinition();
		int testNo = 1;
		try {
		for (TransitionTreeNode leaf : allTests) {
			transitionTree.checkForCancellation();
			ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
			StringBuffer testCodeBuffer = new StringBuffer();
			testCodeBuffer.append("/*"+TestCodeMessage+"*/\n\n");
			testCodeBuffer.append(header);
			testCodeBuffer.append(helper);
			int initStateIndex = Integer.parseInt(testSequence.get(0).getOutlineNumber())-1;
			testCodeBuffer.append(setupDefitnitions[initStateIndex]);
			testCodeBuffer.append(tearDown);
			testCodeBuffer.append(assertDefinition);		
			testCodeBuffer.append(generateTestMethod(testNo, testSequence));
			testCodeBuffer.append(newLine); 
			testCodeBuffer.append(getTestCaseDriverCode(getTestId(testNo, leaf)));
			String testFileName = filePrefix+getTestId(testNo, leaf)+"."+fileExtension;
			String testFilePath = fileDir+File.separator+testFileName;
			FileUtil.saveStringToTextFile(testCodeBuffer.toString(), testFilePath);
			testSuiteWriter.print(testFileName+"\n");
			testNo++;
		}		
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
	}
	
	// called when user clicks on a leaf of the tree
	// test sequence must start with an initial state node
	public String generateSequenceCodeForReview(ArrayList<TransitionTreeNode> testSequence) {
		newLine = "\n";
		String methodBody = newLine + tab +"printf(\"Test case "+ getTestId(testSequence) + "\\n\");";
		methodBody += generateSetupCallCode(testSequence.get(0));
		for (int i=1; i<testSequence.size(); i++) {
			TransitionTreeNode currentNode = testSequence.get(i);
			methodBody += generateTestInputCodeForNode(currentNode)
						+ generateTestOracleCodeForNode(currentNode);
		}
		if (mid.hasTearDownCode())
			methodBody += newLine + tab + "tearDown();";
		return getTestMethodSignature(testSequence) + "{" + methodBody + newLine + "}";
	}
	
	protected String getHeader(){
		return mid.hasImportBlock()? mid.getImportBlock()+"\n": "";
	}


	protected void getTestCaseSegment(ArrayList<TransitionTreeNode> allLeaves, PrintWriter out) throws CancellationException{
		int testNo=1;
		for (TransitionTreeNode leaf : allLeaves) {
			transitionTree.checkForCancellation();
			ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
			out.print(generateTestMethod(testNo, testSequence) + "\n");
			testNo++;
		}
	}

	protected String getAlphaCode(){
		return mid.hasAlphaBlock()?
				newLine + mid.getAlphaBlock().replace("\n", "\n\t") +"\n": 
				"";
	} 

	protected String getOmegaCode(){
		return mid.hasOmegaBlock()?
				newLine+ newLine+ tab + mid.getOmegaBlock().replace("\n", "\n\t"):
				"";
	} 
	
	protected String getTestMethodNamePrefix(){
		return "test";
	}

	protected void getTestSuiteSegment(ArrayList<TransitionTreeNode> leaves, PrintWriter out) throws CancellationException {
		out.print(newLine+"void testAll(){");
		out.print(newLine + tab + getAlphaCode().replace("\n", newLine+tab));
		int testNo =1;
		for (TransitionTreeNode leafNode : leaves){
			transitionTree.checkForCancellation();
			out.print(newLine + tab + getTestMethodNamePrefix() + getTestId(testNo, leafNode) + "();");
			testNo++;
		}
		out.print(newLine + tab + getOmegaCode().replace("\n", newLine+tab));
		out.print(newLine+"}\n");
	}

	protected String getTestSuiteDriverCode(){
		return newLine+"int main(void){" 
			+ newLine+tab+"testAll();"
			+ newLine+tab+"printf(\"\\n\\n\");"
			+ newLine+tab+"system(\"PAUSE\");"
			+ newLine+tab+"return 0;"
			+ newLine+"}\n";		
	}
	
	protected String getTestCaseDriverCode(String testID){
		return newLine+"int main(void){" 
			+ newLine+tab+getTestMethodNamePrefix() + testID + "();"
			+ newLine+tab+"printf(\"\\n\\n\");"
//			+ newLine+tab+"system(\"PAUSE\");"
			+ newLine+tab+"return 0;"
			+ newLine+"}\n";		
	}

	
	protected String getTearDownCode(){
		return mid.hasTearDownCode()? mid.getTearDownCode():"";
	}
	
	protected String getAssertDefinition(){
		return !transitionTree.getSystemOptions().includeAssertDefintionForC() || transitionTree instanceof TransitionTreeForThreatNet? "":
		newLine+"void assert(int condition, char *errorMessage) {"
		+newLine+"\tif (!condition){"
		+newLine+"\t\tprintf(\"%s\\n\\n\", errorMessage);"
		+newLine+"\t\tsystem(\"PAUSE\");"
		+newLine+"\t\texit(0);"
		+newLine+"\t}"
		+newLine+"}\n";	
	}
	
	protected String getHelperCode(){
		return mid.hasHelperCode()? "\n\n"+mid.getHelperCode(): "";
	}
	
	protected String[] getAllSetupDefinitions(){
		Vector<TransitionTreeNode> initNodes = transitionTree.getRoot().children(); 
		String[] setUpMethods = new String[initNodes.size()];
		if (mid.hasSetUpCode()){
			String setUpCode = "\n"+newLine+mid.getSetUpCode().replaceAll("\n", newLine);
			for (int i=0; i<setUpMethods.length; i++)
				setUpMethods[i] = setUpCode;
		} else {
			for (int i=0; i<initNodes.size(); i++)
				setUpMethods[i] = getSetupCode(initNodes.get(i), "");
		}
		return setUpMethods;
	}

	protected String getSetupCode(){
		String setUpAttribute = "\n";
		// user-defined setUp method
		if (mid.hasSetUpCode())
			return setUpAttribute+mid.getSetUpCode();
		// if no user-defined setup  
		// generate setUp method from each of the initial states
		Vector<TransitionTreeNode> initNodes = transitionTree.getRoot().children(); 
		String setupCode = "";
		for (int i=0; i<initNodes.size(); i++){
			String index= i==0? "": ""+(i+1);
			setupCode += getSetupCode(initNodes.get(i), index);
		}
		return setUpAttribute+setupCode;
	}
	
	protected String getSetupCode(TransitionTreeNode initNode, String index){
		Marking marking = initNode.getMarking(); 
		String setupCode = newLine+"void setUp" + index	+ "(){";
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
		setupCode += newLine+"}"+newLine;
		return setupCode;
	} 

	// test sequence must start with an initial state node
	protected String generateTestMethod(int testNo, ArrayList<TransitionTreeNode> testSequence) throws CancellationException {
		String name = transitionTree instanceof TransitionTreeForThreatNet? "Attack":"Test case";
		String methodBody = newLine + tab +"printf(\""+ name+ " "+ getTestId(testNo, testSequence) + "\\n\");";
		methodBody += generateSetupCallCode(testSequence.get(0));
		for (int i=1; i<testSequence.size(); i++) {
			transitionTree.checkForCancellation();
			TransitionTreeNode currentNode = testSequence.get(i);
			methodBody += preferSpeed? currentNode.getTestInputCode(): generateTestInputCodeForNode(currentNode);
			if (!systemOptions.verifyFirstOccurrence() || !currentNode.isTraversed())
				methodBody += preferSpeed? currentNode.getTestOracleCode(): generateTestOracleCodeForNode(currentNode);
			currentNode.setTraversed(true);
		}
		if (transitionTree instanceof TransitionTreeForThreatNet)
			methodBody += newLine + tab +"printf(\"Attack "+ getTestId(testNo, testSequence) + " succeeded!\\n\");";
		if (mid.hasTearDownCode())
			methodBody += newLine +tab+"tearDown();";
		return getTestMethodSignature(testNo, testSequence) + "{" + methodBody + newLine + "}";
	}
	
	protected String getInputActionCode(TransitionTreeNode currentNode) {
		String inputActionCode = getInputActionExpression(currentNode);
		inputActionCode = newLine + tab + inputActionCode.replaceAll("\n", newLine+tab);
		String guard = currentNode.getTransition().getGuard(); 
		if ( guard!= null && guard.length()>0)
			inputActionCode += tab+"//constraint: " + guard;
		return inputActionCode;
	}

	protected String getTestMethodSignature(int testNo, ArrayList<TransitionTreeNode> testSequence){
		return newLine + "void " + getTestMethodNamePrefix()+getTestId(testNo, testSequence) + "() ";
	}
	
	protected String getTestMethodSignature(ArrayList<TransitionTreeNode> testSequence){
		return newLine + "void " + getTestMethodNamePrefix()+getTestId(testSequence) + "() ";
	}

	protected String generateSetupCallCode(TransitionTreeNode currentNode){
		assert currentNode.getEvent().equalsIgnoreCase(MID.ConstructorEvent); 
		String outlineNumber = currentNode.getOutlineNumber();
		String setupIndex = systemOptions.generateSeparateTestFiles() || outlineNumber.charAt(0)=='1' ? "": outlineNumber;
		return newLine +tab+"setUp"+setupIndex+"();";
	}
		
	// overriding 
	protected String normalizeSetupCode(String code) {
//		return !code.endsWith(";")? code+";": code;
		return code;
	}

	// overriding
	protected String assertPredicate(String testID, String condition, boolean needNegation){
		String trimmedCondition = condition.trim();
		if (transitionTree instanceof TransitionTreeForThreatNet) {
			if (trimmedCondition.endsWith(";") || trimmedCondition.endsWith("}"))
				return newLine + tab + trimmedCondition.replaceAll("\n", newLine + tab);
			String normalizedCondition = needNegation? trimmedCondition: "!(" + trimmedCondition +")";
			return tab + "if ("+normalizedCondition+")" + newLine + tab + "\treturn;";
		} else {
			String message = "\""+ testID+"\""; 
			String normalizedCondition = !needNegation? trimmedCondition: "!(" + trimmedCondition +")";
			return tab + "assert("+normalizedCondition+", "+ message+");";
		}
	} 
	
	// overriding
	protected String normalizeEffectCode(String code){
		String effect  = code;
//		if (!effect.trim().endsWith(";")) 
//			effect += ";";
		return tab + effect;
	}
	
}
