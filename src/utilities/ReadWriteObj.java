package utilities;

import java.io.*;
import java.io.Serializable;

public class ReadWriteObj {
	
	public static void write(Serializable object, File file)
			throws IOException {
		ObjectOutputStream out = null;
		if (file.exists()) {
			file.delete();
		}
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(object);
			out.flush();
		} 
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException exception) {
				}
			}
		}
	}

	public static void write(Serializable object, String filename)
			throws IOException {
		write(object, new File(filename));
	}
	
	public static Object read(File file) 
			throws ClassNotFoundException, IOException {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			return in.readObject();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException exception) {
					
				}
			}
		}
	}
	
	public static Object read(String filename)
			throws ClassNotFoundException, IOException {
		return read(new File(filename));
	}

}
