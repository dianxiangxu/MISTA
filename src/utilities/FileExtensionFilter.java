package utilities;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileExtensionFilter extends FileFilter{

	private String ext = "";

	public FileExtensionFilter(String ext){
		super();
		this.ext = "." + ext;
	}
	
	public boolean accept(File file) {
		String filename = file.getName();
		return file.isDirectory() || filename.endsWith(ext);
	}

	public String getDescription() {
		return "*" + ext;
	}
}
