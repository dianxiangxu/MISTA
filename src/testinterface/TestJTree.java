package testinterface;

import java.util.*;

import javax.swing.JTree;
import javax.swing.tree.*;

import kernel.CancellationException;
import kernel.ProgressDialog;
import locales.LocaleBundle;

import testgeneration.TransitionTree;

public class TestJTree extends JTree {
	private static final long serialVersionUID = 1L;

	private TransitionTree transitionTree;
	private ProgressDialog progressDialog;
	
	public TestJTree(TransitionTree transitionTree, ProgressDialog progressDialog) throws CancellationException {
		super(transitionTree.setToMutableTree());
		this.transitionTree = transitionTree;
		this.progressDialog = progressDialog;
		setCellRenderer(new TestTreeRenderer());
		expandAllPaths(true);
		this.setRootVisible(false);
	}

	// //////////////////////////////////////////////////////
	// expand the entire JTree
	// //////////////////////////////////////////////////////
	public void expandAllPaths(boolean expand) throws CancellationException {
		TreeNode root = (TreeNode) getModel().getRoot();
		expandAllPathsWithCancellation(new TreePath(root), 0, expand);
	}

	public void expandAllPathsWithCancellation(TreePath parent,int level, boolean expand) throws CancellationException {
		if (progressDialog!=null && progressDialog.isCancelled()){
			throw new CancellationException(LocaleBundle.bundleString("TEST_VISUALIZATION_CANCELED"));
		}
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				if (progressDialog!=null && progressDialog.isCancelled()) {
					throw new CancellationException(LocaleBundle.bundleString("TEST_VISUALIZATION_CANCELED"));
				}
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				if (level<transitionTree.getSystemOptions().getMaxLevelOfNodeExpansion())
					expandAllPathsWithCancellation(path, level+1, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			expandPath(parent);
		} else {
			collapsePath(parent);
		}	
	}

	public void expandAllPaths(TreePath parent,int level, boolean expand){
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				if (level<transitionTree.getSystemOptions().getMaxLevelOfNodeExpansion())
					expandAllPaths(path, level+1, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			expandPath(parent);
		} else {
			collapsePath(parent);
		}	
	}

	public TransitionTree getTransitionTree() {
		return transitionTree;
	}

}
