/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

public class TargetLanguageCSharp extends TargetLanguageOO{
	private static final long serialVersionUID = 1L;

	public TargetLanguageCSharp(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
		this.packageKeyword = "namespace";
		this.importKeyword = "using";		
		this.indentation = "\t\t";
		this.methodBodyStart="{";
		this.methodBodyEnd="}";
		this.setUpSignature = "public void Init";
		this.setUpMethodName = "Init";
		this.tearDownSignature = "public void Clean";
		this.tearDownMethodName = "Clean";
		this.testSuiteMethodSignature="public void TestAll()";
		this.testMethodNamePrefix = "Test"; 
		this.testFixtureAttribute = "[TestFixture()]\n";
		this.setUpAttribute = "[SetUp()]";
		this.tearDownAttibute = "[TearDown()]";
		this.testAttibute ="\n\t[Test]";  
		this.methodThrowException = "";
		this.systemExitCode = "Environment.Exit(1);";
//		this.systemExitCode = "Application.Exit();";
		this.assertDefinitionCode = "\n\t\tprivate void Assert(bool condition, string errorMessage) {"
			+"\n\t\t\tif (!condition){"
//			+"\n\t\t\t\tthrow new Exception(errorMessage);"
			+"\n\t\t\t\tConsole.WriteLine(errorMessage);"
			+"\n\t\t\t\tConsole.WriteLine(\"\\nPress any key to continue...\");"
			+"\n\t\t\t\tConsole.Read();"
			+"\n\t\t\t\t"+systemExitCode
			+"\n\t\t\t}"
			+"\n\t\t}\n";
		this.endOfStatement = ";";
		this.endOfNameSpace = "\n} // End of namespace";
	}
	
	// C# try-catch block
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
		stringBuffer.append("Console.WriteLine(\"\\nPress any key to continue...\");");
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
		stringBuffer.append("Console.Read();");
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

	
	public String getTestClassSignature(String testClassName, String inheritance){
		return "\tpublic class "+testClassName+inheritance+"{";
	}
	
	public String getVariableDeclaration(String classUnderTest){
		return "private "+classUnderTest + " " + classUnderTest.toLowerCase() +endOfStatement;
	}
	
	public String printLine(String message){
		return "Console.WriteLine("+message+");";
	}

	public String generateAssertStatement(boolean useTestFramework, String condition, String message){
		if (useTestFramework)
			return "Assert.True("+condition+", "+message+");";
		else
			return "Assert("+condition+", "+message+");";
	}
	
	public String getHeader(String packageCode, String importCode){
		if (!packageCode.equals("")) 
			packageCode += " {\n\n";
		return importCode + packageCode;
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
				
			newLine+ "public static void Main() {" 
			+ newLine+tab+testerClass+" tester = new "+testerClass+"();"
			+ newLine+tab+"tester.TestAll();"
			+ newLine+tab+"Console.WriteLine(\"\\nPress any key to continue...\");"
			+ newLine+tab+"Console.Read();"
			+ newLine+ methodBodyEnd+"\n"
			+ "\n\t}\n":
				
			"\n}\t\n";
	}
	
	public String endClassWithMainMathod(String newLine, String tab, String statement){
		return newLine+ "public static void Main() {" 
			+ statement
			+ newLine+tab+"Console.WriteLine(\"\\nPress any key to continue...\");"
			+ newLine+tab+"Console.Read();"
			+ newLine+ methodBodyEnd+"\n"
			+ "\n\t}\n";
	}

	public String endClassWithoutMainMathod(){
		return "\n\t}\n";
	}

}
