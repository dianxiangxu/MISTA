package edit;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

import kernel.Kernel;
import locales.LocaleBundle;


public class MultipleChoicesDialog extends JDialog
                        implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private Vector<String> choices;
	private JList listOfChoices;
	
	private static final String APPLY = "Apply";
	private static final String CANCEL = "Cancel";
	private JTextArea textArea;
	
    public MultipleChoicesDialog(Kernel kernel, Point location, String title, Vector<String> choices, JTextArea textArea) {
    	super(kernel.getParentFrame(), title, true);
    	this.textArea = textArea;
    	this.choices = choices;
    	listOfChoices = new JList(choices);
    	initializeSelectedIndices();
    	setMainContentPane();
		pack();
		setLocation(location);
		setVisible(true); 
    }

    private void initializeSelectedIndices(){
    	ArrayList<String> selections = parseStringList(textArea.getText());
    	if (selections!=null) {
    		for (int i=selections.size()-1; i>=0; i--)
    			if (!choices.contains(selections.get(i)))
    				selections.remove(i);
    		if (selections.size()>0) {
    			int[] indices = new int[selections.size()];
    			for (int i=0; i<indices.length; i++)
    				indices[i] = choices.indexOf(selections.get(i));
    			listOfChoices.setSelectedIndices(indices);
    		}
    	}
    }
    
	private ArrayList<String> parseStringList(String readString) {
		ArrayList<String> list = null;
		try {
			list = MIDParser.parseIdentifierListString(readString);
		}
		catch (ParseException e) {
		}
		catch (TokenMgrError e){
		}
		return list;
	}

    private void setMainContentPane() {
        JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createChoicesPanel(),BorderLayout.NORTH);
        contentPane.add(createButtonPane(),BorderLayout.SOUTH);
        setContentPane(contentPane);
    }
    

	private JPanel createChoicesPanel() {
		JPanel choicesPanel = new JPanel();
		choicesPanel.setLayout(new BorderLayout());
		JScrollPane listScroller = new JScrollPane(listOfChoices);
		listOfChoices.setVisibleRowCount(Math.min(choices.size(), 20));
		choicesPanel.add(listScroller,BorderLayout.NORTH);
		return choicesPanel;
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
        pane.add(createJButton(APPLY));
        pane.add(createJButton(CANCEL));
        return pane;
    }
    
    public synchronized void valueChanged(ListSelectionEvent e) {

    }

    private String getChoicesListString(){
		Object[] choices = listOfChoices.getSelectedValues();
		if (choices.length==0)
			return "";
		String listString = (String)choices[0];
		for (int i=1; i<choices.length; i++)
			listString+= ", "+ (String)choices[i];
		return listString;
    }

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == APPLY) {
			textArea.setText(getChoicesListString());
			dispose();
		} else
		if	(e.getActionCommand() == CANCEL) 
			dispose();
	}
}