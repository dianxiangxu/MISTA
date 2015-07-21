package testgeneration;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

public final class UFTResourceFileGenerator {

  private static final String RESOURCE_FILE_NAME = "Resource.mtr";
  private static final String TEST_FILE_NAME = "Test.tsp";

  public static void main(String... aArgs) throws Exception {
	  UFTResourceFileGenerator generator = new UFTResourceFileGenerator();
//	  generator.findActionDependencyText(SOURCE_FILE_NAME);
	  
	  generator.setResourceFileTemplate(TEST_FILE_NAME);
//	  generator.setResourceFileTemplate(RESOURCE_FILE_NAME);
	  String DEST_FILE_NAME = "Resource1.mtr";
	  generator.generateResourceFile(DEST_FILE_NAME, "Hello");
  }
  
  static int searchForSubSequence(byte[] source, byte[] keys){
	  for (int i=0; i<source.length-keys.length+1; i++) {
		  boolean match = true;
		  for (int j=0; j<keys.length; j++)
			 if (source[i+j]!=keys[j]) {
				   match = false;
				   break;
			  }
		  if (match)
			  return i;
	  }
	  return -1;
  }
  
  static byte[] unicodeByteArray(String str){
/*      String stringToConvert = "SubComponent";
      byte[] theByteArray = stringToConvert.getBytes("UTF-16LE");
      System.out.println(theByteArray.length);
*/  
	  byte[] array = new byte[str.length()*2];
	  try {
		  array = str.getBytes("UTF-16LE");
	  }
	  catch (Exception e){}
	  return array;
  }  

  private Hashtable<String, String> dependencyTexts = new Hashtable<String, String>();   
  private Hashtable<String, String> dependencyActionIDs = new Hashtable<String, String>();   

  public void findActionDependencyText(String aInputFileName){

	   byte[] fileContents = read(aInputFileName);
	   
	   byte[]  subCompStart = unicodeByteArray("<SubComponents>");
	   int subCompStartIndex = searchForSubSequence(fileContents, subCompStart);

	   byte[]  subCompEnd = unicodeByteArray("</SubComponents>");
	   int subCompEndIndex = searchForSubSequence(fileContents, subCompEnd);

	   try {
	    	String subComponentsString = new String(fileContents, subCompStartIndex, subCompEndIndex-subCompStartIndex,"UTF-16LE");
	    	breakDownSubComponents(subComponentsString);
	   }
	   catch (Exception e){}
	 }

 /*
  * Action name example: Buy_one
  * <Dependency Scope="0" Type="1"  Kind="16" Logical="Buy_one" ORDER_ID="0">Action8\Resource.mtr</Dependency>
  */
 private void findActionName(String dependencyString){
	 System.out.println(dependencyString);
	 String key = "Logical=\"";
	 int startIndex = dependencyString.indexOf(key)+key.length();
	 int endIndex = dependencyString.indexOf('"', startIndex);
	 if (endIndex>startIndex+1) {
		 String actionName = dependencyString.substring(startIndex, endIndex);
			dependencyTexts.put(actionName, dependencyString);
		 int actionIDStartIndex = 	dependencyString.indexOf('>', endIndex);
		 int actionIDEndIndex = 	dependencyString.indexOf('\\', endIndex);
		 String actionID = dependencyString.substring(actionIDStartIndex+1, actionIDEndIndex);
		dependencyActionIDs.put(actionName, actionID);
		System.out.println(actionName+" => "+actionID);
	 }
	 else
			System.out.println("");
 }
 
 private void breakDownSubComponents(String subComponentsString){
	 int currentIndex=0;
	 do {
		 currentIndex = subComponentsString.indexOf("<Dependency", currentIndex);
		 int nextIndex = subComponentsString.indexOf("<Dependency", currentIndex+10);
		 if (currentIndex>=0) {
			 if (nextIndex>=0) {
					findActionName(subComponentsString.substring(currentIndex, nextIndex));
			 }
			 else
					findActionName(subComponentsString.substring(currentIndex));
		 }
		 currentIndex = nextIndex;
	 } while (currentIndex>=0);	 
 } 
 
 private byte[] fileContents;
 private int subCompStartIndex;
 private int subCompEndIndex;
 private byte[] subCompStart = unicodeByteArray("<SubComponents>");
 
 private void setResourceFileTemplate(String aInputFileName){
       byte[]  subCompEnd = unicodeByteArray("</SubComponents>");
       fileContents = read(aInputFileName);
       subCompStartIndex = searchForSubSequence(fileContents, subCompStart);
       subCompEndIndex = searchForSubSequence(fileContents, subCompEnd);

       try {
    	   String subComponentsString = new String(fileContents, subCompStartIndex, subCompEndIndex-subCompStartIndex,"UTF-16LE");
    	   breakDownSubComponents(subComponentsString);
       }
       catch (Exception e){}
 }

 public void generateResourceFile(String aOutputFileName, String dependencyTexts){
	   log("Writing binary file...");
	   try {
	     OutputStream output = null;
	     try {
	       output = new BufferedOutputStream(new FileOutputStream(aOutputFileName)); 
	       output.write(fileContents, 0, subCompStartIndex+subCompStart.length);
	       output.write(unicodeByteArray(dependencyTexts));
	       output.write(fileContents, subCompEndIndex, fileContents.length-subCompEndIndex);
	     }
	     finally {
	       output.close();
	     }
	   }
	   catch(FileNotFoundException ex){
	     log("File not found.");
	   }
	   catch(IOException ex){
	     log(ex);
	   }
	 }


  private byte[] read(String aInputFileName){
    log("Reading in binary file named : " + aInputFileName);
    File file = new File(aInputFileName);
    log("File size: " + file.length());
    byte[] result = new byte[(int)file.length()];
    try {
      InputStream input = null;
      try {
        int totalBytesRead = 0;
        input = new BufferedInputStream(new FileInputStream(file));
        while(totalBytesRead < result.length){
          int bytesRemaining = result.length - totalBytesRead;
          //input.read() returns -1, 0, or more :
          int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
          if (bytesRead > 0){
            totalBytesRead = totalBytesRead + bytesRead;
          }
        }
        /*
         the above style is a bit tricky: it places bytes into the 'result' array; 
         'result' is an output parameter;
         the while loop usually has a single iteration only.
        */
        log("Num bytes read: " + totalBytesRead);
      }
      finally {
        log("Closing input stream.");
        input.close();
      }
    }
    catch (FileNotFoundException ex) {
      log("File not found.");
    }
    catch (IOException ex) {
      log(ex);
    }
    return result;
  }
  
  /**
   Write a byte array to the given file. 
   Writing binary data is significantly simpler than reading it. 
  */
  private void write(byte[] aInput, int off, int len, String aOutputFileName){
    log("Writing binary file...");
    try {
      OutputStream output = null;
      try {
        output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
        output.write(aInput, off, len);
      }
      finally {
        output.close();
      }
    }
    catch(FileNotFoundException ex){
      log("File not found.");
    }
    catch(IOException ex){
      log(ex);
    }
  }
  
  /** Read the given binary file, and return its contents as a byte array.*/ 
  private byte[] readAlternateImpl(String aInputFileName){
    log("Reading in binary file named : " + aInputFileName);
    File file = new File(aInputFileName);
    log("File size: " + file.length());
    byte[] result = null;
    try {
      InputStream input =  new BufferedInputStream(new FileInputStream(file));
      result = readAndClose(input);
    }
    catch (FileNotFoundException ex){
      log(ex);
    }
    return result;
  }
  
  /**
   Read an input stream, and return it as a byte array.  
   Sometimes the source of bytes is an input stream instead of a file. 
   This implementation closes aInput after it's read.
  */
  private byte[] readAndClose(InputStream aInput){
    //carries the data from input to output :    
    byte[] bucket = new byte[32*1024]; 
    ByteArrayOutputStream result = null; 
    try  {
      try {
        //Use buffering? No. Buffering avoids costly access to disk or network;
        //buffering to an in-memory stream makes no sense.
        result = new ByteArrayOutputStream(bucket.length);
        int bytesRead = 0;
        while(bytesRead != -1){
          //aInput.read() returns -1, 0, or more :
          bytesRead = aInput.read(bucket);
          if(bytesRead > 0){
            result.write(bucket, 0, bytesRead);
          }
        }
      }
      finally {
        aInput.close();
        //result.close(); this is a no-operation for ByteArrayOutputStream
      }
    }
    catch (IOException ex){
      log(ex);
    }
    return result.toByteArray();
  }
  
  private static void log(Object aThing){
    System.out.println(String.valueOf(aThing));
  }
} 
