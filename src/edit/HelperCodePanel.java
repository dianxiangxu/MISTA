/* 	
	Author Dianxiang Xu
*/
package edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import locales.LocaleBundle;
import mid.MID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import parser.MIDParser;
import parser.ParseException;

import testcode.TargetLanguage;
import testcode.TargetLanguageOO;

public class HelperCodePanel extends JPanel implements ActionListener, DocumentListener{

	private static final long serialVersionUID = 1L;

	private JTextArea packageArea, importArea, alphaArea, omegaArea, setUpArea, tearDownArea;
	
	private ArrayList<JTextArea> codeSegments = new ArrayList<JTextArea>();			// use a table

	private XMIDEditor editor;
	private TargetLanguage language;
	
	private JCheckBoxMenuItem showPackageCheckBox, showImportCheckBox, showAlphaCheckBox, showOmegaCheckBox;
	private JCheckBoxMenuItem showSetUpCheckBox, showTearDownCheckBox, showCodeSegmentCheckBox;
	
	private JMenuItem addCodeSegmentItem, deleteCodeSegmentItem, deselectAllCheckBoxesItem;

	private MouseAdapter mouseAdapter;
	
	// SELENIUM SETUP / TEARDOWN
	private GeneralTablePanel seleniumSetupTablePanel;
	private Vector<Vector<Object>> seleniumSetupTable=new Vector<Vector<Object>>();;

	private GeneralTablePanel seleniumTeardownTablePanel;
	private Vector<Vector<Object>> seleniumTeardownTable=new Vector<Vector<Object>>();

	public HelperCodePanel(XMIDEditor editor, TargetLanguage language){
		super();
		this.editor = editor;
		this.language = language;
		initializeCheckBoxes();
		createSeleniumSetupTeardownPanels();
		createMouseListenerForPopupMenu();
		initializeTextAreas();
		setLayout(new BorderLayout());
		add(createTextAreasPanel(), BorderLayout.CENTER);
	}
	
	public HelperCodePanel(XMIDEditor editor, TargetLanguage language, Sheet sheet){
		super();
		this.editor = editor;
		this.language = language;
		initializeCheckBoxes();
		createMouseListenerForPopupMenu();
		initializeTextAreas();
		XMIDProcessor xmidLoader = new XMIDProcessor();
		xmidLoader.loadHelperCode(sheet);
		packageArea.setText(xmidLoader.getPackageCode());
		importArea.setText(xmidLoader.getImportCode());
		alphaArea.setText(xmidLoader.getAlphaCode());
		omegaArea.setText(xmidLoader.getOmegaCode());
		setUpArea.setText(xmidLoader.getSetUpCode());
		tearDownArea.setText(xmidLoader.getTearDownCode());
		seleniumSetupTable = xmidLoader.getSeleniumSetupCommands();
		seleniumTeardownTable = xmidLoader.getSeleniumTeardownCommands();
		createSeleniumSetupTeardownPanels();
		for (String codeSegment: xmidLoader.getCodeSegments()){
			JTextArea codeArea = createTextArea(20, 60);
			codeArea.setText(codeSegment);
			codeSegments.add(codeArea);
		}
		showSetUpCheckBox.setSelected(language!=TargetLanguage.HTML || !setUpArea.getText().trim().equals(""));
		showTearDownCheckBox.setSelected(language!=TargetLanguage.HTML || !tearDownArea.getText().trim().equals(""));
		showAlphaCheckBox.setSelected(!alphaArea.getText().trim().equals(""));
		showOmegaCheckBox.setSelected(!omegaArea.getText().trim().equals(""));
		setLayout(new BorderLayout());
		add(createTextAreasPanel(), BorderLayout.CENTER);
	}
	
	private void createSeleniumSetupTeardownPanels(){
		int MINROWS = 15;
		seleniumSetupTablePanel=GeneralTablePanel.createSeleniumCommandTablePanel(editor, seleniumSetupTable);
		seleniumSetupTablePanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("setup code")));
		seleniumSetupTablePanel.setMinRows(MINROWS);

		seleniumTeardownTablePanel=GeneralTablePanel.createSeleniumCommandTablePanel(editor, seleniumTeardownTable);
		seleniumTeardownTablePanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("teardown code")));
		seleniumTeardownTablePanel.setMinRows(MINROWS);
		
	}
	
	public void updateLanguage(TargetLanguage newLanguage){
		this.language = newLanguage;
		showPackageCheckBox.setText(LocaleBundle.bundleString("Show")+getPackageTitle());
		showImportCheckBox.setText(LocaleBundle.bundleString("Show")+getImportTitle());
		updatePanel();
	}
	
	public void updateFont(){
		Font font = editor.getTextFont();
		packageArea.setFont(font);
		importArea.setFont(font);
		alphaArea.setFont(font);
		omegaArea.setFont(font);
		setUpArea.setFont(font);
		tearDownArea.setFont(font);
		if (seleniumSetupTablePanel!=null)
			seleniumSetupTablePanel.setFont(font);
		if (seleniumTeardownTablePanel!=null)
			seleniumTeardownTablePanel.setFont(font);
		updateUI();
	}

	
	private void updatePanel(){
		removeAll();
		validate();
		add(createTextAreasPanel(), BorderLayout.CENTER);
		updateUI();
	}
	
	private String getPackageTitle(){
		return (language instanceof TargetLanguageOO)?
			((TargetLanguageOO)language).getPackageKeyword() +" "+LocaleBundle.bundleString("code")+" ": "";
	}
	
	private String getImportTitle(){
		if (language instanceof TargetLanguageOO)
			return ((TargetLanguageOO)language).getImportKeyword()+" "+LocaleBundle.bundleString("code")+" ";
		else if (language==TargetLanguage.C)
			return LocaleBundle.bundleString("header code");
		else if (language==TargetLanguage.KBT)
			return LocaleBundle.bundleString("settings");
		else
			return "";
	}
		
	private void initializeCheckBoxes(){
					
		showPackageCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show")+getPackageTitle());
		showImportCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show")+getImportTitle()); 
		showAlphaCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show alpha code")); 
		showOmegaCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show omega code")); 
		showSetUpCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show setup code")); 
		showTearDownCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show teardown code")); 
		showCodeSegmentCheckBox = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show code segments")); 
		deselectAllCheckBoxesItem = new JMenuItem(LocaleBundle.bundleString("Deselect all"));
		addCodeSegmentItem = new JMenuItem(LocaleBundle.bundleString("Add code segment")); 
		deleteCodeSegmentItem = new JMenuItem(LocaleBundle.bundleString("Remove code segment")); 
		
		showPackageCheckBox.setSelected(language instanceof TargetLanguageOO);
		showImportCheckBox.setSelected(language!=TargetLanguage.HTML);
		showAlphaCheckBox.setSelected(true);
		showOmegaCheckBox.setSelected(true);
		showSetUpCheckBox.setSelected(true);
		showTearDownCheckBox.setSelected(true);

		showCodeSegmentCheckBox.setSelected(language!=TargetLanguage.HTML);

		showPackageCheckBox.addActionListener(this);
		showImportCheckBox.addActionListener(this);
		showAlphaCheckBox.addActionListener(this);
		showOmegaCheckBox.addActionListener(this);
		showSetUpCheckBox.addActionListener(this);
		showTearDownCheckBox.addActionListener(this);

		deselectAllCheckBoxesItem.addActionListener(this);
		showCodeSegmentCheckBox.addActionListener(this);
		
		addCodeSegmentItem.addActionListener(this);
		deleteCodeSegmentItem.addActionListener(this);
	}
	
	private void initializeTextAreas(){
		packageArea = createTextArea(3, 60);
		importArea = createTextArea(10, 60);
		alphaArea = createTextArea(10, 60);
		omegaArea = createTextArea(10, 60);
		setUpArea = createTextArea(10, 60);
		tearDownArea = createTextArea(10, 60);
	}
	
	private JTextArea createTextArea(int row, int column){
		JTextArea editArea = editor.isEditing()? new JTextArea(row, column): new JTextArea();
		editArea.setEditable(editor.isEditing());
		editArea.setName("EditArea");
		editArea.setEnabled(true);
		editArea.setBackground(Color.WHITE);
		editArea.setForeground(Color.BLACK);
		editArea.setFont(editor.getTextFont());
		editArea.setTabSize(2);
		editArea.setMargin(new Insets(1,5,5,5));
		editArea.setCaretPosition(0);
		editArea.getDocument().addDocumentListener(this);
		editArea.addMouseListener(mouseAdapter);
		return editArea;
	}
	
	private JComponent createTextAreasPanel(){
		if (language==TargetLanguage.UFT){
			return new JPanel();
		}
		if (language==TargetLanguage.SELENIUMDRIVER){
	        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, seleniumSetupTablePanel, seleniumTeardownTablePanel);
	        splitPane.setDividerLocation(0.5);
//		    splitPane.setOneTouchExpandable(true);
		return splitPane;
		}
		JPanel textAreasPanel=new JPanel();
		textAreasPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;

		if (hasPackageSection() && showPackageCheckBox.isEnabled() && showPackageCheckBox.isSelected()) {
			textAreasPanel.add(createScrollPane(packageArea, getPackageTitle()), c);
		}
		if (language!=TargetLanguage.HTML && language!=TargetLanguage.RPC && language!=TargetLanguage.SELENIUMDRIVER && showImportCheckBox.isEnabled() && showImportCheckBox.isSelected()){
			textAreasPanel.add(createScrollPane(importArea, getImportTitle()), c );
		}
		if (showSetUpCheckBox.isSelected() && language!=TargetLanguage.KBT ){
			textAreasPanel.add(createScrollPane(setUpArea, LocaleBundle.bundleString("setup code")), c);
		}
		if (showTearDownCheckBox.isSelected() && language!=TargetLanguage.KBT ){
			textAreasPanel.add(createScrollPane(tearDownArea, LocaleBundle.bundleString("teardown code")), c);
		}
		if (showAlphaCheckBox.isSelected() && language!=TargetLanguage.RPC && language!=TargetLanguage.KBT ){
			textAreasPanel.add(createScrollPane(alphaArea, LocaleBundle.bundleString("alpha code")), c);
		}
		if (showOmegaCheckBox.isSelected() && language!=TargetLanguage.RPC && language!=TargetLanguage.KBT ){
			textAreasPanel.add(createScrollPane(omegaArea, LocaleBundle.bundleString("omega code")),c );
		}
		if (language instanceof TargetLanguageOO && showCodeSegmentCheckBox.isSelected())
			for (int i=0; i<codeSegments.size(); i++){
				textAreasPanel.add(createScrollPane(codeSegments.get(i), LocaleBundle.bundleString("code segment")+" ["+(i+1)+"] "), c);
			}
		textAreasPanel.addMouseListener(mouseAdapter);
		JScrollPane scrollPane = new JScrollPane(textAreasPanel);
		scrollPane.addMouseListener(mouseAdapter);
		return scrollPane;
	}
	
	private JScrollPane createScrollPane(JTextArea textArea, String title){
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder(null, title, 0, 0, GeneralEditor.titleFont, GeneralEditor.titleColor));	
		scrollPane.addMouseListener(mouseAdapter);
		return scrollPane;
	}
	
	private void removeCodeSegment(){
		for (int i=codeSegments.size()-1; i>=0; i++){
			JTextArea area = codeSegments.get(i);
			if (area.getText().trim().equals("")){
				codeSegments.remove(i);
				break;
			}
		}
	}
	
	private void deselectAllCheckBoxes(){
		showPackageCheckBox.setSelected(false);
		showImportCheckBox.setSelected(false);
		showAlphaCheckBox.setSelected(false);
		showOmegaCheckBox.setSelected(false);
		showSetUpCheckBox.setSelected(false);
		showTearDownCheckBox.setSelected(false);
		showCodeSegmentCheckBox.setSelected(false);		
	}

	private void setDeselectAllMenuItem(){
		deselectAllCheckBoxesItem.setEnabled(
				showPackageCheckBox.isSelected() ||
				showImportCheckBox.isSelected() ||
				showAlphaCheckBox.isSelected() ||
				showOmegaCheckBox.isSelected() ||
				showSetUpCheckBox.isSelected() ||
				showTearDownCheckBox.isSelected() ||
				showCodeSegmentCheckBox.isSelected()		
		);
	}
	// implements ActionListener
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==showCodeSegmentCheckBox || e.getSource() == showCodeSegmentCheckBoxPopup){
			if ( e.getSource() == showCodeSegmentCheckBoxPopup)
				showCodeSegmentCheckBox.setSelected(showCodeSegmentCheckBoxPopup.isSelected());
			addCodeSegmentItem.setEnabled(showCodeSegmentCheckBox.isSelected());
			deleteCodeSegmentItem.setEnabled(showCodeSegmentCheckBox.isSelected());
		}
		else 
		if (e.getSource()==addCodeSegmentItem || e.getSource() == addCodeSegmentItemPopup){
			codeSegments.add(createTextArea(20, 60));
		}
		else 
		if (e.getSource()==deleteCodeSegmentItem || e.getSource() == deleteCodeSegmentItemPopup){
			removeCodeSegment();
		}
		else 
		if (e.getSource()==deselectAllCheckBoxesItem || e.getSource()== deselectAllCheckBoxesItemPopup){
			deselectAllCheckBoxes();
		}			
		else
		if (e.getSource() == showPackageCheckBoxPopup){
			showPackageCheckBox.setSelected(showPackageCheckBoxPopup.isSelected());
		}
		else
		if (e.getSource() == showImportCheckBoxPopup){
			showImportCheckBox.setSelected(showImportCheckBoxPopup.isSelected());
		}
		else
		if (e.getSource() == showAlphaCheckBoxPopup){
			showAlphaCheckBox.setSelected(showAlphaCheckBoxPopup.isSelected());
		}
		else
		if (e.getSource() == showOmegaCheckBoxPopup){
			showOmegaCheckBox.setSelected(showOmegaCheckBoxPopup.isSelected());
		}
		else
		if (e.getSource() == showSetUpCheckBoxPopup){
			showSetUpCheckBox.setSelected(showSetUpCheckBoxPopup.isSelected());
		}
		else
		if (e.getSource() == showTearDownCheckBoxPopup){
			showTearDownCheckBox.setSelected(showTearDownCheckBoxPopup.isSelected());
		}
		
//		System.out.println("source: "+e.getSource().toString());
		setDeselectAllMenuItem();
		updatePanel();
	}
	

	public void saveHelperCode(Sheet sheet, CellStyle lineWrapStyle){
		XMIDEditor.cleanUpSheet(sheet);
        sheet.setFitToPage(true);
//        sheet.autoSizeColumn((short)0); //adjust width of the first column        
//        sheet.autoSizeColumn((short)1); //adjust width of the second column        
        sheet.setColumnWidth(0, 16*256);
        sheet.setColumnWidth(1, 100*256);
        
        int rowIndex = 0;
	    Row row = sheet.createRow(rowIndex++);
		Cell commentCell = row.createCell(1);
		commentCell.setCellValue("III. HELPER CODE");
        sheet.createRow(rowIndex++);
        rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.getPackageKeyword(language), packageArea.getText(), sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.getImportKeyword(language), importArea.getText(), sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.ALPHA_KEYWORD, alphaArea.getText(), sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.OMEGA_KEYWORD, omegaArea.getText(), sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.SETUP_KEYWORD, setUpArea.getText(), sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.TEARDOWN_KEYWORD, tearDownArea.getText(), sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createSeleniumCommandRows(XMIDProcessor.SELENIUMSETUP_KEYWORD, seleniumSetupTable, sheet, rowIndex, lineWrapStyle);
        rowIndex = XMIDProcessor.createSeleniumCommandRows(XMIDProcessor.SELENIUMTEARDOWN_KEYWORD, seleniumTeardownTable, sheet, rowIndex, lineWrapStyle);
       	for (JTextArea area: codeSegments)
       		rowIndex = XMIDProcessor.createKeyValuePairRow(XMIDProcessor.CODE_KEYWORD, area.getText(), sheet, rowIndex, lineWrapStyle);
      		
 	}
	
	public void insertUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	public void removeUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	public void changedUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}

	public JMenu getHelperCodeMenu(){
		JMenu helperCodeMenu = new JMenu(LocaleBundle.bundleString("Helper Code"));
		if (language==TargetLanguage.SELENIUMDRIVER)
			return helperCodeMenu;
		
		
		if (hasPackageSection())
			helperCodeMenu.add(showPackageCheckBox);
		if	(language !=TargetLanguage.HTML && language!=TargetLanguage.RPC && language!=TargetLanguage.SELENIUMDRIVER)
			helperCodeMenu.add(showImportCheckBox);
		if (language!=TargetLanguage.KBT){
			helperCodeMenu.add(showSetUpCheckBox);
			helperCodeMenu.add(showTearDownCheckBox);
			if (language!=TargetLanguage.RPC) {
				helperCodeMenu.add(showAlphaCheckBox);
				helperCodeMenu.add(showOmegaCheckBox);
			}
		}
		if (language instanceof TargetLanguageOO){
			helperCodeMenu.addSeparator();
			helperCodeMenu.add(showCodeSegmentCheckBox);
			if (editor.isEditing()){
				helperCodeMenu.add(addCodeSegmentItem);
				helperCodeMenu.add(deleteCodeSegmentItem);
				addCodeSegmentItem.setEnabled(showCodeSegmentCheckBox.isSelected());
				deleteCodeSegmentItem.setEnabled(showCodeSegmentCheckBox.isSelected());
			}
		}
		if (language!=TargetLanguage.KBT && language!=TargetLanguage.RPC){
			helperCodeMenu.addSeparator();
			helperCodeMenu.add(deselectAllCheckBoxesItem);
		}
		setDeselectAllMenuItem();
		return helperCodeMenu;
	}
	
	private JCheckBoxMenuItem showPackageCheckBoxPopup, showImportCheckBoxPopup, showAlphaCheckBoxPopup, showOmegaCheckBoxPopup;
	private JCheckBoxMenuItem showSetUpCheckBoxPopup, showTearDownCheckBoxPopup, showCodeSegmentCheckBoxPopup;
	private JMenuItem addCodeSegmentItemPopup, deleteCodeSegmentItemPopup, deselectAllCheckBoxesItemPopup;

	private boolean hasPackageSection(){
		return language instanceof TargetLanguageOO && ((TargetLanguageOO)language).hasPackageSection();
	}
	
	private JPopupMenu createPopupMenu(){		
		showPackageCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show")+getPackageTitle());
		showImportCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show")+getImportTitle()); 
		showAlphaCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show alpha code")); 
		showOmegaCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show omega code")); 
		showSetUpCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show setup code")); 
		showTearDownCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show teardown code")); 
		showCodeSegmentCheckBoxPopup = new JCheckBoxMenuItem(LocaleBundle.bundleString("Show code segments")); 
		deselectAllCheckBoxesItemPopup = new JMenuItem(LocaleBundle.bundleString("Deselect all"));
		deselectAllCheckBoxesItemPopup.setEnabled(deselectAllCheckBoxesItem.isEnabled());
		addCodeSegmentItemPopup = new JMenuItem(LocaleBundle.bundleString("Add code segment")); 
		deleteCodeSegmentItemPopup = new JMenuItem(LocaleBundle.bundleString("Remove code segment")); 
		
		showPackageCheckBoxPopup.setSelected(showPackageCheckBox.isSelected());
		showImportCheckBoxPopup.setSelected(showImportCheckBox.isSelected());
		showAlphaCheckBoxPopup.setSelected(showAlphaCheckBox.isSelected());
		showOmegaCheckBoxPopup.setSelected(showOmegaCheckBox.isSelected());
		showSetUpCheckBoxPopup.setSelected(showSetUpCheckBox.isSelected());
		showTearDownCheckBoxPopup.setSelected(showTearDownCheckBox.isSelected());

		showCodeSegmentCheckBoxPopup.setSelected(showCodeSegmentCheckBox.isSelected());

		showPackageCheckBoxPopup.addActionListener(this);
		showImportCheckBoxPopup.addActionListener(this);
		showAlphaCheckBoxPopup.addActionListener(this);
		showOmegaCheckBoxPopup.addActionListener(this);
		showSetUpCheckBoxPopup.addActionListener(this);
		showTearDownCheckBoxPopup.addActionListener(this);

		deselectAllCheckBoxesItemPopup.addActionListener(this);
		showCodeSegmentCheckBoxPopup.addActionListener(this);
		
		addCodeSegmentItemPopup.addActionListener(this);
		deleteCodeSegmentItemPopup.addActionListener(this);

		JPopupMenu popupMenu = new JPopupMenu();
		if (hasPackageSection())
			popupMenu.add(showPackageCheckBoxPopup);
		if (language!=TargetLanguage.HTML && language!=TargetLanguage.RPC && language!=TargetLanguage.SELENIUMDRIVER)
			popupMenu.add(showImportCheckBoxPopup);
		if (language!=TargetLanguage.KBT){
			popupMenu.add(showSetUpCheckBoxPopup);
			popupMenu.add(showTearDownCheckBoxPopup);
			if (language!=TargetLanguage.RPC) {
				popupMenu.add(showAlphaCheckBoxPopup);
				popupMenu.add(showOmegaCheckBoxPopup);
			}
		}
		if (language instanceof TargetLanguageOO){
			popupMenu.addSeparator();
			popupMenu.add(showCodeSegmentCheckBoxPopup);
			if (editor.isEditing() && showCodeSegmentCheckBox.isSelected()){
				popupMenu.add(addCodeSegmentItemPopup);
				popupMenu.add(deleteCodeSegmentItemPopup);
			}
		}
		if (language!=TargetLanguage.KBT && language!=TargetLanguage.RPC){
			popupMenu.addSeparator();
			popupMenu.add(deselectAllCheckBoxesItemPopup);
		}
		return popupMenu;
	}
	
	private void createMouseListenerForPopupMenu() {
		mouseAdapter = new MouseAdapter() { 
			public void mousePressed( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			} 
			public void mouseReleased( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			}
			private void checkForTriggerEvent( MouseEvent e ) { 
				if ( e.isPopupTrigger()) { 
					JPopupMenu popupMenu = createPopupMenu();
					popupMenu.show( e.getComponent(), e.getX(), e.getY() );
				}	
			} 
		};
	}

	public void parse(MID mid) throws ParseException{
		if (!packageArea.getText().trim().equals(""))
			mid.setPackageBlock(packageArea.getText());
		if (!importArea.getText().trim().equals(""))
			mid.setImportBlock(importArea.getText());
		if (!alphaArea.getText().trim().equals(""))
			mid.setAlphaBlock(alphaArea.getText());
		if (!omegaArea.getText().trim().equals(""))
			mid.setOmegaBlock(omegaArea.getText());
		if (language==TargetLanguage.SELENIUMDRIVER){
			mid.setSetUpCode(parseSeleniumCommands(seleniumSetupTable));
		} else {
			if (!setUpArea.getText().trim().equals("")) {
				if (language==TargetLanguage.RPC) {
					MIDParser.parseRPCString(setUpArea.getText());
				}
				mid.setSetUpCode(setUpArea.getText());
			}
		}
		if (language==TargetLanguage.SELENIUMDRIVER){
			mid.setTearDownCode(parseSeleniumCommands(seleniumTeardownTable));
		} else {
			if (!tearDownArea.getText().trim().equals("")){
				if (language==TargetLanguage.RPC) {
					MIDParser.parseRPCString(tearDownArea.getText());
				}
				mid.setTearDownCode(tearDownArea.getText());
			}
		}
		for (JTextArea codeSegment: codeSegments)
			if (!codeSegment.getText().trim().equals(""))
				mid.addHelperCode(codeSegment.getText());
	}
	
	protected String parseSeleniumCommands(Vector<Vector<Object>> table){
		String cmdString="";
		for (int index=0; index<table.size(); index++){
			Vector<Object> row = table.get(index);
			if (row.get(1)==null)
				continue;
			String cmd = row.get(1).toString();
			String target = row.get(2)!=null? row.get(2).toString():""; 
			String value = row.get(3)!=null? row.get(3).toString():"";
			String seleniumCommand = language==TargetLanguage.HTML?
						MID.getSeleniumCommandHTML(cmd, target, value):
							MID.getSeleniumCommandCall(cmd, target, value);	
			cmdString += seleniumCommand;
		}
		return cmdString;
	}

}
