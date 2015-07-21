/* 	
	Author Dianxiang Xu
*/
package edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import locales.LocaleBundle;

import testcode.TargetLanguage;

public class GeneralTablePanel extends JPanel implements ActionListener, ListSelectionListener{
	private static final long serialVersionUID = 1L;

	private static final String insertRowBefore = "Insert Row Before";
	private static final String insertRowAfter = "Insert Row After";
	private static final String deleteRow = "Delete Row";
	private static final String adjustRowHeight = "Adjust Row Heights";
	
//  non-evaluated guard conditions associated with net transitions are used only for creating dirty tests with branch coverage
//  they can be edited through Excel (next to the "effect" column). 
	private static final String[] functionNetColumnNamesWithGuard = {"No", "Transition", "Precondition", "Postcondition", "When", "Effect", "Guard"};
	private static final String[] functionNetColumnNames = {"No", "Transition", "Precondition", "Postcondition", "When", "Effect"};
	private static final String[] contractColumnNames = {"No", "Module", "Precondition", "Postcondition", "When", "Effect"};
	private static final String[] stateMachineColumnNames = {"No", "Event", "Start State", "End State", "Precondition", "Postcondition"};
	private static final String[] threatNetColumnNames = {"No", "Transition", "Precondition", "Postcondition", "When", "Effect"};
	private static final String[] threatTreeColumnNames = {"No", "Event", "Child Events", "Relation"};

	private static final String[] attributeColumnNames = {"Attribute", "Category", "Attribute Name", "Data Type", "Attrubute Values"};
	private static final String[] ruleColumnNames = {"Rule", "Rule Effect", "Subject Condition", "Action Condition", "Resource Condition", "Environment Condition", "Obligation"};
	private static final String[] CATEGORIES ={"Subject", "Action", "Resource", "Environment"};
	private static final String[] TYPES ={"integer", "boolean", "string"};
	private static final String[] EFFECTS ={"Permit", "Deny"};
	
	private static final String[] objectColumnNames = {"No", "Model-Level Object", "Implementation-Level Object"};

	private static final String[] methodColumnNamesForSelenium = {"No", "Model-Level Event", "Command", "Target", "Value"};
	private static final String[] accessorColumnNamesForSelenium = {"No", "Model-Level State", "Accesor Command", "Target", "Value"};
	private static final String[] mutatorColumnNamesForSelenium = {"No", "Model-Level State", "Mutator Command", "Target", "Value"};

	private static final String[] methodColumnNames = {"No", "Model-Level Event", "Implementation Code"};
	private static final String[] accessorColumnNames = {"No", "Model-Level State", "Implementation Accessor"};
	private static final String[] mutatorColumnNames = {"No", "Model-Level State", "Implementation Mutator"};

	private static final int totalColumnCountForOperators = methodColumnNamesForSelenium.length;

	private static final String[] seleniumCommandColumnNames = {"No", "Command", "Target", "Value"};

	public static enum MIDTableType {FUNCTIONNET, ATTRIBUTE, RULE, STATEMACHINE, CONTRACT, THREATNET, THREATTREE, OBJECT, METHOD, ACCESSOR, MUTATOR, SELENIUMCOMMAND};

	private GeneralEditor editor; 
	private MIDTableType tableType;
	private GeneralTableModel tableModel;
	private JTable table;
	
	private TextAreaCellEditor tableCellEditor;
	
	public GeneralTablePanel(XMIDEditor editor, MIDTableType tableType, Vector<Vector<Object>> data, String[] columnNames, int totalColumnCount){
		this.editor = editor;
		this.tableType = tableType;

		tableModel = new GeneralTableModel(data, columnNames, totalColumnCount, editor.isEditing());
		tableModel.addTableModelListener(editor);
		table = System.getProperty("os.name").contains("Mac")? new JTableMac(tableModel): new JTable(tableModel);
		table.getTableHeader().setFont(GeneralEditor.titleFont);
		table.getTableHeader().setForeground(GeneralEditor.titleColor);

		// align headers of all columns
		TableCellRenderer rendererFromHeader = table.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel)rendererFromHeader;
		headerLabel.setHorizontalAlignment(JLabel.CENTER); 

		// align the header of first column
/*		DefaultTableCellHeaderRenderer headerRenderer = new DefaultTableCellHeaderRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setHeaderRenderer(headerRenderer);
*/
		table.setFont(editor.getTextFont());
		table.setRowHeight(editor.getTextFont().getSize()+8);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setDefaultRenderer(String.class, new TextAreaCellRenderer());
	    table.setFillsViewportHeight(true);
	    table.addKeyListener(new java.awt.event.KeyAdapter() {
	        public void keyPressed(KeyEvent evt) {
	        	if (evt.getKeyCode() == KeyEvent.VK_DOWN){
//	        		evt.consume();
				    if (table.getSelectedRow()>=table.getRowCount()-1 && !tableModel.hasEnoughEmptyRowsAtBottom())
				    	tableModel.insertRow(table.getRowCount());
	        	}
	        }
	   });

/*	    // double click to adjust the row height
		table.addMouseListener( new MouseAdapter() { 
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume();
					int selectedRow = table.getSelectedRow();			
				    if (selectedRow >= 0){
				    	setToPreferredRowHeight(selectedRow);
				    }
				}
			}
		}); 
*/
	    
/*	    if (tableType==MIDTableType.OBJECT || tableType==MIDTableType.METHOD || tableType==MIDTableType.ACCESSOR || tableType==MIDTableType.MUTATOR)
	    	tableCellEditor= new TextAreaCellEditor(table.getFont(), editor.getModelPanel().getChoicesForMIMEntry(tableType));
	    else
*/	    	tableCellEditor= new TextAreaCellEditor(table.getFont(), null);
	    table.setDefaultEditor(Object.class, tableCellEditor);
	    
	    if (tableType==MIDTableType.OBJECT || tableType==MIDTableType.METHOD || tableType==MIDTableType.ACCESSOR || tableType==MIDTableType.MUTATOR){
	    	TableColumn modelColumn = table.getColumnModel().getColumn(1);
	    	modelColumn.setCellEditor(new TextAreaCellEditor(table.getFont(), editor.getModelPanel().getChoicesForMIMEntry(tableType)));
	    } else 
	    if (tableType==MIDTableType.ATTRIBUTE){
	    	TableColumn categoryColumn = table.getColumnModel().getColumn(1);
	    	categoryColumn.setCellEditor(new TextAreaCellEditor(table.getFont(), new ArrayList<String>(Arrays.asList(CATEGORIES)), false));
	    	TableColumn typeColumn = table.getColumnModel().getColumn(3);
	    	typeColumn.setCellEditor(new TextAreaCellEditor(table.getFont(), new ArrayList<String>(Arrays.asList(TYPES)), false));
	    } else 
	    if (tableType==MIDTableType.RULE){
	    	TableColumn effectColumn = table.getColumnModel().getColumn(1);
	    	effectColumn.setCellEditor(new TextAreaCellEditor(table.getFont(), new ArrayList<String>(Arrays.asList(EFFECTS)), false));
	    }

	    if (editor.isEditing())
	    	setupPopupMenu();
		ListSelectionModel listMod =  table.getSelectionModel();
		listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMod.addListSelectionListener(this);

		// align first column center  
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
		
	    setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
		setPreferredColumnWidths();
	}
	
	public static GeneralTablePanel createModelTablePanel(XMIDEditor editor, MIDTableType tableType, Vector<Vector<Object>> data){
		if (tableType==MIDTableType.FUNCTIONNET && hasGuardForPetriNet(data))
			return new GeneralTablePanel(editor, tableType, data, functionNetColumnNamesWithGuard, functionNetColumnNamesWithGuard.length);			
		return new GeneralTablePanel(editor, tableType, data, getColumnNames(tableType), functionNetColumnNames.length);
	}

	public static GeneralTablePanel createAttributeTablePanel(XMIDEditor editor,  Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.ATTRIBUTE, data, attributeColumnNames, attributeColumnNames.length);
	}

	public static GeneralTablePanel createRuleTablePanel(XMIDEditor editor,  Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.RULE, data, ruleColumnNames, ruleColumnNames.length);
	}

	public static GeneralTablePanel createObjectTablePanel(XMIDEditor editor,  Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.OBJECT, data, objectColumnNames, objectColumnNames.length);
	}

	public static GeneralTablePanel createMethodTablePanel(XMIDEditor editor, TargetLanguage language, Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.METHOD, data, getColumnNames(language, MIDTableType.METHOD), totalColumnCountForOperators);
	}

	public static GeneralTablePanel createAccessorTablePanel(XMIDEditor editor, TargetLanguage language, Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.ACCESSOR, data, getColumnNames(language, MIDTableType.ACCESSOR), totalColumnCountForOperators);
	}
	
	public static GeneralTablePanel createMutatorTablePanel(XMIDEditor editor, TargetLanguage language, Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.MUTATOR, data, getColumnNames(language, MIDTableType.MUTATOR), totalColumnCountForOperators);
	}

	public static GeneralTablePanel createSeleniumCommandTablePanel(XMIDEditor editor, Vector<Vector<Object>> data){
		return new GeneralTablePanel(editor, MIDTableType.SELENIUMCOMMAND, data, seleniumCommandColumnNames, seleniumCommandColumnNames.length);
	}

	private static boolean hasGuardForPetriNet(Vector<Vector<Object>> data){
		for (int rowIndex=0; rowIndex<data.size(); rowIndex++){
			Vector<Object> row = data.get(rowIndex);
			if (row.size()>6){
				String guard = (String)row.get(6);
				if (guard!=null && !guard.trim().equals(""))
					return true;
			}
		}
		return false;
	}
	
	public JTable getTable(){
		return table;
	}

	public MIDTableType getTableType(){
		return tableType;
	}
	
	public GeneralTableModel getTableModel(){
		return tableModel;
	}
	
	public void setMinRows(int rows){
		tableModel.setMinimumRows(rows);
	}
	
	public void setFont(Font font){
		super.setFont(font);
		if (tableCellEditor!=null)
			tableCellEditor.updateFont(font);
		if (table!=null){
			table.setFont(font);
			for (int row=0; row<table.getRowCount(); row++)
				setToPreferredRowHeight(row);
		}
	}
	
	public static String[] getColumnNames(MIDTableType tableType){
		switch (tableType){
			case FUNCTIONNET: return functionNetColumnNames;
			case ATTRIBUTE: return attributeColumnNames;
			case RULE: return ruleColumnNames;
			case CONTRACT: return contractColumnNames;
			case STATEMACHINE: return stateMachineColumnNames;
			case THREATNET: return threatNetColumnNames;
			case THREATTREE: return threatTreeColumnNames;
			case OBJECT: return objectColumnNames;
			default: return functionNetColumnNames;
		}
	}
	
	public static String[] getColumnNames(TargetLanguage language, MIDTableType tableType){
		if (language==TargetLanguage.HTML || language==TargetLanguage.SELENIUMDRIVER)
			switch (tableType){
				case METHOD: return methodColumnNamesForSelenium;
				case ACCESSOR: return accessorColumnNamesForSelenium;
				case MUTATOR: return mutatorColumnNamesForSelenium;
			}
		else
			switch (tableType){
				case METHOD: return methodColumnNames;
				case ACCESSOR: return accessorColumnNames;
				case MUTATOR: return mutatorColumnNames;
			}
		return methodColumnNamesForSelenium;
	}
	
	private void setPreferredColumnWidths(){
		int totalWidth = editor.getKernel().getParentFrame().getWidth()-10;	
		table.getColumnModel().getColumn(0).setMinWidth(20);
//		table.getColumnModel().getColumn(0).setPreferredWidth(20);
/*		if (tableModel.getVisibleColumnCount()==3 && tableType==MIDTableType.OBJECT){
			if (editor.getModelType()==ModelType.COMBINATORIAL){
				table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.35));
				table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.60));
			} else {
				table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.15));
				table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.30));
			}
		} else
		if (tableModel.getVisibleColumnCount()==3 && tableType!=MIDTableType.OBJECT){
*/
		if (tableType==MIDTableType.ATTRIBUTE){
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.08));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.20));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.06));
			table.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth*0.57));
		} else
		if (tableType==MIDTableType.RULE){
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.06));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.17));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.17));
			table.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth*0.17));
			table.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth*0.17));
			table.getColumnModel().getColumn(6).setPreferredWidth((int)(totalWidth*0.17));
		} else
		if (tableModel.getVisibleColumnCount()==3){
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.40));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.50));
		} else
		if (tableModel.getVisibleColumnCount()==4 && tableType==MIDTableType.SELENIUMCOMMAND){
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.20));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.35));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.35));
		} else
		if (tableModel.getVisibleColumnCount()==4 && tableType==MIDTableType.THREATTREE){
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.20));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.65));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.05));
		} else
		if (tableModel.getVisibleColumnCount()==5){
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.15));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.15));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.35));
			table.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth*0.25));
		} else
		if (tableModel.getVisibleColumnCount()==6) {
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.15));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.25));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.25));
			table.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth*0.15));
			table.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth*0.10));
		} else
		if (tableModel.getVisibleColumnCount()>6) {
			table.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth*0.10));
			table.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth*0.20));
			table.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth*0.20));
			table.getColumnModel().getColumn(4).setPreferredWidth((int)(totalWidth*0.15));
			table.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth*0.15));
			table.getColumnModel().getColumn(6).setPreferredWidth((int)(totalWidth*0.10));
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int selectedRow = table.getSelectedRow();			
		     if (selectedRow >= 0) {
//		          System.out.println("Row"+selectedRow+tableModel.rowString(selectedRow));
		          if (editor.isEditing() && selectedRow>=tableModel.getRowCount()-1 && !tableModel.isEmptyRow(selectedRow))
		  				tableModel.addRow();
		     }
		}
		validate();
		updateUI();
	}

	private void setToPreferredRowHeight(int rowIndex) {
		int height = 0;
		for (int c=0; c<table.getColumnCount(); c++) {
		    TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
	    	Component comp = table.prepareRenderer(renderer, rowIndex, c);
	    	height = Math.max(height, comp.getPreferredSize().height);
		}
		if (height!=table.getRowHeight(rowIndex))
			table.setRowHeight(rowIndex, height);
	}

	private void setupPopupMenu() {
		final JPopupMenu popupMenu = new JPopupMenu();

		table.addMouseListener( new MouseAdapter() { 
			public void mousePressed( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			} 
			public void mouseReleased( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			}
			private void checkForTriggerEvent( MouseEvent e ) { 
				if ( e.isPopupTrigger()) { 
					popupMenu.removeAll();
					if (editor.isEditing()){
						createPopupMenuItem(popupMenu, insertRowBefore, insertRowBefore);
						createPopupMenuItem(popupMenu, insertRowAfter, insertRowAfter);
						JMenuItem deleteRowItem = createPopupMenuItem(popupMenu, deleteRow, deleteRow);
						deleteRowItem.setEnabled(table.getSelectedRow()>=0);
//						createPopupMenuItem(popupMenu, adjustRowHeight, adjustRowHeight);
					}
					popupMenu.show( e.getComponent(), e.getX(), e.getY() );
				}	
			} 
		}); 
	}
	
	private JMenuItem createPopupMenuItem(JPopupMenu popupMenu, String title, String command){
		JMenuItem menuItem = popupMenu.add(LocaleBundle.bundleString(title));
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}
	
    // implements ActionListener
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == insertRowBefore) {
			int selectedRow= table.getSelectedRow();
			if (selectedRow==-1)
				selectedRow=0;
			tableModel.insertRow(selectedRow);
		} else		
		if (cmd == insertRowAfter) {
			int selectedRow= table.getSelectedRow();
			if (selectedRow==-1)
				selectedRow=table.getRowCount()-1;
			tableModel.insertRow(selectedRow+1);
		} else
		if (cmd == deleteRow){
			tableModel.removeRow(table.getSelectedRow());
		} else
		if (cmd == adjustRowHeight){
			for (int row=0; row<table.getRowCount(); row++)
				setToPreferredRowHeight(row);
		}
	}
	
}
