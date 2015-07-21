package edit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import locales.LocaleBundle;

public class SeparateTextAreaCellEditor extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JTextArea originalTextArea;
	private JTextArea newTextArea;
	
	private static final String OK = "Ok";
	private static final String CANCEL = "Cancel";
	
    public SeparateTextAreaCellEditor(JTextArea textArea) {
    	super(new JFrame(), LocaleBundle.bundleString("Edit Table Cell"), true);
    	this.originalTextArea = textArea;
    	newTextArea= new JTextArea(15, 80);
    	newTextArea.setText(originalTextArea.getText());
    	newTextArea.setFont(originalTextArea.getFont());
		newTextArea.setTabSize(4);
    	setMainContentPane();
		pack();
		setVisible(true); 
    }

    private void setMainContentPane() {
        JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(1,1,0,1));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JScrollPane(newTextArea), BorderLayout.CENTER);
        contentPane.add(createButtonPane(),BorderLayout.SOUTH);
        setContentPane(contentPane);
    }
    
	private JButton createJButton(String command){
		JButton button = new JButton(LocaleBundle.bundleString(command));
		button.setActionCommand(command);
		button.addActionListener(this);
		return button;
	} 

    private JComponent createButtonPane() {
        JPanel pane = new JPanel(); 
        pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pane.add(createJButton(OK));
        pane.add(createJButton(CANCEL));
        return pane;
    }

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == OK) {
			originalTextArea.setText(newTextArea.getText());
		}
		dispose();
	}
}