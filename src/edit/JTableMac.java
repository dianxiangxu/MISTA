package edit;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * A better-looking table than JTable.
 * In particular, on Mac OS this looks more like a Cocoa table than the default Aqua LAF manages.
 * Likewise Linux and the GTK+ LAF.
 * We also fill the entirety of any enclosing JScrollPane by default.
 * Tool tips are automatically provided for truncated cells.
 */
@SuppressWarnings("serial")
public class JTableMac extends JTable {
    private static final Color MAC_FOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR = new Color(0x7daaea);
    private static final Color MAC_UNFOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR = new Color(0xe0e0e0);
    
    private static final Color MAC_UNFOCUSED_SELECTED_CELL_BACKGROUND_COLOR = new Color(0xc0c0c0);
    
    private static final Color MAC_FOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    private static final Color MAC_FOCUSED_SELECTED_VERTICAL_LINE_COLOR = new Color(0x346dbe);
    private static final Color MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    private static final Color MAC_UNFOCUSED_SELECTED_VERTICAL_LINE_COLOR = new Color(0xacacac);

    private static final Color MAC_OS_ALTERNATE_ROW_COLOR = new Color(0.92f, 0.95f, 0.99f);
    
 
    public JTableMac() {
        this(null);
    }
    
    public JTableMac(TableModel model) {
        super(model);
        
        // Although it's the JTable default, most systems' tables don't draw a grid by default.
        // Worse, it's not easy (or possible?) for us to take over grid painting ourselves for those LAFs (Metal, for example) that do paint grids.
        // The Aqua and GTK LAFs ignore the grid settings anyway, so this causes no change there.
        setShowGrid(false);
        
        setIntercellSpacing(new Dimension());
        getTableHeader().setReorderingAllowed(false);
        
         // Work around Apple 4352937 (fixed in 10.5).
        if (System.getProperty("os.version").startsWith("10.4")) {
            ((JLabel) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEADING);
        }
            
        // Use an iTunes-style vertical-only "grid".
        setShowHorizontalLines(false);
        setShowVerticalLines(true);
        
     }

    public static boolean isGtk() {
        return UIManager.getLookAndFeel().getClass().getName().contains("GTK");
    }
    
    public static Color backgroundColorForRow(int row) {
        if (isGtk()) {
            return (row % 2 == 0) ? Color.WHITE : UIManager.getColor("Table.background");
        } else {
            return (row % 2 == 0) ? Color.WHITE : MAC_OS_ALTERNATE_ROW_COLOR;
        }
    }

     public void paint(Graphics g) {
        super.paint(g);
 //       paintEmptyRows(g);
    }

    protected void paintEmptyRows(Graphics g) {
        final int rowCount = getRowCount();
        final Rectangle clip = g.getClipBounds();
        final int height = clip.y + clip.height;
        if (rowCount * rowHeight < height) {
            for (int i = rowCount; i <= height/rowHeight; ++i) {
                g.setColor(backgroundColorForRow(i));
                g.fillRect(clip.x, i * rowHeight, clip.width, rowHeight);
            }
            
            // Mac OS' Aqua LAF never draws vertical grid lines, so we have to draw them ourselves.
            if (getShowVerticalLines()) {
                g.setColor(MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                TableColumnModel columnModel = getColumnModel();
                int x = 0;
                for (int i = 0; i < columnModel.getColumnCount(); ++i) {
                    TableColumn column = columnModel.getColumn(i);
                    x += column.getWidth();
                    g.drawLine(x - 1, rowCount * rowHeight, x - 1, height);
                }
            }
        }
    }
    
    /**
     * Changes the behavior of a table in a JScrollPane to be more like
     * the behavior of JList, which expands to fill the available space.
     * JTable normally restricts its size to just what's needed by its
     * model.
     */
/*    
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            JViewport parent = (JViewport) getParent();
            return (parent.getHeight() > getPreferredSize().height);
        }
        return false;
    }
   */
    
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        return prepareComponent(super.prepareRenderer(renderer, row, column), row, column);
    }
    
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        return prepareComponent(super.prepareEditor(editor, row, column), row, column);
    }
    
    private Component prepareComponent(Component c, int row, int column) {
        boolean focused = hasFocus();
        boolean selected = isCellSelected(row, column);
        if (selected) {
            if (focused == false && isEditing() == false) {
                // Native Mac OS renders the selection differently if the table doesn't have the focus.
                // The Mac OS LAF doesn't imitate this for us.
                c.setBackground(MAC_UNFOCUSED_SELECTED_CELL_BACKGROUND_COLOR);
                c.setForeground(UIManager.getColor("Table.foreground"));
            } else {
                c.setBackground(UIManager.getColor("Table.selectionBackground"));
                c.setForeground(UIManager.getColor("Table.selectionForeground"));
            }
        } else {
            // Outside of selected rows, we want to alternate the background color.
            c.setBackground(backgroundColorForRow(row));
            c.setForeground(UIManager.getColor("Table.foreground"));
        }
        
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            
            if (isGtk() && c instanceof JCheckBox) {
                // The Java 6 GTK LAF JCheckBox doesn't paint its background by default.
                // Sun 5043225 says this is the intended behavior, though presumably not when it's being used as a table cell renderer.
                jc.setOpaque(true);
            } else if (c instanceof JCheckBox) {
                // There's a similar situation on Mac OS.
                jc.setOpaque(true);
                // Mac OS 10.5 lets us use smaller checkboxes in table cells.
                ((JCheckBox) jc).putClientProperty("JComponent.sizeVariant", "mini");
            }
            
            if (getCellSelectionEnabled() == false && isEditing() == false) {
                    jc.setBorder(new AquaTableCellBorder(selected, focused, getShowVerticalLines()));
            }
            
            initToolTip(jc, row, column);
            c.setEnabled(this.isEnabled());
        }
        return c;
    }
    
    /**
     * Native Mac OS doesn't draw a border on the selected cell, but it does various things that we can emulate with a custom cell border.
     */
    private static class AquaTableCellBorder extends AbstractBorder {
        private boolean selected;
        private boolean focused;
        private boolean verticalLines;
        
        public AquaTableCellBorder(boolean selected, boolean focused, boolean verticalLines) {
            this.selected = selected;
            this.focused = focused;
            this.verticalLines = verticalLines;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // Native tables draw a horizontal line under the whole selected row.
            if (selected) {
                g.setColor(focused ? MAC_FOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR : MAC_UNFOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR);
                g.drawLine(x, y + height - 1, x + width, y + height - 1);
            }
            
            // Mac OS' Aqua LAF never draws vertical grid lines, so we have to draw them ourselves.
            if (verticalLines) {
                if (focused) {
                    g.setColor(selected ? MAC_FOCUSED_SELECTED_VERTICAL_LINE_COLOR : MAC_FOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                } else {
                    g.setColor(selected ? MAC_UNFOCUSED_SELECTED_VERTICAL_LINE_COLOR : MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                }
                g.drawLine(x + width - 1, y, x + width - 1, y + height);
            }
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            // Defer to getBorderInsets(Component c, Insets insets)...
            Insets result = new Insets(0, 0, 0, 0);
            return getBorderInsets(c, result);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // FIXME: the whole reason this class exists is because Apple's LAF doesn't like insets other than these, so this might be fragile if they update the LAF.
            insets.left = insets.top = insets.right = insets.bottom = 1;
            return insets;
        }
        
        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
    
    /**
     * Sets the component's tool tip if the component is being rendered smaller than its preferred size.
     * This means that all users automatically get tool tips on truncated text fields that show them the full value.
     */
    private void initToolTip(JComponent c, int row, int column) {
        String toolTipText = null;
        if (c.getPreferredSize().width > getCellRect(row, column, false).width) {
        	if (getValueAt(row, column)!=null)
        		toolTipText = getValueAt(row, column).toString();
        }
        c.setToolTipText(toolTipText);
    }
    
    /**
     * Places tool tips over the cell they correspond to. MS Outlook does this, and it works rather well.
     * Swing will automatically override our suggested location if it would cause the tool tip to go off the display.
     */
    @Override
    public Point getToolTipLocation(MouseEvent e) {
        // After a tool tip has been displayed for a cell that has a tool tip, cells without tool tips will show an empty tool tip until the tool tip mode times out (or the table has a global default tool tip).
        // (ToolTipManager.checkForTipChange considers a non-null result from getToolTipText *or* a non-null result from getToolTipLocation as implying that the tool tip should be displayed. This seems like a bug, but that's the way it is.)
        if (getToolTipText(e) == null) {
            return null;
        }
        final int row = rowAtPoint(e.getPoint());
        final int column = columnAtPoint(e.getPoint());
        if (row == -1 || column == -1) {
            return null;
        }
        return getCellRect(row, column, false).getLocation();
    }
    
    /**
     * Improve the appearance of of a table in a JScrollPane on Mac OS, where there's otherwise an unsightly hole.
     */
    @Override
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();

        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                
                // JTable copy & paste above this point; our code below.
                
                // Remove the scroll pane's focus ring.
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                
                // Put a dummy header in the upper-right corner.
                final Component renderer = new JTableHeader().getDefaultRenderer().getTableCellRendererComponent(null, "", false, false, -1, 0);
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(renderer, BorderLayout.CENTER);
                scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, panel);
            }
        }
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
		JTable table = new JTableMac(tableModel);
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