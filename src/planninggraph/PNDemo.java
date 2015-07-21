package planninggraph;
/**
 * Demonstration of
 * Reachability analysis of predicate/transition nets through planning graph.
 *
 * Usage: java PNDemo <PN-SPEC-FILE>
 * The default PN-SPEC-FILE is blocks.prt
 *
 * @version 1.0, 12/2001
 * @author Dianxiang Xu
 * @    Department of Computer Science
 * @    Texas A&M University
 *
 */
import java.util.*;
import java.io.*;

import kernel.CancellationException;


public class PNDemo {

    public static void main(String args[]){
        String fname;
        if (args.length>0)
            fname = args[0];
        else
            fname = "examples\\blocks\\blocks1.prt";
        ParsingPN prt = new ParsingPN(fname);
        if (prt.isCorrect()){
        	try {
            prt.getPN().graphAnalysis(prt.getInit(), prt.getGoal(), null);
        	}
        	catch (CancellationException e){}
        }
    }
}
