package edit;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;

import locales.LocaleBundle;
import mid.ABACAttribute;
import mid.ABACRule;
import mid.MID;
import mid.Predicate;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import edit.GeneralTablePanel.MIDTableType;

import parser.MIDParser;
import parser.ParseException;

public class AccessControlPanel extends JPanel implements DocumentListener{
	private static final long serialVersionUID = 1L;
	private static final int MINROWS_ATTRIBUTES  = 15;
	private static final int MINROWS_RULES  = 20;
	
	protected XMIDEditor editor;

	private GeneralTablePanel attributeTablePanel; 
	private GeneralTablePanel ruleTablePanel; 
	

	private MouseAdapter mouseAdapter;
	
	private Vector<Vector<Object>> attributeRows;
	private Vector<Vector<Object>> ruleRows;
	
	public AccessControlPanel(XMIDEditor editor){
		this.editor = editor;
		attributeRows = new Vector<Vector<Object>>();
		attributeTablePanel = GeneralTablePanel.createAttributeTablePanel(editor, attributeRows);
		attributeTablePanel.setMinRows(MINROWS_ATTRIBUTES);
		
		ruleRows = new Vector<Vector<Object>>();
		ruleTablePanel = GeneralTablePanel.createRuleTablePanel(editor, ruleRows);
		ruleTablePanel.setMinRows(MINROWS_RULES);

		createAccessControlPanel();
	}
	
	public AccessControlPanel(XMIDEditor editor, Sheet sheet){
		this.editor = editor;
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadABAC(sheet);
		attributeRows = xmidLoader.getAttributeRows();
		attributeTablePanel = GeneralTablePanel.createAttributeTablePanel(editor, attributeRows);
		attributeTablePanel.setMinRows(MINROWS_ATTRIBUTES);
		
		ruleRows = xmidLoader.getRuleRows();
		ruleTablePanel = GeneralTablePanel.createRuleTablePanel(editor, ruleRows);
		ruleTablePanel.setMinRows(MINROWS_RULES);
		
		createAccessControlPanel();
	}

	private void createAccessControlPanel(){
		attributeTablePanel.setFont(editor.getTextFont());
		ruleTablePanel.setFont(editor.getTextFont());
		removeAll();
		JSplitPane wholePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUpperPane(), createLowerPane());
        wholePane.setOneTouchExpandable(false);
        wholePane.setDividerLocation((int)(0.3*editor.getKernel().getParentFrame().getHeight()));
        setLayout(new BorderLayout());
        add(wholePane, BorderLayout.CENTER);
        this.addMouseListener(mouseAdapter);
	}
	
	
	private JPanel createUpperPane(){
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(attributeTablePanel, BorderLayout.CENTER);
		tablePanel.addMouseListener(mouseAdapter);
		return tablePanel;
	}

	private JComponent createLowerPane(){
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(ruleTablePanel, BorderLayout.CENTER);
		tablePanel.addMouseListener(mouseAdapter);
		return tablePanel;
	}

	public void updateFont(){
		Font font = editor.getTextFont();
		attributeTablePanel.setFont(font);
		ruleTablePanel.setFont(font);
		updateUI();
	}
		
	public void saveAccessControl(Sheet sheet, CellStyle lineWrapStyle){
		int rowIndex = sheet.getLastRowNum()+1;
		rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(MIDTableType.ATTRIBUTE), sheet, rowIndex);
	    rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.ATTRIBUTE_KEYWORD, attributeRows, sheet, rowIndex, lineWrapStyle);
		rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(MIDTableType.RULE), sheet, rowIndex);
	    rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.RULE_KEYWORD, ruleRows, sheet, rowIndex, lineWrapStyle);
 	}

	/////////////////////////////////////////////////////
	public void parse(MID mid) throws ParseException {
		parseAttributeRows(mid);
		parseRuleRows(mid);
	}
	
	private void parseAttributeRows(MID mid) throws ParseException{
		for (int index=0; index<attributeRows.size(); index++){
			Vector<Object> row = attributeRows.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			String rowInfo = LocaleBundle.bundleString("Attribute Row")+" "+(index+1)+" - ";
			if (row.get(1)==null || row.get(1).toString().trim().equals(""))
				throw new ParseException(rowInfo+LocaleBundle.bundleString("attribute name is expected"));				
			String name = row.get(1).toString();
			if (!MIDParser.isIdentifier(name)) {
				throw new ParseException(rowInfo+LocaleBundle.bundleString("attribute name should be an identifier"));				
			}
			String type = row.get(2)!=null? row.get(2).toString(): "";
			if (!isLegalDataType(type)) {
				throw new ParseException(rowInfo+LocaleBundle.bundleString("incorrect data type"));				
			}
			String values = row.get(3)!=null? row.get(3).toString(): "";
			String[] valueList = values.split(",\\s");
			if (!checkValueList(type, valueList)) {
				throw new ParseException(rowInfo+LocaleBundle.bundleString("incorrect values"));								
			}
			ABACAttribute attribute = new ABACAttribute(name, type, valueList);
			if (mid.attributeExists(attribute)) {
				throw new ParseException(rowInfo+LocaleBundle.bundleString("duplicate attribute"));								
			} else {
				mid.addAttribute(attribute);
			}
		}
	}
	
	private boolean isLegalDataType(String type){
		if (type.equalsIgnoreCase("int"))
			return true;
		if (type.equalsIgnoreCase("boolean"))
			return true;
		if (type.equalsIgnoreCase("string"))
			return true;		
		return false;
	}
	
	private boolean checkValueList(String type, String[] valueList){
		if (type.equalsIgnoreCase("int")) {
			for (String value: valueList){
				try {
//					System.out.println(value);
					Integer.parseInt(value);
				} catch (Exception e) {
					return false;
				}
			}
		} else
		if (type.equalsIgnoreCase("boolean")) {
			if (valueList.length>2)
				return false;
			for (String value: valueList){
				if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
					return false;
			}
		}
		return true;
	}
	
	private void parseRuleRows(MID mid) throws ParseException{
		for (int index=0; index<ruleRows.size(); index++){
			Vector<Object> row = ruleRows.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			String rowInfo = LocaleBundle.bundleString("Rule Row")+" "+(index+1)+" - ";
			if (row.get(1)==null || row.get(1).toString().trim().equals(""))
				throw new ParseException(rowInfo+LocaleBundle.bundleString("rule effect is expected"));				
			String effectString = row.get(1).toString();
			if (!ABACRule.isLegalEffect(effectString)) {
				throw new ParseException(rowInfo+LocaleBundle.bundleString("incorrect rule effect"));				
			}
			try {
				String subjectConditionString = row.get(2)!=null? row.get(2).toString(): "";
				ArrayList<Predicate> subjectCondition = MIDParser.parseConditionString(subjectConditionString);  
				String actionConditionString = row.get(3)!=null? row.get(3).toString(): "";
				ArrayList<Predicate> actionCondition = MIDParser.parseConditionString(actionConditionString); 
				String resourceConditionString = row.get(4)!=null? row.get(4).toString(): "";
				ArrayList<Predicate> resourceCondition = MIDParser.parseConditionString(resourceConditionString);   
				String environmentConditionString = row.get(5)!=null? row.get(5).toString(): "";
				ArrayList<Predicate> environmentCondition = MIDParser.parseConditionString(environmentConditionString);      
				String obligationString = row.get(6)!=null? row.get(6).toString(): "";
				ArrayList<Predicate> obligations = MIDParser.parseConditionString(obligationString);
				ABACRule rule = new ABACRule(effectString, subjectCondition, actionCondition, resourceCondition, environmentCondition, obligations);
				mid.addRule(rule);
			}
			catch (Exception e) {
				throw new ParseException(rowInfo+LocaleBundle.bundleString("incorrect rule"));				
			}
		}
		
	}

	// implements TableModelListener
	public void tableChanged(TableModelEvent e) {
	    editor.setXMIDSaved(false);
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
}
