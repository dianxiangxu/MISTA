package main;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

import kernel.StatusPanel;

public class InfoPanel extends StatusPanel {
	private static final long serialVersionUID = 1L;

	private static Font infoFont = new Font("SansSerif", Font.PLAIN, 11);  
	
	private JLabel message = new JLabel("");
	private JLabel status = new JLabel("");
	
	public InfoPanel(){
		message.setFont(infoFont);
		status.setFont(infoFont);
		add(message);
		add(status);
	}
	
	public void reset(){
		message.setText("");
		status.setText("");
	}
	
	public void setMessage(String msg) {
		message.setText(msg);
	}

	public void setStatus(String msg) {
		status.setText(msg);
	}

	public void setPreferredLabelSize(){
		message.setPreferredSize(new Dimension(getWidth()/2, 15));
		status.setPreferredSize(new Dimension(getWidth()/2-50, 15));
	}
}
