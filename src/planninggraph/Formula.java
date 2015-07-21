package planninggraph;
import java.util.*;
import java.io.*;

/**
 *
 * @version 1.0, 10/2001
 * @author Dianxiang Xu
 *
 * A formula is a list of literalss (places or built-in literalss)
 */
public class Formula {

	private Literal[] literals;		// a set of literals

	/**
	 * construct a formula
	 */

        public Formula (){
            literals = new Literal[0];
        }

	public Formula(Literal[] literals)
	{
                this.literals = literals;
	}

	public Formula(Vector lits){
                literals = new Literal[lits.size()];
                for (int i=0; i<lits.size(); i++)
                    literals[i] = (Literal) lits.elementAt(i);
	}

        /**
         * get the truth value
         */
        public boolean truth(Environment env) {
            for (int i=0; i<literals.length; i++)
                if (!literals[i].truth(env))
                    return false;
            return true;
        }

       /**
         * get the truth value
         */
        public boolean truth(NetMarking[] state) {

            return true;
        }

	/**
	 * print a formula as strings.
	 */
	public String toString(){
            if (literals.length==0)
                return "";
            StringBuffer str = new StringBuffer(literals[0].toString());
            for (int i=1; i<literals.length; i++)
                    str.append(" AND "+literals[i]);
            return str.toString();
	}
}
