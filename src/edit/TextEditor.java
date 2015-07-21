package edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import kernel.Kernel;
import locales.LocaleBundle;

import utilities.FileUtil;

import mid.MID;

public class TextEditor extends GeneralEditor implements DocumentListener {
	
	private static String midTab = LocaleBundle.bundleString("XMID Specification");

	private JTextArea editArea;
	private JScrollPane editScrollPane; // for editArea listener
		
	public TextEditor(Kernel kernel, boolean editable){
		super(kernel, editable);
		editArea = new JTextArea();
		editScrollPane = new JScrollPane(editArea);
		initializeEditArea();
	    editTabbedPane.addChangeListener(this);		
    	kernel.updateToolBar(null);
	}
	
	public TextEditor(Kernel mainFrame, boolean editable, String text){
		this(mainFrame, editable);
		editArea.setText(text);
		xmidSaved = true;
		editArea.setCaretPosition(0);
    	kernel.updateToolBar(null);
	}

	public TextEditor(Kernel mainFrame, boolean editable, File file){
		this(mainFrame, editable);
		this.midFile = file;
		Scanner in = null; 
		try {
			in = new Scanner(new FileReader(file));
			while (in.hasNextLine()){
				editArea.append(in.nextLine());
				editArea.append("\n");
			}
		} catch (IOException ioe){
		}
		if (in!=null)
			in.close();
		xmidSaved = true;
		editArea.setCaretPosition(0);
    	kernel.updateToolBar(null);
	}
	
	public JComponent getEditingJComponent(){
		if (testTreeComponent==null && testCodeComponent==null)
			return editScrollPane;
		else 
			return editTabbedPane;
	}
	
	public void addComponentToTabbedPane(String tab, String iconPath, JComponent component){
		if (editTabbedPane.getTabCount()==0){
			ImageIcon icon = (midFile!=null)? FileUtil.getFileImageIcon(midFile.getName()):  FileUtil.createImageIcon("images/mid.png");
			editTabbedPane.addTab(midTab, icon, editScrollPane);
		}
		editTabbedPane.addTab(tab, component);
		editTabbedPane.setTabComponentAt(editTabbedPane.getTabCount()-1, new ButtonTabComponent(tab, iconPath, editTabbedPane, this));
		editTabbedPane.setSelectedComponent(component);
		kernel.updateContentPane();
	}
	
	public void updateTabbedPane(){
		if (editTabbedPane.getTabCount()==1){	// text editor
			editTabbedPane.remove(0);
			kernel.updateContentPane();
		}
		else
			super.updateTabbedPane();
	}
	
	public void setTextFont(Font newFont) {
		super.setTextFont(newFont);
     	editArea.setFont(newFont);
    	editArea.repaint();
	}

	public JTextArea getEditArea(){
		return editArea;
	}
	
	public void saveSpecificationToFile(String fileName, boolean needDialog){
		FileUtil.saveStringToTextFile(editArea.getText(), fileName);
		xmidSaved = true;
		if (needDialog)
			kernel.printDialogMessage(LocaleBundle.bundleString("File saved"));

	}

	public void saveSpecificationToFile(File file, boolean needDialog){
		FileUtil.saveStringToTextFile(editArea.getText(), file);
		xmidSaved = true;
		if (needDialog)
			kernel.printDialogMessage(LocaleBundle.bundleString("File saved"));
	}

	
	public String getText(JTextArea editArea){
		return editArea.getText();
	}
	
	public MID parse(){
		MID mid = null;
		return mid;
	}
	
	private void initializeEditArea(){
		editArea.setEnabled(true);
		editArea.setEditable(isEditable);
		editArea.setFont(getTextFont());
     	editArea.setLineWrap(true);
     	editArea.setWrapStyleWord(true);
		editArea.addCaretListener(this);		
		editArea.setName("EditArea");
		editArea.setBackground(Color.WHITE);
		if (isEditable)
			editArea.setForeground(Color.BLACK);
		else
			editArea.setForeground(Color.DARK_GRAY);
		editArea.setTabSize(2);
		editArea.setMargin(new Insets(1,5,5,5));
		if (isEditable)
			editArea.getDocument().addDocumentListener(this);
		editArea.setRequestFocusEnabled(isEditable);	    
		xmidSaved = true;
	}
	
	// implements DocumentListener
    public void insertUpdate(DocumentEvent e) {
    	xmidSaved = false;
    }
    public void removeUpdate(DocumentEvent e) {
    	xmidSaved = false;
    }
    public void changedUpdate(DocumentEvent e) {
    	xmidSaved = false;
    }
    	
	// overridden by XMIDEditor
	public boolean hasSimulator(){
		return false;
	}
	
	public void startSimulator(SimulatorType simulatorType){
	}
	
	public void resetSimulator(){
	}

	// implements (overrides) ChangeListener
	public synchronized void stateChanged(ChangeEvent evt) {
		super.stateChanged(evt);
        Component selection = editTabbedPane.getSelectedComponent();
        if (selection==editScrollPane || editTabbedPane.getTabCount()<=1){
        	kernel.setMenuAndToolBarEnabled(true);
           	getLineAndColumnAtCaret(editArea);
        }
	}
}
