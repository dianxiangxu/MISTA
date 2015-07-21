package edit;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SubModelPanel extends JPanel {
	private File file;
	private JPanel subModelPanel;
	
	public SubModelPanel(File file, JPanel subModelPanel){
	    setLayout(new BorderLayout());
		this.file = file;
		this.subModelPanel = subModelPanel;
	    add(subModelPanel, BorderLayout.CENTER);
	}
	
	public JPanel getSubModelPanel(){
		return subModelPanel;
	}
	
	public String getTitle(){
		return "["+file.getName()+"]";
	}
	
	public boolean isSubModelForFile(File thatFile){
		return file.getAbsolutePath().equalsIgnoreCase(thatFile.getAbsolutePath());
	}
}
