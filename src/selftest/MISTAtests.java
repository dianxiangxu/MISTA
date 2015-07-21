package selftest;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import edit.GeneralEditor;

import main.MISTA;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import kernel.Commands;

public class MISTAtests extends TestCase {
	
	public static Test suite() {
		return new TestSuite(MISTAtests.class);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}
	
	private MISTA MISTA; 
	   
	protected void setUp() throws Exception {
		super.setUp();
		MISTA.setLookAndFeel();
		MISTA = new MISTA();
		MISTA.pack();
		MISTA.setVisible(true);		
	}

	protected void tearDown() throws Exception {
	    super.tearDown();
		MISTA.dispose();
	}
		  
	public void test1(){
	   File file = new File("examples//java//Blocks//test//BlocksNet.xmid");
	   MISTA.getFileManager().openFile(file);
	   assert MISTA.getFileManager().parse()!=null: "Failed to parse!";
	   verify();
	   generateTree();
	   generateCode();
	   closeTree();
	}
	
	public void test2() {
		File file = new File("examples//java//CruiseControl//CarSimulator.xmid");
		MISTA.getFileManager().openFile(file);
		assert MISTA.getFileManager().parse() != null : "Failed to parse!";
		verify();
		generateTree();
		generateCode();
		closeTree();
	}

	public void test3() {
		File file = new File("examples//java//CruiseControl//CruiseControllerNet.xmid");
		MISTA.getFileManager().openFile(file);
		assert MISTA.getFileManager().parse() != null : "Failed to parse!";
		verify();
		generateTree();
		generateCode();
		closeTree();
	}

	private void verify(){
		setTabToMID();
		JMenuItem item = (JMenuItem)ComponentFinder.findComponent(MISTA, new JMenuItem(), Commands.IntegratedGoalReachabilityAnalysis);
		doClickAndWait(item, 3000);		
	}
	
	private void generateTree(){
		setTabToMID();
	   JMenuItem item = (JMenuItem)ComponentFinder.findComponent(MISTA, Commands.GenerateTree);
	   doClickAndWait(item, 3000);
	}

	private void generateCode(){
		setTabToMID();
	   JMenuItem item = (JMenuItem)ComponentFinder.findComponent(MISTA, new JMenuItem(), Commands.GenerateTestCode);
	   doClickAndWait(item, 3000);
	}

	private void closeTree(){
		setTabToTree();
		MISTA.getTestingManager().setTreeSaved(true);
		JMenuItem item = (JMenuItem)ComponentFinder.findComponent(MISTA, Commands.CloseTree);
		doClickAndWait(item, 1000);
	}

	private void doClickAndWait(AbstractButton item, long waitTime){
	   if (item==null) {
		   System.out.println("GUI component not found. ");
		   System.exit(1);
	   }
//	   System.out.println("GUI component name: "+item.getName());
	   item.doClick(1000);
	   try { Thread.sleep(waitTime); }
	   catch (Exception e) {}
	}
	
	private void setTabToMID(){
		GeneralEditor editor = MISTA.getFileManager().getEditor();
		if ( editor.getEditingJComponent() instanceof JTabbedPane){
			JTabbedPane editTabbedPane = (JTabbedPane) editor.getEditingJComponent();
			editTabbedPane.setSelectedIndex(0);
		}
	}
	
	private void setTabToTree(){
		GeneralEditor editor = MISTA.getFileManager().getEditor();
		assert editor.getEditingJComponent() instanceof JTabbedPane;
		assert editor.getTestTreeComponent()!=null;
		JTabbedPane editTabbedPane = (JTabbedPane) editor.getEditingJComponent();
		editTabbedPane.setSelectedComponent(editor.getTestTreeComponent());
	}
}
