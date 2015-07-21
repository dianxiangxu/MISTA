package mid;

import java.util.ArrayList;
import java.util.HashMap;

public class TupleFactory {
	   private static HashMap<String, Tuple> tupleBase = new HashMap<String, Tuple>();
	   
	   private static Tuple DefaultTuple = new Tuple(new ArrayList<String>()); 
	   
	   public static Tuple createTuple(ArrayList<String> arguments) {
		  if (arguments.size()==0)
			  return DefaultTuple;
		  String key = argumentString(arguments);
	      Tuple tupleInBase = (Tuple)tupleBase.get(key);
	      if(tupleInBase == null) {
	         tupleInBase = new Tuple(arguments);
	         tupleBase.put(key, tupleInBase);
	      }
	      return tupleInBase;
	   }

	   public static String argumentString(ArrayList<String> arguments){
	        StringBuffer str = new StringBuffer();
	        str.append(arguments.get(0));
	        for (int i=1; i<arguments.size(); i++) {
	            str.append(",");
	            str.append(arguments.get(i));
	        }
	        return str.toString();
	   }
	   
	   public static void reset(){
		   tupleBase = new HashMap<String, Tuple>();
	   }
}
