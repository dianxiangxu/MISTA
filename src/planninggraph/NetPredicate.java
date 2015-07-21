package planninggraph;
import java.util.*;

/**
 * @Predicate: <name/number>(<arguments>)
 *
 * @version 1.0, 12/2001
 * @revised in March 2011
 * @author Dianxiang Xu
 */
public class NetPredicate {

	private int name;			// predicate name/internal number of place
	private String[] arguments;		// a set of arguments

        public final static int START = 1000;         // starting number of built-in predicates

        private final static int EQUALS = START;
        private final static int BOUND = START+1;
        private final static int GREATERTHAN = START+2;
        private final static int GREATERTHANOREQUALTo = START+3;
        private final static int LESSTHAN = START+4;
        private final static int LESSTHANOREQUALTO = START+5;
        private final static int ISEVEN = START+6;
        private final static int ISODD = START+7;
        private final static int BELONGSTO = START+8;
    	
        public static String[] PREDICATES = {   
        	"EQUALS", "BOUND", "GT", "GTE", "LT", "LTE", "ISEVEN", "ISODD", "BELONGSTO"
        };

	/**
	 * construct a predicate
	 */
	public NetPredicate(int name, String[] arguments){
		this.name = name;
                this.arguments = arguments;
	}

        /**
	 * construct a predicate
	 */
	public NetPredicate(int name, Vector args){
		this.name = name;
                arguments = new String [args.size()];
                for (int i=0; i<args.size(); i++)
                    arguments[i] = (String)args.elementAt(i);
	}

        /**
         * get the internal number of the predicate/places
         */
         public int getName() {
                return name;
         }

        /**
         * get the parameters
         */

         public String[] getArguments() {
                return arguments;
         }

        /**
         * get the number of arguments
         */
        public int arity() {
            return arguments.length;
        }

        /**
         * get the truth value
         */
        public boolean truth(Environment env) {
            String[] args = env.substitute(arguments);
            switch (name) {
                case EQUALS:
                        return (args[0].equals(args[1]));
                case BOUND:         
                		return !args[0].startsWith("?");
                case GREATERTHAN:
                		return isGreaterThan(args[0], args[1]);
                case GREATERTHANOREQUALTo: 
            			return isGreaterThanOrEqualTo(args[0], args[1]);
                 case LESSTHAN: 
                	 	return !isGreaterThanOrEqualTo(args[0], args[1]);
                case LESSTHANOREQUALTO:
            			return !isGreaterThan(args[0], args[1]);
                case ISEVEN:
            			return isEven(args[0]);
                case ISODD:
            			return isOdd(args[0]);
                case BELONGSTO:
                		return belongsTo(args);
                default:
                        break;
            }
            return true;
        }

    	private boolean isGreaterThan(String arg1, String arg2){
    		try {
    			return Integer.parseInt(arg1) > Integer.parseInt(arg2);
    		}
    		catch (Exception e) {
     		}
    		return false;
    	}

    	private boolean isGreaterThanOrEqualTo(String arg1, String arg2){
    		try {
    			return Integer.parseInt(arg1) >= Integer.parseInt(arg2);
    		}
    		catch (Exception e) {
     		}
    		return false;
    	}

    	private boolean isEven(String arg){
    		try {
    			return Integer.parseInt(arg) %2 ==0;
    		}
    		catch (Exception e) {
     		}
    		return false;
    	}

    	private boolean isOdd(String arg){
    		try {
    			return Integer.parseInt(arg) %2 !=0;
    		}
    		catch (Exception e) {
     		}
    		return false;
    	}

        /**
         * get the internal number of the predicate/place
         */
        public static int predicateNumber(String pred) {
            for (int i=0; i<PREDICATES.length; i++) {
                if (PREDICATES[i].equalsIgnoreCase(pred))
                    return i+START;
            }
            return -1;
        }

        /**
         * equals
         */
        public boolean equals(NetPredicate p){
            if (name !=p.getName())
                return false;
            if (arity()!=p.arity())
                return false;
            for (int i=0; i<arity(); i++)
                if (!arguments[i].equalsIgnoreCase(p.getArguments()[i]))
                    return false;
            return true;
        }

     public boolean belongsTo(String[] args){
    	if (args.length>1) {
    		for (int i=1; i<args.length; i++)
    			if (args[0].equals(args[i]))
    				return true;
    	}
   	 	return false;
     }
     
	/**
	 * convert a predicate to a string.
	 */
	public String toString(){

                StringBuffer str = new StringBuffer();
                if (name>=START)
                    str.append(PREDICATES[name-START]+"(");
                else
//                    str.append(name+"(");
                    str.append("(");
        	if (arguments.length>0) {
                    str.append(arguments[0]);
                    for (int i=1; i<arguments.length; i++) {
                        str.append(",");
			str.append(arguments[i]);
                    }
		}
		return str.toString()+")";
	}
}
