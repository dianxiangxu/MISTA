/* 	
	Author Dianxiang Xu
*/
package testcode;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import kernel.CancellationException;

import testgeneration.TransitionTree;
import testgeneration.TransitionTreeNode;
import utilities.FileUtil;

public class TestCodeGeneratorKBT extends TestCodeGenerator {
	
	public TestCodeGeneratorKBT(TransitionTree transitionTree) {
		super(transitionTree);
		tab = "\t";
	}
	
	public void saveTestsToSingleFile(ArrayList<TransitionTreeNode> allLeaves, PrintWriter testSuiteWriter) throws CancellationException {
		testSuiteWriter.print(getHeader()+"*** Testcases ***\n");
		int testNo = 1;
		try {
			for (TransitionTreeNode leaf : allLeaves) {
				transitionTree.checkForCancellation();
				ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
				testSuiteWriter.print("test"+testNo);	
				if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
						systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
					testSuiteWriter.print(getGoalTagsAtBeginningOfTest(testSequence));
				testSuiteWriter.print(generateTestCaseCode(testNo, testSequence) + newLine);
				testNo++;
			}
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
	}

	public void saveTestsToSeparateFiles(ArrayList<TransitionTreeNode> allLeaves, PrintWriter testSuiteWriter, File testSuiteFile) throws CancellationException{
		testSuiteWriter.print(systemOptions.getHeader());
		String filePrefix = FileUtil.getPrefix(testSuiteFile.getName());
		String fileExtension = FileUtil.getExtension(testSuiteFile.getName());
		String fileDir = testSuiteFile.getParent();		
		int testNo = 1;
		try {
			for (TransitionTreeNode leaf : allLeaves) {
				transitionTree.checkForCancellation();
				ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
				String testCaseCode = getHeader()
						+"*** Testcases ***\n"
						+"test"+testNo;
				if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
								systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
						testCaseCode += getGoalTagsAtBeginningOfTest(testSequence);
				testCaseCode += generateTestCaseCode(testNo, testSequence) + newLine; 
				String testFileName = filePrefix+getTestId(testNo, leaf)+"."+fileExtension;
				String testFilePath = fileDir+File.separator+testFileName;
				FileUtil.saveStringToTextFile(testCaseCode, testFilePath);
				String linkInSuite = testFileName+ newLine;
				testSuiteWriter.print(linkInSuite);
				testNo++;
			}	
		} catch (CancellationException e){
			testSuiteWriter.close();
			throw e;
		}
	} 
	
	private String generateTestCaseCode(int testNo, ArrayList<TransitionTreeNode> testSequence) throws CancellationException{
		String  testCase = "\t";
		for (int i=1; i<testSequence.size(); i++) {
			transitionTree.checkForCancellation();
			TransitionTreeNode currentNode = testSequence.get(i);
			testCase += preferSpeed? currentNode.getTestInputCode(): generateTestInputCodeForNode(currentNode);
			if (!systemOptions.verifyFirstOccurrence() || !currentNode.isTraversed())
				testCase += preferSpeed? currentNode.getTestOracleCode(): generateTestOracleCodeForNode(currentNode);
			currentNode.setTraversed(true);
		}
		return testCase;
	}
	
	public String generateSequenceCodeForReview(ArrayList<TransitionTreeNode> testSequence) {
		String  testCase = "test";
		if (mid.getGoalProperties().size()>0 && systemOptions.createGoalTags() && 
				systemOptions.hasTagCodeForTestFramework() && systemOptions.areGoalTagsAtBeginningOfTests())
			testCase += getGoalTagsAtBeginningOfTest(testSequence);
		for (int i=1; i<testSequence.size(); i++) {
			TransitionTreeNode currentNode = testSequence.get(i);
			testCase += generateTestInputCodeForNode(currentNode)+generateTestOracleCodeForNode(currentNode);
		}
		return testCase;
	}
	
	
	protected String getDefaultInputAction(String event, ArrayList<String> parameters) {
		String paraString = "";
		if (parameters.size()>0) {
			for (int i=0; i<parameters.size(); i++)
				paraString += "\t" + parameters.get(i);
		}
		return event + paraString;
	}

	protected String getHeader(){
		return mid.hasImportBlock()? 
				"*** Settings ***\n"+mid.getImportBlock()+"\n\n": 
				"";
	}
	
	protected String getInputActionCode(TransitionTreeNode currentNode) {
		return newLine + tab+ getInputActionExpression(currentNode);
	}

}
