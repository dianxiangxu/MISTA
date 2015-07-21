package planninggraph;
// MyInput.java: Contain the methods for reading int, double, and
// string values from the keyboard

import java.io.*;

public class MyInput
{
  /**Read a string from the keyboard*/
  public static String readString()
  {
    BufferedReader br
      = new BufferedReader(new InputStreamReader(System.in), 1);

    // Declare and initialize the string
    String string = " ";

    // Get the string from the keyboard
    try
    {
      string = br.readLine();
    }
    catch (IOException ex)
    {
      System.out.println(ex);
    }

    // Return the string obtained from the keyboard
    return string;
  }

  /**Read an int value from the keyboard*/
  public static int readInt()
  {
    return Integer.parseInt(readString());
  }

  /**Read a double value from the keyboard*/
  public static double readDouble()
  {
    return Double.parseDouble(readString());
  }

}