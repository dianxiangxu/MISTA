/* 	All Rights Reserved
	Author Dianxiang Xu
*/
package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import locales.LocaleBundle;

import edit.GeneralEditor;

import testcode.TargetLanguage;
import testgeneration.CoverageCriterion;
import testgeneration.TransitionTree;

public class FileUtil {
	public static final boolean OPENFILE = true;
	public static final boolean SAVEFILE = false;

	public static final String XMIDFileExtension = "xmid";
	public static final String XLSFileExtension = "xls";
	public static final String TestDataFileExtension = "test";
	public static final String TestCodeFilePrefix = "Tester";

	public static final String DefaultSeparateModelFileExtension = "xml";

	public static final String CPNFileExtension = "cpn";
	public static final String PNMLFileExtension = "pnml";
	public static final String XMLFileExtension = "xml";

	public static final String LOGFileExtension = "txt";

	public static final String SeparatorInGeneratedFileName = "_";
	
	public static String getPrefix(String fileName) {
		String result = "";
		String strs[] = fileName.split("\\.");
		if (strs.length > 0) {
			result = strs[0];
		} else {
			result = fileName;
		}
		return result;
	}

	public static String getExtension(String fileName) {
		String result = "";
		String strs[] = fileName.split("\\.");
		if (strs.length > 1) {
//			result = strs[1];
			result = strs[strs.length-1];
		}
		return result;
	}

	public static String getTestCodeFileName(TransitionTree transitionTree) {
		return getPrefix(getSystemName(transitionTree)) + TestCodeFilePrefix + getMiddleName(transitionTree)			 
			+ "." + transitionTree.getSystemOptions().getLanguage().getFileExtension();
	}
	
	public static String getTargetExcelFileName(File file) {
		return getPrefix(file.getName()) + "." + XLSFileExtension;
	}

	public static String getTargetMIDFileName(File file) {
		return getPrefix(file.getName()) + "." + XMIDFileExtension;
	}

	public static File getNewSimulationLogFile(File file) {
		File simuLogFile= null;
		int index=0;
		do {
			index++;
			simuLogFile = new File(file.getParent()+File.separator+getPrefix(file.getName()) + "SimuLog" +index+ "."+LOGFileExtension);
		} while (simuLogFile.exists());
		return simuLogFile;
	}

	public static final String TestLog = "TestLog";
	public static final String FailLog = "FailLog";
	public static final String PathLog = "PathLog";
	
	public static File[] getNewTestLogFiles(File file) {
		File[] newLogFiles = new File[2];
		int index=0;
		do {
			index++;
			newLogFiles[0] = new File(file.getParent()+File.separator+getPrefix(file.getName()) + TestLog +index+ "."+LOGFileExtension);
			newLogFiles[1] = new File(file.getParent()+File.separator+getPrefix(file.getName()) + FailLog +index+ "."+LOGFileExtension);
		} while (newLogFiles[0].exists() || newLogFiles[1].exists());
		return newLogFiles;
	}
	
	public static File[] getRecentTestLogFiles(File file) {
		File[] newLogFiles = new File[2];
		int index=0;
		do {
			index++;
			newLogFiles[0] = new File(file.getParent()+File.separator+getPrefix(file.getName()) + TestLog +index+ "."+LOGFileExtension);
			newLogFiles[1] = new File(file.getParent()+File.separator+getPrefix(file.getName()) + FailLog +index+ "."+LOGFileExtension);
		} while (newLogFiles[0].exists() || newLogFiles[1].exists());
		if (index>1) {
			newLogFiles[0] = new File(file.getParent()+File.separator+getPrefix(file.getName()) + TestLog +(index-1)+ "."+LOGFileExtension);
			newLogFiles[1] = new File(file.getParent()+File.separator+getPrefix(file.getName()) + FailLog +(index-1)+ "."+LOGFileExtension);
		}
		return newLogFiles;
	}

	public static File getPathsLogFile(File file) {
		String pathFileName = file.getName().replace(FailLog, PathLog);
		return new File (file.getParent()+File.separator+pathFileName);
	}

	public static String getDefaultSeparateModelFileName(File file) {
		return getPrefix(file.getName()) + "." + DefaultSeparateModelFileExtension;
	}

	private static String getSystemName(TransitionTree transitionTree){
		return transitionTree.getSystemOptions().getLanguage() == TargetLanguage.HTML?
				transitionTree.getMID().getDefaultSystemName(): transitionTree.getMID().getSystemName();
	}
	
	public static String getTestDataFileName(TransitionTree transitionTree) {
		return getPrefix(getSystemName(transitionTree))	+ getMiddleName(transitionTree)			 
			+ "." + TestDataFileExtension;
	}

	public static String getTestClassName(TransitionTree transitionTree) {
		return getPrefix(getSystemName(transitionTree)) + TestCodeFilePrefix + getMiddleName(transitionTree);		 
	}

	private static String getMiddleName(TransitionTree transitionTree) {
		CoverageCriterion coverage = transitionTree.getCoverageCriterion(); 
		return SeparatorInGeneratedFileName	+ coverage.getID();
	}

	public static void copyTextFile(File sourceFile, String destFile) {
		saveStringToTextFile(readTextFile(sourceFile), destFile);
	}
	
	public static void copyTextFile(String sourceFile, String destFile) {
		saveStringToTextFile(readTextFile(sourceFile), destFile);
	}
	
	public static void saveStringToTextFile(String fileString, String fileName) {
		File file = new File(fileName);
		saveStringToTextFile(fileString, file);
	}

	public static void saveStringToTextFile(String fileString, File file) {
		PrintWriter out = null;
		if (file.exists()) {
			file.delete();
		}
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(fileString);
		out.close();
	}

	public static String readTextFile(String fileName){
		return readTextFile(new File(fileName));
	}

	public static String readTextFile(File file){
		String text = "";
		if (file==null || !file.exists())
			return text;
		Scanner in = null; 
		try {
			in = new Scanner(new FileReader(file));
			while (in.hasNextLine())
				text += in.nextLine()+"\n";
		} catch (IOException ioe){
		}
		finally {
			if (in!=null)
				in.close();
		}
		return text;
	}

	public static void copyFile (File fromFile, File toFile) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = from.read(buffer)) != -1)
		    	to.write(buffer, 0, bytesRead);
		} 
		catch (IOException ioe){
			throw ioe;
		}
		finally {
			try {
				if (from != null)
					from.close();
				if (to != null)
					to.close();
			}
			catch (IOException ioe){
				throw ioe;
			}
		}
	}
	
	public static File getCurrentDirectory() {
		File resultFile = null;
		File dir1 = new File(".");
		try {
			resultFile = new File(dir1.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultFile;
	}
	
	public static File chooseFile(JFrame parentFrame, JFileChooser fc, 
			String dialogTitle, javax.swing.filechooser.FileFilter filter, 
			boolean openFile) {
		if (filter!=null)
			fc.addChoosableFileFilter(filter);	  
		fc.getCurrentDirectory();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(dialogTitle);
		int returnVal;
		if (openFile)  
			returnVal=fc.showOpenDialog(parentFrame);
		else
			returnVal=fc.showSaveDialog(parentFrame);		
		if (returnVal != JFileChooser.APPROVE_OPTION) 
			return null;
		return fc.getSelectedFile();
	}
	  
	public static boolean isTestGenerationFile(String fileName){
		String modelType = getExtension(fileName);
		return modelType.equalsIgnoreCase(XMIDFileExtension) 
			|| modelType.equalsIgnoreCase(XLSFileExtension);
	}
	
	public static boolean isXMIDFile(String fileName){
		return getExtension(fileName).equalsIgnoreCase(XMIDFileExtension);
	}

	public static boolean isXLSFile(String fileName){
		return getExtension(fileName).equalsIgnoreCase(XLSFileExtension);
	}

	public static boolean isTestDataFile(String fileName){
		return getExtension(fileName).equalsIgnoreCase(TestDataFileExtension);
	}

	public static boolean isCPNFile(String fileName){
		return getExtension(fileName).equalsIgnoreCase(CPNFileExtension);
	}

	public static boolean isXMLFile(String fileName){
		return getExtension(fileName).equalsIgnoreCase(XMLFileExtension);
	}

	public static boolean isPNMLFile(String fileName){
		return getExtension(fileName).equalsIgnoreCase(PNMLFileExtension) || getExtension(fileName).equalsIgnoreCase(XMLFileExtension);
	}

	public static String getImageIconPath(String fileName){
		String extension = getExtension(fileName);
		if (extension.equalsIgnoreCase(XMIDFileExtension))
			return "images/xmid.png";
		else if (extension.equalsIgnoreCase(XLSFileExtension))
			return "images/xls.png";
		else if (extension.equalsIgnoreCase(TestDataFileExtension))
			return "images/tree.png";
		else
		return "images/unknown.png";
	}

	public static ImageIcon getFileImageIcon(String fileName){
		return createImageIcon(getImageIconPath(fileName));
	}
	
	public static ImageIcon createImageIcon(String path) {
	    URL imgURL = GeneralEditor.class.getClassLoader().getResource(path);
	    if (imgURL != null) {
	    	return new ImageIcon(imgURL);
	    } else {
	        System.err.println(LocaleBundle.bundleString("Cannot find file") + path);
	        return null;
	    }
	}

}