package kernel;
import javax.swing.*;

import testcode.GoalTagCode;

import java.awt.*;
import java.awt.event.*;

import locales.LocaleBundle;

public class TestFrameworkGoalTagEditor extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JTextArea textArea;
	private GoalTagCode goalTagCode;
	
	private static final String OK = "Ok";
	private static final String CANCEL = "Cancel";
	
    public TestFrameworkGoalTagEditor(JFrame parentFrame, GoalTagCode goalTagCode) {
    	super(parentFrame, goalTagCode.getTitle()+LocaleBundle.bundleString("Goal Tag Code"), true);
    	this.goalTagCode = goalTagCode;
    	textArea= new JTextArea(5, 80);
    	textArea.setText(goalTagCode.getTagCode());
		textArea.setTabSize(4);
    	setMainContentPane();
		pack();
		setLocationRelativeTo(parentFrame);
		setVisible(true); 
    }
    
    private void setMainContentPane() {
        JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(1,1,0,1));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
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
			goalTagCode.setTagCode(textArea.getText());
		}
		dispose();
	}
}