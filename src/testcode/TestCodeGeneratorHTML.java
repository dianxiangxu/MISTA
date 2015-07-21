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
import testgeneration.TransitionTreeNode;
import utilities.FileUtil;

import mid.Marking;
import mid.Tuple;

public class TestCodeGeneratorHTML extends TestCodeGenerator {
	
	public TestCodeGeneratorHTML(TransitionTree transitionTree) {
		super(transitionTree);
	}
	
	public void saveTestsToSingleFile(ArrayList<TransitionTreeNode> allLeaves, PrintWriter testSuiteWriter) throws CancellationException {
		testSuiteWriter.print(systemOptions.getHeader());
		if (mid.hasAlphaBlock())
			testSuiteWriter.print(newLine+mid.getAlphaBlock().replace("\n", newLine));
		testSuiteWriter.print("\n<head profile=\"http://selenium-ide.openqa.org/profiles/test-case\">"+
				"\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"+
				"\n</head>"+
				"\n<body>");
		int testNo = 1;
		try {
			for (TransitionTreeNode leaf : allLeaves) {
				transitionTree.checkForCancellation();
				ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
				testSuiteWriter.print(generateTestCaseCode(testNo, testSequence) + newLine);
				testNo++;
			}
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
		testSuiteWriter.print(getTestSuiteEnd()); 
	}

	public void saveTestsToSeparateFiles(ArrayList<TransitionTreeNode> allLeaves, PrintWriter testSuiteWriter, File testSuiteFile) throws CancellationException{
		testSuiteWriter.print(systemOptions.getHeader());
		if (mid.hasAlphaBlock())
			testSuiteWriter.print(newLine+mid.getAlphaBlock().replace("\n", newLine));
		String filePrefix = FileUtil.getPrefix(testSuiteFile.getName());
		String fileExtension = FileUtil.getExtension(testSuiteFile.getName());
		String fileDir = testSuiteFile.getParent();		
		testSuiteWriter.print("\n<head>"+
				"\n\t<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\" />" +
				"\n\t<title>Test Suite</title>"+
				"\n</head>"+
				"\n<body>"+
				"\n<table id=\"suiteTable\" cellpadding=\"1\" cellspacing=\"1\" border=\"1\" class=\"selenium\"><tbody>"+
				"\n<tr><td><b>Test Suite</b></td></tr>\n");
		int testNo = 1;
		try {
			for (TransitionTreeNode leaf : allLeaves) {
				transitionTree.checkForCancellation();
				ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
				String testCaseCode = generateTestCaseCode(testNo, testSequence) + newLine; 
				String testFileName = filePrefix+getTestId(testNo, leaf)+"."+fileExtension;
				String testFilePath = fileDir+File.separator+testFileName;
				FileUtil.saveStringToTextFile(testCaseCode, testFilePath);
				String linkInSuite = "<tr><td><a href=\"" + testFileName +"\"> "+testFileName+" </a></td></tr>"+ newLine;
				testSuiteWriter.print(linkInSuite);
				testNo++;
			}	
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
		testSuiteWriter.print("<br />\n</tbody></table>");
		testSuiteWriter.print(getTestSuiteEnd()); 
	} 
	
	private String generateTestCaseCode(int testNo, ArrayList<TransitionTreeNode> testSequence) throws CancellationException{
		String  testCase = getInitializationCode(getTestId(testNo, testSequence));
		if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
				systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
			testCase += getGoalTagsAtBeginningOfTest(testSequence);
		testCase += getSetupCode(testSequence);
		for (int i=1; i<testSequence.size(); i++) {
			transitionTree.checkForCancellation();
			TransitionTreeNode currentNode = testSequence.get(i);
			testCase += preferSpeed? currentNode.getTestInputCode(): generateTestInputCodeForNode(currentNode);
			if (!systemOptions.verifyFirstOccurrence() || !currentNode.isTraversed())
				testCase += preferSpeed? currentNode.getTestOracleCode(): generateTestOracleCodeForNode(currentNode);
			currentNode.setTraversed(true);
		}
		testCase += newLine+ "</tbody></table>";
		testCase += getTearDownCode();
		return testCase;
	}
	
	private String getTestSuiteEnd(){
		String omegaCode = mid.hasOmegaBlock()? newLine+mid.getOmegaBlock().replace("\n", newLine): "";
		return omegaCode + "\n</body>\n</html>";
	}

	private String getInitializationCode(String testID){
		String initCode = systemOptions.getTestFrameworkTestClass();
		if (mid.getSystemName()!=null){
			initCode = initCode.replaceAll(TargetLanguage.varURL, mid.getSystemName());
		}			
		return initCode.replaceAll(TargetLanguage.varTESTID, testID);
	}
	
	
	// called when user clicks on a leaf of the tree
	// test sequence must start with an initial state node
	public String generateSequenceCodeForReview(ArrayList<TransitionTreeNode> testSequence) {
		String  testCase = getInitializationCode(getTestId(testSequence));
		if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
				systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
			testCase += getGoalTagsAtBeginningOfTest(testSequence);
		testCase += getSetupCode(testSequence);
		for (int i=1; i<testSequence.size(); i++) {
			TransitionTreeNode currentNode = testSequence.get(i);
			testCase += generateTestInputCodeForNode(currentNode)+
			generateTestOracleCodeForNode(currentNode);
		}
		testCase += newLine+ "</tbody></table>";
		testCase += getTearDownCode();
		return testCase;
	}
	
	
	private String getSetupCode(ArrayList<TransitionTreeNode> testSequence){
		int initStateIndex = Integer.parseInt(testSequence.get(0).getOutlineNumber())-1;
		assert initStateIndex>=0 && initStateIndex<setupDefinitions.length;
		return setupDefinitions[initStateIndex];
	}

	private String[] setupDefinitions = getAllSetupDefinitions();
	
	private String[] getAllSetupDefinitions(){
		Vector<TransitionTreeNode> initNodes = transitionTree.getRoot().children(); 
		String[] setUpMethods = new String[initNodes.size()];
		if (mid.hasSetUpCode()){
			String setUpCode = newLine+mid.getSetUpCode();
			for (int i=0; i<setUpMethods.length; i++)
				setUpMethods[i] = setUpCode;
		} else {
			for (int i=0; i<initNodes.size(); i++)
				setUpMethods[i] = getSetupCode(initNodes.get(i));
		}
		return setUpMethods;
	}

	private String getSetupCode(TransitionTreeNode initNode){
		String setupCode = "";
		Marking marking = initNode.getMarking(); 
		for (String place: marking.getPlaces()) {
			ArrayList<Tuple> tuples = marking.getTuples(place);
			for (Tuple tuple: tuples){
				if (!mid.isHidden(place))
				setupCode += newLine + getInitTupleCode(place, tuple);
			}
		}
		return setupCode;
	} 

	private String getTearDownCode(){
		return mid.hasTearDownCode()? newLine+mid.getTearDownCode():"";
	}
	
	protected String getInputActionCode(TransitionTreeNode currentNode) {
		return newLine + getInputActionExpression(currentNode);
	}

}
