/* 	
	Author Dianxiang Xu
*/
package testinterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import locales.LocaleBundle;
import mid.Marking;

import kernel.Kernel;
import kernel.SystemOptions;

import testcode.TestCodeGenerator;
import testgeneration.ParaTableModel;
import testgeneration.TransitionTreeNode;

public class TestTreePanel extends JPanel implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	
	private static final JLabel paraInputTablePrompt = new JLabel(LocaleBundle.bundleString("Click on a node to edit test parameters"));
	private static final Font titleFont = new Font(SystemOptions.DefaultFontName, Font.PLAIN, 12);
	private static final double HorizontalDividor = 0.6; 
	
	private boolean treeSaved = true;
	
	private Kernel kernel;
	
	private TestJTree testTree;
	private JPanel interactiveTablesPanel;
	
	public TestTreePanel(Kernel kernel) {
		super();
		this.kernel = kernel;
		interactiveTablesPanel = new JPanel();
		paraInputTablePrompt.setFont(titleFont);
	}

	public void setTestTreePanel() {
		removeAll();
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTestTreeJScrollPane(), createParaTableJScrollPaneByPath());
        pane.setOneTouchExpandable(true);
        
		int totalWidth = kernel.getFileManager().getEditor().getEditingJComponent().getSize().width;
		pane.setDividerLocation((int)(totalWidth*HorizontalDividor));

        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
	}
		
	private JScrollPane createTestTreeJScrollPane() {
		JScrollPane treePane = new JScrollPane(testTree);
		treePane.setBorder(createTitledBorder(""));		
//		treePane.setBorder(BorderFactory.createBevelBorder(1));		
		treePane.setMinimumSize(new Dimension(120, 100));
		return treePane;
	}

	private JScrollPane createParaTableJScrollPaneByPath() {
		BoxLayout layout = new BoxLayout(interactiveTablesPanel,
				BoxLayout.Y_AXIS);
		interactiveTablesPanel.setLayout(layout);
		JScrollPane jp = new JScrollPane(interactiveTablesPanel);
		jp.setBorder(createTitledBorder(""));
//		jp.setBorder(BorderFactory.createBevelBorder(1));		
		jp.setMinimumSize(new Dimension(80, 100));
		return jp;
	}

	private void reloadTablesPanel(TreeNode[] sequence) {
		if (sequence != null) {
			for (int i = 1; i < sequence.length; i++) {
				DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode) (sequence[i]);
				reloadOneTable(sequence, mutableNode);
			}
		}
	}

	private void reloadOneTable(TreeNode[] sequence, DefaultMutableTreeNode mutableNode){
		TransitionTreeNode node = (TransitionTreeNode) mutableNode.getUserObject();
		ParaTableModel paraTable = node.getParaTable();
		String label = node.nodeIdentityString(true);
		ParaTablePanel iTablePanel = new ParaTablePanel(paraTable, sequence, this);
		Color titleColor = new Color(0.0f, 0.0f, 0.7f); // Color.BLUE;
		iTablePanel.setBorder(BorderFactory
				.createTitledBorder(null, label, 0, 0, titleFont, titleColor));				
		iTablePanel.setVisible(true);
		interactiveTablesPanel.add(iTablePanel);
	}
	
	public void valueChanged(TreeSelectionEvent event) {
		if (event.getSource() == testTree) {
			DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode)(testTree.getLastSelectedPathComponent());
			if (mutableNode != null) {
				displayNodeInfo(mutableNode);
				TreeNode[] nodes = ((DefaultTreeModel)testTree.getModel()).getPathToRoot(mutableNode);
				interactiveTablesPanel.removeAll();
				if (mutableNode.isLeaf()) {
					reloadTablesPanel(nodes);
				} else {
					if (!mutableNode.isRoot())
						reloadOneTable(nodes, mutableNode);
					else
						interactiveTablesPanel.add(paraInputTablePrompt);
				}
				if (mutableNode.isLeaf())
					updateTestCode(nodes);
				interactiveTablesPanel.revalidate();
				interactiveTablesPanel.repaint();
			}
		}
	}
	
	private void displayNodeInfo(DefaultMutableTreeNode mutableNode){
		TransitionTreeNode node = ((TransitionTreeNode) mutableNode.getUserObject());
		if (!node.isRoot()) {
			if (node.getOutlineNumber().length()>0)
				kernel.printToConsole("\n"+LocaleBundle.bundleString("Selected node")+": "+node.getOutlineNumber());
			else 
				kernel.printToConsole("\n"+LocaleBundle.bundleString("Selected node")+": "+ LocaleBundle.bundleString("Depth")+" "+(node.getLevel()-1));
			if (node.getSubstitution()!=null && node.getSubstitution().hasBindings()) 
				kernel.printToConsole(LocaleBundle.bundleString("Transition")+": "+node.getEvent()+" "+node.getSubstitution().toString(node.getTransition().getAllVariables()));
			else
				kernel.printToConsole(LocaleBundle.bundleString("Transition")+": "+node.getEvent());				
			if (node.getParent()!=null){
				Marking parentMarking = node.getParent().getMarking();
				if (parentMarking!=null) 
					kernel.printToConsole(LocaleBundle.bundleString("Previous state")+": "+parentMarking);
			}
			if (node.getMarking()!=null) 
				kernel.printToConsole(LocaleBundle.bundleString("Resultant state")+": "+node.getMarking());
		}
	}
	
	public void updateTestCode(TreeNode[] testSequence) {
		ArrayList<TransitionTreeNode> nodes = new ArrayList<TransitionTreeNode>(); 
//			 root (not test node) when i=0			
		for (int i=1; i<testSequence.length; i++) {
			DefaultMutableTreeNode defaultTreeNode = (DefaultMutableTreeNode) testSequence[i]; 
			nodes.add((TransitionTreeNode)defaultTreeNode.getUserObject());
		}
//		TargetLanguage language = testTree.getTransitionTree().getSystemOptions().getLanguage();
		TestCodeGenerator codeGenerator = TestCodeGenerator.createCodeGenerator(testTree.getTransitionTree());
		String testCode = codeGenerator.generateSequenceCodeForReview(nodes);

		// user may change the language while working on the tree
/*		if (language== TargetLanguage.HTML)
			testMethodArea.setContentType("text/html");
		else
			testMethodArea.setContentType("text/plain");
		Document newDoc = testMethodArea.getEditorKit().createDefaultDocument();
		testMethodArea.setDocument(newDoc);
		GeneralEditor.setTabs(testMethodArea, 2);
		testMethodArea.setText(testCode);
		testMethodArea.setCaretPosition(0);
*/		
		kernel.printToConsole(testCode);
	}
	
	public TestJTree getTestTree() {
		return testTree;
	}

	public void setTestTree(TestJTree tree) {
		testTree = tree;
		testTree.addTreeSelectionListener(this);
		testTree.setSelectionPath(new TreePath(testTree.getModel().getRoot()));
		setTestTreePanel();
	}

	private TitledBorder createTitledBorder(String title) { 
		Font titleFont = new Font("SansSerif", Font.PLAIN, 10);
//		Color titleColor = Color.BLACK;
		Color titleColor = new Color(0.0f, 0.0f, 0.9f);
		return BorderFactory.createTitledBorder(null, title, 0, 0, titleFont, titleColor);
	}
	
	public void cleanUp() {
		testTree = null;
	}
	
	public boolean isTreeSaved() {
		return treeSaved;
	}
	
	public void setTreeSaved(boolean saved) {
		treeSaved = saved;
	}
}
