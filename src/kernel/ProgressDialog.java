package kernel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import locales.LocaleBundle;

public class ProgressDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	public static final boolean CANCELLATION_NOT_ALLOWED = false;
	
	private String CANCEL = "Cancel";
	
	private JLabel progressLabel;
	private JProgressBar progressBar;
	
	private JButton cancelButton;
	private boolean isCancellationAllowed = true;
	private boolean isCancelled = false;

	public ProgressDialog(Frame frame, String title, String message, boolean isCancellationAllowed) {
		super(frame, title, true);
		this.isCancellationAllowed = isCancellationAllowed;
		createUI(frame, title, message);
	}

	public ProgressDialog(Frame frame, String title, String message) {
		super(frame, title, true);
		this.isCancellationAllowed = true;
		createUI(frame, title, message);
	}
	
	public void createUI(Frame frame, String title, String message) {		
		progressLabel = new JLabel(message);
        progressLabel.setPreferredSize(new Dimension(350,30));
		
        progressBar = new JProgressBar();
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300,20));
		progressBar.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
 
		progressPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());

	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	    contentPanel.add(progressPanel, gridBagConstraints);

	    
        cancelButton = new JButton(LocaleBundle.bundleString(CANCEL));
        cancelButton.setActionCommand(CANCEL);
        cancelButton.addActionListener(this);
        
        if (isCancellationAllowed){
        	gridBagConstraints.gridy = 1;
        	gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        	gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        	contentPanel.add(cancelButton, gridBagConstraints);
        }
        
		setContentPane(contentPanel);
		
	    addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
	        	isCancelled = true;
	        	dispose();
	        }
	      });
	    
		setFocusable(true);
		setAlwaysOnTop(true);
		pack();
		setLocationRelativeTo(frame);
//		if (!isCancellationAllowed)
//			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void updateUI(){
        progressBar.setIndeterminate(false);
        getContentPane().paintAll(progressBar.getGraphics());
	}
	
	public JProgressBar getProgressBar(){
		return progressBar;
	}
	
	public void setMessage(String message) {
		progressLabel.setText(message);
	}
	
	public void setCancelText(String text){
		cancelButton.setText(text);
	}
	
	public void finishDialog(String message){
		setVisible(false);
        setModal(true);
        
        progressLabel = new JLabel(message, JLabel.CENTER);

        JButton confirm = new JButton(LocaleBundle.bundleString("OK"));
        confirm.setActionCommand("OK");
        confirm.addActionListener(this);
        
		JPanel confirmPanel = new JPanel();
		confirmPanel.add(confirm);
        confirmPanel.setPreferredSize(new Dimension(300,40));
		confirmPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(confirmPanel, BorderLayout.SOUTH);
		progressPanel.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

        setContentPane(progressPanel);
		setVisible(true);
        pack();
	}
	
	public boolean isCancelled(){
		return isCancelled;
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("OK".equals(e.getActionCommand())) {
			dispose();
		} else
		if (CANCEL.equals(e.getActionCommand())){
			isCancelled = true;
		}
	}
}
