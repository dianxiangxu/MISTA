package cpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import kernel.SystemOptions;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import edit.XMIDProcessor;

import utilities.FileUtil;

// The Petri nets in ISTA are a special type of CPN. 
// 1. No colors/data types - all data are strings.
// 2. The weight of all arcs and tokens is 1. 
// 3. Classical black token and arc label are represented by 1.   
// 4. Arc inscriptions can only contain constants, variables, and their tuples. If-then-else is now supported. 
// 5. Time is not supported.

public class CPNConverter {

	private static final String tmpMIDFile = "ista.tmp";

    static private int rowIndex = 0;
    static private CellStyle lineWrapStyle; 
    
	public static File convertCPNToMIDFile(File file) throws Exception{
		CPNNet net = new CPNNet(file);
		Workbook wb = null;
		Sheet modelSheet = null;
		File targetMIDFile = new File (file.getParent()+File.separator+FileUtil.getTargetMIDFileName(file));
		File tmpFile = null;
		FileInputStream inputStream = null;
		if (targetMIDFile.exists()){
			tmpFile = new File(tmpMIDFile);
			FileUtil.copyFile(targetMIDFile, tmpFile);	// use a copy for modification (reading/writing);   
			inputStream = new FileInputStream(tmpMIDFile);
			wb = new HSSFWorkbook(inputStream);
			modelSheet = wb.getSheetAt(0);
			clearSheet(modelSheet);
		}
		else {
			wb = new HSSFWorkbook();
	        modelSheet = wb.createSheet("MODEL");
	        createMIMSheet(wb);
	        createCodeSheet(wb);
		}
	    lineWrapStyle = wb.createCellStyle();
	    lineWrapStyle.setWrapText(true);
        writeModelSheet(modelSheet, net);
 		FileOutputStream out = new FileOutputStream(targetMIDFile);
	    wb.write(out);
		out.close();
		if (inputStream!=null)
			inputStream.close();
		if (tmpFile!=null && tmpFile.exists())
			tmpFile.delete();
		return targetMIDFile;
	}

	public static File convertPNMLToMIDFile(File file) throws Exception {
		Workbook wb = null;
		Sheet modelSheet = null;
		File targetMIDFile = new File (file.getParent()+File.separator+FileUtil.getTargetMIDFileName(file));
		File tmpFile = null;
		FileInputStream inputStream = null;
		if (targetMIDFile.exists()){
			tmpFile = new File(tmpMIDFile);
			FileUtil.copyFile(targetMIDFile, tmpFile);	// use a copy for modification (reading/writing);   
			inputStream = new FileInputStream(tmpMIDFile);
			wb = new HSSFWorkbook(inputStream);
			modelSheet = wb.getSheetAt(0);
			clearSheet(modelSheet);
		}
		else {
			wb = new HSSFWorkbook();
	        modelSheet = wb.createSheet("MODEL");
	        createMIMSheet(wb);
	        createCodeSheet(wb);
		}
	    lineWrapStyle = wb.createCellStyle();
	    lineWrapStyle.setWrapText(true);
	    String modelFileName = file.getName();
	    if (modelFileName.endsWith(FileUtil.PNMLFileExtension)) {
	    	modelFileName = modelFileName.replace(".pnml", ".xml");
	    	File XMLfile = new File(file.getParent()+File.separator+modelFileName);
	    	convertPNMLFile(file, XMLfile);	   
	    }
        writeModelSheetForPNML(modelSheet, modelFileName);
 		FileOutputStream out = new FileOutputStream(targetMIDFile);
	    wb.write(out);
		out.close();
		if (inputStream!=null)
			inputStream.close();
		if (tmpFile!=null && tmpFile.exists())
			tmpFile.delete();
		return targetMIDFile;
	}


	private static void convertPNMLFile(File inFile, File outFile) {
		Scanner in=null;
		FileWriter out=null;
		try {
			in = new Scanner(new FileReader(inFile));
			out = new FileWriter(outFile);
			while (in.hasNextLine()){
				String line = in.nextLine();
				if (line.indexOf("<text>")>=0)
					line = line.replace("<text>", "<value>");
				if (line.indexOf("</text>")>=0)
					line = line.replace("</text>", "</value>");
				out.write(line);
				out.write("\n");
			}
		} catch (IOException ioe){
		}
		try {
			in.close();
			out.close();
		} catch (IOException ioe){
		}
	}

	static private void writeModelSheetForPNML(Sheet sheet, String modelFileName) {
        sheet.autoSizeColumn((short)0); //adjust width of the first column
        sheet.setFitToPage(true);
        sheet.setColumnWidth(0, 16*256);
        sheet.setColumnWidth(1, 16*256);
        sheet.setColumnWidth(2, 16*256);
        sheet.setColumnWidth(3, 16*256);
        sheet.setColumnWidth(4, 16*256);
        sheet.setColumnWidth(5, 16*256);
        sheet.setColumnWidth(6, 16*256);

		rowIndex = 0;
        Row commentRow = sheet.createRow(rowIndex++);
		Cell commentCell = commentRow.createCell(1);
		commentCell.setCellValue("I. MODEL");
        sheet.createRow(rowIndex++);

        Row modelTypeRow = sheet.createRow(rowIndex++);
		Cell keywordCell = modelTypeRow.createCell(0);
		keywordCell.setCellValue(XMIDProcessor.MODEL_TYPE_KEYWORD);
		Cell valueCell = modelTypeRow.createCell(1);
		valueCell.setCellValue(SystemOptions.FUNCTIONNET_KEYWORD);		
		Cell modelFileCell = modelTypeRow.createCell(2);
		modelFileCell.setCellValue(modelFileName);		
        sheet.createRow(rowIndex++);
	}

	
	static private void writeModelSheet(Sheet sheet, CPNNet net) {
        sheet.autoSizeColumn((short)0); //adjust width of the first column
        sheet.setFitToPage(true);
        sheet.setColumnWidth(0, 16*256);
        sheet.setColumnWidth(1, 16*256);
        sheet.setColumnWidth(2, 16*256);
        sheet.setColumnWidth(3, 16*256);
        sheet.setColumnWidth(4, 16*256);
        sheet.setColumnWidth(5, 16*256);
        sheet.setColumnWidth(6, 16*256);

		rowIndex = 0;
        Row commentRow = sheet.createRow(rowIndex++);
		Cell commentCell = commentRow.createCell(1);
		commentCell.setCellValue("I. MODEL");
        sheet.createRow(rowIndex++);

        Row modelTypeRow = sheet.createRow(rowIndex++);
		Cell keywordCell = modelTypeRow.createCell(0);
		keywordCell.setCellValue(XMIDProcessor.MODEL_TYPE_KEYWORD);
		Cell valueCell = modelTypeRow.createCell(1);
		valueCell.setCellValue(SystemOptions.FUNCTIONNET_KEYWORD);		
        sheet.createRow(rowIndex++);

        createTransitionTitleRow(sheet);
        for (CPNTransition transition: net.getTransitions())
        	createTransitionRow(sheet, transition);

        sheet.createRow(rowIndex++);
        sheet.createRow(rowIndex++);
        createMarkingRow(sheet, "INIT", net.getInitMarking());
        sheet.createRow(rowIndex++);
	}

	static private void createTransitionTitleRow(Sheet sheet){
    	Row row = sheet.createRow(rowIndex++);
    	Cell keywordCell = row.createCell(0);
    	keywordCell.setCellValue("");
    	Cell signatureCell = row.createCell(1);
    	signatureCell.setCellValue("TRANSITION");
    	Cell precondCell = row.createCell(2);
    	precondCell.setCellValue("PRECONDITION");
       	Cell postcondCell = row.createCell(3);
    	postcondCell.setCellValue("POSTCONDITION");
       	Cell whenCell = row.createCell(4);
    	whenCell.setCellValue("WHEN");
       	Cell effectCell = row.createCell(5);
     	effectCell.setCellValue("EFFECT");
 //      	Cell guardCell = row.createCell(6);
 //      	guardCell.setCellValue("GUARD");
	}

	static private void createTransitionRow(Sheet sheet, CPNTransition transition){
    	Row row = sheet.createRow(rowIndex++);
    	Cell keywordCell = row.createCell(0);
    	keywordCell.setCellValue(XMIDProcessor.MODEL_KEYWORD);
    	Cell signatureCell = row.createCell(1);
    	signatureCell.setCellValue(transition.getEvent());
    	signatureCell.setCellStyle(lineWrapStyle);

    	Cell precondCell = row.createCell(2);
    	precondCell.setCellValue(transition.getPrecondition());
    	precondCell.setCellStyle(lineWrapStyle);

       	Cell postcondCell = row.createCell(3);
    	postcondCell.setCellValue(transition.getPostcondition());
    	precondCell.setCellStyle(lineWrapStyle);
    	
       	Cell whenCell = row.createCell(4);
       	whenCell.setCellValue(transition.getInscription());
       	whenCell.setCellStyle(lineWrapStyle);
 
      	Cell effectCell = row.createCell(5);
     	effectCell.setCellValue("");
    	effectCell.setCellStyle(lineWrapStyle);

      	Cell guardCell = row.createCell(6);
       	String guard = "";
       	guardCell.setCellValue(guard);
    	guardCell.setCellStyle(lineWrapStyle);
	}

	static private void createMarkingRow(Sheet sheet, String keyword, String marking){
    	Row row = sheet.createRow(rowIndex++);
       	Cell keywordCell = row.createCell(0);
    	keywordCell.setCellValue(keyword);
    	Cell initCell = row.createCell(1);
    	initCell.setCellValue(marking);
	}
	
	static private void createMIMSheet(Workbook wb){
	    Sheet sheet = wb.createSheet("MIM");
	    sheet.setFitToPage(true);
	    sheet.autoSizeColumn((short)0); //adjust width of the first column
        sheet.setColumnWidth(0, 16*256);
        sheet.setColumnWidth(1, 20*256);
        sheet.setColumnWidth(2, 40*256);
        sheet.setColumnWidth(3, 40*256);
	    
	    rowIndex = 0;
	    Row row = sheet.createRow(rowIndex++);
		Cell commentCell = row.createCell(1);
		commentCell.setCellValue("II. MIM");
		sheet.createRow(rowIndex++);
	}
	
	
	static private void createCodeSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("HELPER CODE");
        sheet.setFitToPage(true);
        
        sheet.setColumnWidth(1, 50*256);
        
        rowIndex = 0;
	    Row row = sheet.createRow(rowIndex++);
		Cell commentCell = row.createCell(1);
		commentCell.setCellValue("III. HELPER CODE");
        sheet.createRow(rowIndex++);
  	}

	static void clearSheet(Sheet sheet){
		for (int i=sheet.getLastRowNum(); i>=0; i--){
			Row rowToBeRemoved = sheet.getRow(i);
			if (rowToBeRemoved!=null)
				sheet.removeRow(rowToBeRemoved);
		}
	}

}
