package simulation;

import java.io.Serializable;

import utilities.ReadWriteObj;

public class SimulationOptions implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String SimulationOptionsFileName = "simulation.dat";
	
	private boolean showCurrentStates = true;
	
	public boolean getShowCurrentStates(){
		return showCurrentStates;
	}
	
	public void setShowCurrentStates(boolean flag){
		showCurrentStates = flag;
	}
	
	private boolean printCurrentStates = true;
	
	public boolean getPrintCurrentStates(){
		return printCurrentStates;
	}
	
	public void setPrintCurrentStates(boolean flag){
		printCurrentStates = flag;
	}

	private int timeInterval = 1000;
	
	public int getTimeInterval(){
		return timeInterval;
	}
	
	public void setTimeInterval(int time) {
		this.timeInterval = time;
	}
	
	private boolean verifyGoalsAndAssertions = true;
	
	public boolean getVerifyGoalsAndAssertions(){
		return verifyGoalsAndAssertions;
	}
	
	public void setVerifyGoalsAndAssertions(boolean flag){
		verifyGoalsAndAssertions = flag;
	}

	private boolean createLogs = false;
	
	public boolean getCreateLogs(){
		return createLogs;
	}
	
	public void setCreateLogs(boolean flag){
		createLogs = flag;
	}

	private boolean automaticRestart = true;
	
	public boolean getAutomaticRestart(){
		return automaticRestart;
	}
	
	public void setAutomaticRestart(boolean flag){
		automaticRestart = flag;
	}

	public void saveSimulationOptionsToFile() {
		try {
			ReadWriteObj.write(this, SimulationOptionsFileName);
		} catch (Exception e) {
		}
	}

	public static SimulationOptions readSimulationOptionsFromFile(){
		SimulationOptions simulationOptions = null;
		try {
			simulationOptions = (SimulationOptions) (ReadWriteObj.read(SimulationOptionsFileName));
		}
		catch (Exception e) {
			simulationOptions = new SimulationOptions();
		}
		return simulationOptions;
	}

}
