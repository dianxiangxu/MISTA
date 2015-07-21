package kernel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import utilities.FileUtil;

public class RecentFiles {

	private final String RecentFilesFileName = "recentfiles.dat";
	private final int MAXFILENO = 30;

	private ArrayList<String> recentFilePaths = new ArrayList<String>();
	
	public RecentFiles(){
		readRecentFilesFile();
	}
	
	private void readRecentFilesFile(){
		Scanner in = null; 
		try {
			in = new Scanner(new FileReader(RecentFilesFileName));
			while (in.hasNextLine()){
				String filePath = in.nextLine().trim();
				if (!filePath.equals("") && new File(filePath).exists() && recentFilePaths.size()<MAXFILENO)
					recentFilePaths.add(filePath);
			}	
		} catch (IOException ioe){
			if (Kernel.IS_DEBUGGING_MODE)
				ioe.printStackTrace();
		}
		finally {
			if (in!=null)
				in.close();
		}
	}

	public boolean hasRecentFiles(){
		return recentFilePaths.size()>0;
	}
	
	public String getMostRecentFile(){
		return recentFilePaths.size()>0? recentFilePaths.get(0): null;
	}
	
	private int searchForFilePath(String filePath){
		for (int index=0; index<recentFilePaths.size(); index++)
			if (filePath.equals(recentFilePaths.get(index)))
				return index;
		return -1;
	}
	
	public void addFilePath(String newFilePath){
		int index=searchForFilePath(newFilePath);
		if (index>=0)
			recentFilePaths.remove(index);
		recentFilePaths.add(0, newFilePath);
	}
	
	public void updateRecentFilesFile() {
		String allFilePaths ="";
		for (String filePath: recentFilePaths)
			allFilePaths += filePath+"\n";
		FileUtil.saveStringToTextFile(allFilePaths, RecentFilesFileName);
	}

	public ArrayList<String> getRecentFilePaths(){
		return recentFilePaths;
	}
	
}
