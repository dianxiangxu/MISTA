package kernel;

import javax.swing.JPanel;

public abstract class StatusPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public abstract void reset();

	public abstract void setMessage(String msg);

	public abstract void setStatus(String msg);

	public abstract void setPreferredLabelSize();

}