package testgeneration;

import java.util.*;
import javax.swing.table.AbstractTableModel;

import locales.LocaleBundle;

public class ParaTableModel extends AbstractTableModel implements
		java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static final int STATEMENT_INDEX = 0;
	public static final int PARAMETER_INDEX = 1;
	public static final int HIDDEN_INDEX = 2;

	public String[] columnNames = { LocaleBundle.bundleString("Parameters or statements"), LocaleBundle.bundleString("Parameter")+"?", "" };
	protected Vector<ParaRecord> dataVector;

	public ParaTableModel() {
		dataVector = new Vector<ParaRecord>();
	}

	public ParaTableModel(Vector<ParaRecord> dataVector){
		this.dataVector = dataVector; 
	}
	
	public Vector<ParaRecord> getDataVector(){
		return dataVector;
	}
		
	public String getColumnName(int column) {
		return columnNames[column];
	}

	public void setColumnName(int column, String name) {
		columnNames[column] = name;
	}

	public boolean isCellEditable(int row, int column) {
		if (column == HIDDEN_INDEX)
			return false;
		else
			return true;
	}

	@SuppressWarnings("unchecked")
	public Class getColumnClass(int column) {
		switch (column) {
			case STATEMENT_INDEX:
				return String.class;
			case PARAMETER_INDEX:
				return Boolean.class;
			default:
				return Object.class;
		}
	}

	public Object getValueAt(int row, int column) {
		ParaRecord record = (ParaRecord) dataVector.get(row);
		switch (column) {
			case STATEMENT_INDEX:
				return record.getExpression();
			case PARAMETER_INDEX:
				return new Boolean(record.isParameter());
			case HIDDEN_INDEX:
				return "";
			default:
				return new Object();
		}
	}

	public void setValueAt(Object value, int row, int column) {
		if (value != null) {
			ParaRecord record = (ParaRecord) dataVector.get(row);
			switch (column) {
			case STATEMENT_INDEX:
				record.setExpression((String) value);
				break;
			case PARAMETER_INDEX:
				record.setParameter(((Boolean) value).booleanValue());
				break;
			case HIDDEN_INDEX:
				//record.setStatement("");
				break;
			default:
				System.out.println("invalid index");
			}
			fireTableCellUpdated(row, column);
		}
	}

	public Object getByRow(int row) {
		ParaRecord record = (ParaRecord) dataVector.get(row);
		return record;
	}

	public boolean isRowEmpty(int row) {
		boolean isEmptyRow = false;
		ParaRecord record = (ParaRecord) dataVector.get(row);
		if (record.getExpression().trim().equals("")) {
			isEmptyRow = true;
		}
		return isEmptyRow;
	}

	public int getRowCount() {
		return dataVector.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public boolean hasEmptyRow() {
		if (dataVector.size() == 0)
			return false;
		ParaRecord record = (ParaRecord) dataVector.get(dataVector.size() - 1);
		if (record.getExpression().trim().equals("")) {
			return true;
		} else
			return false;
	}

	public void addEmptyRow() {
		dataVector.add(new ParaRecord());
		fireTableRowsInserted(dataVector.size() - 1, dataVector.size() - 1);
	}

	public String toString() {
		String result = "";
		for (int i = 0; i < getRowCount(); i++) {
			if (!isRowEmpty(i)) {
				result += "     ";
				for (int j = 0; j < getColumnCount() - 1; j++) {
					if (!getValueAt(i, j).toString().equals("")) {
						if (j > 0)
							result += ",";
						result += getValueAt(i, j).toString();
					}
				}
				result += "\n";
			}
		}
		return result;
	}

	// string in test case parameters
	public String getParaString() {
		String result = "";
		int currentParameterIndex = 0;
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("")) {
				if (record.isParameter()) {
					currentParameterIndex++;
					if (currentParameterIndex > 1) {
						result += ",";
					}
					result += record.getExpression();
				}
			}
		}
		return result;
	}

	public ArrayList<String> getParameters() {
		ArrayList<String> parameters = new ArrayList<String>();
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("")) {
				if (record.isParameter())
					parameters.add(record.getExpression());
			}
		}
		return parameters;
	}
	
	public int getInputCount() {
		int count = 0;
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("")) 
					count++;
		}
		return count;
	}

	public boolean hasParameters() {
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("") &&  record.isParameter()) 
					return true;
		}
		return false;
	}
	
	public int getParaCount() {
		int paraCount = 0;
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("") &&  record.isParameter()) 
					paraCount++;
		}
		return paraCount;
	}

	// string in test case parameter
	public ArrayList<String> getNonParaStrings() {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("")) {
				if (!record.isParameter()) {
					result.add(record.getExpression());
				}
			}
		}
		return result;
	}

	// string in test case parameter
	public int getNonParaCount() {
		int count=0;
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("") && !record.isParameter())
					count++;
		}
		return count;
	}

	public ArrayList<String> getAssigmentsString() {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < getRowCount(); i++) {
			ParaRecord record = (ParaRecord) getByRow(i);
			if (!record.getExpression().trim().equals("")) {
				if (!record.isParameter()) {
					result.add(record.getExpression());
				}
			}
		}
		return result;
	}
	
	public ParaTableModel clone(){
		ParaTableModel newModel  = new ParaTableModel();
		Vector<ParaRecord> newDataVector = new Vector<ParaRecord>();
		for (ParaRecord data: dataVector) {
			newDataVector.add((ParaRecord)data.clone());
		}
		newModel.dataVector = newDataVector;
		return newModel;
	}
}
