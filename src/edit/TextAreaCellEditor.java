package edit;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import locales.LocaleBundle;

public class TextAreaCellEditor extends DefaultCellEditor implements ActionListener {
	private static final long serialVersionUID = 1L;
	
    final JTextArea textArea = new JTextArea();
	private Font font;
	private boolean newWindowAllowed;
	
	public TextAreaCellEditor(Font textFont, ArrayList<String> choices, boolean newWindowAllowed) {
	    super(new JTextField());
	    this.font=textFont;
	    this.newWindowAllowed = newWindowAllowed;
	    textArea.setWrapStyleWord(true);
	    textArea.setLineWrap(true);
		textArea.setMargin(new Insets(3,3,3,3));
		textArea.setTabSize(4);
		setPopupMenuForTextArea(choices);
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    scrollPane.setBorder(null);
	    editorComponent = scrollPane;

	    delegate = new DefaultCellEditor.EditorDelegate() {
			private static final long serialVersionUID = 1L;
			
			public void setValue(Object value) {
				textArea.setFont(font);
				textArea.setText((value != null) ? value.toString() : "");
			}
			
			public Object getCellEditorValue() {
				return textArea.getText();
			}
	    };
	}

	public TextAreaCellEditor(Font textFont, ArrayList<String> choices) {
		this(textFont, choices, true);
	}

	public void updateFont(Font newFont){
		this.font=newFont;
	}
	
	private static final String EditCellInNewWindow = "Edit Cell in New Window";

	private void setPopupMenuForTextArea(ArrayList<String> choices) {

		final JPopupMenu popupMenu = new JPopupMenu();
		if (newWindowAllowed)
			createPopupMenuItem(popupMenu, LocaleBundle.bundleString(EditCellInNewWindow), EditCellInNewWindow);
		if (choices!=null && choices.size()!=0) {
			if (newWindowAllowed)
				popupMenu.addSeparator();
			for (String choice: choices)
				createPopupMenuItem(popupMenu, choice, choice);
		}
		
		textArea.addMouseListener( new MouseAdapter() { 
			public void mousePressed( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			} 
			public void mouseReleased( MouseEvent e ) { 
				checkForTriggerEvent(e); 
			}
			private void checkForTriggerEvent( MouseEvent e ) { 
				if ( e.isPopupTrigger()) { 
					popupMenu.show(e.getComponent(), e.getX(), e.getY() );					
				}	
			} 
			
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume();
//					new SeparateTableCellEditor(textArea);
				}
			}
		}); 
	}

	private JMenuItem createPopupMenuItem(JPopupMenu popupMenu, String title, String command){
		JMenuItem menuItem = popupMenu.add(title);
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == EditCellInNewWindow) {
			new SeparateTextAreaCellEditor(textArea);
		} else
			textArea.setText(e.getActionCommand());
	}

}
