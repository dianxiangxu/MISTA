package edit;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import locales.LocaleBundle;
import mid.MID;

import parser.MIDParser;
import parser.ParseException;
import pige.dataLayer.GraphAbstractNode;
import pige.dataLayer.GraphArc;
import pige.dataLayer.GraphDataLayerInterface;
import pige.dataLayer.GraphNode;
import pige.dataLayer.GraphType;
import pige.gui.CreateGraphGui;
import pige.gui.GraphPanel;
import utilities.FileUtil;

public class ModelPanelVisualThreatTree extends ModelPanel implements VisualModelInterface{
	
	private GraphPanel threatTreePanel;
	
	public ModelPanelVisualThreatTree(XMIDEditor editor, File modelFile) {
		super(editor);
		createModelPanel(modelFile);
	}
	
	private static final long serialVersionUID = 1L;

	private void createModelPanel(File modelFile){
		removeAll();
		threatTreePanel = CreateGraphGui.createGraphPanel(editor.kernel.getParentFrame(), modelFile, editor.isEditable, GraphType.ThreatTree);
	    setLayout(new BorderLayout());
	    add(threatTreePanel, BorderLayout.CENTER);
	}
	
	@Override
	public JMenu getModelMenu() {
//		JMenu modelMenu = new JMenu(LocaleBundle.bundleString("Model"));
		JMenu modelMenu = threatTreePanel.getGraphMenu();
		modelMenu.setText(LocaleBundle.bundleString("Model"));
		return modelMenu;
	}

	@Override
	public void parse(MID mid) throws ParseException {
		checkNodeNameSyntax();
		checkDuplicateNodeNames();
		validateTreeStructure();
		createThreatTree(mid);
//editor.printInConsoleArea(mid.toString(), true);
	}
	
	public boolean isModelChanged(){
		return threatTreePanel.isGraphChanged();
	}

	public void saveModel(File xmidFile, Sheet sheet, CellStyle lineWrapStyle){
		int rowIndex = saveModelHeader(sheet, lineWrapStyle);
		String separateModelFileName = FileUtil.getDefaultSeparateModelFileName(xmidFile);
		File separateModelFile = new File(xmidFile.getParent()+File.separator+separateModelFileName);
		rowIndex = XMIDProcessor.createTableModelTypeRow(editor.getModelType(), separateModelFileName, sheet, rowIndex);
	    threatTreePanel.setFile(separateModelFile);
	    threatTreePanel.saveGraph();
	}

	@Override
	public JToolBar getAdditionalToolBar(){
		return threatTreePanel.getPaletteToolBar();
	}
	
	@Override
	public void updateFont() {

	}

	private void checkNodeNameSyntax() throws ParseException{
        GraphDataLayerInterface threatTree = threatTreePanel.getModel();
		GraphNode[] nodes = threatTree.getGraphNodes();
        for (int i = 0; i < nodes.length; i++){
//System.out.println("ID: "+nodes[i].getId()+" Name: "+nodes[i].getName());        	
        	String nodeName = nodes[i].getName().trim();
        	if (nodeName.equals(""))
        		throw new ParseException(LocaleBundle.bundleString("Unnamed node found"));
        	if (!nodes[i].getConnectFromIterator().hasNext() &&
        			!nodes[i].getConnectToIterator().hasNext())
        		throw new ParseException(nodeName+" "+LocaleBundle.bundleString("is not connected"));
        	if (!MIDParser.isIdentifier(nodeName))
        		throw new ParseException(nodeName+" "+LocaleBundle.bundleString("should start with a letter"));
        }
	}

	private void checkDuplicateNodeNames()throws ParseException{
        GraphDataLayerInterface threatTree = threatTreePanel.getModel();
        GraphNode[] arcs = threatTree.getGraphNodes();
        for (int i = 0; i < arcs.length; i++) {
        	for (int j=i+1; j<arcs.length; j++){
        		if (arcs[i].getName().equals(arcs[j].getName()))
            		throw new ParseException(LocaleBundle.bundleString("Duplicate node name")+": "+arcs[i].getName());
        	}
        }
	}

	private void createThreatTree(MID mid){
        GraphDataLayerInterface threatTree = threatTreePanel.getModel();
		GraphNode[] nodes = threatTree.getGraphNodes();
        for (int i = 0; i < nodes.length; i++){
        	Iterator<GraphArc> arcIterator = nodes[i].getConnectFromIterator();
        	if (arcIterator.hasNext()){
        		String event = nodes[i].getName();
        		String gate = nodes[i].getSiblingRelationString();
        		ArrayList<String> childEvents = new ArrayList<String>();
        		while (arcIterator.hasNext()){
        			GraphArc arc = arcIterator.next();
        			childEvents.add(arc.getTarget().getName());
        		}
        		if (childEvents.size()>1 && nodes[i].getSiblingRelation()==GraphNode.SiblingRelation.PRIORITYAND){
            		Collections.sort(childEvents);
            		gate="AND";
        		}
        		mid.addThreatTreeNode(event, childEvents, gate);
				mid.buildThreatTree();
        	}
        }
	}

	private void validateTreeStructure() throws ParseException{
		ArrayList<GraphAbstractNode> nodesVisited = new ArrayList<GraphAbstractNode>();
        GraphDataLayerInterface threatTree = threatTreePanel.getModel();
		GraphNode[] nodes = threatTree.getGraphNodes();
        for (int i = 0; i < nodes.length; i++){
        	if (nodes[i].getNoOfArcsTo()>1)
        		throw new ParseException(nodes[i].getName()+" "+LocaleBundle.bundleString("has multiple parents"));
        	if (!nodesVisited.contains(nodes[i])){ 
        		nodesVisited.add(nodes[i]);
        		LinkedList<GraphAbstractNode> nodesToExpand = new LinkedList<GraphAbstractNode>();
        		nodesToExpand.add(nodes[i]);
        		while (!nodesToExpand.isEmpty()){
        			GraphAbstractNode node = nodesToExpand.poll();
        			if (node!=null){
        				Iterator<GraphArc> arcIterator = node.getConnectFromIterator();
        				while (arcIterator.hasNext()){
        					GraphArc arc = arcIterator.next();
        					GraphAbstractNode target = arc.getTarget();
        					if (nodesVisited.contains(target))
        						throw new ParseException(target.getName()+" "+LocaleBundle.bundleString("is involved in a non-tree structure"));
        					else{
        						nodesToExpand.add(target);
        						nodesVisited.add(target);
        					}
        				}
        			}
        		}
        	}
        }
//System.out.println("No cycle found!");
	}
}
