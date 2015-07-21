/* 	
	Author Dianxiang Xu
*/
package kernel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cpn.CPNConverter;

import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

import edit.EditingPreferences;
import edit.GeneralEditor;
import edit.GeneralEditor.SimulatorType;
import edit.TestTreeFile;
import edit.TextEditor;
import edit.XMIDEditor;

import mid.MID;
import mid.TupleFactory;

import utilities.FileExtensionFilter;
import utilities.FileUtil;
import utilities.FilterForCPNFiles;
import utilities.FilterForOpenFile;
import utilities.FilterForPNMLFiles;

public class FileManager implements ActionListener{
	private String defaultFileName = "NewFile.";

	private static final boolean NODIALOG = false;
//	private static final boolean NEEDDIALOG = true;
	
	private static final boolean EDITABLE = true;
	private static final boolean READONLY = false;
		
	private Kernel kernel;

	protected boolean lineWrap = true;

	private GeneralEditor editor;
	private String workingFile;
	
	public FileManager(Kernel kernel) {
		this.kernel = kernel;
		workingFile = defaultFileName + FileUtil.XMIDFileExtension;
		editor = new XMIDEditor(kernel, new File(workingFile), SystemOptions.DEFAULT_MODEL_TYPE);
	} 
	
	public GeneralEditor getEditor(){
		return editor;
	}

	public void setEditor(GeneralEditor newEditor){
		this.editor = newEditor;
	}

	public void setTextFont(Font newFont){
		if (editor!=null)
			editor.setTextFont(newFont);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd != Commands.EXIT && editor!=null && editor instanceof XMIDEditor && ((XMIDEditor)editor).isSimulationMode()){
			if (cmd != Commands.SIMULATION)
				kernel.printDialogMessage(LocaleBundle.bundleString("Simulation mode"));
			return;
		}	
    	if (cmd == Commands.EXIT) {
			quit();
		} else		
		if (SystemOptions.isModelTypeKeyword(cmd)) {
			newFile(cmd);
		} else		
		if (cmd == Commands.OPEN) {
			openFile();
		} else		
		if (cmd == Commands.SAVE) {
			saveFile();
		} else		
		if (cmd == Commands.SAVEAS) {
			saveAsFile();
		} else		
		if (cmd == Commands.REFRESH) {
			refresh();
		} else		
		if (cmd == Commands.IMPORTPNMLFILE){
			importPNMLFile();
		}else
		if (cmd == Commands.IMPORTCPNFILE){
			importCPNFile();
		}else
		if (cmd == Commands.CLEARFILELIST) {
			kernel.clearRecentFilesMenu();
		} else		
	    if (cmd == Commands.PARSE) {
			MID mid = parse();
			if (mid!=null && editor!=null)
				editor.printInConsoleArea(LocaleBundle.bundleString("No syntax errors found"));
		} else		
		if (cmd == Commands.SIMULATION) {
			editor.startSimulator(SimulatorType.MODEL_SIMULATION);
		} else		
		if (cmd == Commands.EDITINGPREFERENCES) {
			new EditingPreferences(kernel);
		} else
		if (cmd == Commands.SaveTestCodeAs) {
			editor.saveTestCode();
		} else {
			File xmidFile = new File(cmd);
			if (xmidFile.exists()){ // selected a recent file
				if (saveChanges())
					openFile(xmidFile);
			} else
				kernel.printDialogMessage("Wrong operation! ");
		}
	}
	
	public void newFile(String cmd) {
		if (saveChanges())
			editNewFile(SystemOptions.getModelType(cmd));
	}
	
	public void editNewFile(ModelType modelType){
//		if (editor!=null && editor instanceof XMIDEditor)
//			((XMIDEditor)editor).terminateSimulator();
		workingFile = defaultFileName + FileUtil.XMIDFileExtension;
		editor = new XMIDEditor(kernel, new File(workingFile), modelType);
		kernel.updateContentPane();
		kernel.setMenuAndToolBarEnabled(true);		
		if (kernel.getTestingManager()!=null){
			kernel.getTestingManager().cleanUp(); // close the existing tree if any
		}
		updateSystemTitle();
	}
	
	public void openFile() {
		if (saveChanges()) {
			JFileChooser fc = kernel.getFileChooser();
			if (workingFile!=null)
				fc.setSelectedFile(new File(workingFile));
			File file = FileUtil.chooseFile(kernel.getParentFrame(), fc, LocaleBundle.bundleString("Open file"),new FilterForOpenFile(), FileUtil.OPENFILE);
			if (file!=null){
				if (FileUtil.isXMLFile(file.getName())){
					File targetMIDFile = new File (file.getParent()+File.separator+FileUtil.getTargetMIDFileName(file));
					if (targetMIDFile.exists())
						file = targetMIDFile;
//					else {
//						kernel.printDialogMessage(LocaleBundle.bundleString("Invalid input file"));
//						return;
//					}
				}
				openFile(file);
			}
		}
	}
		
	public void openFile(File file){	
		if (file!=null && file.exists() && !file.isDirectory())
			readFileIntoEditArea(file.getAbsolutePath());
	}

	public void startEditor(String mostRecentFileName){	
		if (mostRecentFileName!=null){
			File mostRecentFile = new File(mostRecentFileName);
			if (mostRecentFile!=null && mostRecentFile.exists() 
					&& !mostRecentFile.isDirectory()
					&& readFileIntoEditArea(mostRecentFileName)){
				return;
			}
		}
		editNewFile(SystemOptions.DEFAULT_MODEL_TYPE);
	}

	//for read-only files (e.g., edited by Excel) or current ISTA editor has no changes yet
	public void refresh() {
		if (workingFile!=null){
			if (!editor.isXMIDSaved())
				kernel.printDialogMessage(LocaleBundle.bundleString("Cannot refresh"));
			else	
				readFileIntoEditArea(workingFile);
		}
	}

	public void importPNMLFile(){
		if (saveChanges()) {
			JFileChooser fc = kernel.getFileChooser();
			File pnmlFile = FileUtil.chooseFile(kernel.getParentFrame(), fc, LocaleBundle.bundleString("Open a PNML file"), new FilterForPNMLFiles(), FileUtil.OPENFILE);
			if (pnmlFile==null)
				return;
			if (!FileUtil.isPNMLFile(pnmlFile.getName())){
				kernel.printDialogMessage(LocaleBundle.bundleString("Please select a PNML file"));
				return;
			}
			File targetMIDFile = new File (pnmlFile.getParent()+File.separator+FileUtil.getTargetMIDFileName(pnmlFile));
			if (targetMIDFile.exists()){
				int selection = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
						LocaleBundle.bundleString("Target file")+" "+targetMIDFile.getName()+" "+LocaleBundle.bundleString("already exists")+"\n"+
						LocaleBundle.bundleString("Do you want to replace it"), LocaleBundle.bundleString("Confirm Import PNML File"),
						JOptionPane.YES_NO_OPTION);
				if (selection==JOptionPane.NO_OPTION)
					return;
			}
			try {
				File xmidFile = CPNConverter.convertPNMLToMIDFile(pnmlFile);
				openFile(xmidFile);
			}
			catch (Exception e){
				if (Kernel.IS_DEBUGGING_MODE)
					e.printStackTrace();				
				kernel.printDialogMessage(LocaleBundle.bundleString("Fail to import PNML file")+": "+pnmlFile.getName());
			}
/*
			ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Import PNML"), LocaleBundle.bundleString("Importing PNML file")+" "+pnmlFile.getName()+"...", ProgressDialog.CANCELLATION_NOT_ALLOWED);
			Thread importPNMLThread = new Thread(new ImportPNMLThread(progressDialog, pnmlFile));
			importPNMLThread.start();
			progressDialog.setVisible(true);
*/		}
	}
	
	class ImportPNMLThread implements Runnable {
		private ProgressDialog progressDialog;
		private File pnmlFile;
		
		ImportPNMLThread(ProgressDialog progressDialog, File pnmlFile) {
			this.progressDialog = progressDialog;
			this.pnmlFile = pnmlFile;
		}
		
		public void run () {
			try {
				File xmidFile = CPNConverter.convertPNMLToMIDFile(pnmlFile);
				openFile(xmidFile);
				progressDialog.dispose();
			}
			catch (Exception e){
				if (Kernel.IS_DEBUGGING_MODE)
					e.printStackTrace();
				progressDialog.dispose();
				kernel.printDialogMessage(LocaleBundle.bundleString("Fail to import PNML file")+": "+pnmlFile.getName());
			}
		}
	}

	public void importCPNFile(){
		if (saveChanges()) {
			JFileChooser fc = kernel.getFileChooser();
			File cpnFile = FileUtil.chooseFile(kernel.getParentFrame(), fc, LocaleBundle.bundleString("Open a CPN file"), new FilterForCPNFiles(), FileUtil.OPENFILE);
			if (cpnFile==null)
				return;
			if (!FileUtil.isCPNFile(cpnFile.getName())){
				kernel.printDialogMessage(LocaleBundle.bundleString("Please select a CPN file"));
				return;
			}
			File targetMIDFile = new File (cpnFile.getParent()+File.separator+FileUtil.getTargetMIDFileName(cpnFile));
			if (targetMIDFile.exists()){
				int selection = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
						LocaleBundle.bundleString("Target file")+" "+targetMIDFile.getName()+" "+LocaleBundle.bundleString("already exists")+"\n"+
						LocaleBundle.bundleString("Do you want to replace it"), LocaleBundle.bundleString("Confirm Import CPN File"),
						JOptionPane.YES_NO_OPTION);
				if (selection==JOptionPane.NO_OPTION)
					return;
			}
			try {
				File xmidFile = CPNConverter.convertCPNToMIDFile(cpnFile);
				openFile(xmidFile);
			}
			catch (Exception e){
				if (Kernel.IS_DEBUGGING_MODE)
					e.printStackTrace();
				kernel.printDialogMessage(LocaleBundle.bundleString("Fail to import CPN file")+": "+cpnFile.getName());
			}
/*
			ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Import CPN"), LocaleBundle.bundleString("Importing CPN file")+" "+cpnFile.getName()+"...", ProgressDialog.CANCELLATION_NOT_ALLOWED);
			Thread convertCPNThread = new Thread(new ConvertCPNThread(progressDialog, cpnFile));
			convertCPNThread.start();
			progressDialog.setVisible(true);
*/		}
	}
	
	class ConvertCPNThread implements Runnable {
		private ProgressDialog progressDialog;
		private File cpnFile;
		
		ConvertCPNThread(ProgressDialog progressDialog, File cpnFile) {
			this.progressDialog = progressDialog;
			this.cpnFile = cpnFile;
		}
		
		public void run () {
			try {
				File xmidFile = CPNConverter.convertCPNToMIDFile(cpnFile);
				openFile(xmidFile);
				progressDialog.dispose();
			}
			catch (Exception e){
				if (Kernel.IS_DEBUGGING_MODE)
					e.printStackTrace();
				progressDialog.dispose();
				kernel.printDialogMessage(LocaleBundle.bundleString("Fail to import CPN file")+": "+cpnFile.getName());
			}
		}
	}

	public void saveFile() {
		if (!editor.isEditing())
			return;
		if (isDefaultFileName(workingFile))
			saveAsFile();
		else
			saveFile(workingFile, NODIALOG);
	}
	
	public void saveFile(String filePath, boolean hasDialog) {
		if (!editor.isEditing())
			return;
		if (isDefaultFileName(filePath) && !new File(filePath).exists())
			saveAsFile(filePath);
		else {
			editor.saveSpecificationToFile(filePath, hasDialog);
		}
	}

	public void saveAsFile() {
		saveAsFile(workingFile);
	}

	public void saveAsFile(String filePath) {
		if (!editor.isEditing())
			return;
		JFileChooser fc = kernel.getFileChooser();
		File originalFile = new File(filePath);
		fc.setSelectedFile(originalFile);		
		File target = FileUtil.chooseFile(kernel.getParentFrame(), fc, LocaleBundle.bundleString("Save As File"),  
				new FileExtensionFilter(FileUtil.XMIDFileExtension), 
				FileUtil.SAVEFILE);
		if (target!=null){
			if (target.isDirectory())
				target = new File(target.getAbsolutePath()+File.separator+originalFile.getName());
			String targetFileName = target.getName();
			if (!FileUtil.isXMIDFile(targetFileName))
				target = new File(target.getAbsolutePath()+"."+FileUtil.XMIDFileExtension);
			boolean save = true;
			if (target.exists()){
				int selection = JOptionPane.showConfirmDialog(kernel.getParentFrame(),
						target.getName()+" "+LocaleBundle.bundleString("already exists")+"\n"+
						LocaleBundle.bundleString("Do you want to replace it"), LocaleBundle.bundleString("Confirm Save As"),
						JOptionPane.YES_NO_OPTION);
				if (selection==JOptionPane.NO_OPTION)
					save = false;
			}
			if (save){
				editor.saveSpecificationToFile(target, NODIALOG);
//				editorInUse.saveSpecificationToFile(target, NEEDDIALOG);
				editor.setMidFile(target);
				workingFile = target.getAbsolutePath();
				kernel.getRecentFiles().addFilePath(workingFile);
				updateSystemTitle();
				kernel.updateRecentFilesMenu();
			}
		}
	}
	
	// return false if user cancels the "save" operation
	private boolean saveChanges(){
		if (workingFile!=null && !editor.isXMIDSaved()){
			int choice = confirmSaveFile(LocaleBundle.bundleString("Save file")+"?", LocaleBundle.bundleString("Confirm Save"));
			if (choice==JOptionPane.CANCEL_OPTION)
				return false;
			else if (choice == JOptionPane.YES_OPTION)
				saveFile();
		}
    	if (!kernel.getTestingManager().saveTestTreeDialog()) {
    		return false;	// cancel the "save tree" operation
    	}
		return true;
	}
	
	private boolean loadTestTreeData(File testDataFile){
		File midFile = TestTreeFile.getMidFileOfTestData(kernel, testDataFile); 
		if (midFile==null)
			return false;
		XMIDEditor tmpEditor = readMIDFileIntoEditor(midFile, READONLY);
		if (tmpEditor==null)
			return false;
		TupleFactory.reset();
		MID mid = tmpEditor.parse();
		if (mid==null){
			editNewFile(SystemOptions.DEFAULT_MODEL_TYPE);
			kernel.printDialogMessage(LocaleBundle.bundleString("Invalid XMID file"));
		} else {
			editor = tmpEditor;
			TestTreeFile.loadTestDataFromExcelFile(kernel, testDataFile, midFile, mid, kernel.getSystemOptions());
		}
		return true;
	}
	
	private boolean readFileIntoEditArea(String fileName) {
		kernel.getStatusPanel().setStatus("");
		File file = new File(fileName);
		if (!isDefaultFileName(fileName) && !file.exists()){
			kernel.printDialogMessage(LocaleBundle.bundleString("File")+" "+fileName+" "+LocaleBundle.bundleString("does not exist"));
			return false;
		}
		if (FileUtil.isXMIDFile(fileName) || FileUtil.isXLSFile(fileName)) {
			XMIDEditor tmpEditor = readMIDFileIntoEditor(file, EDITABLE);
			if (tmpEditor==null){
				return false;
			} else {
				editor = tmpEditor;
				TupleFactory.reset();
				if (kernel.getTestingManager()!=null)
					kernel.getTestingManager().cleanUp(); // close the existing tree if any
			}
		}
		else 
		if (FileUtil.isTestDataFile(file.getName())) {
			return loadTestTreeData(file);
		}
		else {
//			if (editor instanceof XMIDEditor)
//				((XMIDEditor)editor).terminateSimulator();
			editor = new TextEditor(kernel, false, file);
		}
		updateAfterFileLoading(kernel, fileName);
		return true;
	}
	
	public void updateAfterFileLoading(Kernel kernel, String fileName){
		kernel.getFileManager().getEditor().setTextFont(kernel.getSystemOptions().getTextFont());
		if (!isDefaultFileName(fileName))
			kernel.getFileChooser().setCurrentDirectory((new File(fileName)).getParentFile());
		workingFile = fileName;
		kernel.updateContentPane();
		kernel.setMenuAndToolBarEnabled(true);
		kernel.getRecentFiles().addFilePath(fileName);
		kernel.updateRecentFilesMenu();
	}

	
	public XMIDEditor readMIDFileIntoEditor(File file, boolean editable){
		XMIDEditor newEditor = null;
		try {
			if (FileUtil.isXMIDFile(file.getName())) 
				newEditor = new XMIDEditor(kernel, editable, file);
			else if (FileUtil.isXLSFile(file.getName()))
				newEditor = new XMIDEditor(kernel, READONLY, file);
//			if (editor instanceof XMIDEditor)
//				((XMIDEditor)editor).terminateSimulator();
		}
		catch (Exception e){
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
			kernel.printDialogMessage(LocaleBundle.bundleString("Fail to read file")+": "+file.getAbsolutePath());
		}
		return newEditor;
	}

	/*
	private XMIDEditor newEditor = null;
	
	private synchronized XMIDEditor readMIDFileIntoEditor(File file, boolean editable){
		if (file == null) 
			return null;
		final ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Load xmid file"), LocaleBundle.bundleString("Load file ")+file.getName(), ProgressDialog.CANCELLATION_NOT_ALLOWED);
		newEditor = null;
		final LoadXMIDFileTask task = new LoadXMIDFileTask(file, editable, progressDialog);
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
 		return newEditor;
	}

	class LoadXMIDFileTask extends SwingWorker<XMIDEditor, Void> {
		private File file;
		private boolean editable;
		private ProgressDialog progressDialog;
		
		LoadXMIDFileTask(File file, boolean editable, ProgressDialog progressDialog){
			this.file = file;
			this.editable = editable;
			this.progressDialog=progressDialog;
		}
	     @Override
	     public XMIDEditor doInBackground() {
	    	XMIDEditor editor = null;
			try {
				if (FileUtil.isXMIDFile(file.getName())) 
					editor = new XMIDEditor(kernel, editable, file);
				else if (FileUtil.isXLSFile(file.getName()))
					editor = new XMIDEditor(kernel, READONLY, file);
				if (editor instanceof XMIDEditor)
					((XMIDEditor)editor).cleanup();
				setProgress(100);
			}
			catch (Exception e){
				kernel.print(LocaleBundle.bundleString("Fail to read file")+": "+file.getAbsolutePath());
			}
			return editor;
	     }

       public void done() {
    	   try {
    		   newEditor = get();
    	   }
    	   catch (Exception ex){
    	   }
    	   progressDialog.dispose();
    	   Toolkit.getDefaultToolkit().beep();
       }

	}
*/	
	public boolean workingFileExists() {
		if (workingFile!=null)
			return new File(workingFile).exists();
		else
			return false;
	}

	public String getWorkingFile() {
		return workingFile;
	}

	public boolean hasWorkingFile() {
		return workingFile!=null;
	}

	public boolean isWorkingFileTestTree() {
		return workingFile!=null && FileUtil.isTestDataFile(workingFile);
	}

	public boolean isDefaultFileName(String fname) {
		return fname.equals(defaultFileName + FileUtil.XMIDFileExtension);
	}
	
	public void updateSystemTitle() {
		if (workingFile==null) {
			kernel.setSystemTitle("");
		} 
		else {
			ModelType modelType = editor.getModelType() !=null? editor.getModelType(): null;
			String modelTypeString = modelType==null?"": " ["+LocaleBundle.bundleString(SystemOptions.getModelTypeString(modelType))+"]";
			kernel.setSystemTitle(workingFile + modelTypeString);
		}
		kernel.updateModelType();
	}
	
	public int confirmSaveFile(String message, String title){
		return JOptionPane.showConfirmDialog(kernel.getParentFrame(),
				message, title,
				JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	public void quit() {
    	if (!editor.isXMIDSaved()) { 
    		int choice = confirmSaveFile(LocaleBundle.bundleString("Save file")+"?", LocaleBundle.bundleString("Confirm Save"));
    		if (choice == JOptionPane.CANCEL_OPTION)
    			return;
    		else
    			if (choice == JOptionPane.YES_OPTION)
    				saveFile();
    	}
    	if (!kernel.getTestingManager().saveTestTreeDialog()){
    		return;	// cancel the "save tree" operation
    	}
		kernel.getRecentFiles().updateRecentFilesFile();			
		System.exit(0);
	}
		
	public MID parse(){
		if (editor!=null && !SystemOptions.isLegalModelType(editor.getModelType())){
			editor.printInConsoleArea(LocaleBundle.bundleString("Invalid XMID file"));
			return null;
		}
		if (workingFile!=null && !editor.isXMIDSaved() && !isDefaultFileName(workingFile)) {
			editor.saveSpecificationToFile(workingFile, NODIALOG);
		}
		return editor.parse();
	}

}

