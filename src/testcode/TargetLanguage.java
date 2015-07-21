/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package testcode;

import java.io.File;
import java.util.ArrayList;

import kernel.Kernel;

import parser.MIDParser;
import parser.ParseException;

import utilities.FileUtil;

public class TargetLanguage implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String varURL = "VARURL"; 
	public static final String varTESTID = "VARTESTID";

	private static final String seleniumHeader =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		"\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"+
		"\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">";

	private static final String seleniumInitCode = "\n<link rel=\"selenium.base\" href=\""+varURL+"\" />"+
		"\n<title>test "+varTESTID+"</title>"+
		"\n<table cellpadding=\"1\" cellspacing=\"1\" border=\"1\">"+
		"\n<thead>"+
		"\n<tr><td rowspan=\"1\" colspan=\"3\">test "+varTESTID+"</td></tr>"+
		"\n</thead><tbody>";	

	private static final String testGen4WebHeader =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		"\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"+
		"\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">";

	private static final String testGen4WebInitCode = "\n<link rel=\"selenium.base\" href=\""+varURL+"\" />"+
		"\n<title>test "+varTESTID+"</title>"+
		"\n<table cellpadding=\"1\" cellspacing=\"1\" border=\"1\">"+
		"\n<thead>"+
		"\n<tr><td rowspan=\"1\" colspan=\"3\">test "+varTESTID+"</td></tr>"+
		"\n</thead><tbody>";	

	public static final String NO_TEST_ENGINE = "No Test Engine";
	public static final String JUNIT4 ="JUnit4";
	public static final String JUNIT3 ="JUnit3";
	public static final String WINDOW_TESTER = "WindowTester";
	public static final String JFCUNIT ="JfcUnit";
	public static final String NUNIT = "NUnit";
	public static final String PHPUNIT = "PHPUnit";
	public static final String PYUNIT = "PyUnit";
	public static final String SELENIUM_IDE = "Selenium IDE";
	public static final String ROBOT_FRAMEWORK = "Robot Framework";
	
	// default test frameworks; overridden if test frameworks are specified in the frameworks file
	private static String [][] DefaultTestFrameworksForJava = {
		{NO_TEST_ENGINE, "", ""},
		{JUNIT4, "import org.junit.*;\nimport static org.junit.Assert.*;", ""},
		{JUNIT3, "import junit.framework.*;", "extends TestCase"},
		{WINDOW_TESTER, "import com.windowtester.runtime.swt.UITestCaseSWT;", "extends UITestCaseSWT"},
		{JFCUNIT,"import junit.extensions.jfcunit.*;", "extends JFCTestCase"} 
	};
	private static String[][] DefaultTestFrameworksForCSharp = {
		{NO_TEST_ENGINE, "", ""},
		{NUNIT, "using NUnit.Framework;", ""}
	};
	
	private static String[][] DefaultTestFrameworksForPHP = {
		{PHPUNIT, "", "extends PHPUnit_Framework_TestCase"}
	};

	private static String[][] DefaultTestFrameworksForPython = {
		{PYUNIT, "", "unittest.TestCase"}
	};

	public static final int JSONRPC = 0;
	public static final int XMLRPC = 1;

	public static final int FIREBOXBROWSER=0;
	public static final int INTERNETEXPLORER=1;
	
	private static String[][] DefaultTestFrameworksForCPP  	= {{NO_TEST_ENGINE, "", ""}};
	private static String[][] DefaultTestFrameworksForVB   	= {{NO_TEST_ENGINE, "", ""}};
	private static String[][] DefaultTestFrameworksForHTML 	= {{SELENIUM_IDE, seleniumHeader, seleniumInitCode}}; 
	private static String[][] DefaultTestFrameworksForXML 	= {{"TestGen4Web", testGen4WebHeader, testGen4WebInitCode}}; 
	private static String[][] DefaultTestFrameworksForC 	= {{NO_TEST_ENGINE, "", ""}};
	private static String[][] DefaultTestFrameworksForKBT 	= {{ROBOT_FRAMEWORK, "", ""}};
	private static String[][] DefaultTestFrameworksForRPC 	= {{"JSON-RPC", "", ""}, {"XML-RPC", "", ""}};
	private static String[][] DefaultTestFrameworksForSeleniumDriver 	= {{"Firefox Driver", "", ""}, {"IE Driver", "", ""}};
	private static String[][] DefaultTestFrameworksForUFT 	= {{"GUI", "", ""}};
	
	public static final TargetLanguage JAVA = new TargetLanguageJava("Java", "java", DefaultTestFrameworksForJava);
	public static final TargetLanguage CSHARP = new TargetLanguageCSharp("C#", "cs", DefaultTestFrameworksForCSharp);
	public static final TargetLanguage PHP = new TargetLanguagePHP("PHP", "php", DefaultTestFrameworksForPHP);
	public static final TargetLanguage CPP = new TargetLanguageCPP("C++", "cpp", DefaultTestFrameworksForCPP);
	public static final TargetLanguage PYTHON = new TargetLanguagePython("Python", "py", DefaultTestFrameworksForPython);
	public static final TargetLanguage HTML = new TargetLanguage("HTML", "html", DefaultTestFrameworksForHTML);
	public static final TargetLanguage XML = new TargetLanguage("XML", "xml", DefaultTestFrameworksForXML);
	public static final TargetLanguage C = new TargetLanguage("C", "c", DefaultTestFrameworksForC);
	public static final TargetLanguage VB = new TargetLanguageVB("VB", "vb", DefaultTestFrameworksForVB);
	public static final TargetLanguage KBT = new TargetLanguage("KBT", "txt", DefaultTestFrameworksForKBT);
	public static final TargetLanguage RPC = new TargetLanguage("RPC", "rpc", DefaultTestFrameworksForRPC);
	public static final TargetLanguage SELENIUMDRIVER = new TargetLanguage("Selenium", "sel", DefaultTestFrameworksForSeleniumDriver);
	public static final TargetLanguage UFT = new TargetLanguage("HP UFT", "uft", DefaultTestFrameworksForUFT);

	private String name;
	private String fileExtension;

	// each row: unit test framework name, import path, inheritance syntax
	protected String[][] testFrameworks; 

	public TargetLanguage(String name, String fileExtension, String[][] testFrameworks){
		this.name = name; 
		this.fileExtension = fileExtension;
		this.testFrameworks = testFrameworks;
	}
	
	public String getName(){
		return name;
	}
	
	public String getFileExtension(){
		return fileExtension;
	}
	
	public int getNumberOfTestFrameworks(){
		return testFrameworks.length;
	}
		
	public void setTestFrameworks(String[][] frameworks){
		this.testFrameworks = frameworks;
	}
	
	public String[] getTestFrameworkList(){
		String [] list = new String[testFrameworks.length];
		for (int i=0; i<testFrameworks.length; i++)
			list[i] = testFrameworks[i][0];
		return list;	
	}

	public String getTestFrameworkName(int testFrameworkIndex){
		return testFrameworkIndex <testFrameworks.length?
			testFrameworks[testFrameworkIndex][0]: "";
	}

	public String getTestFrameworkPackage(int testFrameworkIndex){
		return testFrameworkIndex <testFrameworks.length?
			testFrameworks[testFrameworkIndex][1]: "";
	}

	public String getTestFrameworkTestClass(int testFrameworkIndex){
		return testFrameworkIndex <testFrameworks.length?
			testFrameworks[testFrameworkIndex][2]: "";
	}
	
	public boolean equals(TargetLanguage language){
		return name.equalsIgnoreCase(language.name);
	}
	
	// load the test frameworks specified in the file 
	private static final String testFrameworkFileName = "frameworks.dat";

	
	// system level initialization of test frameworks
	public static void initializeTestFrameworkList(Kernel frame){
		File testFrameworkFile = new File(testFrameworkFileName);
		if (!testFrameworkFile.exists())
			return;		// use built-in lists
		String testFrameworksString = FileUtil.readTextFile(testFrameworkFile);
		if (testFrameworksString.equals(""))
			return;		// use built-in lists
		try {
			ArrayList<String[]> frameworks = MIDParser.parseTestFrameworksString(testFrameworksString);
			if (frameworks.size()==0)
				return;
			setTestFrameworkList(frameworks, JAVA);
			setTestFrameworkList(frameworks, CSHARP);
			setTestFrameworkList(frameworks, PHP);
			setTestFrameworkList(frameworks, CPP);
			setTestFrameworkList(frameworks, VB);
			setTestFrameworkList(frameworks, HTML);
			setTestFrameworkList(frameworks, XML);
			setTestFrameworkList(frameworks, C);
	    }
	    catch (ParseException exception) { 
	    	String exceptionMessage = exception.toString().replace("parser.ParseException:", "");
			frame.printDialogMessage("Problem with specification of test frameworks in file "+testFrameworkFileName + ": \n" + exceptionMessage
					+ "\nIf this problem is not fixed, the built-in lists of test frameworks will be used.");
			// use the built-in lists
 	    }
	}
	
	private static void setTestFrameworkList(ArrayList<String[]> allFrameworks, TargetLanguage language){
		int frameworkCount = countTestFrameworks(allFrameworks, language);
		if (frameworkCount==0)
			return;
		String[][] testFrameworks = new String[frameworkCount+1][];
		int index = 0;
		testFrameworks[index++] = getDefaultFramework(language);
		for (String[] record: allFrameworks) 
			if (record[0].equalsIgnoreCase(language.getName())){
				String[] framework = new String[3];
				framework[0] = record[1];
				framework[1] = record[2];
				framework[2] = record[3];
				testFrameworks[index] = framework;
				index++;
			}
		language.setTestFrameworks(testFrameworks);
	}
	
	private static String[] getDefaultFramework(TargetLanguage language){
		if (language == JAVA)
			return DefaultTestFrameworksForJava[0];
		else if (language==CSHARP)
			return DefaultTestFrameworksForCSharp[0];
		else if (language==PHP)
			return DefaultTestFrameworksForPHP[0];
		else if (language==CPP)
			return DefaultTestFrameworksForCPP[0];
		else if (language==VB)
			return DefaultTestFrameworksForVB[0];
		else if (language==HTML)
			return DefaultTestFrameworksForHTML[0];
		else if (language==XML)
			return DefaultTestFrameworksForXML[0];
		else if (language==C)
			return DefaultTestFrameworksForC[0];
		else
			return DefaultTestFrameworksForJava[0];
	}
	
	private static int countTestFrameworks(ArrayList<String[]> allFrameworks, TargetLanguage language){
		int count = 0;
		for (String[] record: allFrameworks) 
			if (record[0].equalsIgnoreCase(language.getName()))
				count++;
		return count;
	}
		
	public String toString(){
		return name;
	}
	
	public static void main(String[] args) {
		initializeTestFrameworkList(null);
	}
}
