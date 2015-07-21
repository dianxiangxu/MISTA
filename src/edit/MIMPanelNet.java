package edit;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import locales.LocaleBundle;
import mid.MID;
import mid.Predicate;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;
import testcode.TargetLanguage;
import edit.GeneralTablePanel.MIDTableType;

public class MIMPanelNet extends MIMPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final int MinRows =30;
	
	private static final String showHidden = "Show Hidden Events-Conditions";
	private static final String showOptions = "Show Options";
	private static final String showParameters = "Show Test Parameters";

	private JCheckBoxMenuItem showHiddenItem;
	private JCheckBoxMenuItem showOptionsItem;
	private JCheckBoxMenuItem showParametersItem;

	private MouseAdapter mouseAdapter;

	private JTextArea hiddenArea, optionArea, parameterArea;
	private GeneralTablePanel objectTablePanel, accessorTablePanel, mutatorTablePanel;
	
	private Vector<Vector<Object>> objectTable, accessorTable, mutatorTable;

	public MIMPanelNet(XMIDEditor editor, TargetLanguage language){
		this(editor, language, "", "", "", "", 
				new Vector<Vector<Object>>(), new Vector<Vector<Object>>(), 
				new Vector<Vector<Object>>(), new Vector<Vector<Object>>());
	}

	public static MIMPanel createMIMPanel(XMIDEditor editor, TargetLanguage language, Sheet sheet){
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadMIM(sheet);
		return new MIMPanelNet(editor, language, 
				xmidLoader.getSystemName(),
				xmidLoader.getHidden(),
				xmidLoader.getOptions(),
				xmidLoader.getParameters(),
				xmidLoader.getObjects(),
				xmidLoader.getMethods(),
				xmidLoader.getAccessors(),
				xmidLoader.getMutators());
	}

	private MIMPanelNet(XMIDEditor editor, TargetLanguage language, 
			String systemName, String hidden, String options, String parameters,
			Vector<Vector<Object>> objects, Vector<Vector<Object>> methods, Vector<Vector<Object>> accessors, Vector<Vector<Object>> mutators){
		super();
		this.editor = editor;
		this.language = language;
		setSystemNameLabel();
		createMIMMenuItems();
		initializeComponents(language);
		if (systemName!=null)
			systemNameArea.setText(systemName);
		if (hidden!=null)
			hiddenArea.setText(hidden);
		if (options!=null)
			optionArea.setText(options);
		if (parameters!=null)
			parameterArea.setText(parameters);
		this.objectTable = objects;
		objectTablePanel = GeneralTablePanel.createObjectTablePanel(editor, objectTable);
		
		this.methodTable = methods;
		methodTablePanel = GeneralTablePanel.createMethodTablePanel(editor, language, methodTable);
		
		this.accessorTable = accessors;
		accessorTablePanel = GeneralTablePanel.createAccessorTablePanel(editor, language, accessorTable);
		
		this.mutatorTable = mutators;
		mutatorTablePanel = GeneralTablePanel.createMutatorTablePanel(editor, language, mutatorTable);

		showHiddenItem.setSelected(true);
		showOptionsItem.setSelected(language!=TargetLanguage.HTML);

		setPanel();
	}
	
	private void initializeComponents(TargetLanguage language){
		systemNameArea = createTextArea(3, 40);
		hiddenArea = createTextArea();
		optionArea = createTextArea();
		parameterArea = createTextArea();
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
		objectTablePanel = GeneralTablePanel.createObjectTablePanel(editor, objectTable);
		methodTablePanel = GeneralTablePanel.createMethodTablePanel(editor, language, methodTable);
		accessorTablePanel = GeneralTablePanel.createAccessorTablePanel(editor, language, accessorTable);
		mutatorTablePanel = GeneralTablePanel.createMutatorTablePanel(editor, language, mutatorTable);
		
		showOptionsItem.setSelected(language!=TargetLanguage.HTML);

		Font font = editor.getTextFont();
		objectTablePanel.setFont(font);
		methodTablePanel.setFont(font);
		accessorTablePanel.setFont(font);
		mutatorTablePanel.setFont(font);
		setPanel();
		updateUI();
		enableMIMMenuItems();
	}
	
	public void updateFont(){
		Font font = editor.getTextFont();
		systemNameArea.setFont(font);
		hiddenArea.setFont(font);
		optionArea.setFont(font);
		parameterArea.setFont(font);
		objectTablePanel.setFont(font);
		methodTablePanel.setFont(font);
		accessorTablePanel.setFont(font);
		mutatorTablePanel.setFont(font);
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
		JPanel textAreasPanel = new JPanel();
		int numberOfComponents =0;
		if (showHiddenItem.isSelected())
			numberOfComponents++;
		if (showOptionsItem.isSelected())
			numberOfComponents++;
		if (showParametersItem.isSelected())
			numberOfComponents++;
		if (numberOfComponents==0){
			numberOfComponents =1;
			showHiddenItem.setSelected(true);
		}
			
		textAreasPanel.setLayout(new GridLayout(1, numberOfComponents));
		
		if (showHiddenItem.isSelected()){
			JScrollPane hiddenPane = new JScrollPane(hiddenArea);
			hiddenPane.addMouseListener(mouseAdapter);
			hiddenPane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Hidden Events-Conditions"), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
			textAreasPanel.add(hiddenPane);
		}
		
		if (showOptionsItem.isSelected() && language!=TargetLanguage.UFT){
			JScrollPane optionPane = new JScrollPane(optionArea);
			optionPane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Optional Conditions"), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));
			optionPane.addMouseListener(mouseAdapter);
			textAreasPanel.add(optionPane);
		}
		
		if (showParametersItem.isSelected()){
			JScrollPane parameterPane = new JScrollPane(parameterArea);
			parameterPane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Test Parameters"), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
			parameterPane.addMouseListener(mouseAdapter);
			textAreasPanel.add(parameterPane);
		}
		
		JScrollPane systemNamePane= new JScrollPane(systemNameArea);
		systemNamePane.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString(systemNameLabel), 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
		
		JPanel upperLeftPanel = new JPanel();
		upperLeftPanel.setLayout(new BorderLayout());
		if (language!=TargetLanguage.KBT && language!=TargetLanguage.UFT){
			upperLeftPanel.add(systemNamePane, BorderLayout.WEST);
		}
		upperLeftPanel.add(textAreasPanel, BorderLayout.CENTER);
		upperLeftPanel.addMouseListener(mouseAdapter);
		return upperLeftPanel;
	}

	private JTabbedPane createLowerPanel(){
		JTabbedPane editTabbedPane = new JTabbedPane();
		editTabbedPane.setFont(GeneralEditor.titleFont);
		editTabbedPane.addTab(LocaleBundle.bundleString("Objects"), null, objectTablePanel);
		editTabbedPane.addTab(LocaleBundle.bundleString("Methods"), null, methodTablePanel);
		
		if (language!=TargetLanguage.UFT){
			editTabbedPane.addTab(LocaleBundle.bundleString("Accessors"), null, accessorTablePanel);
			editTabbedPane.addTab(LocaleBundle.bundleString("Mutators"), null, mutatorTablePanel);
		}
		
		if (editor.isEditing()){
			objectTablePanel.setMinRows(MinRows);
			methodTablePanel.setMinRows(MinRows);
			accessorTablePanel.setMinRows(MinRows);
			mutatorTablePanel.setMinRows(MinRows);
		} 
		editTabbedPane.addMouseListener(mouseAdapter);

		return editTabbedPane;
	}


	public JMenu getMIMMenu(){
		JMenu mimMenu = new JMenu(LocaleBundle.bundleString("MIM"));
		mimMenu.add(showHiddenItem);
		mimMenu.add(showOptionsItem);
		mimMenu.add(showParametersItem);
		enableMIMMenuItems();
		return mimMenu;
	}
	
	private void enableMIMMenuItems(){
		boolean enabled = true;
		showHiddenItem.setEnabled(enabled);
		showOptionsItem.setEnabled(enabled);
		showParametersItem.setEnabled(enabled);
	}
	
	private void createMIMMenuItems() {

		showHiddenItem = createCheckBoxMenuItem(showHidden, showHidden, true, this);
		showOptionsItem = createCheckBoxMenuItem(showOptions, showOptions, true, this);
		showParametersItem = createCheckBoxMenuItem(showParameters, showParameters, false, this);
		
		mouseAdapter = new MouseAdapterForMIM(this);
	}
	
	private JCheckBoxMenuItem showHiddenPopupItem;
	private JCheckBoxMenuItem showOptionsPopupItem;
	private JCheckBoxMenuItem showParametersPopupItem;
	
	private class MouseAdapterForMIM extends MouseAdapter{
		private ActionListener listener;
		
		MouseAdapterForMIM(ActionListener listener){
			this.listener = listener;
		}
		
		public void mousePressed( MouseEvent e ) { 
			checkForTriggerEvent(e); 
		} 
		
		public void mouseReleased( MouseEvent e ) { 
			checkForTriggerEvent(e); 
		}
		
		private void checkForTriggerEvent( MouseEvent e ) { 
			if (e.isPopupTrigger() && e.getSource()!=hiddenArea && e.getSource()!=optionArea) { 
				
				JPopupMenu popupMenu = new JPopupMenu();
				showHiddenPopupItem = createCheckBoxMenuItem(showHidden, showHidden, showHiddenItem.isSelected(), listener);
				showOptionsPopupItem = createCheckBoxMenuItem(showOptions, showOptions, showOptionsItem.isSelected(), listener);
				showParametersPopupItem = createCheckBoxMenuItem(showParameters, showParameters, showParametersItem.isSelected(), listener);

				popupMenu.add(showHiddenPopupItem);
				popupMenu.add(showOptionsPopupItem);
				popupMenu.add(showParametersPopupItem);
				popupMenu.show( e.getComponent(), e.getX(), e.getY() );
			} else
			if (e.isPopupTrigger() && e.getSource()==hiddenArea && editor.isEditable) {
				Vector<String> choices = editor.getModelPanel().getHiddenChoicesForMIM();
				if (choices.size()>0) {
					new MultipleChoicesDialog(editor.getKernel(), e.getLocationOnScreen(), LocaleBundle.bundleString("Hidden Events-Conditions"), choices, hiddenArea);
				}
			}
			else
			if (e.isPopupTrigger() && e.getSource()==optionArea && editor.isEditable) {
				Vector<String> choices = editor.getModelPanel().getOptionChoicesForMIM();
				if (choices.size()>0) {
					new MultipleChoicesDialog(editor.getKernel(), e.getLocationOnScreen(), LocaleBundle.bundleString("Optional Conditions"), choices, optionArea);
				}
			}
		} 
	}
	
	private JCheckBoxMenuItem createCheckBoxMenuItem(String title, String command, boolean selected, ActionListener listener){
		JCheckBoxMenuItem checkBoxItem = new JCheckBoxMenuItem(LocaleBundle.bundleString(title));
		checkBoxItem.setSelected(selected);
		checkBoxItem.setActionCommand(command);
		checkBoxItem.addActionListener(listener);
		return checkBoxItem;
	} 

	 // implements ActionListener
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == showHidden || cmd == showOptions || cmd == showParameters){
			if (e.getSource() == showHiddenPopupItem)
				showHiddenItem.setSelected(showHiddenPopupItem.isSelected());
			else
			if (e.getSource() == showOptionsPopupItem)
				showOptionsItem.setSelected(showOptionsPopupItem.isSelected());
			else
			if (e.getSource() == showParametersPopupItem)
				showParametersItem.setSelected(showParametersPopupItem.isSelected());
			setPanel();
			updateUI();
		}
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
		rowIndex = XMIDProcessor.createSplitColumnsRow(XMIDProcessor.OPTION_KEYWORD, optionArea, sheet, rowIndex, lineWrapStyle);
		rowIndex = XMIDProcessor.createSplitColumnsRow(XMIDProcessor.PARAMETER_KEYWORD, parameterArea, sheet, rowIndex, lineWrapStyle);
		if (!objectTablePanel.getTableModel().isEmptyTable())
			rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(MIDTableType.OBJECT), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.OBJECT_KEYWORD, objectTable, sheet, rowIndex, lineWrapStyle);
       
		rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(language, MIDTableType.METHOD), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.METHOD_KEYWORD, methodTable, sheet, rowIndex, lineWrapStyle);
		if (!accessorTablePanel.getTableModel().isEmptyTable())
			rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(language, MIDTableType.ACCESSOR), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.ACCESSOR_KEYWORD, accessorTable, sheet, rowIndex, lineWrapStyle);
		if (!mutatorTablePanel.getTableModel().isEmptyTable())
			rowIndex = XMIDProcessor.createTableTitleRow(GeneralTablePanel.getColumnNames(language, MIDTableType.MUTATOR), sheet, rowIndex);
		rowIndex = XMIDProcessor.createTableRows(XMIDProcessor.MUTATOR_KEYWORD, mutatorTable, sheet, rowIndex, lineWrapStyle);
	}

	
	public void parse(MID mid) throws ParseException {
		parseSystemName(mid);
		parseOptions(mid);
		parseHidden(mid);
		parseParameters(mid);
		
		parseObjects(mid);
		parseMethods(mid);
		parseStateOperators(mid, accessorTable, StateOperatorType.STATEACCESSOR);
		parseStateOperators(mid, mutatorTable, StateOperatorType.STATEMUTATOR);
	}
	
	private void parseOptions(MID mid) throws ParseException {
		String optionsString = optionArea.getText().trim();
		if (optionsString.equals(""))
			return;
		ArrayList<String> optionList = parseStringList(LocaleBundle.bundleString("Optional Conditions")+": ", optionsString);
		mid.addOptions(optionList);
	}

	private void parseHidden(MID mid) throws ParseException {
		String hiddenString = hiddenArea.getText().trim();
		if (hiddenString.equals(""))
			return;
		ArrayList<String> hiddenList = parseStringList(LocaleBundle.bundleString("Hidden Events-Conditions")+": ", hiddenString);
		mid.addHiddenPlacesAndEvents(hiddenList);
	}

	private void parseParameters(MID mid) throws ParseException {
		String parameterString = parameterArea.getText().trim();
		if (parameterString.equals(""))
			return;
		ArrayList<Predicate> parameterList = null;
		try {
			parameterList = MIDParser.parseTestParameterString(parameterString);
		}
		catch (ParseException e) {
			throw new ParseException(LocaleBundle.bundleString("Test parameters")+": "+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(LocaleBundle.bundleString("Test parameters")+": "+LocaleBundle.bundleString("Lexical error"));				
		}
		for (Predicate parameter: parameterList){
			mid.addParameters(parameter.getName(), parameter.getArguments());
		}
	}

	private void parseObjects(MID mid)throws ParseException {
		for (int index=0; index<objectTable.size(); index++){
			Vector<Object> row = objectTable.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			if (isEmpty(row.get(1)) || isEmpty(row.get(2)))
				throw new ParseException(LocaleBundle.bundleString("Objects Row")+" "+(index+1)+": "+ LocaleBundle.bundleString("both model- and implementation-level objects are expected"));
			mid.addObject(row.get(1).toString().trim(), row.get(2).toString());
		}
	}

}
