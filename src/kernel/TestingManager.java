/* 	
	Author Dianxiang Xu
*/
package kernel;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.tree.*;

import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

import edit.GeneralEditor;
import edit.GeneralEditor.SimulatorType;
import edit.TestTreeFile;
import edit.XMIDEditor;

import parser.MIDParser;
import parser.ParseException;

import mid.MID;
import mid.UserDefinedSequences;

import simulation.OnTheFlyTestingAnalyzer;
import testcode.TargetLanguage;
import testcode.TestCodeGenerator;
import testgeneration.TransitionTree;
import testgeneration.TransitionTreeFromSequences;
import testgeneration.TransitionTreeFromUserDefinedSequences;
import testgeneration.TransitionTreeNode;
import testinterface.TestJTree;
import testinterface.TestTreePanel;
import utilities.*;

public class TestingManager implements ActionListener {

	public static boolean DisplayStatesInTestTree = false; 
	
	private Kernel kernel;
	
	private TestTreePanel treePanel; 

	private JPopupMenu	popupMenu; 
	private JMenuItem 	deleteNodeItem, cloneSubtreeItem, 
		//editNodeItem, addNodeItem, 
//					expandNodeItem,
						swapNodeItem,
						closeTreeItem;
	
	public TestingManager(Kernel kernel) {
		this.kernel = kernel;
		treePanel = new TestTreePanel(kernel);
		createPopupMenu();
	}
	
/////////////////////////////////////////////////////////////	
	public void actionPerformed(ActionEvent e) {
		if (kernel.getFileManager()!=null){
			GeneralEditor editor=kernel.getFileManager().getEditor();
			if (editor!=null && editor instanceof XMIDEditor && ((XMIDEditor)editor).isSimulationMode()){
				kernel.printDialogMessage(LocaleBundle.bundleString("Simulation mode"));
				return;
			}	
		}
		String command = e.getActionCommand();
		if (command == Commands.GenerateTree) {
			generateTestTree();
		} else if (command == Commands.CloseTree) { 
			closeTreeMenuItem();
		} else if (command == Commands.SaveTree) {
			saveTree();
		} else if (command == Commands.GenerateTestCode) {
			generateTestCode();		// save test code to file, no display
		} else if (command == Commands.ShowSequences) {
			showTestSequences();	// display test sequences
		} else if (command == Commands.PrintTree) {
			PrintUtilities.printComponent(treePanel.getTestTree());
		} else if (command == Commands.AddTreeNode) {
			addTreeNode();
		} else if (command == Commands.EditTreeNode) {
			
		} else if (command == Commands.DeleteTreeNode) {
			deleteTreeNode();
		} else if (command == Commands.CloneTreeNode) {
			cloneSubtree();
		} else if (command == Commands.MoveTreeNode) {
			expandNode();
		} else if (command == Commands.MoveTreeNode) {
			moveTreeNode();
		} else if (command == Commands.SetSystemOptions) {
			setSystemOptions();
		} else if (command == Commands.OnlineExecution) {
			kernel.getFileManager().getEditor().startSimulator(SimulatorType.ONLINE_TEST_EXECUTION);
		} else if (command == Commands.OnTheFlyTesting) {
			kernel.getFileManager().getEditor().startSimulator(SimulatorType.ON_THE_FLY_TESTING);
		} else if (command == Commands.AnalyzeOnTheFlyTests) {
			analyzeTests();
		} else { 
			kernel.printDialogMessage("Wrong operation!");
		}
	} 
	
	//////////////////////////////////////////////////////////////////////////
	public void setSystemOptions(){
		TransitionTree transitionTree = kernel.getFileManager().getEditor().getTransitionTree();
		new SystemOptionsJDialog(kernel, LocaleBundle.bundleString("Test Generation Options"), kernel.getSystemOptions(), transitionTree==null);
		kernel.showSystemOptionsInInfoPanel();
	}	
	
	public void updateTreePresentation(){
		if (treePanel.getTestTree()!=null)
			treePanel.getTestTree().updateUI();		
	}
	
/////////////////////////////////////////////////////////////	
	public void generateTestCode() {
		if (treePanel.getTestTree() == null)
			generateTree(false);		// generate tree and code (tree is not shown)
		else {
			assert treePanel.getTestTree().getTransitionTree()!=null;
			ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Test Generation"), LocaleBundle.bundleString("Generating test code"));
			Thread codeGenerationThread = new Thread(new CodeGenerationThread(progressDialog));
			codeGenerationThread.start();
			progressDialog.setVisible(true);
		}
	}

	class CodeGenerationThread implements Runnable {
		private ProgressDialog progressDialog;
		
		CodeGenerationThread(ProgressDialog progressDialog) {
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				TransitionTree tree = treePanel.getTestTree().getTransitionTree();
//				long start= System.currentTimeMillis();
				File testCodeFile = getTestCodeFile(tree);
				TestCodeGenerator codeGenerator = TestCodeGenerator.createCodeGenerator(tree);
				codeGenerator.saveTestSuiteCode(testCodeFile);
//				long end = System.currentTimeMillis();
//				double codeGenerationTime = (end-start)/1000.0;
				if (kernel.getSystemOptions().getLanguage()==TargetLanguage.UFT)
 					kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Test code generation completed"));	
				else {
//					String codeGenerationInfo = "\n"+LocaleBundle.bundleString("Time for test code generation")+": "+codeGenerationTime+" "+LocaleBundle.bundleString("seconds")+".\n";
					String codeGenerationInfo = "";
					codeGenerationInfo += LocaleBundle.bundleString("Test code file")+": "+testCodeFile.getAbsolutePath();
					kernel.printToConsole(codeGenerationInfo);
				}
				if (tree.getSystemOptions().viewTestCode() && kernel.getSystemOptions().getLanguage()!=TargetLanguage.UFT){
					progressDialog.setMessage(LocaleBundle.bundleString("Presenting test code"));
					kernel.getFileManager().getEditor().createAndSetTestCodePane(testCodeFile);
				}
				else{
					Toolkit.getDefaultToolkit().beep();
				}
				progressDialog.dispose();
				limitationCheck(tree.getAllTests().size());
			}
			catch (IOException e){
				progressDialog.dispose();				
				kernel.getFileManager().getEditor().printInConsoleArea(e.toString(), false);
			}
			catch (CancellationException e){
				progressDialog.dispose();				
				kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Code generation canceled"), false);
			}
			kernel.setMenuAndToolBarEnabled(true);
		}
	}
	
	private void limitationCheck(int testCount){
		if (Kernel.IS_LIMITATION_SET && testCount>Kernel.MAX_TESTS_FOR_LIMITATION) {
			JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("Total tests")+": "+testCount+"\n"+LocaleBundle.bundleString("The evaluation version only generates test code for")+" "+Kernel.MAX_TESTS_FOR_LIMITATION+".");					
		}
	}

	public void generateTestTree() {
		generateTree(true);			// generate tree, not code (tree is shown)
	}
	
	private void generateTree(boolean displayTree) {
		TransitionTree tree = openFileForTestGeneration();
		if (tree!=null){
			ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Test Generation"), LocaleBundle.bundleString("Generating test tree"));
			Thread treeGenerationThread = new Thread(new TreeGenerationThread(tree, progressDialog, displayTree));
			treeGenerationThread.start();
			progressDialog.setVisible(true);
		}
	}
	
	class TreeGenerationThread implements Runnable {
		
		private TransitionTree transitionTree;
		private ProgressDialog progressDialog;
		private boolean displayTree;
		
		TreeGenerationThread(TransitionTree tree, ProgressDialog progressDialog, boolean displayTree) {
			this.transitionTree = tree;
			this.progressDialog = progressDialog;
			this.displayTree = displayTree;
		}
		
		public void run () {
			long generationStart= System.currentTimeMillis();
			try {
				transitionTree.setProgressDialog(progressDialog);
				transitionTree.generateTransitionTree();
			} 
			catch (CancellationException e){
				kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Test generation canceled"), false);
				progressDialog.dispose();
				kernel.setMenuAndToolBarEnabled(true);
				return;
			}
//			long generationEnd = System.currentTimeMillis();
//			double treeGenerationTime = (generationEnd-generationStart)/1000.0;
			if (!transitionTree.getRoot().hasChildren()) {
				if (transitionTree.getSystemOptions().getCoverageCriterion()==SystemOptions.DeadlockStateCoverage)
					progressDialog.finishDialog(LocaleBundle.bundleString("No deadlock state found"));
				else
				if (transitionTree.getSystemOptions().getCoverageCriterion()==SystemOptions.CounterExampleCoverage)
					progressDialog.finishDialog(LocaleBundle.bundleString("No counterexample found"));
				else
					progressDialog.finishDialog(LocaleBundle.bundleString("No tests generated"));
				kernel.setMenuAndToolBarEnabled(true);
				return;
			}
			if (displayTree) {		// generate and display tree
				String info = "";
				if (transitionTree instanceof TransitionTreeFromSequences)
					info += ((TransitionTreeFromSequences)transitionTree).getTestGenerationMessage();
				else {
					String searchStrategy = kernel.getSystemOptions().isBreadthFirstSearch()? LocaleBundle.bundleString("breadth first"):LocaleBundle.bundleString("depth first");
					info += LocaleBundle.bundleString("Search strategy")+": " + searchStrategy 
						+ "; "+LocaleBundle.bundleString("Maximum search depth")+" "+kernel.getSystemOptions().getSearchDepth();
				}
				long generationEnd = System.currentTimeMillis();
				double treeGenerationTime = (generationEnd-generationStart)/1000.0;
				info+="\n"+LocaleBundle.bundleString("Time for test tree generation")+": "+ treeGenerationTime+" "+LocaleBundle.bundleString("seconds")+".";
				info+=transitionTree.getStatisticsString();
				try {
					progressDialog.setMessage(LocaleBundle.bundleString("VISUALIZING_TEST_TREE"));
					visualizeTree(transitionTree, info, progressDialog);
					progressDialog.dispose();
//					presentTestTreeForEditing(info, transitionTree);
					setTreeSaved(true);
				}
				catch (CancellationException e){
					kernel.getFileManager().getEditor().printInConsoleArea(info+"\n"+LocaleBundle.bundleString("TEST_VISUALIZATION_CANCELED"), false);
					progressDialog.dispose();
					kernel.setMenuAndToolBarEnabled(true);
					return;
				}
			}
			else {		// save test code to file
				try {
					progressDialog.setMessage(LocaleBundle.bundleString("Generating test code"));
//					long start= System.currentTimeMillis();
					File testCodeFile = getTestCodeFile(transitionTree);
					TestCodeGenerator codeGenerator = TestCodeGenerator.createCodeGenerator(transitionTree);
					codeGenerator.saveTestSuiteCode(testCodeFile);
					double codeGenerationTime = (System.currentTimeMillis()-generationStart)/1000.0;
					kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Time for test code generation")+": "+codeGenerationTime+" "+LocaleBundle.bundleString("seconds")+".\n", false);
 					if (kernel.getSystemOptions().getLanguage()==TargetLanguage.UFT)
 						kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Test code generation completed"));	
 					else
 						kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Test code file")+": "+testCodeFile.getAbsolutePath(), false);	
					boolean viewTestCode = transitionTree.getSystemOptions().viewTestCode();
					int testCount = transitionTree.getAllTests().size();
					transitionTree = null;  
					if (viewTestCode && kernel.getSystemOptions().getLanguage()!=TargetLanguage.UFT){
						progressDialog.setMessage(LocaleBundle.bundleString("Presenting test code"));
						kernel.getFileManager().getEditor().createAndSetTestCodePane(testCodeFile);
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
					progressDialog.dispose();
					limitationCheck(testCount);
					kernel.setMenuAndToolBarEnabled(true);
				}
				catch (IOException e) {
					kernel.getFileManager().getEditor().printInConsoleArea(e.toString(), false);
					progressDialog.dispose();
				}
				catch (CancellationException e){
					kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Code generation canceled"), false);
					progressDialog.dispose();
				}
			}	
		}
	}
	
	private File getTestCodeFile(TransitionTree tree) throws IOException {
		String currentDir =  kernel.getFileChooser().getCurrentDirectory().getCanonicalPath();
		String outputFilePath = currentDir + File.separator + FileUtil.getTestCodeFileName(tree);
		return new File(outputFilePath);
	}
	
	private TransitionTree openFileForTestGeneration(){
		MID mid = kernel.getFileManager().parse();
		if (mid==null)
			return null;
//System.out.println(mid);		
		ModelType modelType = kernel.getFileManager().getEditor().getModelType();
		if (Kernel.IS_LIMITATION_SET && mid.getTransitions().size()>Kernel.MAX_TRANSITIONS_FOR_LIMITATION){
			JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("The model is too complex"));
			return null;
		}
		if (modelType!=ModelType.THREATTREE && mid.getInitialMarkings().size()==0) {
			kernel.printDialogMessage(LocaleBundle.bundleString("No initial state is specified"));
			return null;
		}
		if (mid.getGoalProperties().size()==0 && kernel.getSystemOptions().getCoverageCriterion()==SystemOptions.GoalCoverage) {
			kernel.printDialogMessage(LocaleBundle.bundleString("No goal state is specified"));
			return null;
		}
		if (mid.getAssertionProperties().size()==0 && kernel.getSystemOptions().getCoverageCriterion()==SystemOptions.CounterExampleCoverage) {
			kernel.printDialogMessage(LocaleBundle.bundleString("NO_ASSERTION_IS_SPECIFIED"));
			return null;
		}
		if (kernel.getSystemOptions().getCoverageCriterion()==SystemOptions.SequenceCoverage)
			return parseTestSequenceSpecification(mid);
		else
			return kernel.getSystemOptions().createTransitionTree(mid, modelType);
	}
	
	private TransitionTree parseTestSequenceSpecification(MID mid){
		String sequenceFileName = mid.getSequencesFile();
		if (sequenceFileName==null){
			kernel.printDialogMessage(LocaleBundle.bundleString("Specify a sequences file"));
			return null;
		}
		if (sequenceFileName.indexOf(File.separatorChar)<0){
			String midDir = new File(mid.getFileName()).getParent();
			sequenceFileName = midDir+File.separator+sequenceFileName;
		}
		File sequenceFile = new File(sequenceFileName);
		if (!sequenceFile.exists()){
			kernel.printDialogMessage(sequenceFile.getAbsolutePath()+" "+LocaleBundle.bundleString("is not found"));
			return null;
		}
		try {
			ArrayList<UserDefinedSequences> allSequences = MIDParser.parseUserDefinedTestSequences(FileUtil.readTextFile(sequenceFile), mid);
			assert allSequences.size() == mid.getInitialMarkings().size();
			boolean hasSequences = false;
			for (int index=0; index<allSequences.size(); index++){
				if (allSequences.get(index).hasSequences())
					hasSequences = true; 
//				System.out.println("\nInit state "+mid.getInitialMarkings().get(index)+allSequences.get(index));
			}
			if (!hasSequences){
				kernel.printDialogMessage(LocaleBundle.bundleString("No sequences found in the given file"));
				return null;
			}
			return new TransitionTreeFromUserDefinedSequences(mid, kernel.getSystemOptions(), allSequences);
		}
		catch (ParseException e) {
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
			kernel.printDialogMessage(e.toString().replace("parser.ParseException:", ""));
		}
		return null; 
	} 
	
	////////////////////////////////////////////////////////////////////
	private void showTestSequences() {
		if (treePanel.getTestTree()!=null){
			ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("SHOW_TEST_SEQUENCES"), LocaleBundle.bundleString("CREATING_TEST_SEQUENCES"));
			Thread showTestSequencesThread = new Thread(new ShowTestSequencesThread(treePanel, progressDialog));
			showTestSequencesThread.start();
			progressDialog.setVisible(true);
		}
	}
	
	class ShowTestSequencesThread implements Runnable {
		
		private TestTreePanel treePanel;
		private ProgressDialog progressDialog;
		
		ShowTestSequencesThread(TestTreePanel treePanel, ProgressDialog progressDialog) {
			this.treePanel = treePanel;
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				TransitionTree transitionTree = treePanel.getTestTree().getTransitionTree();
				transitionTree.setProgressDialog(progressDialog);
				kernel.printToConsole(LocaleBundle.bundleString("Model level tests"));
				ArrayList<TransitionTreeNode> allLeaves = transitionTree.getAllTests();
				String initStateIndex = "1";
				int sequenceNo =1;
				for (TransitionTreeNode leaf : allLeaves) {
						ArrayList<TransitionTreeNode> testSequence = transitionTree.getTestSequence(leaf);
						if (!testSequence.get(0).getOutlineNumber().equals(initStateIndex))
							kernel.printToConsole("\n"+LocaleBundle.bundleString("Initial State")+":"+testSequence.get(0).getOutlineNumber());		
						initStateIndex = testSequence.get(0).getOutlineNumber();
						kernel.printToConsole(sequenceNo+". "+transitionTree.getSequenceString(testSequence));
						if (transitionTree.getSystemOptions().showStatesInNodes())
							kernel.printToConsole(LocaleBundle.bundleString("Resultant State")+": "+leaf.getMarking());
						sequenceNo++;
				}
				kernel.printToConsole("");
				progressDialog.dispose();
			} 
			catch (CancellationException e){
				progressDialog.dispose();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////
	public boolean saveTree() {
		assert treePanel.getTestTree()!=null;
		TransitionTree tree = treePanel.getTestTree().getTransitionTree();
		assert tree!=null;
		JFileChooser fc = kernel.getFileChooser();
		fc.setSelectedFile(new File(FileUtil.getTestDataFileName(tree)));
		File file = FileUtil.chooseFile(kernel.getParentFrame(), fc, LocaleBundle.bundleString("Save Test Data"), new FileExtensionFilter(FileUtil.TestDataFileExtension), FileUtil.SAVEFILE);
		if (file == null)
			return false;
		if (file.isDirectory())
			file = new File(file.getAbsolutePath()+ File.separator + FileUtil.getTestDataFileName(tree));
		else if (!file.getName().endsWith("."+FileUtil.TestDataFileExtension))
			file = new File(file.getParent() + File.separator +file.getName()+"."+FileUtil.TestDataFileExtension);
		Boolean saved = saveTree(file);
		if (saved) {
			kernel.printToConsole(tree.getStatisticsString());
			kernel.printDialogMessage(LocaleBundle.bundleString("Test data have been saved"));
		}
		return saved;
	}

    private boolean isTreeSavedSuccessfully=true;

	private boolean saveTree(File file) {
		if (file == null) 
			return false;
		final ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Save Test Data"), LocaleBundle.bundleString("Saving test data"), ProgressDialog.CANCELLATION_NOT_ALLOWED);
        isTreeSavedSuccessfully=false;
		final SaveTreeTask task = new SaveTreeTask(file, progressDialog);
        task.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ("progress".equals(e.getPropertyName())) {
                    progressDialog.getProgressBar().setValue((Integer) e.getNewValue());
                }
            }
        });
        task.execute();
        progressDialog.setVisible(true);
//        System.out.println("Done: "+result);
 		return isTreeSavedSuccessfully;
	}

	class SaveTreeTask extends SwingWorker<Boolean, Void> {
		private File file;
		private ProgressDialog progressDialog;
		
		SaveTreeTask(File file, ProgressDialog progressDialog){
			this.file = file;
			this.progressDialog=progressDialog;
		}
	     @Override
	     public Boolean doInBackground() {
				TransitionTree tree = treePanel.getTestTree().getTransitionTree();
			try {
				TestTreeFile.saveTestDataToExcelFile(tree, file);
				setTreeSaved(true);
				setProgress(100);
//				Thread.sleep(5000);
				return Boolean.TRUE;
			} catch ( CancellationException e) {
				return Boolean.FALSE;
			} catch (IOException e) {
				if (Kernel.IS_DEBUGGING_MODE)
					e.printStackTrace();
				kernel.printDialogMessage(LocaleBundle.bundleString("Fail to save test data")+"\n" + e.getMessage());
				return Boolean.FALSE;
			} catch (Exception e) {
				return Boolean.FALSE;
			}
	     
	     }

       public void done() {
    	   try {
    		   isTreeSavedSuccessfully = get()==Boolean.TRUE? true: false;
    	   }
    	   catch (Exception ex){
    	   }
    	   progressDialog.dispose();
    	   Toolkit.getDefaultToolkit().beep();
       }

	}
	
	public boolean saveTestTreeDialog(){
		if (!isTreeSaved()) {
			int choice = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
				LocaleBundle.bundleString("Save tests"), LocaleBundle.bundleString("Confirm Save"),
				JOptionPane.YES_NO_CANCEL_OPTION);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			else if (choice == JOptionPane.YES_OPTION) {
				if (!saveTree())
					return false;
			}
		}
		return true;
	}
	
	/////////////////////////////////////////////////////////////////////////
	// return false if 
	// the tree is imported from test data (no need to close it), or
	// the user has canceled the "save" action
	// for program use
	public boolean closeTree() {
		assert treePanel!=null && treePanel.getTestTree()!=null;
		TransitionTree tree = treePanel.getTestTree().getTransitionTree();
		if (!tree.isTreeGenerated()){
			kernel.printDialogMessage(LocaleBundle.bundleString("No need to close an imported tree"));
			return false;
		}
		if (!saveTestTreeDialog())
			return false;
		cleanUp();
		return true;
	}

	/////////////////////////////////////////////////////////////////////////
	// return false only if the user has canceled the action
	// for menu item selection
	private void closeTreeMenuItem() {
		if (closeTree()){
			kernel.getFileManager().getEditor().removeTestTreeComponent();
		}
	}


	public boolean hasSelectedNode() {
		return getSelectedNode()!=null;
	}

	private TransitionTreeNode getSelectedNode(){
		TestJTree testTree = treePanel.getTestTree();
		if (testTree==null)
			return null;
		DefaultMutableTreeNode selectedMutableNode = (DefaultMutableTreeNode) testTree.getLastSelectedPathComponent();
		if (selectedMutableNode == null)
			return null;
		TransitionTreeNode node = (TransitionTreeNode) selectedMutableNode.getUserObject();
		if (node.isRoot()) {
			return null;
		}
		return node;
	}

	/////////////////////////////////////////////////////////////////////////
	public void addTreeNode(){
		
	}
	
	/////////////////////////////////////////////////////////////////////////
	public void deleteTreeNode() {
		TestJTree testTree = treePanel.getTestTree();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) testTree
				.getLastSelectedPathComponent();
		if (node == null || ((TransitionTreeNode) (node.getUserObject())).isRoot()) {
			JOptionPane.showMessageDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Please choose a node"));
			return;
		}
		String warning = (!node.isLeaf())?
			LocaleBundle.bundleString("Are you sure to remove it"):
				LocaleBundle.bundleString("Want to remove this node");
		int choice = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
				warning, LocaleBundle.bundleString("Confirm Remove"),
				JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			removeTestTreeNode(node, treePanel.getTestTree());
			setTreeSaved(false);
		}
	}
	
	public void removeTestTreeNode(DefaultMutableTreeNode node, TestJTree testTree) {
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) (node.getParent());	
		MutableTreeNode toBeSelNode = getSibling(node);
		// if there are no siblings select the parent node after
		// removing the node	
		if (toBeSelNode == null) {
			toBeSelNode = parentNode;
		}
		if (parentNode != null) {
			((DefaultTreeModel) testTree.getModel()).removeNodeFromParent(node);
		}
		TransitionTreeNode testNode = ((TransitionTreeNode) node.getUserObject());		
		TransitionTreeNode testParent = testNode.getParent();
		testParent.removeNodeFromChildren(testNode);
		testParent.resetChildrenOutlineNumbers(testTree.getTransitionTree().getSystemOptions().getMaxIdDepth());
		// should enforce garbage collection???
		testNode.setParent(null);
		testNode =  null;
		setNewVisiblePath(testTree, (DefaultMutableTreeNode) toBeSelNode);
	}

	/////////////////////////////////////////////////////////////////////////
	public void cloneSubtree() {
		TestJTree testTree = treePanel.getTestTree();
		DefaultMutableTreeNode selectedMutableNode = (DefaultMutableTreeNode) testTree
		.getLastSelectedPathComponent();
		if (selectedMutableNode == null)
			return;
		TransitionTreeNode selectedUserNode = (TransitionTreeNode) selectedMutableNode.getUserObject();
		if (selectedUserNode.isRoot()) {
			kernel.printDialogMessage(LocaleBundle.bundleString("Please choose a node"));
			return;			
		} 
		DefaultMutableTreeNode parentMutableNode = (DefaultMutableTreeNode) (selectedMutableNode
				.getParent());
		TransitionTreeNode parentUserNode = (TransitionTreeNode) parentMutableNode.getUserObject();
		int index = parentUserNode.getChildNodePosition(selectedUserNode)+1;
		TransitionTreeNode newNode = selectedUserNode.clone();
		if (newNode==null){
			kernel.printDialogMessage(LocaleBundle.bundleString("failed to clone node"));
			return;			
		}
		newNode.setParent(parentUserNode);
		parentUserNode.insert(index, newNode);
		parentUserNode.resetChildrenOutlineNumbers(testTree.getTransitionTree().getSystemOptions().getMaxIdDepth());

		DefaultMutableTreeNode newMutableNode = newNode.setToMutableNode();
		((DefaultTreeModel) testTree.getModel()).insertNodeInto(newMutableNode,
				parentMutableNode, index);
		testTree.expandAllPaths(new TreePath(parentMutableNode), 0, true);
		setNewVisiblePath(testTree, newMutableNode);
		setTreeSaved(false);
	}

	public void insertTestTreeNode(DefaultMutableTreeNode node, DefaultMutableTreeNode parent, int index, TestJTree testTree) {
		TransitionTreeNode parentUserNode = (TransitionTreeNode) parent.getUserObject();
		TransitionTreeNode newNode = (TransitionTreeNode) node.getUserObject();
		newNode.setParent(parentUserNode);
		parentUserNode.insert(index, newNode);
		parentUserNode.resetChildrenOutlineNumbers(testTree.getTransitionTree().getSystemOptions().getMaxIdDepth());
		DefaultMutableTreeNode newMutableNode = newNode.setToMutableNode();
		((DefaultTreeModel) testTree.getModel()).insertNodeInto(newMutableNode,
				parent, index);
		testTree.expandAllPaths(new TreePath(parent), 0, true);
		setNewVisiblePath(testTree, newMutableNode);
		setTreeSaved(false);
	}

	/////////////////////////////////////////////////////////////////////////
	public void moveTreeNode() {
		TestJTree testTree = treePanel.getTestTree();
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) testTree.getLastSelectedPathComponent();
		if (currentNode == null || ((TransitionTreeNode) (currentNode.getUserObject())).isRoot()) {
			kernel.printDialogMessage(LocaleBundle.bundleString("Please choose a node"));
			return;
		}
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();
		int siblingCount = getSiblingCount(); 
		String input = JOptionPane.showInputDialog(LocaleBundle.bundleString("Sibling Number")+" (1-"+siblingCount+"):");
		if (input!=null) {
			try {
				int newIndex = Integer.parseInt(input);
				if (newIndex<1 || newIndex>siblingCount) {
					kernel.printDialogMessage(LocaleBundle.bundleString("Wrong sibling number"));
					return;
				}
				int currentIndex = parentNode.getIndex(currentNode)+1;
				if (newIndex == currentIndex) {
					kernel.printDialogMessage(LocaleBundle.bundleString("Choose a sibling"));
					return;
				}
				removeTestTreeNode(currentNode, testTree);
				insertTestTreeNode(currentNode, parentNode, newIndex-1, testTree);
			}
			catch (Exception e) {
				kernel.printDialogMessage(LocaleBundle.bundleString("Wrong sibling number"));
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	public void expandNode() {
		TestJTree testTree = treePanel.getTestTree();
		DefaultMutableTreeNode selectedMutableNode = (DefaultMutableTreeNode) testTree.getLastSelectedPathComponent();
		if (selectedMutableNode == null)
			return;
		TransitionTreeNode selectedUserNode = (TransitionTreeNode) selectedMutableNode.getUserObject();
		if (selectedUserNode.isRoot()) {
			kernel.printDialogMessage(LocaleBundle.bundleString("Please choose a node"));
			return;			
		} 
		DefaultMutableTreeNode parentMutableNode = (DefaultMutableTreeNode) (selectedMutableNode.getParent());
		testTree.expandAllPaths(new TreePath(parentMutableNode), 0, true);
	}

	public int getSiblingCount() {
		TestJTree testTree = treePanel.getTestTree();
		if (testTree==null)
			return 0;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) testTree
				.getLastSelectedPathComponent();
		if (node == null || ((TransitionTreeNode) (node.getUserObject())).isRoot()) {
			return 0;
		}
		return node.getSiblingCount();
	}

	public boolean hasSiblingNode() {
		return getSiblingCount()>1;
	}

	private MutableTreeNode getSibling(DefaultMutableTreeNode selNode) {
		if (selNode != null) {
			MutableTreeNode sibling = (MutableTreeNode) selNode
					.getPreviousSibling();
			if (sibling == null) {
				sibling = (MutableTreeNode) selNode.getNextSibling();
			}
			if (sibling == null) {
				sibling = getSibling((DefaultMutableTreeNode) (MutableTreeNode) selNode
						.getParent());
			}
			return sibling;
		} else {
			return null;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	private void setNewVisiblePath(TestJTree testTree, DefaultMutableTreeNode newMutableNode){
		TreeNode[] nodes = ((DefaultTreeModel) testTree.getModel())
				.getPathToRoot(newMutableNode);
		TreePath path = new TreePath(nodes);
		testTree.scrollPathToVisible(path);
		// select the newly added node
		testTree.setSelectionPath(path);
		//Make the newly added node editable
		testTree.startEditingAtPath(path);
	}
	
	public void visualizeTree(TransitionTree transitionTree, String info, ProgressDialog progressDialog) throws CancellationException{
		DisplayStatesInTestTree = transitionTree.getSystemOptions().showStatesInNodes();
//		long visualizationStart= System.currentTimeMillis();		
		TestJTree testTree = new TestJTree(transitionTree, progressDialog);		// time-consuming when the tree is large 
		testTree.setName(transitionTree.getMID().getSystemName());	
		testTree.addMouseListener( new MouseAdapter() { 
			public void mousePressed( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			} 
			public void mouseReleased( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			}
			private void checkForTriggerEvent( MouseEvent e ) { 
				kernel.enableTestTreeEditMenuItems(hasSelectedNode());
				if ( e.isPopupTrigger() ) { 
					popupMenu.show( e.getComponent(), e.getX(), e.getY() );
					deleteNodeItem.setEnabled(hasSelectedNode());
					cloneSubtreeItem.setEnabled(hasSelectedNode());
//					expandNodeItem.setEnabled(hasSelectedNode());
					swapNodeItem.setEnabled(hasSiblingNode());
					closeTreeItem.setEnabled(!FileUtil.isTestDataFile(kernel.getFileManager().getWorkingFile()));
				}
			}
		});
		treePanel.setTestTree(testTree);
		kernel.printToConsole(info);
		kernel.getFileManager().getEditor().setTestTreeComponent(treePanel);				
//		long visualizationEnd= System.currentTimeMillis();
//		treePanel.appendTestInfo("\n"+LocaleBundle.bundleString("Time for test tree visualization")+": "+((visualizationEnd-visualizationStart)/1000.0)+" "+LocaleBundle.bundleString("seconds")+".\n");
		progressDialog.dispose();
		kernel.setMenuAndToolBarEnabled(true);
	}

	public void cleanUp() {
		setTreeSaved(true);
		treePanel.cleanUp();
	}
	
	public boolean isTreeSaved() {
		return treePanel.isTreeSaved();
	}
	
	public void setTreeSaved(boolean saved) {
		treePanel.setTreeSaved(saved);
	}
	
	public void analyzeTests(){
		MID mid = kernel.getFileManager().parse();
		if (mid!=null){
			new OnTheFlyTestingAnalyzer(kernel.getFileManager().getEditor(), mid);
		}
	}

	private JMenuItem createPopupMenuItem(String title, String command){
		JMenuItem menuItem = popupMenu.add(title);
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}
	
	private void createPopupMenu() {
		popupMenu = new JPopupMenu();
//		addNodeItem = createPopupMenuItem(LocaleBundle.getBundleString("Add Child Node"), Commands.AddTreeNode);
		deleteNodeItem = createPopupMenuItem(LocaleBundle.bundleString("Delete Node"), Commands.DeleteTreeNode);
		cloneSubtreeItem = createPopupMenuItem(LocaleBundle.bundleString("Clone Node"), Commands.CloneTreeNode);
//		editNodeItem = createPopupMenuItem(LocaleBundle.getBundleString("Modify Node"), Commands.EditTreeNode);
		swapNodeItem = createPopupMenuItem(LocaleBundle.bundleString("Move Node"), Commands.MoveTreeNode);
//		expandNodeItem = createPopupMenuItem(LocaleBundle.bundleString("Expand Node"), Commands.EditTreeNode);
		popupMenu.addSeparator();
//		createPopupMenuItem(LocaleBundle.getBundleString("Generate Test Code"), Commands.GenerateTestCode);
		createPopupMenuItem(LocaleBundle.bundleString("Save Test Tree"), Commands.SaveTree);
		closeTreeItem = createPopupMenuItem(LocaleBundle.bundleString("Close Test Tree"), Commands.CloseTree);
	}

}
