package utilities;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FilterForCPNFiles extends FileFilter{
	
	public boolean accept(File file) {
		return file.getName().endsWith(FileUtil.CPNFileExtension) || file.isDirectory() ;
	}

	public String getDescription() {
		return "*."+FileUtil.CPNFileExtension;
	}

}
