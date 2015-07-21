package edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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

public class MIMPanelStateMachine extends MIMPanel{
	private static final long serialVersionUID = 1L;
	private static final int MINROWS = 30;
	
	private MouseAdapter mouseAdapter;

	private JTextArea hiddenArea;
	private GeneralTablePanel accessorTablePanel;	
	private Vector<Vector<Object>> accessorTable;

	public MIMPanelStateMachine(XMIDEditor editor, TargetLanguage language){
		this(editor, language, "", "",  
				new Vector<Vector<Object>>(), new Vector<Vector<Object>>()
				);
	}

	public static MIMPanel createMIMPanel(XMIDEditor editor, TargetLanguage language, Sheet sheet){
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadMIM(sheet);
		return new MIMPanelStateMachine(editor, language, 
				xmidLoader.getSystemName(),
				xmidLoader.getHidden(),
				xmidLoader.getMethods(),
				xmidLoader.getAccessors());
	}

	private MIMPanelStateMachine(XMIDEditor editor, TargetLanguage language, 
			String systemName, String hidden, 
			Vector<Vector<Object>> methods, Vector<Vector<Object>> accessors){
		super();
		this.editor = editor;
		this.language = language;
		setSystemNameLabel();
		initializeComponents(language);
		if (systemName!=null)
			systemNameArea.setText(systemName);
		if (hidden!=null)
			hiddenArea.setText(hidden);
		
		this.methodTable = methods;
		methodTablePanel = GeneralTablePanel.createMethodTablePanel(editor, language, methodTable);
		
		this.accessorTable = accessors;
		accessorTablePanel = GeneralTablePanel.createAccessorTablePanel(editor, language, accessorTable);
		
		setPanel();
	}
	
	private void initializeComponents(TargetLanguage language){
		systemNameArea = createTextArea(3, 50);
		hiddenArea = createTextArea();
	}

	private JTextArea createTextArea(){
		JTextArea area = new JTextArea();
		area.setRows(3);
		initializeTextArea(area);
		return area;
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
	    area.addMouseListener(mouseAdapter);
		area.getDocument().addDocumentListener(this);
	}
	
	public void updateMIMPanel(){
		methodTablePanel = GeneralTablePanel.createMethodTablePanel(editor, language, methodTable);
		accessorTablePanel = GeneralTablePanel.createAccessorTablePanel(editor, language, accessorTable);
		
		Font font = editor.getTextFont();
		methodTablePanel.setFont(font);
		accessorTablePanel.setFont(font);
		setPanel();
		updateUI();
	}
	
	public void updateFont(){
		Font font = editor.getTextFont();
		systemNameArea.setFont(font);
		hiddenArea.setFont(font);
		methodTablePanel.setFont(font);
		accessorTablePanel.setFont(font);
		updateUI();
	}
	
	private void setPanel(){
		removeAll();
        setLayout(new BorderLayout());
        add(createUpperPanel(), BorderLayout.NORTH);
        add(createLowerPanel(), BorderLayout.CENTER);
        this.addMouseListener(mouseAdapter);
	}
	
	private JPanel createUpperPanel(){
		JScrollPane hiddenPane = new JScrollPane(hiddenArea);
		hiddenPane.addMouseListener(mouseAdapter);
		hiddenPane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Hidden Events-Conditions"), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	

		JScrollPane systemNamePane = new JScrollPane(systemNameArea);
		systemNamePane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString(systemNameLabel), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	

		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		if (language!=TargetLanguage.KBT && language!=TargetLanguage.UFT){
			upperPanel.add(systemNamePane, BorderLayout.WEST);
		}
		upperPanel.add(hiddenPane, BorderLayout.CENTER);
		upperPanel.addMouseListener(mouseAdapter);
		return upperPanel;
	}

	private JTabbedPane createLowerPanel(){
		JTabbedPane editTabbedPane = new JTabbedPane();
		editTabbedPane.setFont(GeneralEditor.titleFont);
		editTabbedPane.addTab(LocaleBundle.bundleString("Methods"), null, methodTablePanel);
		editTabbedPane.addTab(LocaleBundle.bundleString("Accessors"), null, accessorTablePanel);

		if (editor.isEditing()){
			methodTablePanel.setMinRows(MINROWS);
			accessorTablePanel.setMinRows(MINROWS);
		} 
		editTabbedPane.addMouseListener(mouseAdapter);

		return editTabbedPane;
	}

	public void setOperatorPanelSizes(int preferredTotalWidth, int preferredTotalHeight, int numberOfComponents){
		int rowsForBorderAndTitle = 3;
		int methodRowCount = methodTablePanel.getTableModel().getRowCount()+rowsForBorderAndTitle;
		int accessorRowCount = accessorTablePanel.getTableModel().getRowCount()+rowsForBorderAndTitle;
		methodTablePanel.setPreferredSize(new Dimension(preferredTotalWidth, methodTablePanel.getTable().getRowHeight()*methodRowCount));
		accessorTablePanel.setPreferredSize(new Dimension(preferredTotalWidth, accessorTablePanel.getTable().getRowHeight()*accessorRowCount));
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
		rowIndex = XMIDProcessor.createSplitColumnsRow(XMIDProcessor.HIDDEN_KEYWORD, hiddenArea, sheet, rowIndex, lineWrapStyle);
       
		rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(language, MIDTableType.METHOD), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.METHOD_KEYWORD, methodTable, sheet, rowIndex, lineWrapStyle);
		if (!accessorTablePanel.getTableModel().isEmptyTable())
			rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(language, MIDTableType.ACCESSOR), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.ACCESSOR_KEYWORD, accessorTable, sheet, rowIndex, lineWrapStyle);
	}

	
	public void parse(MID mid) throws ParseException {
		parseSystemName(mid);
		parseHidden(mid);
		parseMethods(mid);
		parseStateOperators(mid, accessorTable, StateOperatorType.STATEACCESSOR);
	}
	
	private void parseHidden(MID mid) throws ParseException {
		String hiddenString = hiddenArea.getText().trim();
		if (hiddenString.equals(""))
			return;
		ArrayList<String> hiddenList = parseStringList(LocaleBundle.bundleString("Hidden Events-Conditions")+": ", hiddenString);
		mid.addHiddenPlacesAndEvents(hiddenList);
	}

}
