/* 	
	Author Dianxiang Xu
*/
package edit;

import java.awt.Dimension;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import locales.LocaleBundle;

public class GeneralTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	protected String[] columnNames;
	protected Vector<Vector<Object>> data;

	protected boolean editable;
	
	private int totalColumnCount;
	private int visibleColumnCount;
	
	public GeneralTableModel(Vector<Vector<Object>> data, String[] columnNames, int totalColumnCount, boolean editable){
		this.data = data; 
		this.columnNames = new String[columnNames.length];
		for (int i=0; i<columnNames.length; i++)
			this.columnNames[i] = LocaleBundle.bundleString(columnNames[i]);
		this.totalColumnCount = totalColumnCount;
		this.editable = editable;
		visibleColumnCount = columnNames.length;
	}
		
	public void setMinimumRows(int rows){
		int index = data.size();
		while (index++<rows){
			addRow();
		}
	}
	
	public Vector<Vector<Object>> getData(){
		return data;
	}
		
	public String getColumnName(int column) {
		return columnNames[column];
	}

	public void setColumnName(int column, String name) {
		columnNames[column] = name;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int columnIndex) {
//    	if (columnIndex==0)
//    		return Integer.class;
        return String.class;
    }

	public boolean isCellEditable(int row, int column) {
		return column!=0 && editable;
	}
	
	public Object getValueAt(int row, int column) {
		Vector<Object> rowVector = data.get(row);
		return rowVector.get(column);
	}

	public void setValueAt(Object value, int row, int column) {
		Vector<Object> rowVector = data.get(row);
		rowVector.set(column, value);
		fireTableCellUpdated(row, column);
	}

	public boolean isEmptyRow(int row) {
		Vector<Object> rowData = data.get(row);
		// 0 index is the number
		for (int index=1; index<rowData.size(); index++){
			Object cell = rowData.get(index);
//			if (cell!=null && cell instanceof String && !((String)cell).trim().equals(""))
//				return false;
			if (cell!=null && !cell.toString().trim().equals(""))
				return false;
		}
		return true;
	}

	public boolean isEmptyTable(){
		for (int row=0; row<data.size(); row++)
			if (!isEmptyRow(row))
				return false;
		return true;
	}
	
	public boolean hasEnoughEmptyRowsAtBottom(){
		int MAX=30;
		if (data.size()<MAX)
			return false;
		int numberOfEmptyRows=0;
		for (int row=data.size()-1; row>=0 && numberOfEmptyRows<MAX; row--){
			if (isEmptyRow(row)) 
				numberOfEmptyRows++;
			else
				break;
		}
		return numberOfEmptyRows>=MAX;
	}
	
	public int getRowCount() {
		return data.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getVisibleColumnCount(){
		return visibleColumnCount;
	}
	
	public void removeRow(int index){
		data.remove(index);
		updateNumbers(index);
		fireTableRowsDeleted(index, index);
	}
	
	public void addRow() {
		Vector<Object> newRow = new Vector<Object>();
		newRow.add(""+(data.size()+1));
		for (int column =1; column<totalColumnCount; column++)
			newRow.add(null);
		data.add(newRow);
		fireTableRowsInserted(data.size()-1, data.size()-1);
	}

	public void insertRow(int index) {
		Vector<Object> newRow = new Vector<Object>();
		newRow.add(""+index);
		for (int column =1; column<totalColumnCount; column++)
			newRow.add(null);
		data.insertElementAt(newRow, index);
		updateNumbers(index);
		fireTableRowsInserted(index, index);

	}

	public String rowString(int index){
		String result = "";
		Vector<Object> vector = data.get(index);
		for (Object object: vector)
			if (object!=null)
				result +=" "+object.toString();
		return result;
	}
	
	private void updateNumbers(int start){
		int index = start;
		while (index<data.size()){
			Vector<Object> row = data.get(index);
			row.set(0, ""+(index+1));
			index++;
		}
	}
	
	public String toString(){
		String result = "";
		for (int i=0; i<data.size(); i++){
			result+= rowString(i);
		}
		return result;
	}
	
	public static void main(String[] args) {
		   
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
		}

		
		Vector<Object> v1 = new Vector<Object>();
		v1.add("1");
		v1.add("picpup(x)");
		v1.add("handempty");
		v1.add("ontable");
		v1.add("when");
		v1.add("effect");

		v1.add("inscription");

		Vector<Object> v2 = new Vector<Object>();
		v2.add("1");
		v2.add("picpup(x)");
		v2.add("handempty");
		v2.add("ontable");
		v2.add("when");
		v2.add("effect");
		v2.add("inscription");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		data.add(v1);
		data.add(v2);
		
String[] columnNames = {"1", "2", "3", "4"};
		GeneralTableModel tableModel = new GeneralTableModel(data, columnNames, 7, true);
		JTable table = new JTable(tableModel);
		table.setColumnSelectionAllowed(false);
			table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());//new TextAreaCellRenderer());

		JFrame frame = new JFrame("Table");
		
/*		frame.addWindowListener( new WindowAdapter() {
			      public void windowClosing( WindowEvent e ) {
			        System.exit(0);
			      }
			    });
*/

	   JScrollPane scrollpane = new JScrollPane(table);
	   scrollpane.setPreferredSize(new Dimension(800,500));
	   frame.getContentPane().add(scrollpane);
	   frame.pack();
	   frame.setVisible(true);

	   }


}
