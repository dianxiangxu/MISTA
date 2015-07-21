package testinterface;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import kernel.SystemOptions;

import testgeneration.TransitionTreeNode;

public class TestTreeRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;

	public TestTreeRenderer() {
//		setLeafIcon(null);
		setFont(new Font(SystemOptions.DefaultFontName, Font.PLAIN, 12));
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if(leaf) {
				TransitionTreeNode node = (TransitionTreeNode) ((DefaultMutableTreeNode)value).getUserObject();
				if (node.isNegative())
					setForeground(new Color(0.0f, 0.2f, 0.0f));  
		}
		
		return this;
	}
}
