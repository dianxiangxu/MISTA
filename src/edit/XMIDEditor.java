/* 	
	Author Dianxiang Xu
*/
package edit;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import kernel.Kernel;
import kernel.SystemOptions;
import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import testcode.TargetLanguage;
import utilities.FileUtil;
import parser.ParseException;
import pipeprt.gui.CreateGui;

import mid.MID;

public class XMIDEditor extends GeneralEditor implements TableModelListener{
	
	private static final String tmpXMIDFile = "xmid.tmp";
	
	private File separateModelFile = null;
	
	private ModelPanel modelPanel;
	private AccessControlPanel accessControlPanel;	
	private MIMPanel mimPanel;
	private HelperCodePanel helperCodePanel;
	
	private HSSFWorkbook workBook = null;
	
	// new file
	public XMIDEditor(Kernel kernel, File file, ModelType modelType ){
		super(kernel, true);
		this.midFile = file;
		separateModelFile = new File(FileUtil.getDefaultSeparateModelFileName(file));
//System.out.println("Separate model file: "+separateModelFile.getAbsolutePath());
		this.modelType = modelType;
		modelPanel = createModelPanel();
		if (modelType==ModelType.ABAC) {
			accessControlPanel = new AccessControlPanel(this);
		}
		mimPanel = createMIMPanel(kernel.getSystemOptions().getLanguage());
		helperCodePanel = new HelperCodePanel(this, kernel.getSystemOptions().getLanguage());
		createTabbedPane();
	}

	// existing file
	public XMIDEditor(Kernel kernel, boolean editable, File file) throws Exception {
		super(kernel, editable);
		this.midFile = file;
		FileInputStream inputStream;
		if (editable){
			FileUtil.copyFile(file, new File(tmpXMIDFile));	// use a copy;   
			inputStream = new FileInputStream(new File(tmpXMIDFile));
		}
		else 
			inputStream = new FileInputStream(file);				
		workBook = new HSSFWorkbook (inputStream);
		Sheet modelSheet = workBook.getSheetAt(0);
		Sheet mimSheet = workBook.getSheetAt(1);
		Sheet helperCodeSheet = workBook.getSheetAt(2);			
		XMIDProcessor xmidLoader = new XMIDProcessor();
		modelType = xmidLoader.getModelType(modelSheet);
		String separateModelFileName = xmidLoader.getSeparateModelFileName(modelSheet);
		if (separateModelFileName!=null && !separateModelFileName.equals(""))
			separateModelFile = new File(midFile.getParent()+File.separator+separateModelFileName);
//if (separateModelFile!=null)
//System.out.println("Separate model file: "+separateModelFile.getAbsolutePath());
		if (modelType==null)
			modelType = SystemOptions.DEFAULT_MODEL_TYPE;
		determineTargetLanguage(mimSheet, helperCodeSheet);
		modelPanel = createModelPanel(modelSheet);
		if (modelType==ModelType.ABAC) {
			accessControlPanel = new AccessControlPanel(this, modelSheet);
		}
		mimPanel = createMIMPanel(kernel.getSystemOptions().getLanguage(), mimSheet);
		helperCodePanel = helperCodeSheet != null? new HelperCodePanel(this,  kernel.getSystemOptions().getLanguage(), helperCodeSheet): 
						new HelperCodePanel(this,  kernel.getSystemOptions().getLanguage());
		inputStream.close();
		createTabbedPane();
	}

	private ModelPanel createModelPanel(){
		if (modelType!=null) {
			switch (modelType){
			case FUNCTIONNET:
			case ABAC:
			case THREATNET:
				if (kernel.getSystemOptions().useGraphicalEditor(modelType))
					return new ModelPanelVisualNet(this, separateModelFile);
				else
					return new ModelPanelTabularNet(this);
			case STATEMACHINE:
				if (kernel.getSystemOptions().useGraphicalEditor(modelType))
					return new ModelPanelVisualStateMachine(this, separateModelFile);
				else
					return new ModelPanelTabularNet(this);
			case CONTRACT:
				return new ModelPanelTabularNet(this);
			case THREATTREE:
				if (kernel.getSystemOptions().useGraphicalEditor(modelType))
				return new ModelPanelVisualThreatTree(this, separateModelFile);
			else
				return new ModelPanelTabularThreatTree(this);
			}
		} 
		return new ModelPanelTabularNet(this);			
	}

	private ModelPanel createModelPanel(Sheet modelSheet){
		if (modelSheet==null)
			return createModelPanel();
		if (modelType!=null) {
			switch (modelType){
			case FUNCTIONNET:
			case ABAC:
			case THREATNET:
				if (separateModelFile!=null)
					return new ModelPanelVisualNet(this, separateModelFile);
				else
					return new ModelPanelTabularNet(this, modelSheet);
			case STATEMACHINE:
				if (separateModelFile!=null)
					return new ModelPanelVisualStateMachine(this, separateModelFile);
				else
					return new ModelPanelTabularNet(this, modelSheet);
			case CONTRACT:
				return new ModelPanelTabularNet(this, modelSheet);
			case THREATTREE:
				if (separateModelFile!=null)
				return new ModelPanelVisualThreatTree(this, separateModelFile);
			else
				return new ModelPanelTabularThreatTree(this, modelSheet);
			}
		} 
		return new ModelPanelTabularNet(this, modelSheet);			
	}

	private MIMPanel createMIMPanel(TargetLanguage language){
		if (modelType!=null) {
			switch (modelType){
			case FUNCTIONNET:
			case ABAC:
			case CONTRACT:
			case THREATNET:
				return new MIMPanelNet(this, language);
			case STATEMACHINE:
				return new MIMPanelStateMachine(this, language);
			case THREATTREE:
				return new MIMPanelThreatTree(this, language);
			}
		} 
		return new MIMPanelNet(this, language);			
	}

	private MIMPanel createMIMPanel(TargetLanguage language, Sheet sheet){
		if (sheet==null)
			createMIMPanel(language);
		if (modelType!=null) {
			switch (modelType){
			case FUNCTIONNET:
			case ABAC:
			case CONTRACT:
			case THREATNET:
				return MIMPanelNet.createMIMPanel(this, language, sheet);
			case STATEMACHINE:
				return MIMPanelStateMachine.createMIMPanel(this, language, sheet);
			case THREATTREE:
				return MIMPanelThreatTree.createMIMPanel(this, language, sheet);
			}
		} 
		return new MIMPanelNet(this, language);			
	}
	
	private void createTabbedPane(){
		editTabbedPane.addTab(LocaleBundle.bundleString("Model"),FileUtil.createImageIcon("images/model.png"), modelPanel);
		if (modelType==ModelType.ABAC) {
			editTabbedPane.addTab(LocaleBundle.bundleString("ABAC"),FileUtil.createImageIcon("images/model.png"), accessControlPanel);
		}
		editTabbedPane.addTab(LocaleBundle.bundleString("MIM"), FileUtil.createImageIcon("images/implementation.png"), mimPanel);
		editTabbedPane.addTab(LocaleBundle.bundleString("Helper Code"), FileUtil.createImageIcon("images/helper.png"), helperCodePanel);
	    editTabbedPane.addChangeListener(this);		
	    kernel.updateEditMenu(modelPanel.getModelMenu(), mimPanel.getMIMMenu(), helperCodePanel.getHelperCodeMenu());
    	kernel.updateToolBar(modelPanel.getAdditionalToolBar());
    	openAllSubModels(midFile.getName());
	    editTabbedPane.setSelectedComponent(modelPanel);
		setXMIDSaved(true);
	}
	
	public void setToModelTab(){
		editTabbedPane.setSelectedComponent(modelPanel);		
	}

	public JComponent getEditingJComponent(){
		return editTabbedPane;
	}
	
	private void determineTargetLanguage(Sheet mimSheet, Sheet helperCodeSheet){
		TargetLanguage newTargetLanguage = null;
		TargetLanguage mimLanguage = XMIDProcessor.getMIMLanguage(mimSheet);
		TargetLanguage helperCodeLanguage = XMIDProcessor.getHelperCodeLanguage(helperCodeSheet);
		if (mimLanguage!=null && (
				mimLanguage==TargetLanguage.C || 
				mimLanguage==TargetLanguage.HTML || 
				mimLanguage==TargetLanguage.RPC ||
				mimLanguage==TargetLanguage.SELENIUMDRIVER ||
				mimLanguage==TargetLanguage.UFT)) {
				newTargetLanguage = mimLanguage;
		}
		else if (helperCodeLanguage!=null){	// OO language
			newTargetLanguage = helperCodeLanguage;		}
		// otherwise cannot be determined from MIM or HelperCode
		// use the current language
		if (newTargetLanguage!=null)
			kernel.updateLanguage(newTargetLanguage);
	}

	public void setMidFile(File file){
		super.setMidFile(file);
		if (modelPanel instanceof ModelPanelVisualNet
			|| modelPanel instanceof ModelPanelVisualStateMachine
			|| modelPanel instanceof ModelPanelVisualThreatTree
				){
			separateModelFile = new File(FileUtil.getDefaultSeparateModelFileName(file));
		}
	}
	
	public boolean isXMIDSaved() {
		if (modelPanel instanceof VisualModelInterface){
			if (((VisualModelInterface)modelPanel).isModelChanged())
				return false;
		}
		return xmidSaved;
	}

	public void updateLanguage(){
		TargetLanguage newLanguage = kernel.getSystemOptions().getLanguage();
		if (mimPanel!=null)
			mimPanel.updateLanguage(newLanguage);
		if (helperCodePanel!=null)
			helperCodePanel.updateLanguage(newLanguage);
		kernel.updateEditMenu(modelPanel.getModelMenu(), mimPanel.getMIMMenu(), helperCodePanel.getHelperCodeMenu());
	}
	
	public void setTextFont(Font newFont){
		super.setTextFont(newFont);
		modelPanel.updateFont();
		mimPanel.updateFont();
		helperCodePanel.updateFont();
	}
	
	public void saveSpecificationToFile(String fileName, boolean needDialog){
		saveSpecificationToFile(new File(fileName), needDialog);
	}
	
	public void saveSpecificationToFile(File file, boolean needDialog){
		if (workBook == null) {
			workBook = new HSSFWorkbook();
			workBook.createSheet("MODEL");
			workBook.createSheet("MIM");
			workBook.createSheet("HELPER CODE");
		}
		CellStyle lineWrapStyle = workBook.createCellStyle();
		lineWrapStyle.setWrapText(true);
		modelPanel.saveModel(file, workBook.getSheetAt(0), lineWrapStyle);
		if (modelType==ModelType.ABAC) {
			accessControlPanel.saveAccessControl(workBook.getSheetAt(0), lineWrapStyle);
		}
		mimPanel.saveMIM(workBook.getSheetAt(1), lineWrapStyle);
		helperCodePanel.saveHelperCode(workBook.getSheetAt(2), lineWrapStyle);
		try {
			FileOutputStream out = new FileOutputStream(file);
			workBook.write(out);
			out.close();
			xmidSaved = true;
			if (needDialog)
				printInConsoleArea(LocaleBundle.bundleString("File saved"));
		}
		catch (IOException ioe){
			printInConsoleArea(ioe.toString());
			ioe.printStackTrace();
		}
		saveSubModels();
	}
	
	public static void cleanUpSheet(Sheet sheet){	// keep the first row
		for (int i=sheet.getLastRowNum(); i>0; i--){
			Row rowToBeRemoved = sheet.getRow(i);
			if (rowToBeRemoved!=null)
				sheet.removeRow(rowToBeRemoved);
		}
	}
	
	public ModelPanel getModelPanel(){
		return modelPanel;
	}
	
	public boolean isModelPanelSelected(){
		return editTabbedPane.getSelectedComponent() == modelPanel;
	}
	
	public boolean isMIMPanelSelected(){
		return editTabbedPane.getSelectedComponent() == mimPanel;		
	}
	
	public void updateMIMPanel(){
		mimPanel.updateMIMPanel();
	}
	
	public boolean isHelperCodePanelSelected(){
		return editTabbedPane.getSelectedComponent() == helperCodePanel;		
	}

	public boolean hasSimulator(){
		return modelPanel!=null && 
		(modelPanel instanceof ModelPanelVisualNet ||
//				 (modelPanel instanceof ModelPanelTabularNet));
		 (modelPanel instanceof ModelPanelTabularNet && modelType!=ModelType.STATEMACHINE));
	}

	public void startSimulator(SimulatorType simulatorType){
		MID mid = simulatorType==SimulatorType.MODEL_SIMULATION? parseModel(): parse();
		if (mid==null)
			return;
		if (mid.getInitialMarkings().size()==0) {
			kernel.printDialogMessage(LocaleBundle.bundleString("No initial state is specified"));
			return;
		}
		if (simulatorType!=SimulatorType.MODEL_SIMULATION && (mid.specifiedSystemName()==null ||  mid.specifiedSystemName().equals(""))){
			kernel.printDialogMessage(LocaleBundle.bundleString("URL is not specified"));
			return;
		}
		setToModelTab();
		modelPanel.startSimulator(mid, simulatorType);
	}

	public boolean isSimulationMode(){
		return modelPanel.getSimulator()!=null;
	}
	
	public void resetSimulator(){
		modelPanel.simulator = null;		
	}

	// implements TableModelListener
	public void tableChanged(TableModelEvent e) {
	    setXMIDSaved(false);
	}

	// implements ChangeListener
	public synchronized void stateChanged(ChangeEvent evt) {
		super.stateChanged(evt);
        Component selection = editTabbedPane.getSelectedComponent();
        if (selection==modelPanel || selection==mimPanel || selection==helperCodePanel){
        	if (selection==mimPanel)
        		updateMIMPanel();
        	kernel.setMenuAndToolBarEnabled(true);
            if (selection==modelPanel){
            	if (modelPanel instanceof ModelPanelVisualNet){
            		ModelPanelVisualNet visualNet = (ModelPanelVisualNet)modelPanel;
              		kernel.updateToolBar(visualNet.getMainNet().getPaletteToolBar());
            		kernel.updateModelMenu(createModelMenu(visualNet.getMainNet().getPrTMenu()));
               		CreateGui.setPrTPanel(visualNet.getMainNet());
            	}
         	    kernel.updateToolBar(modelPanel.getAdditionalToolBar());
            }
            else
            	kernel.updateToolBar(null);
        }
	}

	public MID parse(){
		MID mid = new MID();
		try {
			modelPanel.parse(mid);
			if (modelType==ModelType.THREATNET && !mid.hasAttackTransition()){
				kernel.printDialogMessage(LocaleBundle.bundleString("One or more attack transitions should be specified"));
		   		return null;
			}
			if (modelType==ModelType.ABAC) {
				accessControlPanel.parse(mid);
			}
			mimPanel.parse(mid);
			helperCodePanel.parse(mid);
			mid.setFileName(midFile.getAbsolutePath());
		  	String errorMessage = mid.findErrors();
		   	if (errorMessage!=null){
		   		printInConsoleArea(errorMessage);
		   		return null;
		   	}
		   	if (Kernel.IS_DEBUGGING_MODE){
		   		printInConsoleArea(mid+"\n");
		   		printInConsoleArea(LocaleBundle.bundleString("Number of transitions")+": "+mid.getTransitions().size()+"; "
		   				+LocaleBundle.bundleString("Number of places")+": "+mid.getPlaces().size());
		   	}
		}
		catch (ParseException exception){
			printInConsoleArea(LocaleBundle.bundleString("Error")+": "+exception.toString().replace("parser.ParseException:", ""));
			Toolkit.getDefaultToolkit().beep();
			mid = null;
		}
		return mid;	
	}

	public MID parseModel(){
		MID mid = new MID();
		try {
			modelPanel.parse(mid);
			if (modelType==ModelType.THREATNET && !mid.hasAttackTransition()){
				kernel.printDialogMessage(LocaleBundle.bundleString("One or more attack transitions should be specified"));
		   		return null;
			}
			mid.setFileName(midFile.getAbsolutePath());
		  	String errorMessage = mid.findErrors();
		   	if (errorMessage!=null){
		   		printInConsoleArea(errorMessage);
		   		return null;
		   	}
		   	if (Kernel.IS_DEBUGGING_MODE){
		   		printInConsoleArea(mid+"\n");
		   		printInConsoleArea(LocaleBundle.bundleString("Number of transitions")+": "+mid.getTransitions().size()+"; "
		   				+LocaleBundle.bundleString("Number of places")+": "+mid.getPlaces().size());
		   	}
		}
		catch (ParseException exception){
			printInConsoleArea(LocaleBundle.bundleString("Error")+": "+exception.toString().replace("parser.ParseException:", ""));
			Toolkit.getDefaultToolkit().beep();
			mid = null;
		}
		return mid;	
	}

}
