package edit;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import locales.LocaleBundle;
import mid.MID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import parser.ParseException;
import testcode.TargetLanguage;
import edit.GeneralTablePanel.MIDTableType;

public class MIMPanelThreatTree extends MIMPanel{
	private static final long serialVersionUID = 1L;
	private static final int MINROWS = 30;

	public MIMPanelThreatTree(XMIDEditor editor, TargetLanguage language){
		this(editor, language, "", new Vector<Vector<Object>>());
	}

	public static MIMPanel createMIMPanel(XMIDEditor editor, TargetLanguage language, Sheet sheet){
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadMIM(sheet);
		return new MIMPanelThreatTree(editor, language, 
				xmidLoader.getSystemName(),
				xmidLoader.getMethods());
	}

	private MIMPanelThreatTree(XMIDEditor editor, TargetLanguage language, 
			String systemName,  
			Vector<Vector<Object>> methods){
		super();
		this.editor = editor;
		this.language = language;
		setSystemNameLabel();
		initializeComponents(language);
		if (systemName!=null)
			systemNameArea.setText(systemName);
		
		this.methodTable = methods;
		methodTablePanel = GeneralTablePanel.createMethodTablePanel(editor, language, methodTable);
		methodTablePanel.setMinRows(MINROWS);		
	
		setPanel();
	}
	
	private void initializeComponents(TargetLanguage language){
		systemNameArea = createTextArea(1, 40);
	}

	private JTextArea createTextArea(int row, int column){
		JTextArea area = new JTextArea(row, column);
		initializeTextArea(area);
		return area;
	}
	
	private void initializeTextArea(JTextArea area){
		area.setEditable(editor.isEditing());
		area.setLineWrap(true);
		area.setFont(editor.getTextFont());
		area.getDocument().addDocumentListener(this);
	}
	
	public void updateMIMPanel(){
		methodTablePanel = GeneralTablePanel.createMethodTablePanel(editor, language, methodTable);
		Font font = editor.getTextFont();
		methodTablePanel.setFont(font);
		setPanel();
		updateUI();
	}
	
	public void updateFont(){
		Font font = editor.getTextFont();
		systemNameArea.setFont(font);
		methodTablePanel.setFont(font);
		updateUI();
	}
	
	private void setPanel(){
		removeAll();
        setLayout(new BorderLayout());
		if (language!=TargetLanguage.KBT){
			JSplitPane wholePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(createUpperPanel()), new JScrollPane(createLowerPanel()));
			wholePane.setOneTouchExpandable(false);
			wholePane.setDividerLocation(0.1);
	        add(wholePane, BorderLayout.CENTER);
		} else
			add(new JScrollPane(createLowerPanel()), BorderLayout.CENTER);
	}
	
	private JPanel createUpperPanel(){
		systemNameArea.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString(systemNameLabel), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
		JPanel upperLeftPanel = new JPanel();
		upperLeftPanel.setLayout(new BorderLayout());
		upperLeftPanel.add(systemNameArea, BorderLayout.NORTH);
		return upperLeftPanel;
	}
	
	private JPanel createLowerPanel(){
		JPanel lowerPanel = new JPanel(new BorderLayout());
		methodTablePanel.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Methods"), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
		methodTablePanel.setMinRows(MINROWS);
		lowerPanel.add(methodTablePanel, BorderLayout.CENTER);
		return lowerPanel;
	}
	
	public JMenu getMIMMenu(){
		JMenu mimMenu = new JMenu(LocaleBundle.bundleString("MIM"));
		return mimMenu;
	}
	
	public void saveMIM(Sheet sheet, CellStyle lineWrapStyle){
		XMIDEditor.cleanUpSheet(sheet);
		sheet.setFitToPage(true);
		sheet.autoSizeColumn((short)0); //adjust width of the first column
		sheet.setColumnWidth(0, 20*256);
		sheet.setColumnWidth(1, 30*256);
		sheet.setColumnWidth(2, 50*256);
		sheet.setColumnWidth(3, 50*256);
		sheet.setColumnWidth(4, 30*256);

		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex++);
		Cell commentCell = row.createCell(1);
		commentCell.setCellValue("II. MIM");
		sheet.createRow(rowIndex++);
       
		rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.getSystemNameKeyword(language), systemNameArea.getText(), sheet, rowIndex, lineWrapStyle);
       
		rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(language, MIDTableType.METHOD), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.METHOD_KEYWORD, methodTable, sheet, rowIndex, lineWrapStyle);
	}

	
	public void parse(MID mid) throws ParseException {
		parseSystemName(mid);
		parseMethods(mid);
	}

}
