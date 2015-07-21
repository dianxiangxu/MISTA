/* 	
	Author Dianxiang Xu
*/
package kernel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import edit.GeneralEditor;
import edit.XMIDEditor;

import kernel.SystemOptions.ModelType;
import locales.LocaleBundle;

import netconverter.NetConverter;
import netconverter.Verifier;

import testgeneration.TransitionTree;
import testgeneration.TransitionTreeForDeadlockStateCoverage;
import testgeneration.TransitionTreeForRoundTripWithTransitionCap;
import testgeneration.TransitionTreeNode;
import verification.AssertionVerifier;
import verification.AssertionVerifierBFS;
import verification.AssertionVerifierDFS;
import verification.GoalVerifier;
import verification.GoalVerifierBFS;
import verification.GoalVerifierDFS;

import mid.Functions;
import mid.GoalProperty;
import mid.MID;
import mid.Marking;
import mid.Predicate;
import mid.Transition;
import mid.TupleFactory;

public class VerificationManager implements ActionListener {
	private Kernel kernel;

	public VerificationManager(Kernel kernel) {
		this.kernel = kernel; 
	} 
	
	public void actionPerformed(ActionEvent e) {
		if (kernel.getFileManager()!=null){
			GeneralEditor editor=kernel.getFileManager().getEditor();
			if (editor!=null && editor instanceof XMIDEditor && ((XMIDEditor)editor).isSimulationMode()){
				kernel.printDialogMessage(LocaleBundle.bundleString("Simulation mode"));
				return;
			}	
		}
		String command = e.getActionCommand();
		if (command == Commands.IntegratedGoalReachabilityAnalysis) {
			checkForGoalReachability(command);
		} else		
		if (command==Commands.TransitionReachabilityAnalysis){
			checkForTransitionReachability();
		} else		
		if (command==Commands.DEADLOCKSTATES){
			checkForDeadlockStates();
		} else		
		if (command==Commands.CHECKASSERTIONS){
			checkAssertions();
		} else	
			kernel.printDialogMessage("Under construction... ");
	}

	private void checkForTransitionReachability(){
		if (!isVerificationApplicable())
			return;
		MID mid = kernel.getFileManager().parse();
		if (mid==null)
			return;
		if (mid.getInitialMarkings().size()==0){
			kernel.printDialogMessage(LocaleBundle.bundleString("No initial state is specified"));
			return;
		}	
		ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Check transition reachability"), LocaleBundle.bundleString("Checking for transition reachability"));
		Thread checkTransitionReachabilityThread = new Thread(new CheckTransitionReachabilityThread(mid, progressDialog));
		checkTransitionReachabilityThread.start();
		progressDialog.setVisible(true);
	}

	class CheckTransitionReachabilityThread implements Runnable {
		private MID mid;
		private ProgressDialog progressDialog;
		
		CheckTransitionReachabilityThread(MID mid, ProgressDialog progressDialog) {
			this.mid = mid;
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				TransitionTreeForRoundTripWithTransitionCap tree = 
					new TransitionTreeForRoundTripWithTransitionCap(mid, kernel.getSystemOptions());
				tree.setDirtyTestsAllowd(false);
				tree.setProgressDialog(progressDialog);
				tree.generateTransitionTree();
				progressDialog.dispose();
				if (tree.hasDeadTransitions())
					kernel.getFileManager().getEditor().printInConsoleArea(
							LocaleBundle.bundleString("The following transitions are not reachable")+
							"("+LocaleBundle.bundleString("depth")+" "+kernel.getSystemOptions().getSearchDepth()
							+"):\n"+tree.getDeadTransitions(),true);
				else {
					kernel.printDialogMessage(LocaleBundle.bundleString("All transitions are reachable"));
				}
			} 
			catch (CancellationException e){
				kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Reachability analysis canceled")+"\n", false);
				progressDialog.dispose();
			}
		}
	}
	
	
	public void checkForGoalReachability(String strategy){
		if (!isVerificationApplicable())
			return;
		MID mid = kernel.getFileManager().parse();
		if (mid==null)
			return;
		if (mid.getInitialMarkings().size()==0){
			kernel.printDialogMessage(LocaleBundle.bundleString("No initial state is specified"));
			return;
		}	
		if (mid.getGoalProperties().size()==0){
			kernel.printDialogMessage(LocaleBundle.bundleString("No goal state is specified"));
			return;
		}	
		ProgressDialog progressFrame = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Reachability Analysis"), 
				LocaleBundle.bundleString("Checking for goal reachability"));
		Thread verificationThread = new Thread(new IntegratedGoalReachabilityAnalysisThread(progressFrame, mid));
		verificationThread.start();
		progressFrame.setVisible(true);
	}

	private boolean isVerificationApplicable(){
		ModelType modelType = kernel.getFileManager().getEditor().getModelType();
		boolean applicable = 
			modelType == SystemOptions.ModelType.FUNCTIONNET ||
			modelType == SystemOptions.ModelType.ABAC ||
			modelType == SystemOptions.ModelType.CONTRACT ||
			modelType == SystemOptions.ModelType.STATEMACHINE ||
			modelType == SystemOptions.ModelType.THREATNET;
		if (!applicable)
			kernel.printDialogMessage(LocaleBundle.bundleString("Verification does not apply to")+" "+LocaleBundle.bundleString(SystemOptions.getModelTypeString(modelType)).toLowerCase()+"!");
		return applicable;
	}
	
	private static boolean isPlanningGraphAnalysisApplicable(MID mid){
		String[] notApplicablePredicates = {
				Functions.ADD, 
				Functions.SUBTRACT,
				Functions.MULTIPLY,
				Functions.DIVIDE,
				Functions.MODULUS,
				Functions.ASSERT, 
				Functions.TOKENCOUNT
				};
		for (Transition transition: mid.getTransitions()){
			if (transition.getWhenCondition()!=null){
				for (Predicate predicate: transition.getWhenCondition()){
					String name = predicate.getName();
					for (String naPred: notApplicablePredicates){
						if (name.equalsIgnoreCase(naPred))
							return false;
					}
				}
			}
		}
		for (GoalProperty goalProperty: mid.getGoalProperties()){
			if (goalProperty.getAllVariables()!=null && goalProperty.getAllVariables().size()>0 || (goalProperty.getWhenCondition()!=null && goalProperty.getWhenCondition().size()>0))
				return false;
		}
		return true;
	}

	private static ArrayList<Marking> tranformGoalPropertiesToGoalMarkings(MID mid){
		ArrayList<Marking> goalMarkings = new ArrayList<Marking>();
		for (GoalProperty goalProperty: mid.getGoalProperties()){
			Marking goalMarking = new Marking();
			for (Predicate predicate: goalProperty.getPrecondition())
				goalMarking.addTuple(predicate.getName(),TupleFactory.createTuple(predicate.getArguments()));
			goalMarkings.add(goalMarking);
		}
		return goalMarkings;
	} 
	
	class IntegratedGoalReachabilityAnalysisThread implements Runnable {
		private ProgressDialog progressFrame;
		private MID mid;
		
		IntegratedGoalReachabilityAnalysisThread(ProgressDialog progressFrame, MID mid) {
			this.progressFrame = progressFrame;
			this.mid = mid;
		}
		
		public void run () {
			try {
				boolean isBreadthFirstSearch = kernel.getSystemOptions().isBreadthFirstSearch(); 
				int searchDepth = kernel.getSystemOptions().getSearchDepth();
				boolean searchForHomeStates = kernel.getSystemOptions().searchForHomeStates();
//				long start= System.currentTimeMillis();
//				GoalVerifier verifier = getGoalVerifier(mid, isBreadthFirstSearch, searchDepth, searchForHomeStates, progressFrame);
				GoalVerifier verifier = getPropertyVerifier(mid, isBreadthFirstSearch, searchDepth, searchForHomeStates, progressFrame);
//				long end = System.currentTimeMillis();
				progressFrame.dispose();
				String result = verifier.reportResult();
//				result += LocaleBundle.bundleString("Analysis completed in")+" " +((end-start)/1000.0) +" "+LocaleBundle.bundleString("seconds");
				kernel.getFileManager().getEditor().printInConsoleArea(result, true);
			}
			catch (CancellationException e){
				kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Reachability analysis canceled")+"\n", false);
				progressFrame.dispose();				
			}			
		}
	}

	public static GoalVerifier getPropertyVerifier(MID mid, boolean isBreadthFirstSearch, int searchDepth, boolean searchForHomeStates, ProgressDialog progressFrame) throws CancellationException{
		GoalVerifier verifier=null;
		if (isBreadthFirstSearch) { // planning graph is a breadth-first strategy
			if (isPlanningGraphAnalysisApplicable(mid)){
				ArrayList<Marking> goalMarkings = tranformGoalPropertiesToGoalMarkings(mid);
				NetConverter converter = new NetConverter(mid);
				// planning graph may report incorrect firings (found in blocks)
				// 12/27/2011 planning graph found no paths to the reachable goals in Magento
				Verifier pgVerifier= converter.analyzePlanningGraph(goalMarkings, searchDepth, progressFrame);
				pgVerifier.removeInvalidFiringSequences();
				verifier = new GoalVerifierBFS(mid, searchDepth, searchForHomeStates, progressFrame, pgVerifier.getFiringSequences(), true);
			} else {
				verifier = new GoalVerifierBFS(mid, searchDepth, searchForHomeStates, progressFrame);
			}
		}
		else 
			verifier = new GoalVerifierDFS(mid, searchDepth, searchForHomeStates, progressFrame);
		return verifier;
	}

	
	private void checkForDeadlockStates(){
		if (!isVerificationApplicable())
			return;
		MID mid = kernel.getFileManager().parse();
		if (mid==null)
			return;
		if (mid.getInitialMarkings().size()==0){
			kernel.printDialogMessage(LocaleBundle.bundleString("No initial state is specified"));
			return;
		}	
		TransitionTree tree = new TransitionTreeForDeadlockStateCoverage(mid, kernel.getSystemOptions());
		ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("Check Deadlock States"), LocaleBundle.bundleString("Checking for deadlock states"));
		Thread deadlockStateCheckingThread = new Thread(new DeadlockStateCheckingThread(tree, progressDialog));
		deadlockStateCheckingThread.start();
		progressDialog.setVisible(true);
	}

	class DeadlockStateCheckingThread implements Runnable {
		
		private TransitionTree transitionTree;
		private ProgressDialog progressDialog;
		
		DeadlockStateCheckingThread(TransitionTree tree, ProgressDialog progressDialog) {
			this.transitionTree = tree;
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				transitionTree.setProgressDialog(progressDialog);
				transitionTree.generateTransitionTree();
				if (transitionTree.getRoot().children().size()==0){
					progressDialog.dispose();
					kernel.printDialogMessage(LocaleBundle.bundleString("No deadlock state found"));
				}
				else {
					GeneralEditor editor = kernel.getFileManager().getEditor();
					editor.printInConsoleArea(LocaleBundle.bundleString("Sequences leading to deadlock states"), true);
					ArrayList<TransitionTreeNode> allLeaves = transitionTree.getAllTests();
					String initStateIndex = "1";
					int sequenceNo =1;
					for (TransitionTreeNode leaf : allLeaves) {
							ArrayList<TransitionTreeNode> testSequence = transitionTree.getTestSequence(leaf);
							if (!testSequence.get(0).getOutlineNumber().equals(initStateIndex))
								editor.printInConsoleArea("\n"+LocaleBundle.bundleString("Initial State")+":"+testSequence.get(0).getOutlineNumber());		
							initStateIndex = testSequence.get(0).getOutlineNumber();
							editor.printInConsoleArea(sequenceNo+". "+transitionTree.getSequenceString(testSequence));
							editor.printInConsoleArea(LocaleBundle.bundleString("Resultant State")+": "+leaf.getMarking());
							sequenceNo++;
					}
					editor.printInConsoleArea("\n");
					progressDialog.dispose();
				}
			} 
			catch (CancellationException e){
				kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Deadlock checking canceled")+"\n", false);
				progressDialog.dispose();
				return;
			}
		}
	}
	
	private void checkAssertions(){
		if (!isVerificationApplicable())
			return;
		MID mid = kernel.getFileManager().parse();
		if (mid==null)
			return;
		if (mid.getInitialMarkings().size()==0){
			kernel.printDialogMessage(LocaleBundle.bundleString("No initial state is specified"));
			return;
		}	
		if (mid.getAssertionProperties().size()==0){
			kernel.printDialogMessage(LocaleBundle.bundleString("NO_ASSERTION_IS_SPECIFIED"));
			return;
		}	
		ProgressDialog progressDialog = new ProgressDialog(kernel.getParentFrame(), LocaleBundle.bundleString("VERIFY_ASSERTIONS"), LocaleBundle.bundleString("VERIFYING_ASSERTIONS"));
		Thread checkAssertionsThread = new Thread(new CheckAssertionsThread(mid, progressDialog));
		checkAssertionsThread.start();
		progressDialog.setVisible(true);
	}

	class CheckAssertionsThread implements Runnable {
		
		private MID mid;
		private ProgressDialog progressDialog;
		
		CheckAssertionsThread(MID mid, ProgressDialog progressDialog) {
			this.mid = mid;
			this.progressDialog = progressDialog;
		}
		
		public void run () {
			try {
				int searchDepth = kernel.getSystemOptions().getSearchDepth();
//				long start= System.currentTimeMillis();
				AssertionVerifier verifier = kernel.getSystemOptions().isBreadthFirstSearch()?
						new AssertionVerifierBFS(mid, searchDepth, progressDialog):
							new AssertionVerifierDFS(mid, searchDepth, progressDialog);
//				long end = System.currentTimeMillis();
				progressDialog.dispose();
				String result = verifier.reportResult();
//				result += LocaleBundle.bundleString("Analysis completed in")+" " +((end-start)/1000.0) +" "+LocaleBundle.bundleString("seconds");
				kernel.getFileManager().getEditor().printInConsoleArea(result, true);
			}
			catch (CancellationException e){
				kernel.getFileManager().getEditor().printInConsoleArea(LocaleBundle.bundleString("Verification canceled")+"\n", false);
				progressDialog.dispose();				
			}			
		}
	}


}
