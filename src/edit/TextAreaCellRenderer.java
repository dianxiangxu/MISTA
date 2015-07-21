package edit;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;

import java.awt.*;
import java.util.ArrayList;
 
public class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	
	private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

	public TextAreaCellRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(true);
		setTabSize(4);
	}
 
	public Component getTableCellRendererComponent(JTable table, Object value,
               boolean isSelected, boolean hasFocus, int row, int column) {
		adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setForeground(adaptee.getForeground());
		setBackground(adaptee.getBackground());

		setBorder(new EmptyBorder(3, 3, 3, 3));
		setFont(table.getFont());
		setText((value == null) ? "" : value.toString());

/*		// v1: do not adjust cell height when text is deleted 
		setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
		if (table.getRowHeight(row) < getPreferredSize().height) {
			table.setRowHeight(row, getPreferredSize().height);
		} 
*/
		// v2: adjust cell height when text is deleted
		adjustRowHeight(table, row, column);

		return this;
	}
 
    private ArrayList<ArrayList<Integer>> rowColHeights = new ArrayList<ArrayList<Integer>>();

    private void adjustRowHeight(JTable table, int row, int column) {
        int columnWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        setSize(new Dimension(columnWidth, 1000));
        int prefHeight = getPreferredSize().height;
        while (rowColHeights.size() <= row) {
          rowColHeights.add(new ArrayList<Integer>(column));
        }
        ArrayList<Integer> colHeights = rowColHeights.get(row);
        while (colHeights.size() <= column) {
          colHeights.add(0);
        }
        colHeights.set(column, prefHeight);
        int maxHeight = prefHeight;
        for (Integer colHeight : colHeights) {
        	if (colHeight > maxHeight) {
        		maxHeight = colHeight;
        	}
        }
        // deal with the height problem of empty rows in Linux
        if (maxHeight<getFont().getSize()+8)
        	maxHeight = getFont().getSize()+8;
        // 
        
        if (table.getRowHeight(row) != maxHeight) {
        	table.setRowHeight(row, maxHeight);
        }
    }
}
