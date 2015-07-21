package edit;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;


import utilities.FileUtil;

import java.awt.*;
import java.awt.event.*;

import locales.LocaleBundle;

public class ButtonTabComponent extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	private final JTabbedPane pane;

    public ButtonTabComponent(String title, String iconPath, final JTabbedPane pane, GeneralEditor editor) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.pane = pane;
        setOpaque(false);
        
        JLabel label = new JLabel(title, FileUtil.createImageIcon(iconPath), SwingConstants.LEFT);
        label.setFont(GeneralEditor.tabFont);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        JButton button = new TabButton(editor);
        add(label);
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    /*
    private ImageIcon scale(Image src, double scale) {
        int w = (int)(scale*src.getWidth(this));
        int h = (int)(scale*src.getHeight(this));
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage dst = new BufferedImage(w, h, type);
        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(src, 0, 0, w, h, this);
        g2.dispose();
        return new ImageIcon(dst);
    }
*/
    private class TabButton extends JButton implements ActionListener {
 		private static final long serialVersionUID = 1L;

 		private GeneralEditor editor;
 		
		public TabButton(GeneralEditor editor) {
			this.editor = editor;
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText(LocaleBundle.bundleString("close this tab"));
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int index = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (index != -1) {
            	if (editor.isClosingTestTree(index) && !editor.closeTestTree())
            		return;
            	if (editor.confirmCloseTab(index))
                	editor.removeComponentFromTabbedPane(index);
            }
        }

        //don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(0.7f, 0.0f, 0.0f));
//            g2.setColor(Color.BLACK);
           if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}

