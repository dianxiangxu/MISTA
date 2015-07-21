package edit;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import locales.LocaleBundle;
import mid.MID;
import mid.Marking;
import mid.GoalProperty;
import mid.Transition;
import mid.TupleFactory;

import parser.MIDParser;
import parser.ParseException;
import pige.dataLayer.GraphAnnotationNote;
import pige.dataLayer.GraphArc;
import pige.dataLayer.GraphDataLayerInterface;
import pige.dataLayer.GraphNode;
import pige.dataLayer.GraphType;
import pige.gui.CreateGraphGui;
import pige.gui.GraphPanel;
import utilities.FileUtil;

public class ModelPanelVisualStateMachine extends ModelPanel implements VisualModelInterface{
	
	private GraphPanel fsmPanel;
	
	public ModelPanelVisualStateMachine(XMIDEditor editor, File modelFile) {
		super(editor);
		createModelPanel(modelFile);
	}
	
	private static final long serialVersionUID = 1L;

	private void createModelPanel(File modelFile){
		removeAll();
		fsmPanel = CreateGraphGui.createGraphPanel(editor.kernel.getParentFrame(), modelFile, editor.isEditable, GraphType.FiniteStateMachine);
	    setLayout(new BorderLayout());
	    add(fsmPanel, BorderLayout.CENTER);
	}
	
	@Override
	public JMenu getModelMenu() {
//		JMenu modelMenu = new JMenu(LocaleBundle.bundleString("Model"));
		JMenu modelMenu = fsmPanel.getGraphMenu();
		modelMenu.setText(LocaleBundle.bundleString("Model"));
		return modelMenu;
	}

	@Override
	public void parse(MID mid) throws ParseException {
		parseStates(mid);		// including the default initial marking
		checkDuplicateStates();
		checkDuplicateTransitions(); 
		parseTransitions(mid);	// including arcs
		parseAnnotations(mid);	// initial/goal states, sink events, test parameters, sequence file
//editor.printInConsoleArea(mid.toString(), true);
	}
	
	public boolean isModelChanged(){
		return fsmPanel.isGraphChanged();
	}

	public void saveModel(File xmidFile, Sheet sheet, CellStyle lineWrapStyle){
		int rowIndex = saveModelHeader(sheet, lineWrapStyle);
		String separateModelFileName = FileUtil.getDefaultSeparateModelFileName(xmidFile);
		File separateModelFile = new File(xmidFile.getParent()+File.separator+separateModelFileName);
		rowIndex = XMIDProcessor.createTableModelTypeRow(editor.getModelType(), separateModelFileName, sheet, rowIndex);
	    fsmPanel.setFile(separateModelFile);
	    fsmPanel.saveGraph();
	}

	@Override
	public JToolBar getAdditionalToolBar(){
		return fsmPanel.getPaletteToolBar();
	}
	
	@Override
	public void updateFont() {

	}

	private void checkDuplicateStates()throws ParseException{
        GraphDataLayerInterface fsm = fsmPanel.getModel();
        GraphNode[] arcs = fsm.getGraphNodes();
        for (int i = 0; i < arcs.length; i++) {
        	for (int j=i+1; j<arcs.length; j++){
        		if (arcs[i].getName().equals(arcs[j].getName()))
            		throw new ParseException(arcs[i].getName()+": "+LocaleBundle.bundleString("Duplicate states")+".");
        	}
        }
	}

	private void parseStates(MID mid) throws ParseException{
        GraphDataLayerInterface fsm = fsmPanel.getModel();
		GraphNode[] nodes = fsm.getGraphNodes();
        for (int i = 0; i < nodes.length; i++){
//System.out.println("State ID: "+nodes[i].getId()+" Name: "+nodes[i].getName());        	
        	String stateName = nodes[i].getName().trim();
        	if (stateName.equals(""))
        		throw new ParseException(LocaleBundle.bundleString("Unnamed state found"));
        	if (!nodes[i].getConnectFromIterator().hasNext() &&
        			!nodes[i].getConnectToIterator().hasNext())
        		throw new ParseException(stateName+" "+LocaleBundle.bundleString("is not connected"));
        	if (!MIDParser.isIdentifier(stateName))
        		throw new ParseException(stateName+" "+LocaleBundle.bundleString("shoud start with a letter"));
        	mid.addPlace(stateName);
        }
	}
	
	private void checkDuplicateTransitions()throws ParseException{
        GraphDataLayerInterface fsm = fsmPanel.getModel();
        GraphArc[] arcs = fsm.getArcs();
        for (int i = 0; i < arcs.length; i++) {
        	for (int j=i+1; j<arcs.length; j++){
        		if (arcs[i].getSource()==arcs[j].getSource() &&
        			arcs[i].getTarget()==arcs[j].getTarget() &&
        			arcs[i].getName().equals(arcs[j].getName()) &&
        			arcs[i].getPrecondition().equals(arcs[j].getPrecondition()))
            		throw new ParseException(LocaleBundle.bundleString("Duplicate transitions of event")+": "
            				+ arcs[i].getSource().getName()+" - "+
            				arcs[i].getTarget().getName());
        	}
        }
	}
	
	private void parseTransitions(MID mid) throws ParseException{
        GraphDataLayerInterface fsm = fsmPanel.getModel();
        GraphArc[] arcs = fsm.getArcs();
        for (int i = 0; i < arcs.length; i++) {
//System.out.println("Transition ID: "+arcs[i].getId()+" Name: "+arcs[i].getName());        	
        	String event = arcs[i].getName().trim();
        	if (event.equals(""))
        		throw new ParseException(LocaleBundle.bundleString("Event is missing in the transition")+" "+
        				arcs[i].getSource().getName()+" - "
        				+arcs[i].getTarget().getName());
        	if (!MIDParser.isIdentifier(event))
        		throw new ParseException(event+" "+ LocaleBundle.bundleString("should start with a letter"));
        	Transition istaTransition = 
				MIDParser.parseStateMachineTransition(arcs[i].getSource().getName().trim(), 
        				arcs[i].getTarget().getName().trim(),
        				event,
        				arcs[i].getPrecondition().trim(),
        				arcs[i].getPostcondition().trim()
        		);       			
       		mid.addTransition(istaTransition); 
        }
	}

	protected void parseInitState(MID mid, String text) throws ParseException {
		String initStateString = text.substring(XMIDProcessor.INIT_KEYWORD.length()).trim();
//System.out.println("State: "+markingString);
		if (initStateString.equals(""))
			return;
    	if (!MIDParser.isIdentifier(initStateString))
    		throw new ParseException(initStateString+" "+LocaleBundle.bundleString("should start with a letter"));
		if (!mid.getPlaces().contains(initStateString))
			throw new ParseException(XMIDProcessor.INIT_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n");
		Marking marking = new Marking();
		marking.addTuple(initStateString, TupleFactory.createTuple(new ArrayList<String>()));
		mid.addInitialMarking(marking);
	}

	protected void parseGoalState(MID mid, String text) throws ParseException {
		String goalString = text.substring(XMIDProcessor.GOAL_KEYWORD.length()).trim();
//System.out.println("State: "+markingString);
		if (goalString.equals(""))
			return;
    	if (!MIDParser.isIdentifier(goalString))
    		throw new ParseException(goalString+" "+LocaleBundle.bundleString("should start with a letter"));
		if (!mid.getPlaces().contains(goalString))
			throw new ParseException(XMIDProcessor.GOAL_KEYWORD+" "+LocaleBundle.bundleString("annotation")+" "+text+"\n");
		GoalProperty propertyTransition = new GoalProperty(MID.DEFAULT_GOAL_TAG, goalString);
		mid.addGoalProperty(propertyTransition);
	}
	
	private void parseAnnotations(MID mid) throws ParseException{
        GraphDataLayerInterface fsm = fsmPanel.getModel();
		GraphAnnotationNote[] annotationNotes = fsm.getLabels();
		for (GraphAnnotationNote annotation: annotationNotes){
			String text = annotation.getText();	
			if (text.startsWith(XMIDProcessor.INIT_KEYWORD))
				parseInitState(mid, text);
			else
			if (text.startsWith(XMIDProcessor.GOAL_KEYWORD))
				parseGoalState(mid, text);
		}
	}
	

}
