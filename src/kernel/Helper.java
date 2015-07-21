package kernel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import locales.LocaleBundle;



public class Helper implements ActionListener{

	public Helper() {
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == Commands.About)
			aboutInfo();	
	}
		
	private void aboutInfo(){			
		String about = 
			  "\n                               " + Kernel.SYSTEM_NAME + " "+Kernel.SYSTEM_VERSION
			+ "\n                   "+LocaleBundle.bundleString("System Name")+"             "
//			+ "\n                            by Dr. Dianxiang Xu "
			+ "\n\n"+LocaleBundle.bundleString("Bug reports")+"     "
			+ "\n                           dianxiang.xu@gmail.com"
			+ "\n\n"+LocaleBundle.bundleString("Copyright")
			+ "\n\n"+LocaleBundle.bundleString("Credits")
			+ "\n\n\n";
//			+ "\n\n"+LocaleBundle.bundleString("Pipe Copyright")+"\n\n\n";
		JOptionPane.showMessageDialog(null, about);
	}
	
}
