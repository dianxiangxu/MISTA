package edit;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JPanel;

import locales.LocaleBundle;
import mid.MID;
import mid.ThreatTreeNode;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;

public class ModelPanelTabularThreatTree extends ModelPanel{
	private static final long serialVersionUID = 1L;
	
	private static final int MINROWS = 30;
	
	private GeneralTablePanel modelTablePanel; 
	
	private Vector<Vector<Object>> coreModelRows;
	
	public ModelPanelTabularThreatTree(XMIDEditor editor){
		super(editor);
		coreModelRows = new Vector<Vector<Object>>();
		modelTablePanel = GeneralTablePanel.createModelTablePanel(editor, GeneralTablePanel.MIDTableType.THREATTREE, coreModelRows);
		modelTablePanel.setMinRows(MINROWS);
		createModelPanel();
	}
	
	public ModelPanelTabularThreatTree(XMIDEditor editor, Sheet sheet){
		super(editor);
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadModel(sheet);
		coreModelRows = xmidLoader.getCoreModelRows();
		modelTablePanel = GeneralTablePanel.createModelTablePanel(editor, GeneralTablePanel.MIDTableType.THREATTREE, coreModelRows);
		modelTablePanel.setMinRows(MINROWS);		
		createModelPanel();
	}

	
	private void createModelPanel(){
		modelTablePanel.setFont(editor.getTextFont());
		removeAll();
        setLayout(new BorderLayout());
        add(createUpperPane(), BorderLayout.CENTER);
 	}
	
	private JPanel createUpperPane(){
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(modelTablePanel, BorderLayout.CENTER);
		return tablePanel;
	}

	public JMenu getModelMenu(){
		JMenu modelMenu = new JMenu(LocaleBundle.bundleString("Model"));
		return modelMenu;
	}
	
	public void updateFont(){
		Font font = editor.getTextFont();
		modelTablePanel.setFont(font);
		updateUI();
	}
	
	public void saveModel(File xmidFile, Sheet sheet, CellStyle lineWrapStyle){
		int rowIndex = saveModelHeader(sheet, lineWrapStyle);
		rowIndex = XMIDProcessor.createTableModelTypeRow(editor.getModelType(), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(GeneralTablePanel.MIDTableType.THREATTREE), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.MODEL_KEYWORD, coreModelRows, sheet, rowIndex, lineWrapStyle);
 	}

	/////////////////////////////////////////////////////
	public void parse(MID mid) throws ParseException {
		parseCoreModelRows(mid);
	}
	
	private void parseCoreModelRows(MID mid) throws ParseException{
		for (int index=0; index<coreModelRows.size(); index++){
			Vector<Object> row = coreModelRows.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			String rowInfo = LocaleBundle.bundleString("Model Row")+" "+(index+1)+" - ";
			if (row.get(1)==null || row.get(1).toString().trim().equals(""))
				throw new ParseException(rowInfo+LocaleBundle.bundleString("transition-module-event is expected"));				
			String event = row.get(1).toString();
			String children = row.get(2)!=null? row.get(2).toString(): "";
			String gate = row.get(3)!=null? row.get(3).toString(): "";
			try {
						if (!MIDParser.isIdentifier(event))
							throw new ParseException(event+" - "+LocaleBundle.bundleString("invalid name"));
						ArrayList<String> childEvents = null;
						if (!children.equals("")){
							childEvents = MIDParser.parseIdentifierListString(children);
							if (childEvents.size()>1 && !ThreatTreeNode.isValidRelation(gate))
								throw new ParseException(LocaleBundle.bundleString("A relation is expected"));
						}
						mid.addThreatTreeNode(event, childEvents, gate);
						mid.buildThreatTree();
			}
			catch (ParseException e) {
				throw new ParseException(rowInfo+e.toString());
			}
			catch (TokenMgrError e){
				throw new ParseException(rowInfo+": "+LocaleBundle.bundleString("Lexical error"));				
			}
		}
	}

}
