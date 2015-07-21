/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

public class TargetLanguageVB extends TargetLanguageOO{
	private static final long serialVersionUID = 1L;

	public TargetLanguageVB(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
		this.packageKeyword = "namespace";
		this.importKeyword = "imports";
		this.lineCommentToken = "'";
		this.indentation = "\t\t";
		this.negationToken = "Not";
		this.newOperator = "New";
		this.methodBodyStart="";
		this.methodBodyEnd="End Sub";
		this.testSuiteMethodSignature="Public Sub TestAll()";
		this.setUpSignature = "Public Sub Init";
		this.setUpMethodName = "Init";
		this.tearDownSignature = "Public Sub Clean";
		this.tearDownMethodName = "Clean";
		this.testMethodNamePrefix = "Test"; 
		this.testFixtureAttribute = "[TestFixture()]";
		this.setUpAttribute = "[SetUp()]";
		this.tearDownAttibute = "[TearDown()]";
		this.testAttibute ="[Test]";  
		this.methodThrowException = "";
		this.systemExitCode = "Application.[Exit]()";
		this.assertDefinitionCode = "\n\t\tPublic Sub Assert(ByVal condition As Boolean, ByVal errorMessage As String)"
			+"\n\t\t\tIf Not condition Then"
			+"\n\t\t\t\tConsole.WriteLine(errorMessage)"		
			+"\n\t\t\t\tThrow New ApplicationException"
			+"\n\t\t\tEnd If"
			+"\n\t\tEnd Sub\n";
		this.endOfStatement = "";
		this.endOfNameSpace = "\nEnd Namespace";

	}
	
	// VB try-catch block 
	public String getExceptionHandlingCode(String newLine, String tab, String testInputCode, String testID){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("Try"); 
		stringBuffer.append(testInputCode.replaceFirst("\n", "\n\t"));
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
		stringBuffer.append(printLine("\"Test failed at test "+testID+": an expected exception is not thrown!\""));
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
		stringBuffer.append("Return");
//		stringBuffer.append(getSystemExitCode());
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("Catch e As Exception");
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("End Try");
		return stringBuffer.toString();
	}

	public String getTestClassSignature(String testClassName, String inheritance){
		return "\tPublic Class "+testClassName+inheritance;
	}
	
	public String getVariableDeclaration(String classUnderTest){
		return "Private "+classUnderTest.toLowerCase() + " As " + classUnderTest +endOfStatement;
	}

	public String printLine(String message){
		return "Console.WriteLine("+message+")";
	}

	public String generateAssertStatement(boolean useTestFramework, String condition, String message){
		if (useTestFramework)
			return "Assert.True("+condition+", "+message+");";
		else
			return "Assert("+condition+", "+message+")";
	}
	
	public String getHeader(String packageCode, String importCode){
		if (!packageCode.equals("")) 
			packageCode += "\n\n";
		return importCode + packageCode;
	}

	public String getTestMethodSignature(String testID){
		return "Public Sub "+ testMethodNamePrefix + testID + "() " + methodThrowException;
	}

	public String getTestMethodCall(String testID){
		return testMethodNamePrefix + testID + "()" ;
	}

	public String getTestMethodCall(String className, String index, String testID){
		String reference = "Tester"+index;
		return "Dim "+reference+" As "+ className + " = New "+className+
		"\n"+reference+"." + testMethodNamePrefix + testID + "()" ;
	}

	public String createMainAndClassEnding(String newLine, String tab, String tester, boolean includeMain){
		return includeMain? 
		
		newLine+ "Public Shared Sub Main()" 
		+ newLine+tab+"Dim Tester As "+tester + " = New "+tester
		+ newLine+tab+"Tester.TestAll()"
		+ newLine+ methodBodyEnd+"\n"
		+ "\n\tEnd Class\n":
			
		"\n\tEnd Class\n";
	}

	public String endClassWithMainMathod(String newLine, String tab, String statement){
		return newLine+ "Public Shared Sub Main()" 
		+ statement
		+ newLine+ methodBodyEnd+"\n"
		+ "\n\tEnd Class\n";
	}

	public String endClassWithoutMainMathod(){
		return "\n\tEnd Class\n";
	}

}
