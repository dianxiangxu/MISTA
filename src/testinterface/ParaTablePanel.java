package testinterface;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import kernel.SystemOptions;

import testgeneration.ParaTableModel;

public class ParaTablePanel extends JPanel{
	private static final long serialVersionUID = 1;

	protected JTable table;
	protected JScrollPane scroller;
	protected ParaTableModel paraTable;

	public ParaTablePanel(ParaTableModel paraTable, TreeNode[] sequence, TestTreePanel testTreeEditor) {
		this.paraTable=paraTable;
		paraTable.addTableModelListener(new ParaTableModelListener(sequence, testTreeEditor));
		createTablePanel();
		this.setPreferredSize(new Dimension(100,85));
	}

	private void createTablePanel(){	
		table = new JTable();
		table.setModel(paraTable);
		scroller = new javax.swing.JScrollPane(table);
		scroller.setFont(new Font(SystemOptions.DefaultFontName, Font.PLAIN, 12)); 
		table.setPreferredScrollableViewportSize(new java.awt.Dimension(100,300));
		setColumnWidth(table);;
		setLayout(new BorderLayout());
		add(scroller, BorderLayout.CENTER);
	}

	public void setColumnWidth(JTable table) {
		table.setSurrendersFocusOnKeystroke(true);
		if (!paraTable.hasEmptyRow()) {
			paraTable.addEmptyRow();
		}

		TableColumn hidden = table.getColumnModel().getColumn(
				ParaTableModel.HIDDEN_INDEX);
		hidden.setMinWidth(2);
		hidden.setPreferredWidth(2);
		hidden.setMaxWidth(2);
		hidden.setCellRenderer(new InteractiveRenderer(
				ParaTableModel.HIDDEN_INDEX));
		
		TableColumn paraColumn = table.getColumnModel().getColumn(
				ParaTableModel.PARAMETER_INDEX);
		paraColumn.setMinWidth(60);
		paraColumn.setPreferredWidth(80);
		paraColumn.setMaxWidth(100);
	}

	public void highlightLastRow(int row) {
		int lastrow = paraTable.getRowCount();
		if (row == lastrow - 1) {
			table.setRowSelectionInterval(lastrow - 1, lastrow - 1);
		} else {
			table.setRowSelectionInterval(row + 1, row + 1);
		}

		table.setColumnSelectionInterval(0, 0);
	}

	class InteractiveRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1;
		protected int interactiveColumn;

		public InteractiveRenderer(int interactiveColumn) {
			this.interactiveColumn = interactiveColumn;
			setFont(new Font(SystemOptions.DefaultFontName, Font.PLAIN, 12));
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			if (column == interactiveColumn && hasFocus) {
				if ((paraTable.getRowCount() - 1) == row
						&& !paraTable.hasEmptyRow()) {
					paraTable.addEmptyRow();
				}

				highlightLastRow(row);
			}

			return c;
		}
	}


	public class ParaTableModelListener implements TableModelListener {
		private TreeNode[] testSequence;
		private TestTreePanel testTreeEditor;
		public ParaTableModelListener(TreeNode[] testSequence, TestTreePanel testTreeEditor) {
			this.testSequence = testSequence;
			this.testTreeEditor = testTreeEditor;
		}
		
		public void tableChanged(TableModelEvent evt) {
			if (evt.getType() == TableModelEvent.UPDATE 
//					|| evt.getType() == TableModelEvent.INSERT
					) {
				int column = evt.getColumn();
				int row = evt.getFirstRow();
				//System.out.println("row: " + row + " column: " + column);
				table.setColumnSelectionInterval(column + 1, column + 1);
				table.setRowSelectionInterval(row, row);
				testTreeEditor.updateTestCode(testSequence);
				testTreeEditor.setTreeSaved(false);
			}
		}
		

	}

	public ParaTableModel getParaTable() {
		return paraTable;
	}

	public void setParaTable(ParaTableModel tableModel) {
		this.paraTable = tableModel;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}


}
