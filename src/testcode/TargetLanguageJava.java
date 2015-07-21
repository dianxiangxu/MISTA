/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

public class TargetLanguageJava extends TargetLanguageOO{
	private static final long serialVersionUID = 1L;

	public TargetLanguageJava(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
		this.packageKeyword = "package";
		this.importKeyword = "import";
		this.methodBodyStart="{";
		this.methodBodyEnd="}";
		this.testSuiteMethodSignature="public void testAll()";
		this.setUpSignature = "public void setUp";
		this.setUpMethodName = "setUp";
		this.tearDownSignature = "public void tearDown";
		this.tearDownMethodName = "tearDown";
		this.setUpAttribute = "";
		this.tearDownAttibute = "";
		this.testMethodNamePrefix = "test"; 
		this.methodThrowException = "throws Exception ";
		this.assertDefinitionCode = "";
		this.systemExitCode = "System.exit(1);";
		this.endOfStatement = ";";
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
		return "public class "+testClassName+inheritance+"{";
	}
	
	public String getVariableDeclaration(String classUnderTest){
		return "private "+classUnderTest + " " + classUnderTest.toLowerCase() +endOfStatement;
	}

	public String printLine(String message){
		return "System.out.println("+message+");";
	}
	
	public String generateAssertStatement(boolean useTestFramework, String condition, String message){
		if (useTestFramework)
			return "assertTrue("+message+", "+condition+");";
		else
			return "assert "+condition+" : "+message+";";
	}
	
	public String getHeader(String packageCode, String importCode){
		if (!packageCode.equals("")) 
			packageCode += "\n\n";
		return packageCode + importCode;
	}
	
	public String getTestMethodSignature(String testID){
		return "public void "+ testMethodNamePrefix + testID + "() " + methodThrowException;
	}

	public String getTestMethodCall(String testID){
		return testMethodNamePrefix + testID + "();";
	}

	public String getTestMethodCall(String className, String index, String testID){
		String reference = "tester"+index;
		return className+" "+reference+" = new "+className+"();\n"
		+ reference+"."+testMethodNamePrefix + testID + "();";
	}

	public String createMainAndClassEnding(String newLine, String tab, String testerClass, boolean includeMain){
		return includeMain?
				
			newLine+ "public static void main(String[] args) throws Exception {" 
			+ newLine+tab+" new "+testerClass+"().testAll();"
			+ newLine+ methodBodyEnd+"\n"
			+ "\n}\n":
		
			"\n}\n";		
	}

	public String endClassWithMainMathod(String newLine, String tab, String statement){
		return newLine+ "public static void main(String[] args) throws Exception {" 
			+ statement
			+ newLine+ methodBodyEnd+"\n"
			+ "\n}\n";
	}

	public String endClassWithoutMainMathod(){
		return "\n}\n";
	}
	
	public void setJUnit4(boolean isJUnit4){
		if (isJUnit4){
			this.setUpAttribute = "@Before";
			this.tearDownAttibute = "@After";
			this.testAttibute ="\n\t@Test";  
		} else {
			this.setUpAttribute = "";
			this.tearDownAttibute = "";
			this.testAttibute ="";  			
		}
	}
	
}
