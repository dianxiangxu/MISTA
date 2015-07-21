package planninggraph;
import java.util.*;
import java.io.*;

/**
 * @Literal: [not] <predicate>
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 */
public class Literal extends NetPredicate {

        private boolean negation = false;       // default no negation, regular literal

	/**
	 * construct a literal
	 */
	public Literal(boolean negation, int name, String[] args){
                super(name, args);
                this.negation = negation;
	}

        /**
	 * construct a literal
	 */
	public Literal(boolean negation, int name, Vector args){
                super(name, args);
                this.negation = negation;
	}

        /**
         * get negation sign
         */
         public boolean getNegation() {
                return negation;
         }

        /**
         * get the truth value
         */
        public boolean truth(Environment env) {
            if (negation)
                return !super.truth(env);
            else
                return super.truth(env);
        }

	/**
	 * convert a literal to a string.
	 */
	public String toString(){
            if (negation)
                return "NOT "+super.toString();
            else
                return super.toString();
	}
}
