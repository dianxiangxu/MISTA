/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

import edit.XMIDProcessor;

public class TargetLanguagePHP extends TargetLanguageOO {

	private static final long serialVersionUID = 1L;
	private static final String PHPsignature = XMIDProcessor.PHP_KEYWORD+"\n";
	
	public TargetLanguagePHP(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
		this.packageKeyword="";
		this.importKeyword = "header";	
		this.newOperator ="new";
		this.methodBodyStart="{";
		this.methodBodyEnd="}";
		this.testSuiteMethodSignature="public: void testAll()";
		this.setUpSignature = "public function setUp";
		this.setUpMethodName = "setUp";		
		this.tearDownSignature = "public function tearDown";
		this.tearDownMethodName = "tearDown";
		this.testMethodNamePrefix = "test"; 
		this.setUpAttribute = "";
		this.tearDownAttibute = "";
		this.methodThrowException = "";
		this.systemExitCode = "exit(1);";
		this.assertDefinitionCode = "";	
		this.endOfStatement = ";";
		this.endOfNameSpace = "";
	}
	
	public String getExceptionHandlingCode(String newLine, String tab, String testInputCode, String testID){
		return "";
	}
	
	public String getTestClassSignature(String testClassName, String inheritance){
		return "class "+testClassName+inheritance+"{";
	}

	public String getDefaultObjectVariableName(String className){
		return "$" + className.toLowerCase();
	}

	public String getDefaultObjectVariableInitDeclaration(String className){
		return "$this->"+className.toLowerCase();
	}

	public String getVariableDeclaration(String classUnderTest){
		return "protected "+getDefaultObjectVariableName(classUnderTest) +endOfStatement;
	}

	public String printLine(String message){
		return "echo " + message + ";";
	}

	public String generateAssertStatement(boolean useTestFramework, String condition, String message){
		return "$this->assertTrue("+condition+");";
	}

	public String getHeader(String packageCode, String importCode){
		if (importCode.equals(""))
			return PHPsignature;
		if (importCode.startsWith(PHPsignature))
			return importCode;
		else
			return PHPsignature+importCode;
	}

	public String getObjectReference(String className){
		return "$this->"+className.toLowerCase() +"->";
	}

	public String objectConstructionParaString(String paraString){
		if (paraString.equals(""))
			return "";
		else
			return	"(" + paraString + ")";
	}
		
	public String getTestMethodSignature(String testID){
		return "public function "+ testMethodNamePrefix + testID + "() " + methodThrowException;
	}

	public String getTestMethodCall(String testID){
		return testMethodNamePrefix + testID + "();";
	}

	public String getTestMethodCall(String className, String index, String testID){
		String reference = "tester"+index;
		return className+" "+reference+";\n"+reference+"."+testMethodNamePrefix + testID + "();";
	}
	
	public String createMainAndClassEnding(String newLine, String tab, String testerClass, boolean includeMain){
		return "\n}\n";
	}

	public String endClassWithMainMathod(String newLine, String tab, String statement){
		return "";
	}
	
	public String endClassWithoutMainMathod(){
		return "\n}\n";
	}

}
