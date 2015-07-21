package planninggraph;
/**
 * A token/fact for a certain place/predicate is an ordered list of arguments
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 */

import java.util.*;
import java.io.*;

public class Token {
    protected int place;
    protected String[] arguments;

    public final static String[] DEFAULTARGUMENTS = {"."};

    /**
     * Construct a token with an array of constant strings.
     */
    public Token(int place, String[] arguments){
        this.place = place;
        if (arguments.length>0)
            this.arguments = arguments;
        else
            this.arguments = DEFAULTARGUMENTS;
    }

    /**
     * Construct a token with a vector of constant strings.
     */
    public Token(int place, Vector args){
        this.place = place;
        if (args.size()==0)
            arguments = DEFAULTARGUMENTS;
        else {
            arguments = new String[args.size()];
            for (int i=0; i<args.size(); i++)
                arguments[i] = (String)args.elementAt(i);
        }
    }

    /**
     * Get index of place/predicate name
     */
    public int getPlace(){
        return place;
    }

    /**
     * Get the arguments.
     */
    public String[] getArguments(){
        return arguments;
    }

    /**
     * get the arity (number of arguments) of token.
     */
    public int arity(){
        return arguments.length;
    }

    /**
     * The token is equal to another token
     */
    public boolean equals(Object token) {
        if (place!=((Token)token).place)
            return false;
        if (arguments.length!=((Token)token).arguments.length)
            return false;
        for (int i=0; i<arguments.length; i++)
            if (!arguments[i].equals(((Token)token).arguments[i]))
                return false;
        return true;
    }

    /**
     * The arguments of this token are the same as given strings
     */
    public boolean equalsTo(String[] args) {
        if (arguments.length!=args.length)
            return false;
        for (int i=0; i<arguments.length; i++)
            if (!arguments[i].equals(args[i]))
                return false;
        return true;
    }

    /**
     * compare two tokens
     * return 0 if equals
     */
    public int compareTo(Token token) {
        int res;
        for (int i=0; i<arguments.length; i++){
            res=(arguments[i].compareTo(token.getArguments()[i]));
            if (res!=0) return res;
        }
        return 0;
    }

    /**
     * Convert a token to a haskKey.
     */
    public String hashKey(){
        return String.valueOf(super.hashCode());
    }

    /**
     * token string (parameter str as the place name)
     */
    public String tokenString(String str){
        return str+toString();
    }

    /**
     * convert a token to a string.
     */
    public String toString(){
        StringBuffer str = new StringBuffer("(");
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

