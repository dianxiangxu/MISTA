/* 	
	Author Dianxiang Xu
*/
package edit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;

import kernel.Kernel;
import locales.LocaleBundle;

import mid.MID;
import mid.Marking;
import mid.GoalProperty;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import edit.GeneralEditor.SimulatorType;
import edit.GeneralTablePanel.MIDTableType;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;
import simulation.PrTEngine;

public abstract class ModelPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	protected XMIDEditor editor;
	
	public ModelPanel(XMIDEditor editor) {
		super();
		this.editor=editor;
	}
	
	// implements DocumentListener
	public void insertUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	public void removeUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	public void changedUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	protected void parseInitialState(MID mid, ArrayList<JTextArea> stateAreas) throws ParseException {
		for (JTextArea stateArea: stateAreas) {
			String markingString = stateArea.getText().trim();
			if (markingString.equals(""))
				continue;
			try {
				Marking marking = MIDParser.parseMarkingString(markingString);
				mid.addInitialMarking(marking);
			}
			catch (ParseException e) {
				String rowInfo = LocaleBundle.bundleString("Initial state")+" ";
				rowInfo += stateAreas.indexOf(stateArea)+1;
				throw new ParseException(rowInfo+e.toString());
			}
			catch (TokenMgrError e){
				String rowInfo = LocaleBundle.bundleString("Initial state")+" ";
				rowInfo += stateAreas.indexOf(stateArea)+1;
				throw new ParseException(rowInfo+": "+LocaleBundle.bundleString("Lexical error"));				
			}
		}
	}

	protected void parseGoalProperty(MID mid, ArrayList<JTextArea> stateAreas)  throws ParseException{
		for (JTextArea stateArea: stateAreas) {
			String propertyString = stateArea.getText().trim();
			if (propertyString.equals(""))
				continue;
			try {
				GoalProperty propertyTransition = MIDParser.parseGoalPropertyString(propertyString);
				mid.addGoalProperty(propertyTransition);
			}
			catch (ParseException e) {
				String rowInfo =LocaleBundle.bundleString("Goal state")+" ";
				rowInfo += stateAreas.indexOf(stateArea)+1;
				throw new ParseException(rowInfo+e.toString());
			}
			catch (TokenMgrError e) {
				String rowInfo =LocaleBundle.bundleString("Goal state")+" ";
				rowInfo += stateAreas.indexOf(stateArea)+1;
				throw new ParseException(rowInfo+": "+LocaleBundle.bundleString("Lexical error"));				
			}
		}

	}
	
	protected void removeState(ArrayList<JTextArea> states){
		for (int index = states.size()-1; index>0; index--){
			JTextArea state = states.get(index);
			if (state==null || state.getText().trim().equals("")){
				states.remove(index);
				return;
			}
		}
	}
	
	public JToolBar getAdditionalToolBar(){
		return null;
	}

	protected int saveModelHeader(Sheet sheet, CellStyle lineWrapStyle){
		XMIDEditor.cleanUpSheet(sheet);
	       sheet.setFitToPage(true);
	       sheet.setColumnWidth(0, 16*256);
	       sheet.setColumnWidth(1, 20*256);
	       sheet.setColumnWidth(2, 40*256);
	       sheet.setColumnWidth(3, 40*256);
	       sheet.setColumnWidth(4, 20*256);
	       sheet.setColumnWidth(5, 20*256);
	       sheet.setColumnWidth(6, 20*256);
	        
	       int rowIndex = 0;
		   Row row = sheet.createRow(rowIndex++);
		   Cell systemNameCell = row.createCell(0);
		   systemNameCell.setCellValue(Kernel.SYSTEM_NAME);
		   Cell systemVersionCell = row.createCell(1);
		   systemVersionCell.setCellValue(Kernel.SYSTEM_VERSION);
	       sheet.createRow(rowIndex++);
		   row = sheet.createRow(rowIndex++);
		   Cell commentCell = row.createCell(1);
		   commentCell.setCellValue("I. MODEL");
	       sheet.createRow(rowIndex++);
	       return rowIndex;
	}
	
	protected PrTEngine simulator = null;
	public void startSimulator(MID mid, SimulatorType simulatorType){
		if (simulator!=null)
			simulator.dispose();
		simulator = createSimulator(mid, simulatorType);
	}
	
	public PrTEngine createSimulator(MID mid, SimulatorType simulatorType){
		return null;
	}
	
	public PrTEngine getSimulator(){
		return simulator;
	}

	public ArrayList<String> getChoicesForMIMEntry(MIDTableType tableType){
		// it is ok if the model has errors
		MID mid = new MID();
		try {
			parse(mid);
		} 
		catch (ParseException exception){
			if (Kernel.IS_DEBUGGING_MODE)
				exception.printStackTrace();
		}
		mid.findErrors();	// get info while checking for consistency; error message ignored
		switch (tableType){
			case OBJECT: return mid.getListOfObjects();
			case METHOD: return mid.getListOfMethods();
			case ACCESSOR: 
			case MUTATOR: return mid.getListOfPredicates();
			default: return new ArrayList<String>();
		}
	}

	public Vector<String> getOptionChoicesForMIM(){
		// it is ok if the model has errors
		MID mid = new MID();
		try {
			parse(mid);
		} 
		catch (ParseException exception){
			if (Kernel.IS_DEBUGGING_MODE)
				exception.printStackTrace();
		}
		mid.findErrors();	// get info while checking for consistency; error message ignored
		Vector<String> predicates = new Vector<String>();
		predicates.addAll(mid.getPlaces());
		Collections.sort(predicates);
		return predicates;
	}

	public Vector<String> getHiddenChoicesForMIM(){
		// it is ok if the model has errors
		MID mid = new MID();
		try {
			parse(mid);
		} 
		catch (ParseException exception){
			if (Kernel.IS_DEBUGGING_MODE)
				exception.printStackTrace();
		}
		mid.findErrors();	// get info while checking for consistency; error message ignored
		Vector<String> options = new Vector<String>();
		options.addAll(mid.getPlaces());
		Collections.sort(options);
		ArrayList<String> sortedEventList = new ArrayList<String>();
		for (String event: mid.getEvents())
			sortedEventList.add(event);
		Collections.sort(sortedEventList);
		options.addAll(sortedEventList);
		return options;
	}

	
	public abstract JMenu getModelMenu();
	public abstract void updateFont();
	public abstract void saveModel(File xmidFile, Sheet sheet, CellStyle lineWrapStyle);

	public abstract void parse(MID mid) throws ParseException;

}
