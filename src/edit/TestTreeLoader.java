/* 	
	Author Dianxiang Xu
*/
package edit;

import java.io.File;

import kernel.CancellationException;
import kernel.Kernel;
import kernel.ProgressDialog;
import locales.LocaleBundle;
import mid.MID;

import org.apache.poi.ss.usermodel.Sheet;

import testgeneration.TransitionTree;

class TestTreeLoader implements Runnable {
	
	private Kernel kernel;
	private ProgressDialog progressDialog;
	private TransitionTree tree;
	private File testDataFile;
	private File midFile;
	private MID mid;
	private Sheet sheet;
	
	TestTreeLoader(Kernel kernel, ProgressDialog progressDialog, TransitionTree tree, File testDataFile, File midFile, MID mid, Sheet sheet) {
		this.kernel = kernel;
		this.progressDialog = progressDialog;
		this.tree = tree;
		this.testDataFile = testDataFile;
		this.midFile = midFile;
		this.mid = mid;
		this.sheet = sheet;
	}
	
	public void run ()  {
		try {
			tree.setProgressDialog(progressDialog);
			TestTreeFile.loadAllNodes(progressDialog, tree, mid, sheet);	
			progressDialog.setMessage(LocaleBundle.bundleString("VISUALIZING_TEST_TREE"));
			kernel.getTestingManager().visualizeTree(tree, LocaleBundle.bundleString("Imported tree")+tree.getStatisticsString(), progressDialog);
			kernel.getTestingManager().setTreeSaved(true);
			progressDialog.dispose();
			kernel.getFileManager().updateAfterFileLoading(kernel, testDataFile.getAbsolutePath());
		}
		catch (CancellationException e){
			handleException();
		}
		catch (Exception e){
			handleException();
		}
	}
	
	private void handleException(){
		progressDialog.dispose();
		kernel.getFileManager().updateAfterFileLoading(kernel, midFile.getAbsolutePath());
		if (kernel.getTestingManager()!=null)
			kernel.getTestingManager().cleanUp(); // close the existing tree if any
		kernel.printDialogMessage(LocaleBundle.bundleString("Loading test data is canceled"));
	}
}
