package edit;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import java.awt.*;
import java.awt.event.*;

import kernel.Kernel;
import kernel.SystemOptions;
import locales.LocaleBundle;


public class EditingPreferences extends JDialog
                        implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private Kernel kernel;

	private JRadioButton useGraphicalNetEditorButton, useTabularNetEditorButton;
	private JRadioButton useGraphicalStateMachineEditorButton, useTabularStateMachineEditorButton;
	private JRadioButton useGraphicalThreatTreeEditorButton, useTabularThreatTreeEditorButton;
	
	private JRadioButton netHierarchyEnabledButton, netHierarchyDisabledButton;
	
	private GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private String[] faceNames =  e.getAvailableFontFamilyNames();
	private String[] fontTypes = { "Plain", "Bold", "Italic", "Bold and Italic"};
	private String[] fontSizes = { "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22", "24", "28", "32", "36"};

	private JList faceNameList = new JList(faceNames);
	private JList fontTypeList = new JList(fontTypes);
	private JList fontSizeList = new JList(fontSizes);
	
	private JTextField faceName;
	private JTextField fontType;
	private JTextField fontSize;

	private JLabel sampleText = new JLabel("____Software Testing____");

	private Font textFont;
	private boolean isTextFontChanged = false;
	
	private static final String APPLY = "Apply";
	private static final String CANCEL = "Cancel";
	
	
    public EditingPreferences(Kernel kernel) {
    	super(kernel.getParentFrame(), LocaleBundle.bundleString("Editing Preferences"), true);
    	this.kernel = kernel;
    	setMainContentPane();
		pack();
        if (kernel != null)
    		setLocationRelativeTo(kernel.getParentFrame());
		setVisible(true); 
    }

    private void setMainContentPane() {
        JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createPreferencesPanel(),BorderLayout.NORTH);
        contentPane.add(createFontPanel(),BorderLayout.CENTER);
        contentPane.add(createButtonPane(),BorderLayout.SOUTH);
        setContentPane(contentPane);
    }
    
	private JPanel createNetEditorPreferencePanel(){
		useGraphicalNetEditorButton = new JRadioButton(LocaleBundle.bundleString("Graphical editor"));
		useGraphicalNetEditorButton.addActionListener(this);
		useGraphicalNetEditorButton.setSelected(kernel.getSystemOptions().useGraphicalNetEditor());

		useTabularNetEditorButton = new JRadioButton(LocaleBundle.bundleString("Spreadsheet editor"));
		useTabularNetEditorButton.addActionListener(this);
		useTabularNetEditorButton.setSelected(!kernel.getSystemOptions().useGraphicalNetEditor());

		ButtonGroup group = new ButtonGroup();
		group.add(useGraphicalNetEditorButton);
		group.add(useTabularNetEditorButton);

		JPanel panel = new JPanel();
		panel.add(useGraphicalNetEditorButton);
		panel.add(useTabularNetEditorButton);
		
		return panel;
	}

	private JPanel createFunctionNetHierarchyPanel(){
		netHierarchyEnabledButton = new JRadioButton(LocaleBundle.bundleString("Enable hierarchy"));
		netHierarchyEnabledButton.addActionListener(this);
		netHierarchyEnabledButton.setSelected(kernel.getSystemOptions().isNetHierarchyEnabled());

		netHierarchyDisabledButton = new JRadioButton(LocaleBundle.bundleString("Disable hierarchy"));
		netHierarchyDisabledButton.addActionListener(this);
		netHierarchyDisabledButton.setSelected(!kernel.getSystemOptions().isNetHierarchyEnabled());

		ButtonGroup group = new ButtonGroup();
		group.add(netHierarchyEnabledButton);
		group.add(netHierarchyDisabledButton);

		JPanel panel = new JPanel();
		panel.add(netHierarchyEnabledButton);
		panel.add(netHierarchyDisabledButton);
		
		netHierarchyEnabledButton.setEnabled(useGraphicalNetEditorButton.isSelected());
		netHierarchyDisabledButton.setEnabled(useGraphicalNetEditorButton.isSelected());
		return panel;
	}

	private JPanel createStateMachineEditorPreferencePanel(){
		useGraphicalStateMachineEditorButton = new JRadioButton(LocaleBundle.bundleString("Graphical editor"));
		useGraphicalStateMachineEditorButton.addActionListener(this);
		useGraphicalStateMachineEditorButton.setSelected(kernel.getSystemOptions().useGraphicalStateMachineEditor());

		useTabularStateMachineEditorButton = new JRadioButton(LocaleBundle.bundleString("Spreadsheet editor"));
		useTabularStateMachineEditorButton.addActionListener(this);
		useTabularStateMachineEditorButton.setSelected(!kernel.getSystemOptions().useGraphicalStateMachineEditor());

		ButtonGroup group = new ButtonGroup();
		group.add(useGraphicalStateMachineEditorButton);
		group.add(useTabularStateMachineEditorButton);

		JPanel panel = new JPanel();
		panel.add(useGraphicalStateMachineEditorButton);
		panel.add(useTabularStateMachineEditorButton);

		return panel;
	}

	private JPanel createThreatTreeEditorPreferencePanel(){
		useGraphicalThreatTreeEditorButton = new JRadioButton(LocaleBundle.bundleString("Graphical editor"));
		useGraphicalThreatTreeEditorButton.addActionListener(this);
		useGraphicalThreatTreeEditorButton.setSelected(kernel.getSystemOptions().useGraphicalThreatTreeEditor());

		useTabularThreatTreeEditorButton = new JRadioButton(LocaleBundle.bundleString("Spreadsheet editor"));
		useTabularThreatTreeEditorButton.addActionListener(this);
		useTabularThreatTreeEditorButton.setSelected(!kernel.getSystemOptions().useGraphicalThreatTreeEditor());

		ButtonGroup group = new ButtonGroup();
		group.add(useGraphicalThreatTreeEditorButton);
		group.add(useTabularThreatTreeEditorButton);

		JPanel panel = new JPanel();
		panel.add(useGraphicalThreatTreeEditorButton);
		panel.add(useTabularThreatTreeEditorButton);

		return panel;
	}

	private JPanel createFontPanel() {
	    JPanel fontPanel = new JPanel();
	    fontPanel.setLayout(new BorderLayout());
	    fontPanel.add(createBox(), BorderLayout.NORTH);
	    fontPanel.add(createSampleText(),BorderLayout.CENTER);
		fontPanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("Text Font")));

		return fontPanel;
	}

	private JPanel createPreferencesPanel() {
		int row =0;
		JPanel preferencesPanel = new JPanel();
	    preferencesPanel.setLayout(new GridBagLayout());
	    GridBagConstraints gridBagConstraints = new GridBagConstraints();

	    String netTitle = (SystemOptions.isModelTypeKeyword(SystemOptions.THREATNET_KEYWORD)?
	    		LocaleBundle.bundleString("Function-threat nets"): LocaleBundle.bundleString("Function nets"))+":";
	    	
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = row++;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    preferencesPanel.add(new JLabel(netTitle), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    preferencesPanel.add(createNetEditorPreferencePanel(), gridBagConstraints);

	    gridBagConstraints = new GridBagConstraints();
	    	
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = row++;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    preferencesPanel.add(new JLabel(LocaleBundle.bundleString("Net hierarchy")+":"), gridBagConstraints);

	    gridBagConstraints.gridx = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    preferencesPanel.add(createFunctionNetHierarchyPanel(), gridBagConstraints);
	    
 	    if (SystemOptions.isModelTypeKeyword(SystemOptions.STATEMACHINE_KEYWORD)){
 	    	gridBagConstraints = new GridBagConstraints();
	      
 	    	gridBagConstraints.gridx = 0;
 	    	gridBagConstraints.gridy = row++;
 	    	gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
 	    	gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
 	    	preferencesPanel.add(new JLabel(LocaleBundle.bundleString("State machines")+":"), gridBagConstraints);

 	    	gridBagConstraints.gridx = 1;
 	    	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 	    	preferencesPanel.add(createStateMachineEditorPreferencePanel(), gridBagConstraints);
 	    }

 	    if (SystemOptions.isModelTypeKeyword(SystemOptions.THREATTREE_KEYWORD)){
	      gridBagConstraints = new GridBagConstraints();
	      gridBagConstraints.gridx = 0;
	      gridBagConstraints.gridy = row++;
	      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
	      preferencesPanel.add(new JLabel(LocaleBundle.bundleString("Threat trees")+":"), gridBagConstraints);

	      gridBagConstraints.gridx = 1;
		  gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	      preferencesPanel.add(createThreatTreeEditorPreferencePanel(), gridBagConstraints);
 	    }
		
		preferencesPanel.setBorder(new TitledBorder(new EtchedBorder(), LocaleBundle.bundleString("Default Model Editor")));
		return preferencesPanel;
	}

    public synchronized void valueChanged(ListSelectionEvent e) {
    	if (e.getSource() != faceNameList && e.getSource() != fontTypeList &&
    			e.getSource() != fontSizeList)
    		return;
    	
    	if (e.getSource() == faceNameList)
    		faceName.setText((String)faceNameList.getSelectedValue());
   		else
    	if (e.getSource() == fontTypeList)
    		fontType.setText((String)fontTypeList.getSelectedValue());
   		else
    	if (e.getSource() == fontSizeList)
    		fontSize.setText((String)fontSizeList.getSelectedValue());
    	isTextFontChanged = true;
    	textFont = createFont();
    	sampleText.setFont(textFont);
    	sampleText.repaint();
    }
    	
    protected JComponent createBox() {
    	    	    	
        JLabel faceNameLabel = new JLabel(LocaleBundle.bundleString("Name")+":");
        JLabel fontTypeLabel = new JLabel(LocaleBundle.bundleString("Style")+":");
        JLabel fontSizeLabel = new JLabel(LocaleBundle.bundleString("Size")+":");
        textFont = kernel.getSystemOptions().getTextFont();
    	faceName = new JTextField(textFont.getName(),20);
    	fontType = new JTextField(fontTypes[textFont.getStyle()],10);
    	fontSize = new JTextField(""+textFont.getSize(),4);

        JScrollPane faceNameScrollPane = createFontScrollPane(faceNameList, faceName);
        JScrollPane fontTypeScrollPane = createFontScrollPane(fontTypeList, fontType);
        JScrollPane fontSizeScrollPane = createFontScrollPane(fontSizeList, fontSize);
        
        JPanel facePanel = createFontElementPanel(faceNameLabel, faceName, faceNameScrollPane);
        JPanel typePanel = createFontElementPanel(fontTypeLabel, fontType, fontTypeScrollPane);
        JPanel sizePanel = createFontElementPanel(fontSizeLabel, fontSize, fontSizeScrollPane);

        JPanel selection = new JPanel();
        selection.add(facePanel);
        selection.add(typePanel);
        selection.add(sizePanel);
//        selection.setBorder(BorderFactory.createTitledBorder(null, "Font", 0, 0, null, Color.black));
        
        return selection;
    }
    private JScrollPane createFontScrollPane(JList faceList, JTextField faceName) {
    	faceName.setEditable(false);
    	faceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	faceList.setSelectedValue(faceName.getText(), true);
    	faceList.setVisibleRowCount(5);
    	faceList.addListSelectionListener(this);
    	JScrollPane listFontName = new JScrollPane(faceList);
    	return listFontName;
    }
    private JPanel createFontElementPanel(JLabel label, JTextField field, JScrollPane list) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.add(list, BorderLayout.SOUTH);
        return panel; 
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

    private JPanel createSampleText(){
        JPanel samplePanel = new JPanel(); 
        samplePanel.setBorder(BorderFactory.createTitledBorder(null, LocaleBundle.bundleString("Preview"), 0, 0, null, Color.black));
        samplePanel.setPreferredSize(new Dimension(200,80));
        samplePanel.add(sampleText);
        sampleText.setFont(getFont());
        sampleText.setForeground(Color.BLUE);
        return samplePanel;
    }
    
    private Font createFont(){
    	int type =0;
    	while (type<fontTypes.length) {
    		if (fontType.getText().equals(fontTypes[type]))
    			break;
    		type++;
    	}
    	return new Font(faceName.getText(), type, Integer.parseInt(fontSize.getText()));
    }
    
	private void saveOptions(){
		kernel.getSystemOptions().setUseGraphicalNetEditor(useGraphicalNetEditorButton.isSelected());
		if (SystemOptions.isModelTypeKeyword(SystemOptions.STATEMACHINE_KEYWORD))
			kernel.getSystemOptions().setUseGraphicalStateMachineEditor(useGraphicalStateMachineEditorButton.isSelected());
		if (kernel.getSystemOptions().isNetHierarchyEnabled()!=netHierarchyEnabledButton.isSelected()){
			kernel.getSystemOptions().setNetHierarchyEnabled(netHierarchyEnabledButton.isSelected());
			kernel.getFileManager().getEditor().stateChanged(null);
//			kernel.getFileManager().getEditor().closeSubModelPanels();	// maybe unsaved!
		}
		if (SystemOptions.isModelTypeKeyword(SystemOptions.THREATTREE_KEYWORD))
			kernel.getSystemOptions().setUseGraphicalThreatTreeEditor(useGraphicalThreatTreeEditorButton.isSelected());
		if (isTextFontChanged){
			kernel.getSystemOptions().setTextFont(textFont);
			kernel.getFileManager().setTextFont(textFont);
		}
		kernel.getSystemOptions().saveSystemOptionsToFile();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==useGraphicalNetEditorButton || e.getSource()==useTabularNetEditorButton){
			netHierarchyEnabledButton.setEnabled(useGraphicalNetEditorButton.isSelected());
			netHierarchyDisabledButton.setEnabled(useGraphicalNetEditorButton.isSelected());
		} else
		if (e.getActionCommand() == APPLY) {
			saveOptions();
			dispose();
		} else
		if	(e.getActionCommand() == CANCEL) 
			dispose();
	}
}