/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

public class TargetLanguagePython extends TargetLanguageOO{
	private static final long serialVersionUID = 1L;

	public TargetLanguagePython(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
		this.lineCommentToken = "#";
		this.packageKeyword = "";
		this.importKeyword = "import";
		this.methodBodyStart=":";
		this.methodBodyEnd="";
		this.testSuiteMethodSignature="\nif __name__ == '__main__'";
		this.setUpSignature = "def setUp";
		this.setUpMethodName = "setUp";
		this.tearDownSignature = "def tearDown";
		this.tearDownMethodName = "tearDown";
		this.setUpAttribute = "";
		this.tearDownAttibute = "";
		this.testMethodNamePrefix = "test"; 
		this.methodThrowException = "";
		this.assertDefinitionCode = "";
		this.systemExitCode = "System.exit(1);";
		this.endOfStatement = "";
		this.endOfNameSpace = "";
	}
	
	// Java try-catch block
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
//		stringBuffer.append("return");
		stringBuffer.append(getSystemExitCode());
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("} catch (Exception e) {}");
		return stringBuffer.toString();
	}

	
	public String getTestClassSignature(String testClassName, String inheritance){
		return "class "+testClassName+"("+inheritance+"):";
	}
	
	public String getVariableDeclaration(String classUnderTest){
		return "";
	}

	public String printLine(String message){
		return "print "+message;
	}
	
	public String generateAssertStatement(boolean useTestFramework, String condition, String message){
//		return "self.assert_("+condition+", "+message.replace('"',  '\'')+")";
		return "self.assertTrue("+condition+")";
	}
	
	public String getHeader(String packageCode, String importCode){
		if (!packageCode.equals("")) 
			packageCode += "\n\n";
		return packageCode + importCode;
	}
	
	public String getTestMethodSignature(String testID){
		return "def "+ testMethodNamePrefix + testID + "(self) " + methodThrowException;
	}

	public String getTestMethodCall(String testID){
		return testMethodNamePrefix + testID + "()";
	}

	public String getTestMethodCall(String className, String index, String testID){
		return "suite.addTest("+className+"(\""+testMethodNamePrefix + testID + "\"))";
	}

	public String createMainAndClassEnding(String newLine, String tab, String testerClass, boolean includeMain){
		return "";
/*		return 
			"\nif __name__ == '__main__':"+
			"\n\tsuite = unittest.TestSuite()"+		
			"\n\tsuite.addTest(unittest.makeSuite("+testerClass+"))"+
			"\n\tunittest.TextTestRunner(verbosity=2).run(suite)";
*/	}

	public String endClassWithMainMathod(String newLine, String tab, String statement){
		return newLine+ "public static void main(String[] args) throws Exception {" 
			+ statement
			+ newLine+ methodBodyEnd+"\n"
			+ "\n}\n";
	}

	public String endClassWithoutMainMathod(){
		return "\n\n";
	}
		
}
