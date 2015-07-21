package utilities;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FilterForOpenFile extends FileFilter{
	
	public boolean accept(File file) {
		String filename = file.getName();
		return file.isDirectory()
		|| filename.endsWith(FileUtil.XLSFileExtension)
		|| filename.endsWith(FileUtil.XMIDFileExtension)
		|| filename.endsWith(FileUtil.TestDataFileExtension);
	}

	public String getDescription() {
		return "*."+FileUtil.XMIDFileExtension;
	}

}
