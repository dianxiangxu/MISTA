/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package edit;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JTextArea;

import kernel.SystemOptions;
import kernel.SystemOptions.ModelType;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import testcode.TargetLanguage;
import testcode.TargetLanguageOO;

 
public class XMIDProcessor {

	public static final String MODEL_TYPE_KEYWORD = "MODELTYPE";
	public static final String MODEL_KEYWORD = "MODEL";

	public static final String CLASS_KEYWORD = "CLASS";			// OO 
	public static final String URL_KEYWORD = "URL";				// HTML/Selenium IDE
	public static final String RPC_KEYWORD = "RPC";				// Online RPC
	public static final String SELENIUM_KEYWORD = "SELENIUM";	// Online Selenium
	public static final String UFT_KEYWORD = "UFT";				// HP Unified Functional Testing
	public static final String FUNCTION_KEYWORD = "FUNCTION";	// C 
	public static final String PHP_KEYWORD = "<?php";	// C 	
	
	public static final String INIT_KEYWORD = "INIT";
	public static final String DATA_KEYWORD = "DATA";
	public static final String GOAL_KEYWORD = "GOAL";
	public static final String ASSERTION_KEYWORD = "ASSERTION";
	public static final String CONSTANTS_KEYWORD = "CONSTANTS";
	public static final String ENUM_KEYWORD = "ENUM";
	public static final String UNITTESTS_KEYWORD = "UNIT TESTS";
	public static final String SEQUENCESFILE_KEYWORD = "SEQUENCES FILE";
	public static final String SEQUENCES_KEYWORD = "SEQUENCES";

	public static final String SINKS_KEYWORD = "SINKS";
	public static final String SINK_KEYWORD = "SINK";

	public static final String NONNEGATIVE_KEYWORD = "NONNEGATIVE";

	public static final String ATTRIBUTE_KEYWORD = "ATTRIBUTE";
	public static final String RULE_KEYWORD = "RULE";

	public static final String OPTION_KEYWORD = "OPTION";
	public static final String OPTIONS_KEYWORD = "OPTIONS";

	public static final String HIDDEN_KEYWORD = "HIDDEN";
	
	public static final String PARAMETER_KEYWORD = "PARAMETER";
	public static final String PARAMETERS_KEYWORD = "PARAMETERS";

	public static final String REGION_KEYWORD = "REGION";

	public static final String OBJECT_KEYWORD = "OBJECT";
	public static final String METHOD_KEYWORD = "METHOD";
	public static final String ACCESSOR_KEYWORD = "ACCESSOR";	
	public static final String MUTATOR_KEYWORD = "MUTATOR";
	private static final String STATE_KEYWORD = "STATE";

	private static final String PACKAGE_KEYWORD = "PACKAGE";
	private static final String NAMESPACE_KEYWORD = "NAMESPACE";
	private static final String IMPORT_KEYWORD = "IMPORT";
	private static final String FROM_KEYWORD = "FROM";
	private static final String IMPORTS_KEYWORD = "IMPORTS";
	private static final String USING_KEYWORD = "USING";
	private static final String INCLUDE_KEYWORD = "#INCLUDE";
	private static final String SETTINGS_KEYWORD = "SETTINGS";
	
	public static final String SETUP_KEYWORD = "SETUP";
	public static final String TEARDOWN_KEYWORD = "TEARDOWN";
	public static final String SELENIUMSETUP_KEYWORD = "SELENNIUMSETUP";
	public static final String SELENIUMTEARDOWN_KEYWORD = "SELENIUMTEARDOWN";
	public static final String ALPHA_KEYWORD = "ALPHA";
	public static final String OMEGA_KEYWORD = "OMEGA";
	public static final String CODE_KEYWORD = "CODE";

	
	public XMIDProcessor(){	
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Model
	
	private Vector<Vector<Object>> coreModelRows = new Vector<Vector<Object>>();
	private ArrayList<String> initialStates = new ArrayList<String>();
	private ArrayList<String> dataFiles = new ArrayList<String>();
	private ArrayList<String> goalStates = new ArrayList<String>();
	private ArrayList<String> assertions = new ArrayList<String>();
	private String sinkEvents="";
	private String unitTests="";
	private String sequencesFile = "";
	
	public Vector<Vector<Object>> getCoreModelRows(){
		return coreModelRows;
	}

	public ArrayList<String> getInitialStates(){
		return initialStates;
	}

	public ArrayList<String> getDataFiles(){
		return dataFiles;
	}

	public ArrayList<String> getGoalStates(){
		return goalStates;
	}

	
	public ArrayList<String> getAssertions(){
		return assertions;
	}

	public String getSinkEvents(){
		return sinkEvents;
	}
	
	public String getUnitTests(){
		return unitTests;
	}
	
	public String getSequencesFile(){
		return sequencesFile;
	}
	
	private int coreModelRowCount = 1;
	
	public ModelType getModelType(Sheet sheet){
		for (Row row: sheet){
			if (row.getCell(0)==null || row.getCell(1)==null)
				continue;
			String keyword = row.getCell(0).toString().trim();
			if (keyword.equalsIgnoreCase(MODEL_TYPE_KEYWORD))
				return SystemOptions.getModelType(row.getCell(1).toString().trim());
		}
		return null;
	}

	public String getSeparateModelFileName(Sheet sheet){
		for (Row row: sheet){
			if (row.getCell(0)==null || row.getCell(1)==null)
				continue;
			String keyword = row.getCell(0).toString().trim();
			if (keyword.equalsIgnoreCase(MODEL_TYPE_KEYWORD)){
				if (row.getCell(2)!=null)
					return row.getCell(2).toString().trim();
			}
		}
		return "";
	}
	
	public void loadModel(Sheet sheet){
		for (Row row: sheet){
			loadModelRow(row);
		}
	}
	
	private void loadModelRow(Row row){
		if (row.getCell(0)==null || row.getCell(1)==null)
			return;
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equals("")|| keyword.startsWith("//") )
			return;
		if (keyword.equalsIgnoreCase(MODEL_TYPE_KEYWORD)){
			return;
		}
		else
		if (keyword.equalsIgnoreCase(MODEL_KEYWORD)){
			loadCoreModelRow(row);
			coreModelRowCount++;
		}
		else if (keyword.equalsIgnoreCase(INIT_KEYWORD))
			initialStates.add(getRowString(row));
		else if (keyword.equalsIgnoreCase(DATA_KEYWORD))
			dataFiles.add(row.getCell(1).toString());
		else if (keyword.equalsIgnoreCase(GOAL_KEYWORD))
			goalStates.add(getRowString(row));
		else if (keyword.equalsIgnoreCase(ASSERTION_KEYWORD))
			assertions.add(getRowString(row));
		else if (keyword.equalsIgnoreCase(UNITTESTS_KEYWORD))
			unitTests = getRowString(row);
		else if (keyword.equalsIgnoreCase(SINKS_KEYWORD)||keyword.equalsIgnoreCase(SINK_KEYWORD))
			sinkEvents = getRowString(row);
		else if (keyword.equalsIgnoreCase(SEQUENCESFILE_KEYWORD) || keyword.equalsIgnoreCase(SEQUENCES_KEYWORD)){
			if (row.getCell(1)!=null)
				sequencesFile = row.getCell(1).toString();
		}
	}
		
	private void loadCoreModelRow(Row row) {
		Vector<Object> coreModelRow = new Vector<Object>();
		coreModelRow.add(""+coreModelRowCount);
		coreModelRow.add(row.getCell(1).toString());	// event 
		coreModelRow.add(row.getCell(2)!=null? row.getCell(2).toString(): "");	// precondition/threat tree child events
		coreModelRow.add(row.getCell(3)!=null? row.getCell(3).toString(): "");	// postcondition/threat tree relation
		coreModelRow.add(row.getCell(4)!=null? row.getCell(4).toString(): "");	// when condition
		coreModelRow.add(row.getCell(5)!=null? row.getCell(5).toString(): "");	// effect
		coreModelRow.add(row.getCell(6)!=null? row.getCell(6).toString(): "");	// guard for petri net
		coreModelRows.add(coreModelRow);
	}

	private String getRowString(Row row){		
		String readString = row.getCell(1).toString().trim();
		for (int column =2; column<row.getPhysicalNumberOfCells(); column++){
			Cell cell = row.getCell(column);
			if (cell!=null) { 
				String text = cell.toString().trim();
				if (!text.equals(""))
					readString += readString.endsWith(",")? "\n\n"+text: ", \n\n"+text;
			}
		}
		return readString;
	}

	// Access control, June 2014
	private Vector<Vector<Object>> attributeRows = new Vector<Vector<Object>>();
	private Vector<Vector<Object>> ruleRows = new Vector<Vector<Object>>();

	private int attributeRowCount = 1;
	private int ruleRowCount = 1;

	public void loadABAC(Sheet sheet){
		for (Row row: sheet){
			loadABACRow(row);
		}
	}
	
	private void loadABACRow(Row row){
		if (row.getCell(0)==null || row.getCell(1)==null)
			return;
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equals("")|| keyword.startsWith("//") )
			return;
		if (keyword.equalsIgnoreCase(ATTRIBUTE_KEYWORD)){
			loadAttributeRow(row);
		} else
		if (keyword.equalsIgnoreCase(RULE_KEYWORD)){
			loadRuleRow(row);
		}			
	}
		
	private void loadAttributeRow(Row row) {
		Vector<Object> attributeRow = new Vector<Object>();
		attributeRow.add(""+attributeRowCount);
		attributeRow.add(row.getCell(1).toString());	// category 
		attributeRow.add(row.getCell(2).toString());	// name 
		attributeRow.add(row.getCell(3)!=null? row.getCell(3).toString(): "");	// type
		attributeRow.add(row.getCell(4)!=null? row.getCell(4).toString(): "");	// values
		attributeRows.add(attributeRow);
		attributeRowCount++;
	}

	private void loadRuleRow(Row row) {
		Vector<Object> ruleRow = new Vector<Object>();
		ruleRow.add(""+ruleRowCount);
		ruleRow.add(row.getCell(1).toString());	// rule effect
		ruleRow.add(row.getCell(2)!=null? row.getCell(2).toString(): "");	// subject
		ruleRow.add(row.getCell(3)!=null? row.getCell(3).toString(): "");	// action
		ruleRow.add(row.getCell(4)!=null? row.getCell(4).toString(): "");	// resource
		ruleRow.add(row.getCell(5)!=null? row.getCell(5).toString(): "");	// environment
		ruleRow.add(row.getCell(6)!=null? row.getCell(6).toString(): "");	// obligation
		ruleRows.add(ruleRow);
		ruleRowCount++;
	}
	
	public Vector<Vector<Object>> getAttributeRows(){
		return attributeRows;
	}

	public Vector<Vector<Object>> getRuleRows(){
		return ruleRows;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// MIM
	private String systemName = "";		// class or URL
	private String hidden = "";
	private String options ="";
	private String parameters ="";
	private Vector<Vector<Object>> objects = new Vector<Vector<Object>>();
	private Vector<Vector<Object>> methods = new Vector<Vector<Object>>();
	private Vector<Vector<Object>> accessors = new Vector<Vector<Object>>();
	private Vector<Vector<Object>> mutators = new Vector<Vector<Object>>();

	private Vector<Vector<Object>> regions = new Vector<Vector<Object>>();

	private TargetLanguage mimLanguage = null;
		
	public String getSystemName(){
		return systemName;
	}
	
	public String getHidden(){
		return hidden;
	}
	
	public String getOptions(){
		return options;
	}
	
	public String getParameters(){
		return parameters;
	}
	
	public Vector<Vector<Object>> getObjects(){
		return objects;
	}
	
	public Vector<Vector<Object>> getMethods(){
		return methods;
	}
	
	public Vector<Vector<Object>> getAccessors(){
		return accessors;
	}
	
	public Vector<Vector<Object>> getMutators(){
		return mutators;
	}
	
	public Vector<Vector<Object>> getRegions(){
		return regions;
	}
	
	public TargetLanguage getMimLanguage(){
		return mimLanguage;
	}
	
	public void loadMIM(Sheet sheet){
		for (Row row: sheet){
			loadMIMRow(row);
		}
	}

	public void loadMIMRow(Row row){
		if (row.getCell(0)==null || row.getCell(1)==null)
			return;
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equalsIgnoreCase(FUNCTION_KEYWORD) || 
				keyword.equalsIgnoreCase(CLASS_KEYWORD) || 
				keyword.equalsIgnoreCase(URL_KEYWORD) || 
				keyword.equalsIgnoreCase(RPC_KEYWORD) ||
				keyword.equalsIgnoreCase(SELENIUM_KEYWORD) ||
				keyword.equalsIgnoreCase(UFT_KEYWORD)
				){
			systemName = row.getCell(1).toString();
			if (keyword.equalsIgnoreCase(URL_KEYWORD))
				mimLanguage = TargetLanguage.HTML;
			else
			if (keyword.equalsIgnoreCase(RPC_KEYWORD))
				mimLanguage = TargetLanguage.RPC;
			else
			if (keyword.equalsIgnoreCase(SELENIUM_KEYWORD))
				mimLanguage = TargetLanguage.SELENIUMDRIVER;
			else
			if (keyword.equalsIgnoreCase(UFT_KEYWORD))
				mimLanguage = TargetLanguage.UFT;
		}
		else if (keyword.equalsIgnoreCase(OBJECT_KEYWORD))
			loadObjectRow(row);
		else if (keyword.equalsIgnoreCase(METHOD_KEYWORD))
			loadMethodRow(row);
		else if (keyword.equalsIgnoreCase(ACCESSOR_KEYWORD))
			loadAccessorRow(row);
		else if (keyword.equalsIgnoreCase(MUTATOR_KEYWORD))
			loadMutatorRow(row);
		else if (keyword.equalsIgnoreCase(STATE_KEYWORD))
			loadStateMappingRow(row);
		else if (keyword.equalsIgnoreCase(OPTION_KEYWORD) || keyword.equalsIgnoreCase(OPTIONS_KEYWORD))
			options = getRowString(row);
		else if (keyword.equalsIgnoreCase(HIDDEN_KEYWORD))
			hidden = getRowString(row);
		else if (keyword.equalsIgnoreCase(REGION_KEYWORD))
			loadRegionRow(row);
		else if (keyword.equalsIgnoreCase(PARAMETER_KEYWORD) || keyword.equalsIgnoreCase(PARAMETERS_KEYWORD))
			parameters = getRowString(row);
	}

	private void loadObjectRow(Row row){
		Vector<Object> object = new Vector<Object>();
		String objectCount = ""+(objects.size()+1);
		object.add(objectCount);
		object.add(row.getCell(1)!=null? row.getCell(1).toString(): "");
		object.add(row.getCell(2)!=null? row.getCell(2).toString(): "");		
		objects.add(object);
	}

	private void loadMethodRow(Row row){
		Vector<Object> methodMapping = loadMappingRow(row);
		String methodCount = ""+(methods.size()+1);
		methodMapping.set(0, methodCount);
		methods.add(methodMapping);
	}

	private void loadAccessorRow(Row row){
		Vector<Object> accessorMapping = loadMappingRow(row);
		String accessorCount = ""+(accessors.size()+1);
		accessorMapping.set(0, accessorCount);
		accessors.add(accessorMapping);
	}
	
	private void loadMutatorRow(Row row){
		Vector<Object> mutatorMapping = loadMappingRow(row);
		String mutatorCount = ""+(mutators.size()+1);
		mutatorMapping.set(0, mutatorCount);
		mutators.add(mutatorMapping);
	}

	private Vector<Object> loadMappingRow(Row row){
		Vector<Object> mapping = new Vector<Object>();
		mapping.add("");
		mapping.add(row.getCell(1)!=null? row.getCell(1).toString(): "");
		mapping.add(row.getCell(2)!=null? row.getCell(2).toString(): "");
		mapping.add(row.getCell(3)!=null? row.getCell(3).toString(): "");
		mapping.add(row.getCell(4)!=null? row.getCell(4).toString(): "");
		return mapping;
	}

	// accessor and mutator for the same state predicate on the same row
	private void loadStateMappingRow(Row row){
		if (row.getCell(2)!=null && !row.getCell(2).toString().trim().equals("")) {
			Vector<Object> accessor = new Vector<Object>();
			String accessorCount = ""+(accessors.size()+1);
			accessor.add(accessorCount);
			accessor.add(row.getCell(1).toString());
			accessor.add(row.getCell(2).toString());
			accessor.add("");
			accessor.add("");
			accessors.add(accessor);
		}
		if (row.getCell(3)!=null && !row.getCell(3).toString().trim().equals("")) {
			Vector<Object> mutator = new Vector<Object>();
			String mutatorCount = ""+(mutators.size()+1);
			mutator.add(mutatorCount);
			mutator.add(row.getCell(1).toString());
			mutator.add(row.getCell(3).toString());
			mutator.add("");
			mutator.add("");
			mutators.add(mutator);
		}
	}
	
	private void loadRegionRow(Row row){
		if (row.getCell(1)!=null  && row.getCell(2)!=null){
			Vector<Object> region = new Vector<Object>();
			region.add(row.getCell(1).toString().trim());
			region.add(row.getCell(2).toString().trim());
			regions.add(region);
		}
	}

	public static void printVector(Vector<Object> objects){
		for (Object object: objects)
			if (object==null)
				System.out.print("null");
			else
				System.out.print(object.toString()+" ");
		System.out.println();
	}

	// get language indicated by MIM
	
	public static TargetLanguage getMIMLanguage(Sheet sheet){
		TargetLanguage mimLanguage = null;
		for (Row row: sheet){
			if (row.getCell(0)==null)
				continue;
			String keyword = row.getCell(0).toString().trim();
			if (keyword.equals("")|| keyword.startsWith("//") )
				continue;
			if (keyword.equalsIgnoreCase(CLASS_KEYWORD))	// OO language
				return TargetLanguage.JAVA;
			else if (keyword.equalsIgnoreCase(URL_KEYWORD))
				return TargetLanguage.HTML;
			else if (keyword.equalsIgnoreCase(RPC_KEYWORD))
				return TargetLanguage.RPC;
			else if (keyword.equalsIgnoreCase(SELENIUM_KEYWORD))
				return TargetLanguage.SELENIUMDRIVER;
			else if (keyword.equalsIgnoreCase(FUNCTION_KEYWORD))
				return TargetLanguage.C;
			else if (keyword.equalsIgnoreCase(UFT_KEYWORD))
				return TargetLanguage.UFT;
		}
		return mimLanguage;
	}

	// language indicated by helper code
	public static TargetLanguage getHelperCodeLanguage(Sheet sheet){
		TargetLanguage helperCodeLanguage = null;
		for (Row row: sheet){
			if (row.getCell(0)==null || row.getCell(1)==null)
				continue;
			String keyword = row.getCell(0).toString().trim();
			if (keyword.equals("")|| keyword.startsWith("//") )
				continue;
			if (keyword.equalsIgnoreCase(PACKAGE_KEYWORD))
				return TargetLanguage.JAVA;
			else if (keyword.equalsIgnoreCase(IMPORT_KEYWORD))
				return keyword.endsWith(";")? TargetLanguage.JAVA: TargetLanguage.PYTHON;
			else if (keyword.equalsIgnoreCase(FROM_KEYWORD))
				return TargetLanguage.PYTHON;
			else if (keyword.equalsIgnoreCase(IMPORTS_KEYWORD))
				return TargetLanguage.VB;
			else if (keyword.equalsIgnoreCase(USING_KEYWORD))
				return TargetLanguage.CSHARP;
			else if (keyword.equalsIgnoreCase(PHP_KEYWORD))
				return TargetLanguage.PHP;
			else if (keyword.equalsIgnoreCase(INCLUDE_KEYWORD))
				return TargetLanguage.CPP;
			else if (keyword.equalsIgnoreCase(SETTINGS_KEYWORD))
				return TargetLanguage.KBT;
		}
		return helperCodeLanguage;
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	// helper code
	
	private String packageCode = "";
	private String importCode = "";
	private String setUpCode = "";
	private String tearDownCode = "";
	private String alphaCode = "";
	private String omegaCode = "";
	private ArrayList<String> codeSegments = new ArrayList<String>();
	
	public String getPackageCode(){
		return packageCode;
	}
	
	public String getImportCode(){
		return importCode;
	}
	
	public String getSetUpCode(){
		return setUpCode;
	}
	
	public String getTearDownCode(){
		return tearDownCode;
	}
	
	public String getAlphaCode(){
		return alphaCode;
	}
	
	public String getOmegaCode(){
		return omegaCode;
	}
	
	public ArrayList<String> getCodeSegments(){
		return codeSegments;
	}

	public void loadHelperCode(Sheet sheet){
		for (Row row: sheet){
			loadHelperCodeRow(row);
		}
	}
	
	private void loadHelperCodeRow(Row row){
		if (row.getCell(0)==null || row.getCell(1)==null)
			return;
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equals("")|| keyword.startsWith("//") )
			return;
		if (keyword.equalsIgnoreCase(PACKAGE_KEYWORD) || keyword.equalsIgnoreCase(NAMESPACE_KEYWORD)){
			packageCode = row.getCell(1).toString();
		}
		else if (keyword.equalsIgnoreCase(IMPORT_KEYWORD) || keyword.equalsIgnoreCase(IMPORTS_KEYWORD) 
				|| keyword.equalsIgnoreCase(USING_KEYWORD) || keyword.equalsIgnoreCase(INCLUDE_KEYWORD)
				|| keyword.equalsIgnoreCase(SETTINGS_KEYWORD) 
				|| keyword.equalsIgnoreCase(PHP_KEYWORD)
				){
			importCode = row.getCell(1).toString();
		}
		else if (keyword.equalsIgnoreCase(ALPHA_KEYWORD))
			alphaCode = row.getCell(1).toString();
		else if (keyword.equalsIgnoreCase(OMEGA_KEYWORD))
			omegaCode = row.getCell(1).toString();
		else if (keyword.equalsIgnoreCase(SETUP_KEYWORD))
			setUpCode = row.getCell(1).toString();
		else if (keyword.equalsIgnoreCase(TEARDOWN_KEYWORD))
			tearDownCode = row.getCell(1).toString();
		else if (keyword.equalsIgnoreCase(SELENIUMSETUP_KEYWORD))
			loadSeleniumSetupRow(row);
		else if (keyword.equalsIgnoreCase(SELENIUMTEARDOWN_KEYWORD))
			loadSeleniumTeardownRow(row);
		else if (keyword.equalsIgnoreCase(CODE_KEYWORD))
			codeSegments.add(row.getCell(1).toString());
	}
	
	public static int createKeyValuePairRow(String key, String value, Sheet sheet, int currentRow, CellStyle lineWrapStyle){
		int rowIndex = currentRow;
		
//		if (value!=null && !value.trim().equals("")){
		if (value!=null){
			sheet.createRow(rowIndex++);
			Row row = sheet.createRow(rowIndex++);
			Cell keywordCell = row.createCell(0);
			keywordCell.setCellStyle(lineWrapStyle);
			keywordCell.setCellValue(key);
			Cell valueCell = row.createCell(1);
			valueCell.setCellStyle(lineWrapStyle);
			valueCell.setCellValue(value);
		}
		return rowIndex;
	}
	
	public static int createSplitColumnsRow(String keyword, JTextArea textArea, Sheet sheet, int currentRow, CellStyle lineWrapStyle){
		int rowIndex = currentRow;
		String text = textArea.getText().trim();
		if (text!=null && !text.equals("")){
	        sheet.createRow(rowIndex++);
	        Row row = sheet.createRow(rowIndex++);
			Cell keywordCell = row.createCell(0);
			keywordCell.setCellValue(keyword);
			String[] segments = text.split("\n\n");			// refer to getRowString
			for (int index = 0; index<segments.length; index++){
				Cell cell = row.createCell(index+1);
			   	cell.setCellStyle(lineWrapStyle);
				cell.setCellValue(segments[index]);
			}
		}
		return rowIndex;
	}

	public static int createTableModelTypeRow(ModelType modelType, Sheet sheet, int currentRow){
		int rowIndex = currentRow;
		sheet.createRow(rowIndex++);
        Row row = sheet.createRow(rowIndex++);
        Cell keywordCell = row.createCell(0);
        keywordCell.setCellValue(MODEL_TYPE_KEYWORD);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(SystemOptions.getModelTypeString(modelType));
        return rowIndex;
 	}

	public static int createTableModelTypeRow(ModelType modelType, String separateModelFileName, Sheet sheet, int currentRow){
		int rowIndex = currentRow;
		sheet.createRow(rowIndex++);
        Row row = sheet.createRow(rowIndex++);
        Cell keywordCell = row.createCell(0);
        keywordCell.setCellValue(MODEL_TYPE_KEYWORD);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(SystemOptions.getModelTypeString(modelType));
        Cell separateFileCell = row.createCell(2);
        separateFileCell.setCellValue(separateModelFileName);
        return rowIndex;
 	}

	
	public static int createTableTitleRow(String[] columnNames, Sheet sheet, int currentRow){
		int rowIndex = currentRow;
		sheet.createRow(rowIndex++);
        Row row = sheet.createRow(rowIndex++);
        for (int index=1; index<columnNames.length; index++){
        	Cell cell = row.createCell(index);
        	cell.setCellValue(columnNames[index]);
        }
        return rowIndex;
 	}
	
	public static int createTableRows(String keyword, Vector<Vector<Object>> data, Sheet sheet, int currentRow, CellStyle lineWrapStyle){
		int rowIndex = currentRow;
		for (Vector<Object> record: data){
//printVector(record);			
			if (!isRecordEmpty(record)){
				Row row = sheet.createRow(rowIndex++);
				Cell keywordCell = row.createCell(0);
				keywordCell.setCellValue(keyword);
				// Column 0 is number
				for (int index=1; index<record.size(); index++){
					Cell cell = row.createCell(index);
					cell.setCellStyle(lineWrapStyle);
					Object element = record.get(index);
					if (element!=null)
						cell.setCellValue(element.toString());
				}
			}
		}
		return rowIndex;
	}

	public static boolean isRecordEmpty(Vector<Object> record){
		for (int index=1; index<record.size(); index++){
			Object element = record.get(index);
			if (element!=null && !element.toString().trim().equals("")){
				return false;
			}
		}
		return true;
	}
	
	public static String getPackageKeyword(TargetLanguage language){
		if (language==TargetLanguage.JAVA)
			return PACKAGE_KEYWORD;
		else
		if (language==TargetLanguage.CSHARP)
			return NAMESPACE_KEYWORD;
		return "";
	}

	public static String getImportKeyword(TargetLanguage language){		
		if (language==TargetLanguage.JAVA || language==TargetLanguage.PYTHON)
			return IMPORT_KEYWORD;
		else
		if (language==TargetLanguage.CSHARP)
			return USING_KEYWORD;
		else
		if (language==TargetLanguage.PHP)
			return PHP_KEYWORD;
		else
		if (language==TargetLanguage.VB)
			return IMPORTS_KEYWORD;
		else
		if (language==TargetLanguage.KBT)
			return SETTINGS_KEYWORD;
		else if (language!=TargetLanguage.HTML)
			return INCLUDE_KEYWORD;
		else
			return "";
	}

	public static String getSystemNameKeyword(TargetLanguage language){
		if (language==TargetLanguage.HTML)
			return URL_KEYWORD;
		else
		if (language==TargetLanguage.RPC)
			return RPC_KEYWORD;
		else
		if (language==TargetLanguage.SELENIUMDRIVER)
				return SELENIUM_KEYWORD;
		else
		if (language==TargetLanguage.UFT)
				return UFT_KEYWORD;
		else
		if (language instanceof TargetLanguageOO)
			return CLASS_KEYWORD;
		else // C
			return FUNCTION_KEYWORD;
	}
	
	private Vector<Vector<Object>> seleniumSetupCommands = new Vector<Vector<Object>>();
	private Vector<Vector<Object>> seleniumTeardownCommands = new Vector<Vector<Object>>();

	public Vector<Vector<Object>> getSeleniumSetupCommands(){
		return seleniumSetupCommands;
	}

	public Vector<Vector<Object>> getSeleniumTeardownCommands(){
		return seleniumTeardownCommands;
	}

	private void loadSeleniumSetupRow(Row row){
		Vector<Object> command = loadSeleniumRow(row);
		String commandCount = ""+(seleniumSetupCommands.size()+1);
		command.set(0, commandCount);
		seleniumSetupCommands.add(command);
	}

	private void loadSeleniumTeardownRow(Row row){
		Vector<Object> command = loadSeleniumRow(row);
		String commandCount = ""+(seleniumTeardownCommands.size()+1);
		command.set(0, commandCount);
		seleniumTeardownCommands.add(command);
	}

	private Vector<Object> loadSeleniumRow(Row row){
		Vector<Object> command = new Vector<Object>();
		command.add("");
		command.add(row.getCell(1)!=null? row.getCell(1).toString(): "");
		command.add(row.getCell(2)!=null? row.getCell(2).toString(): "");
		command.add(row.getCell(3)!=null? row.getCell(3).toString(): "");
		return command;
	}

	public static int createSeleniumCommandRows(String key, Vector<Vector<Object>> table, Sheet sheet, int currentRow, CellStyle lineWrapStyle){
		int rowIndex = currentRow;
		for (int index=0; index<table.size(); index++){
			Vector<Object> tableRow = table.get(index);
			if (tableRow.get(1)==null)
				continue;
			String cmd = tableRow.get(1).toString();
			String target = tableRow.get(2)!=null? tableRow.get(2).toString():""; 
			String value = tableRow.get(3)!=null? tableRow.get(3).toString():"";
			
			Row row = sheet.createRow(rowIndex++);
			
			Cell keywordCell = row.createCell(0);
			keywordCell.setCellStyle(lineWrapStyle);
			keywordCell.setCellValue(key);
			
			Cell commandCell = row.createCell(1);
			commandCell.setCellStyle(lineWrapStyle);
			commandCell.setCellValue(cmd);
			
			Cell targetCell = row.createCell(2);
			targetCell.setCellStyle(lineWrapStyle);
			targetCell.setCellValue(target);
			
			Cell valueCell = row.createCell(3);
			valueCell.setCellStyle(lineWrapStyle);
			valueCell.setCellValue(value);
		}
		return rowIndex;
	}

}
