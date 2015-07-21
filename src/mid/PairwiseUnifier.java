package mid;

import java.util.ArrayList;

import allpairs.AllPairs;

public class PairwiseUnifier extends CombinatorialUnifier{
	
	public PairwiseUnifier(Transition transition, Marking marking) {
		super(transition, marking);
	}

	public ArrayList<int[]> computeCombinatorialTests(int[] choices){
		
/*		System.out.println("Numbers: ");
		for (int i=0; i<choices.length; i++)
			System.out.println(choices[i]);
*/			
		  boolean noShuffle = true;
		  int maxGoes = 100;
		  long seed = 42;
		  return AllPairs.generatePairs(choices, seed, maxGoes, !noShuffle, null, false);
		}
}
