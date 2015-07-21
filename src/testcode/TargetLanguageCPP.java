/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

public class TargetLanguageCPP extends TargetLanguageOO {

	private static final long serialVersionUID = 1L;

	public TargetLanguageCPP(String name, String fileExtension, String[][] unitTestFrameworks){
		super(name, fileExtension, unitTestFrameworks);
		this.packageKeyword="";
		this.importKeyword = "header";	
		this.newOperator ="";
		this.methodBodyStart="{";
		this.methodBodyEnd="}";
		this.testSuiteMethodSignature="public: void testAll()";
		this.setUpSignature = "void setUp";
		this.setUpMethodName = "setUp";		
		this.tearDownSignature = "void tearDown";
		this.tearDownMethodName = "tearDown";
		this.testMethodNamePrefix = "test"; 
		this.testFixtureAttribute = "\n";
		this.setUpAttribute = "";
		this.tearDownAttibute = "";
		this.testAttibute ="\n\t";  
		this.methodThrowException = "";
		this.systemExitCode = "exit(1);";
		this.assertDefinitionCode = "\n\tvoid assert(bool condition, string errorMessage) {"
			+"\n\t\tif (!condition) {"
			+"\n\t\t\tcout << \"\\n\" << errorMessage << \"\\n\\n\";"
			+"\n\t\t\tsystem(\"PAUSE\");"
			+"\n\t\t\t"+systemExitCode
			+"\n\t\t}"
			+"\n\t}\n";	
		this.endOfStatement = ";";
		this.endOfNameSpace = "";
	}
	
	// C++ try-catch block 
	public String getExceptionHandlingCode(String newLine, String tab, String testInputCode, String testID){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("try {"); 
		stringBuffer.append(testInputCode.replaceFirst("\n", "\n\t"));
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
		stringBuffer.append(printLine("\"Test failed at test "+testID+": an expected exception is not thrown!\\n\\n\""));
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
//		stringBuffer.append("return;");
		stringBuffer.append("system(\"PAUSE\");");
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append(tab);
		stringBuffer.append(getSystemExitCode());
		stringBuffer.append(newLine);
		stringBuffer.append(tab);
		stringBuffer.append("} catch (...) {}");
		return stringBuffer.toString();
	}
	
	public String getTestClassSignature(String testClassName, String inheritance){
		return "class "+testClassName+inheritance+"{";
	}
	
	public String getVariableDeclaration(String classUnderTest){
		return classUnderTest + " " + classUnderTest.toLowerCase() +endOfStatement;
	}

	public String printLine(String message){
		return "cout << " + message + " <<\"\\n\";";
	}

	public String generateAssertStatement(boolean useTestFramework, String condition, String message){
		return "assert("+condition+", "+message+");";
	}

	public String getHeader(String packageCode, String importCode){
		return !importCode.equals("")? importCode+"\n\n": "";
	}

	public String getTestMethodSignature(String testID){
		return "void "+ testMethodNamePrefix + testID + "() " + methodThrowException;
	}

	public String getTestMethodCall(String testID){
		return testMethodNamePrefix + testID + "();";
	}

	public String getTestMethodCall(String className, String index, String testID){
		String reference = "tester"+index;
		return className+" "+reference+";\n"+reference+"."+testMethodNamePrefix + testID + "();";
	}
	
	public String createMainAndClassEnding(String newLine, String tab, String testerClass, boolean includeMain){
		return includeMain?
				
				"\n};\n"
				+ "\nint main() {"  
				+ "\n\t"+testerClass+" tester;"
				+ "\n\ttester.testAll();"
				+ "\n\tcout << \"\\n\\n\";"
				+ "\n\tsystem(\"PAUSE\");"
				+ "\n"+methodBodyEnd+"\n":
				
				"\n};\n";
	}

	public String endClassWithMainMathod(String newLine, String tab, String statement){
		return newLine+ "int main() {"  
				+ statement
				+ newLine+tab+"cout << \"\\n\\n\";"
				+ newLine+tab+"system(\"PAUSE\");"
				+ newLine+methodBodyEnd
				+ "\n};";
	}
	
	public String endClassWithoutMainMathod(){
		return "\n};\n";
	}

}
