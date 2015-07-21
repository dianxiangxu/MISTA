package simulation;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import edit.GeneralEditor;

import locales.LocaleBundle;
import mid.MID;
import mid.Marking;
import mid.Substitution;
import mid.Transition;
import pipeprt.dataLayer.DataLayerInterface;
import pipeprt.dataLayer.PipePlace;
import pipeprt.dataLayer.PipeTransition;
import pipeprt.gui.PrTPanel;
import testcode.TestCodeGeneratorOnline;

public abstract class PrTEngine extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	protected GeneralEditor editor;
	protected MID mid;
	protected PrTPanel prtPanel;	
	protected TestCodeGeneratorOnline codeGenerator =null;

	public PrTEngine(GeneralEditor editor, MID mid, PrTPanel prtPanel){
		super(editor.getKernel().getParentFrame(), "", false);
		this.editor = editor;
		this.mid = mid;
		this.prtPanel = prtPanel;
	}
	
    protected void setTokensInPrTPlaceForSimulation(Marking marking){
    	for (String placeName: mid.getPlaces()){
    		placeAnnimationTokens(prtPanel, marking, placeName);
    		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
    			for (JPanel subModel: editor.getSubModels())
    				if (subModel instanceof PrTPanel){
    					placeAnnimationTokens((PrTPanel)subModel, marking, placeName);
    				}
    		}
    	}
    }

    protected void placeAnnimationTokens(PrTPanel prtPanel, Marking marking, String placeName){
        DataLayerInterface net = prtPanel.getModel();
        PipePlace place = net.getPlaceByName(placeName);
        if (place!=null){
        	int numberOfTokens = 0;
        	if (marking.getTuples(placeName)!=null)
        		numberOfTokens = marking.getTuples(placeName).size();
        	place.setNumberOfTokensForSimulation(numberOfTokens);
        }
    }

    protected PrTPanel findSubModelForTransition(Transition transition){
    	PipeTransition pipeTransition = mid.getPipeTransition(transition);
    	for  (JPanel subModel: editor.getSubModels())
			if (subModel instanceof PrTPanel){
		        DataLayerInterface net = ((PrTPanel)subModel).getModel();
		        for (PipeTransition currentTransition: net.getTransitions())
		        	if (pipeTransition==currentTransition)
		        		return (PrTPanel)subModel;
			}
    	return null;
    }
    
    protected void setModelPanelsEditingEnabled(boolean enabled){
    	if (prtPanel!=null) {
    		prtPanel.enableActions(enabled);
    		prtPanel.setEditionAllowed(enabled);
    		if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
    			for (JPanel subModel: editor.getSubModels())
    				if (subModel instanceof PrTPanel){
    					((PrTPanel)subModel).enableActions(enabled);
    					((PrTPanel)subModel).setEditionAllowed(enabled);
    				}
    		}
    	}

    }
   
	public static String getActualParameterList(Transition transition, Substitution substitution) {
		ArrayList<String> formalParameters = transition.getArguments();
		if (formalParameters==null)
			formalParameters = transition.getAllVariables();
		if (formalParameters==null || formalParameters.size()==0)
			return "";
		StringBuffer buffer = new StringBuffer("(");
		buffer.append(substitution.getBinding(formalParameters.get(0)));
		for (int i=1; i<formalParameters.size(); i++){
			String value = substitution.getBinding(formalParameters.get(i));
			buffer.append(", ");
			buffer.append(value); 
		}
		buffer.append(")"); 
		return buffer.toString(); 
	}
	
   protected void updateModelPanelUIs(){
	   if (prtPanel!=null){
		   prtPanel.updateUI();
		   if (editor.getKernel().getSystemOptions().isNetHierarchyEnabled()){
			   for (JPanel subModel: editor.getSubModels())
				   if (subModel instanceof PrTPanel){
					   ((PrTPanel)subModel).updateUI();
				   }
		   }    	
	   }
    } 

   protected String exceptionMessage(Exception e){
	   String msg = e.toString();
	   int lastIndexOfColon = msg.lastIndexOf(":");
	   return lastIndexOfColon>=0? msg.substring(lastIndexOfColon+1)+".": msg+".";
   }

	protected JButton createJButton(String command){
		JButton button = new JButton(LocaleBundle.bundleString(command));
		button.setActionCommand(command);
		button.addActionListener(this);
		return button;
	} 

	protected String formatMarkingString(Marking marking){
		return marking.getStringForSimulation();
//		return marking.toString().replace("), ","),\n");
	}

}
