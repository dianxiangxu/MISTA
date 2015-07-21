/* 	
	Author Dianxiang Xu
*/
package edit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import kernel.CancellationException;
import kernel.Kernel;
import kernel.ProgressDialog;
import kernel.SystemOptions;
import locales.LocaleBundle;

import parser.MIDParser;
import parser.ParseException;

import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;

import testgeneration.ParaRecord;
import testgeneration.ParaTableModel;
import testgeneration.TransitionTree;
import testgeneration.TransitionTreeNode;

// Save/load test tree into/from an excel file
//		First row: test generation information	
//			version for dealing with compatibility of different versions 
//			model file name used to generate the test tree 
//			coverage used to generate the test tree
//		For each node in the tree
// 			One row for the firing: node id, transition index, substitution, #children, negative, marking
//			One row for each test input <expression, isParameter>
//

public class TestTreeFile {

	private static int rowIndex = 0; 
	
	static public void saveTestDataToExcelFile(TransitionTree tree, File outputFile) throws Exception {
		// open file stream
		rowIndex = 0;
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Test data");
		writeHeader(tree, sheet);
		writeAllNodes(tree, sheet);
		FileOutputStream out = new FileOutputStream(outputFile);
	    wb.write(out);
		out.close();
 	}
	
	static private void writeHeader(TransitionTree tree, Sheet sheet){
        Row headerRow = sheet.createRow(rowIndex++);
        Cell versionCell = headerRow.createCell(0);
        versionCell.setCellValue(Kernel.SYSTEM_NAME+Kernel.SYSTEM_VERSION);

        Cell midCell = headerRow.createCell(1);
        midCell.setCellValue(tree.getMID().getFileName());
        
        Cell coverageCell = headerRow.createCell(2);
        coverageCell.setCellValue(tree.getSystemOptions().getCoverageCriterion().getID());
 
/*        
        Cell languageCell = headerRow.createCell(3);
        languageCell.setCellValue(tree.getSystemOptions().getLanguageIndex());

        Cell testFrameworkCell = headerRow.createCell(4);
        testFrameworkCell.setCellValue(tree.getSystemOptions().getTestFrameworkIndex());
*/
	}
	
	static private void writeAllNodes(TransitionTree tree, Sheet sheet) throws CancellationException{
		MID mid = tree.getMID();
		TransitionTreeNode root = tree.getRoot();
		writeRootFiring(root, sheet);		
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode child: root.children())
		queue.addLast(child);
		while (!queue.isEmpty()) {
			tree.checkForCancellation();
			TransitionTreeNode node = queue.poll();
			writeNodeFiring(node, mid, sheet);
			writeUserInput(node, sheet);
			for (TransitionTreeNode child: node.children())
				queue.addLast(child);
		}
	}

	// Separate from writeFiring to make it more efficient
	static private void writeRootFiring(TransitionTreeNode node, Sheet sheet){
		// one row in excel file
        Row currentRow = sheet.createRow(rowIndex++);
        
        Cell nodeNumberCell = currentRow.createCell(0);
        nodeNumberCell.setCellValue(node.getOutlineNumber());

        Cell transitionIndexCell = currentRow.createCell(1);
       	transitionIndexCell.setCellValue("");

        Cell subsitutionCell = currentRow.createCell(2);
		subsitutionCell.setCellValue("");

		Cell numberOfChildrenCell = currentRow.createCell(3);
		int numberOfChildren = node.hasChildren()? node.children().size(): 0; 
		numberOfChildrenCell.setCellValue(numberOfChildren);

		Cell negativeCell = currentRow.createCell(4);
		negativeCell.setCellValue(node.isNegative());

		Cell markingCell = currentRow.createCell(5);
		markingCell.setCellValue("");

	}
	
	static private void writeNodeFiring(TransitionTreeNode node, MID mid, Sheet sheet){
		// one row in excel file
        Row currentRow = sheet.createRow(rowIndex++);
        
        Cell nodeNumberCell = currentRow.createCell(0);
        nodeNumberCell.setCellValue(node.getOutlineNumber());

        Cell transitionIndexCell = currentRow.createCell(1);
        Transition transition = node.getTransition();
        if (node.isNegative())
        	transitionIndexCell.setCellValue(transition.getEvent());
        else
        	transitionIndexCell.setCellValue(mid.getTransitionIndex(transition));

        Cell subsitutionCell = currentRow.createCell(2);
		String substitutionString = "";
		if (node.getSubstitution()!=null){
			ArrayList<String> arguments = transition.getArguments();
			if (arguments==null)
				arguments= transition.getAllVariables();
			substitutionString = node.getSubstitution().toString(arguments);
		}
		subsitutionCell.setCellValue(substitutionString);

		Cell numberOfChildrenCell = currentRow.createCell(3);
		int numberOfChildren = node.hasChildren()? node.children().size(): 0; 
		numberOfChildrenCell.setCellValue(numberOfChildren);

		Cell negativeCell = currentRow.createCell(4);
		negativeCell.setCellValue(node.isNegative());

		Cell markingCell = currentRow.createCell(5);
		Marking marking = node.getMarking();
		markingCell.setCellValue(marking.toString());
	}
	
	static private void writeUserInput(TransitionTreeNode node, Sheet sheet){
		ParaTableModel paraTable = node.getParaTable();
		if (paraTable!=null) {
			for (ParaRecord record: paraTable.getDataVector())
				if (!record.getExpression().trim().equals("")) {
					Row currentRow = sheet.createRow(rowIndex++);
					Cell expressionCell = currentRow.createCell(0);
					expressionCell.setCellValue(record.getExpression());
					Cell isParameterCell = currentRow.createCell(1);
					isParameterCell.setCellValue(record.isParameter());
				}
		}
	}

	//////////////////////////////////////////////////////
	// read test data from excel file	
	static public File getMidFileOfTestData(Kernel frame, File testDataFile) {
		File midFile = null;
		try {
			HSSFWorkbook workBook = new HSSFWorkbook (new FileInputStream(testDataFile));
			Sheet sheet = workBook.getSheetAt(0);
			Row headerRow = sheet.getRow(0);
			if (headerRow==null) {
				frame.printDialogMessage(LocaleBundle.bundleString("Invalid test data format"));
				return null;
			}
			Cell versionCell = headerRow.getCell(0);
			Cell midCell = headerRow.getCell(1);	        
			if (versionCell==null || midCell==null){
				frame.printDialogMessage(LocaleBundle.bundleString("Invalid test data format"));
				return null;
			}
		    midFile = new File(midCell.toString());
		    if (!midFile.exists()){
		    	String fileName = midFile.getName();
		    	String dir = testDataFile.getParent();
		    	midFile = new File(dir+File.separator+fileName);
		    	if (!midFile.exists()){
		    		frame.printDialogMessage(LocaleBundle.bundleString("Cannot find model file")
		    				+" "+midCell.toString() + " "+LocaleBundle.bundleString("or")+" "+fileName+".");
		    		return null;
		    	}	
		    }
		}
		catch (Exception e) {
	//		e.printStackTrace();
			frame.printDialogMessage(LocaleBundle.bundleString("Fail to load test data"));			
		}
		return midFile;
	}

	//////////////////////////////////////////////////////
	// read test data from excel file	
	public static void loadTestDataFromExcelFile(Kernel kernel, File testDataFile, File midFile, MID mid, SystemOptions options) {
		try {
			HSSFWorkbook workBook = new HSSFWorkbook (new FileInputStream(testDataFile));
			Sheet sheet = workBook.getSheetAt(0);
			Row headerRow = sheet.getRow(0);
			if (headerRow==null)
				throw new Exception(LocaleBundle.bundleString("Invalid test data format"));
			Cell versionCell = headerRow.getCell(0);
			Cell midCell = headerRow.getCell(1);	        
			if (versionCell==null || midCell==null)
				throw new Exception(LocaleBundle.bundleString("Invalid test data format"));
			updateSystemOptions(headerRow, options); 
			TransitionTree tree = new TransitionTree(mid, options, false);
			if (mid.checkAttackTransition()==null){
				for (Transition transition: mid.getTransitions())
					if (transition.isAttackTransition())
						mid.addHiddenPlaceOrEvent(transition.getEvent());
			}	
			ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Load Test Data"), LocaleBundle.bundleString("Loading test data"));
			TestTreeLoader testImport = new TestTreeLoader(kernel, progressDialog, tree, testDataFile, midFile, mid, sheet);
			Thread testImportThread = new Thread(testImport);
			testImportThread.start();
			progressDialog.setVisible(true);
		}
		catch (Exception e){
			kernel.getFileManager().editNewFile(SystemOptions.DEFAULT_MODEL_TYPE);
			kernel.printDialogMessage(LocaleBundle.bundleString("Invalid test data format"));
		}
	}
	
	static private void updateSystemOptions(Row headerRow, SystemOptions options) throws Exception{
		Cell coverageCell = headerRow.getCell(2);	    
		if (coverageCell==null){
			throw new Exception(LocaleBundle.bundleString("Invalid test data format"));
		}
		options.setCoverageCriterion(coverageCell.toString().trim());
/*		
		Cell languageCell = headerRow.getCell(3);
		Cell testFrameworkCell = headerRow.getCell(4);
		if (languageCell!=null && testFrameworkCell!=null){
			try {
				options.setLanguage((int)Double.parseDouble(languageCell.toString()));
				options.setTestFrameworkIndex((int)Double.parseDouble(testFrameworkCell.toString()));
			}
			catch (Exception e) {
				throw new Exception("Invalid lanaguage/test framework options!");			
			}
		}
*/
	}
	
	static public void loadAllNodes(ProgressDialog progressDialog, TransitionTree tree, MID mid, Sheet sheet) throws Exception {
		int numberOfRows = sheet.getPhysicalNumberOfRows();
		TransitionTreeNode root = readRootFiring(sheet);
		tree.setRoot(root);
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		queue.addLast(root);	
		while (!queue.isEmpty()) {
			tree.checkForCancellation();
			TransitionTreeNode node = queue.poll();
			progressDialog.setMessage(LocaleBundle.bundleString("Loading")+" "+rowIndex+" "+LocaleBundle.bundleString("out of")+" "+numberOfRows+"...");
//System.out.println("node: "+node);	
			int numberOfChildren = node.getNumberOfSuccessors();
//System.out.println("#Children: "+numberOfChildren);	
			if (numberOfChildren>0) {
				for (int i=0; i<numberOfChildren; i++){
					tree.checkForCancellation();
					TransitionTreeNode child = readNodeFiring(node, mid, sheet);
					readUserInput(child, sheet);
					node.add(child);
				}
				for (TransitionTreeNode child: node.children())
					queue.addLast(child);
			}
		}		
		root.resetChildrenOutlineNumbers(tree.getSystemOptions().getMaxIdDepth());
	} 

	static TransitionTreeNode readRootFiring(Sheet sheet) throws IOException {
		rowIndex = 1;
	    Row rootRow = sheet.getRow(rowIndex++);
	    if (rootRow==null) {
	        throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));	 
	    }
		Cell numberOfChildrenCell = rootRow.getCell(3);
		TransitionTreeNode root = new TransitionTreeNode(null, null, null);
		root.setNumberrOfSuccessors(getNumberOfChildren(numberOfChildrenCell.toString()));
		return root;
	}

	static private int getNumberOfChildren(String numberString) throws IOException{
		int numberOfChildren = 0;
		try {
			numberOfChildren = (int)Double.parseDouble(numberString);
		}
		catch (NumberFormatException e){
			throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));
		}
	    if (numberOfChildren<0)	
			throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));
 
//	   System.out.println("#children "+numberOfChildren);	    
	    return numberOfChildren;
	}
	
	static TransitionTreeNode readNodeFiring(TransitionTreeNode parent, MID mid, Sheet sheet) throws IOException {
		
	    Row rootRow = sheet.getRow(rowIndex++);
	    if (rootRow==null)
	        throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));	        
	    Cell nodeNumberCell = rootRow.getCell(0);
//System.out.println("Node number "+nodeNumberCell.toString());
	    if (!parent.getOutlineNumber().equals(""))
	    	assert nodeNumberCell.toString().startsWith(parent.getOutlineNumber()): LocaleBundle.bundleString("Incorrect test data format");
        Cell transitionIndexCell = rootRow.getCell(1);
        Cell substitutionCell = rootRow.getCell(2);
		Cell numberOfChildrenCell = rootRow.getCell(3);
		Cell negativeCell = rootRow.getCell(4);
		Cell markingCell = rootRow.getCell(5);
		
		boolean negative = negativeCell.toString().equalsIgnoreCase("true");

		Substitution substitution = getSubstitution(substitutionCell.toString());
		Marking marking = getMarking(markingCell.toString());

		Transition transition = null;
		if (!negative)
			transition = getTransition(transitionIndexCell.toString(), mid);
		else {
			transition = new Transition(transitionIndexCell.toString()); // event name
			ArrayList<String> arguments = getVariablesInSubstitution(substitutionCell.toString());
			transition.setArguments(arguments);
			transition.setAllVariables(arguments);
		}
//System.out.println("Transition "+transition);
		TransitionTreeNode newNode = new TransitionTreeNode(transition, substitution, marking, negative);
		newNode.setParent(parent);
		newNode.setNumberrOfSuccessors(getNumberOfChildren(numberOfChildrenCell.toString()));
//System.out.println("read node: "+newNode);	
		return newNode;
	}

	static private Transition getTransition(String transitionIndexString, MID mid) throws IOException {
		int transitionIndex = -1;
		try {
			transitionIndex = (int)Double.parseDouble(transitionIndexString);
		}
		catch (NumberFormatException e){
			throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));
		}
		if (transitionIndex==-1)	
			return new Transition(MID.ConstructorEvent);
		else 
			return mid.getTransitionAtIndex(transitionIndex);
	}
	
	static private ArrayList<String> getVariablesInSubstitution(String substitutionString) throws IOException{
		ArrayList<String> variables = null; 
		try {
			variables = MIDParser.parseVariablesInSubstitutionString(substitutionString);
		}
		catch (ParseException e){
			throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));			
		}
		return variables;
	}

	static private Substitution getSubstitution(String substitutionString) throws IOException{
		Substitution substitution = null; 
		try {
			substitution = MIDParser.parseSubstitutionString(substitutionString);
//			System.out.println("Substitution:" + substitution.printAllBindings());
		}
		catch (ParseException e){
			throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));			
		}
		return substitution;
	}
	
	static private Marking getMarking(String markingString) throws IOException {
		Marking marking = new Marking(); 
		if (markingString!=null && !markingString.equals(""))
			try {
				marking = MIDParser.parseMarkingString(markingString);
			}
			catch (ParseException e){
//				System.out.println("Marking: " + markingString);
				throw new IOException(LocaleBundle.bundleString("Incorrect test data format"));						
			}	
		return marking;
	}

	static void readUserInput(TransitionTreeNode node, Sheet sheet) {
		Row row = sheet.getRow(rowIndex);
//System.out.println("Columns: "+row.getPhysicalNumberOfCells());					
		Vector<ParaRecord> dataVector = new Vector<ParaRecord>();
		while (row!=null && row.getPhysicalNumberOfCells()==2) {
			Cell expressionCell = row.getCell(0);
			Cell isParameterCell = row.getCell(1);
//System.out.println("Exp: " + expressionCell.toString());			
			if (expressionCell!=null && !expressionCell.toString().equals("")){
				boolean isParameter = isParameterCell!=null && isParameterCell.toString().equalsIgnoreCase("true")? true: false;
				dataVector.add(new ParaRecord(expressionCell.toString(), isParameter));
				rowIndex++;
				row = sheet.getRow(rowIndex);
			}
		}
		if (dataVector.size()>0)
			node.setParaTable(new ParaTableModel(dataVector));
	}
	
	
}
