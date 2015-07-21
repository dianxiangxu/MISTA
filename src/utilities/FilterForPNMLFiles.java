package utilities;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FilterForPNMLFiles extends FileFilter{
	
	public boolean accept(File file) {
		return file.getName().endsWith(FileUtil.PNMLFileExtension) || file.getName().endsWith(FileUtil.XMLFileExtension) || file.isDirectory() ;
	}

	public String getDescription() {
		return "*."+FileUtil.PNMLFileExtension;
	}

}
