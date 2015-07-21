/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;


public abstract class TargetLanguageOO extends TargetLanguage implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	protected String packageKeyword;
	protected String importKeyword;	
	
	protected String lineCommentToken = "//";
	protected String indentation = "\t";
	protected String negationToken = "!";
	protected String newOperator ="new";
	
	protected String methodBodyStart;
	protected String methodBodyEnd;
	
	protected String methodThrowException;

	// unit test framework
	protected String setUpSignature;
	protected String setUpMethodName;
	
	protected String tearDownSignature;
	protected String tearDownMethodName;
	
	protected String testSuiteMethodSignature;
	
	protected String testMethodNamePrefix; 
	protected String testFixtureAttribute = "";
	protected String setUpAttribute = "";
	protected String tearDownAttibute = "";
	protected String testAttibute = "";  

	protected String systemExitCode = "";
	protected String assertDefinitionCode = "";
	
	protected String endOfStatement = ";";
	protected String endOfNameSpace = "";
	
	public TargetLanguageOO(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
	}
	
	public String getPackageKeyword(){
		return packageKeyword;
	}

	public String getImportKeyword(){
		return importKeyword;
	}
	
	public String getLineCommentToken(){
		return lineCommentToken;
	}

	public String getIndentation(){
		return indentation;
	}

	public String getNegationToken(){
		return negationToken;
	}
	
	public String getNewOperator(){
		return newOperator;
	}

	public String getDefaultObjectVariableName(String className){
		return className.toLowerCase();
	}

	public String getDefaultObjectVariableInitDeclaration(String className){
		return getDefaultObjectVariableName(className);
	}

	public String getObjectReference(String className){
		return getDefaultObjectVariableName(className) +".";
	}
	
	public String objectConstructionParaString(String paraString){
		return "(" + paraString + ")";
	}

	public String getMethodBodyStart(){
		return methodBodyStart;
	}

	public String getMethodBodyEnd(){
		return methodBodyEnd;
	}
	
	public String getSetUpSignature(){
		return setUpSignature;
	}

	public String getSetUpMethodName(){
		return setUpMethodName;
	}
	
	public String getTearDownSignature(){
		return tearDownSignature;
	}

	public String getTearDownMethodName(){
		return tearDownMethodName;
	}
	
	public String getTestSuiteMethodSignature(){
		return testSuiteMethodSignature;
	}

	public String getTestMethodNamePrefix(){
		return testMethodNamePrefix;
	}
	
	public String getMethodThrowException(){
		return methodThrowException.equals("")? "": " "+methodThrowException;			
	}
	 
	public String getTestFixtureAttribute(){
		return testFixtureAttribute;
	}
	
	public String getSetUpAttribute(){
		return setUpAttribute;
	}
	
	
	public String getTearDownAttribute(){
		return tearDownAttibute;
	}
	
	public String getTestAttribute(){
		return testAttibute;
	}  
	
	public String getAssertDefinition(){
		return assertDefinitionCode;
	}
		
	public String getSystemExitCode(){
		return systemExitCode;
	}
	
	public String getEndOfStatement(){
		return endOfStatement;
	}
	
	public String getEndOfNameSpace(){
		return endOfNameSpace;
	}

	// default try-catch block
	public String getExceptionHandlingCode(String newLine, String tab, String testInputCode, String testID){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("try {"); 
		stringBuffer.append(testInputCode.replaceFirst("\n", "\n\t"));
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
		stringBuffer.append(printLine("\"Test failed at test "+testID+": an expected exception is not thrown!\""));
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
//		stringBuffer.append("return;");
		stringBuffer.append(getSystemExitCode());
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("} catch (Exception e) {}");
		return stringBuffer.toString();
	}
	
	public boolean hasPackageSection(){
		return packageKeyword!=null && !packageKeyword.equals("");
	}
	
	abstract public String getTestClassSignature(String testClassName, String inheritance);

	abstract public String getVariableDeclaration(String classUnderTest);

	abstract public String printLine(String message);
	
	abstract public String generateAssertStatement(boolean useTestFramework, String condition, String message);
	
	abstract public String getHeader(String packageCode, String importCode);
	
	abstract public String getTestMethodSignature(String testID);

	abstract public String getTestMethodCall(String testID); // for calls from the same class
	abstract public String getTestMethodCall(String className, String index, String testID); // for calls from a different class

	abstract public String createMainAndClassEnding(String newLine, String tab, String testerClass, boolean includeMain);

	abstract public String endClassWithMainMathod(String newLine, String tab, String statement);
	
	abstract public String endClassWithoutMainMathod();

}
