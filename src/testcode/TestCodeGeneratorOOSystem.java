/* 	
	Author Dianxiang Xu
*/
package testcode;

import testgeneration.TransitionTree;
import testgeneration.TransitionTreeNode;

public class TestCodeGeneratorOOSystem extends TestCodeGeneratorOO{
	
	public TestCodeGeneratorOOSystem(TransitionTree transitionTree) {
		super(transitionTree);
	}	

	// overriding
	protected String generateObjectConstructionCode(TransitionTreeNode currentNode) {
		// no object construction
		return "";
	}

	// overriding
	protected String createObjectVariableDeclaration(){
		// No object reference
		return "";
	}

	// overriding 
	protected String normalizeSetupCode(String code) {
		String normalizedCode = code;
		String endOfStatement = language.getEndOfStatement();
		if (!endOfStatement.equals("") && !normalizedCode.endsWith(endOfStatement))
			normalizedCode += endOfStatement;
		return normalizedCode;
	}

	// overriding
	protected String assertPredicate(String testID, String condition, boolean needNegation){
		String message = "\""+ testID+"\""; 
		String normalizedCondition = condition;
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

}
