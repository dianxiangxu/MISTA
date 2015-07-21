package mid;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import locales.LocaleBundle;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import parser.MIDParser;

public class ExcelTestDataLoader {

	private File dataFile;
	private ArrayList<String> places;
	private ArrayList<Marking> initMarkings;
	
	public ExcelTestDataLoader(String fileName, ArrayList<String> places) throws Exception {
		dataFile = new File(fileName);
		if (dataFile==null || !dataFile.exists())
			throw new Exception(fileName+" "+LocaleBundle.bundleString("DOES_NOT_EXIST"));
//System.out.println(fileName+": "+dataFile.exists());
		this.places = places;
		initMarkings = new ArrayList<Marking>();
		FileInputStream inputStream = new FileInputStream(dataFile);				
		HSSFWorkbook workBook = new HSSFWorkbook (inputStream);
		for (int sheetIndex=0; sheetIndex<workBook.getNumberOfSheets(); sheetIndex++)
			readDataSheet(workBook.getSheetAt(sheetIndex));
		inputStream.close();
	}
	
	private void readDataSheet(Sheet sheet) throws Exception{
		String place;
		Marking newMarking = new Marking();
		for (Row row: sheet) {
			if (row.getCell(0)!=null) {
				place=findPlace(row.getCell(0).toString().trim());
				if (place!=null)
					newMarking.addTuples(place, readTokens(row));
			}
		}
		initMarkings.add(newMarking);
	}

	private ArrayList<Tuple> readTokens(Row row) throws Exception{
		int arity = -1;
		ArrayList<Tuple> tokens = new ArrayList<Tuple>();
		for (int column =1; column<row.getPhysicalNumberOfCells(); column++) {
			Cell cell = row.getCell(column);
			if (cell!=null) { 
				String tokenString = cell.toString().trim();
				if (!tokenString.equals("")){
					Tuple tuple = MIDParser.parseTokenFromDataFile(tokenString);
					if (arity==-1)
						arity = tuple.arity();
					else if (arity!=tuple.arity()) 
						throw new Exception(tokenString+" "+LocaleBundle.bundleString("has inconsistent arguments"));
					tokens.add(tuple);
				}
			}
		}
		return tokens;
	}
		
	private String findPlace(String keyword){
		for (String place: places)
			if (place.equalsIgnoreCase(keyword))
				return place;
		return null;
	}
	
	public ArrayList<Marking> getInitMarkings(){
		return initMarkings;
	}
}
