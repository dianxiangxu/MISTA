package simulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import locales.LocaleBundle;

import edit.GeneralEditor;

import mid.Marking;
import mid.Transition;
import mid.Unifier;
import pipeprt.dataLayer.PipeTransition;
import pipeprt.gui.PrTPanel;
import testcode.TestCodeGeneratorOnline;
import testgeneration.TransitionTree;
import testgeneration.TransitionTreeNode;

public class PrTOnlineTester extends PrTEngine implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	private JTextField testNoTextField = new JTextField(6);
	private JTextField failedTestTextField = new JTextField(6);
	private JTextField timeIntervalTextField = new JTextField(6);
	
	private JTextArea currentTestTextArea = new JTextArea(3, 50);
	
	private int testIndex = 0;
	private int numberOfFailures = 0;
	
	private static final String PLAY = "Play";
	private static final String PAUSE = "Pause";
	private static final String RESET = "Reset";
	private static final String EXIT = "Exit";
	
	private JButton playButton;
	private JButton pauseButton;
	private JButton resetButton;
		
	private TransitionTree transitionTree;
	private ArrayList<TransitionTreeNode> allTests;
	
	public PrTOnlineTester(GeneralEditor editor, PrTPanel prtPanel, TransitionTree transitionTree) throws Exception{
    	super(editor, transitionTree.getMID(), prtPanel);
    	setTitle(LocaleBundle.bundleString("Online Testing Title"));
		this.transitionTree = transitionTree;
		setModelPanelsEditingEnabled(false);
		codeGenerator = new TestCodeGeneratorOnline(editor, mid);
		if (!codeGenerator.hasTestEngine()){
			setModelPanelsEditingEnabled(true);
			throw new Exception("Online execution failure");
		}
		allTests = transitionTree.getAllTestsForCodeGeneration();
		this.setAlwaysOnTop(true);
		createButtons();
		setMainContentPane();
		JFrame parentFrame = editor.getKernel().getParentFrame();
        if (parentFrame != null) {
            Dimension parentSize = parentFrame.getSize(); 
            Point p = parentFrame.getLocation(); 
            setLocation(p.x + parentSize.width /8, p.y + (int)(parentSize.height*0.7));
//            setLocation(p.x, p.y);
        }
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
		        	doExit();
		    }
		});
		pack();
		setVisible(true); 
	}
	
	private void createButtons(){
		playButton = createJButton(PLAY);
		pauseButton = createJButton(PAUSE);
		pauseButton.setEnabled(false);
		resetButton = createJButton(RESET);

		currentTestTextArea.setFont(editor.getTextFont());
		currentTestTextArea.setEditable(false);
		currentTestTextArea.setLineWrap(true);

		testNoTextField.setText("0");
//		testNoTextField.setEnabled(false);
		testNoTextField.setEditable(false);
		
		failedTestTextField.setText("0");
//		failedTestTextField.setEnabled(false);
		failedTestTextField.setEditable(false);
		
		timeIntervalTextField.setText("1000");

	}

    private void setMainContentPane() {
    	JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createTransitionFiringPanel(),BorderLayout.NORTH);
        contentPane.add(createButtonPanel(),BorderLayout.CENTER);
        setContentPane(contentPane);
    }
    
	private void highlightFirableTransitionsInPrTPanel(Marking marking){
		for (Transition transition: transitionTree.getMID().getTransitions()){
			PipeTransition pipeTransition = transitionTree.getMID().getPipeTransition(transition);
			pipeTransition.setHighlighted(new Unifier(transition, marking).getSubstitutions().size()>0);
		}
	}

   private JPanel createCurrentMarkingPanel(){    	
    	JPanel currentMarkingPanel = new JPanel();
       	currentMarkingPanel.setLayout(new BorderLayout());
       	currentMarkingPanel.add(new JScrollPane(currentTestTextArea), BorderLayout.CENTER);
    	return currentMarkingPanel;
    }

    private JPanel createTestPanel(){    	
    	JPanel testPanel= new JPanel();
    	
       	testPanel.setLayout(new GridBagLayout());

	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    testPanel.add(new JLabel(LocaleBundle.bundleString("TEST_NO")+": "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    testPanel.add(testNoTextField, gridBagConstraints);

	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    testPanel.add(new JLabel(LocaleBundle.bundleString("FAILED_TESTS")+": "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    testPanel.add(failedTestTextField, gridBagConstraints);

	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 2;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    testPanel.add(new JLabel(LocaleBundle.bundleString("Interval")+"(ms): "), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    testPanel.add(timeIntervalTextField, gridBagConstraints);
	    
    	return testPanel;
    }
	
	private JPanel createTransitionFiringPanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(createTestPanel(), BorderLayout.WEST);
			panel.add(createCurrentMarkingPanel(), BorderLayout.CENTER);
			panel.setBorder(new TitledBorder(new EtchedBorder(), ""));
			return panel;
	}
	

    private JPanel createButtonPanel() {
        JPanel pane = new JPanel(); 
        pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pane.add(playButton);
        pane.add(pauseButton);
        pane.add(resetButton);
        pane.add(createJButton(EXIT));
        return pane;
    }

	class OnlineTestingTask extends SwingWorker<Boolean, Void> {
		ArrayList<TransitionTreeNode> leaves;
		int timeInterval=1000;
		OnlineTestingTask(ArrayList<TransitionTreeNode> leaves, int timeInterval){
			this.leaves = leaves;
			this.timeInterval = timeInterval;
		}
	     @Override
	    public Boolean doInBackground() {
	     	for (; !isCancelled() && testIndex<leaves.size(); testIndex++){
	    		ArrayList<TransitionTreeNode> testSequence = transitionTree.getTestSequence(leaves.get(testIndex));
	    		if (testSequence.size()>0) {
	    			if (codeGenerator!=null){
	    				try { codeGenerator.executeSetUp(testSequence.get(0).getMarking());} 
	    				catch (Exception exception){
	    					currentTestTextArea.setText(LocaleBundle.bundleString("FAIL_TO_EXECUTE_THE_SETUP")+exceptionMessage(exception));
	    					pauseButton.setEnabled(false);
	    					return false;}
	    			}
	    			testNoTextField.setText(""+(testIndex+1));
	    			for (int i=1; !isCancelled() && i<testSequence.size(); i++) {
	    				TransitionTreeNode node = testSequence.get(i);
	    				currentTestTextArea.setText(LocaleBundle.bundleString("Test input") +": " + node.getTransition().getEvent() +
	    					PrTEngine.getActualParameterList(node.getTransition(), node.getSubstitution()) +"\n" +
	    					LocaleBundle.bundleString("TEST_ORACLE") +": " +
	    					node.getMarking());
	    				if (codeGenerator!=null) {
	    					try {
	    						codeGenerator.executeTestInput(node.getTransition(), node.getSubstitution());
	    						if (!codeGenerator.checkTestOracles(node.getTransition(), node.getSubstitution(), node.getMarking())) {
	    							numberOfFailures++;
	    							failedTestTextField.setText(""+numberOfFailures);
	    							editor.printInConsoleArea(LocaleBundle.bundleString("FAILED_TEST")+" "+(testIndex+1)+": "+testSequenceString(testSequence, i), false);
	    							break;
	    						} 
	    					}
	    					catch (Exception exception){
	    	   					currentTestTextArea.setText(LocaleBundle.bundleString("FAIL_TO_EXECUTE_THE_TEST")+exceptionMessage(exception));
	    	   					pauseButton.setEnabled(false);
	    	   					return false;
	    					}
	    				}
						if (prtPanel!=null){
							highlightFirableTransitionsInPrTPanel(node.getMarking());
							setTokensInPrTPlaceForSimulation(node.getMarking());
							updateModelPanelUIs();
						}
						try {
							Thread.sleep(timeInterval);
						} catch (Exception e){}
	    			}
	    			if (codeGenerator!=null){
	    				try { codeGenerator.executeTearDown();} 
	    				catch (Exception exception){
	    					currentTestTextArea.setText(LocaleBundle.bundleString("FAIL_TO_EXECUTE_THE_TEARDOWN")+exceptionMessage(exception));
	    					pauseButton.setEnabled(false);
	    					return false;}
	    			}

	    		}
	    	}
	     	if (!isCancelled()){
	     		currentTestTextArea.append("\n"+LocaleBundle.bundleString("ONLINE_TEST_EXECUTION_DONE"));
	     		currentTestTextArea.setCaretPosition(currentTestTextArea.getDocument().getLength());
	     		pauseButton.setEnabled(false);
				Toolkit.getDefaultToolkit().beep();
	     	}
		    return true;
	    }
	}

    private String testSequenceString(ArrayList<TransitionTreeNode> testSequence, int index){
    	String result = "";
		for (int i=1; i<=index; i++) {
			if (i>1) 
				result +=", ";
			TransitionTreeNode node = testSequence.get(i);
			result += node.getTransition().getEvent()+PrTEngine.getActualParameterList(node.getTransition(), node.getSubstitution());
		}
		return result;
    }
    
	public void doExit(){
		if (onlineTestingTask!=null){
			onlineTestingTask.cancel(true);
			try {Thread.sleep(200);
			} catch (Exception ex) {}
			onlineTestingTask=null;
		}
		if (prtPanel!=null){
			highlightFirableTransitionsInPrTPanel(new Marking());
			setTokensInPrTPlaceForSimulation(new Marking());
			setModelPanelsEditingEnabled(true);			
			updateModelPanelUIs();
		}
		editor.resetSimulator();
		dispose();
		if (codeGenerator!=null)
			codeGenerator.terminate();
	}
	
	private OnlineTestingTask onlineTestingTask; 
	synchronized public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == PLAY) {
			int timeInterval = 0;
			try {
				timeInterval = Integer.parseInt(timeIntervalTextField.getText().trim());
				if (timeInterval<1)
					JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("Time interval should be a nonnegative integer"));
				else {
					playButton.setEnabled(false);
					pauseButton.setEnabled(true);
					onlineTestingTask = new OnlineTestingTask(allTests, timeInterval);
			        onlineTestingTask.execute();
				}
			} catch (Exception ex){
				JOptionPane.showMessageDialog(null, LocaleBundle.bundleString("Time interval should be a nonnegative integer"));
			}
		} else
		if	(e.getActionCommand() == PAUSE){
			if (onlineTestingTask!=null){
				onlineTestingTask.cancel(true);
				try {Thread.sleep(200);
				} catch (Exception ex) {}
				onlineTestingTask=null;
			}
			playButton.setEnabled(testIndex<allTests.size());
			pauseButton.setEnabled(false);			
		} else
		if	(e.getActionCommand() == RESET){
			editor.printInConsoleArea("", true);
			if (onlineTestingTask!=null){
				onlineTestingTask.cancel(true);
				try {
					Thread.sleep(200);
				} catch (Exception ex) {}
				onlineTestingTask=null;
				if (prtPanel!=null){
					highlightFirableTransitionsInPrTPanel(new Marking());
					setTokensInPrTPlaceForSimulation(new Marking());
					updateModelPanelUIs();
				}
			}
			testNoTextField.setText("0");
			failedTestTextField.setText("0");
			currentTestTextArea.setText("");
			testIndex = 0;
			numberOfFailures = 0;
			playButton.setEnabled(true);
			pauseButton.setEnabled(false);
		} else
		if	(e.getActionCommand() == EXIT) {
			doExit();
		}
	}

}
