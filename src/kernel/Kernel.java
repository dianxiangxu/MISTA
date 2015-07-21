/* 	
	All Rights Reserved
*/
package kernel;

import javax.swing.*;

import locales.LocaleBundle;

import testcode.TargetLanguage;

public interface Kernel {

	public static final boolean IS_DEBUGGING_MODE = false;
	
	public static final boolean IS_LIMITATION_SET = false;
	public static final int MAX_TESTS_FOR_LIMITATION = 1000;	// for test code generation.
	public static final int MAX_TRANSITIONS_FOR_LIMITATION = 100;
	public static final int MAX_SEARCH_DEPTH_FOR_LIMITATION = 50;
	public static final int MAX_RANDOM_TESTS_FOR_LIMITATION = 1000;

	public static final String SYSTEM_NAME = "MISTA";
	public static final String SYSTEM_VERSION = LocaleBundle.bundleString("Version"); 
	
	public abstract FileManager getFileManager();
	public abstract VerificationManager getVerificationManager();
	public abstract TestingManager getTestingManager();
	public abstract SystemOptions getSystemOptions();
	public abstract JFileChooser getFileChooser();
	
	public abstract JFrame getParentFrame();
	public abstract void setSystemTitle(String message);
	public abstract void updateContentPane();
	public abstract void updateToolBar(JToolBar additionalToolBar);
	public abstract void showSystemOptionsInInfoPanel();
	public abstract StatusPanel getStatusPanel();
	public abstract void printDialogMessage(String message);
	public abstract void printToConsole(String message);

	public abstract void updateEditMenu(JMenu newModelMenu, JMenu newMIMMenu, JMenu newHelperCodeMenu);
	public abstract void updateModelMenu(JMenu newModelMenu);
	public abstract void setMenuAndToolBarEnabled(boolean enabled);
	public abstract void enableTestTreeEditMenuItems(boolean enabled);
	
	public abstract RecentFiles getRecentFiles();	
	public abstract void clearRecentFilesMenu();	
	public abstract void updateRecentFilesMenu();

	public abstract void updateModelType();
    public abstract void updateLanguage(TargetLanguage newLanguage);
	
}
