/* 	
	Author Dianxiang Xu
*/
package testcode;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import testgeneration.TransitionTree;
import testgeneration.TransitionTreeNode;

import kernel.CancellationException;

public class TestCodeGeneratorUFT extends TestCodeGenerator {
	
	public TestCodeGeneratorUFT(TransitionTree transitionTree) {
		super(transitionTree);
		tab = "\t";
	}
	
	public void saveTestsToSingleFile(ArrayList<TransitionTreeNode> allLeaves, PrintWriter testSuiteWriter) throws CancellationException {
		 generateTestSuite(allLeaves);
	}

	public void saveTestsToSeparateFiles(ArrayList<TransitionTreeNode> allLeaves, PrintWriter testSuiteWriter, File testSuiteFile) throws CancellationException{
		 generateTestSuite(allLeaves);
	} 
	
	private void generateTestSuite(ArrayList<TransitionTreeNode> allLeaves) throws CancellationException{
//		String fileDir = testSuiteFile.getParent();		
		int testNo = 1;
		for (TransitionTreeNode leaf : allLeaves) {
			transitionTree.checkForCancellation();
			ArrayList<TransitionTreeNode> testSequence = getTestSequence(leaf);
			generateTestCaseCode(testNo, testSequence); 
			testNo++;
		}			
	}
	
	private String generateTestCaseCode(int testNo, ArrayList<TransitionTreeNode> testSequence) throws CancellationException{
		String  testCase = "\t";
		for (int i=1; i<testSequence.size(); i++) {
			transitionTree.checkForCancellation();
			TransitionTreeNode currentNode = testSequence.get(i);
			testCase += preferSpeed? currentNode.getTestInputCode(): generateTestInputCodeForNode(currentNode);
			currentNode.setTraversed(true);
		}
		return testCase;
	}
	
	public String generateSequenceCodeForReview(ArrayList<TransitionTreeNode> testSequence) {
		String  testCase = "test";
		for (int i=1; i<testSequence.size(); i++) {
			TransitionTreeNode currentNode = testSequence.get(i);
			testCase += generateTestInputCodeForNode(currentNode);
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
