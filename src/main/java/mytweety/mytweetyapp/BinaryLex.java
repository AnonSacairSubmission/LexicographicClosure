package mytweety.mytweetyapp;

import org.tweetyproject.logics.pl.syntax.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.reasoner.*;
import java.io.IOException;


import org.tweetyproject.commons.ParserException;



import java.util.*;

public class BinaryLex {

    static SatReasoner classicalReasoner = new SatReasoner();
    
    static int rankFromWhichToRemove = -1;
    static int counter = 0;
    static int counterR = 0;
    static Boolean checkEntailmentBinarySearch(PlBeliefSet[] rKB, PlFormula formula, int left, int right
            ) throws ParserException, IOException {
        PlFormula negationOfAntecedent = new Negation(((Implication) formula).getFormulas().getFirst());
        SatSolver.setDefaultSolver(new Sat4jSolver());
        PlBeliefSet[] rankedKB = rKB.clone();
        if (right > left) {
            
            int mid = left + ((right - left) / 2);
            // If the query is still compatible after removing middle one and the ones above it, remove the top half
            counter++;
            if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, mid + 1, rankedKB.length)),
                    negationOfAntecedent)) {
                      
 
                return checkEntailmentBinarySearch(rankedKB, formula, mid + 1, right);
            }
            // Since the query is not compatible after removing the top half, check if adding in one rank back makes the query compatible
            else {
                counter++;
                if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, mid, rankedKB.length)),
                        negationOfAntecedent)) {
                            

                    rankFromWhichToRemove = mid;
                } else { // removing it still means the query is compatible. The corresponding rank is in the bottom half.

                    return checkEntailmentBinarySearch(rankedKB, formula, left, mid);
                }
            }
        } 
        else {
            if (right == left)
                rankFromWhichToRemove = right;
            else
                return false;

        }
        if (rankFromWhichToRemove == 0){
            counter++;

            if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                    formula)) {
                     
                return true;
            } else
                return false;
        }
        else if (rankFromWhichToRemove +1 < rankedKB.length) {

            Object[] rank = rankedKB[rankFromWhichToRemove].toArray();
            
            List<Set<Object>> sortedRank = PowerSetLex.sortList(rank);

            ArrayList<String> refinements = new ArrayList<>(PowerSetLex.combineRefine(sortedRank)); // Calling the powerset function
            for (String f : refinements) { // Checking every subsets
                PlBeliefSet combSet = new PlBeliefSet();
                PlParser parser = new PlParser();
                combSet.add((PlFormula) parser.parseFormula(f));
                rankedKB[rankFromWhichToRemove] = combSet;
                counterR++;
                //System.out.println("ranked kb" + rankedKB[rankFromWhichToRemove].toString());
                if (!classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                        new Negation(((Implication) formula).getFormulas().getFirst()))) {
                          
                 //   System.out.println((new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                 //           + " is not entailed by this refinement.");
                 //   System.out.println("We now check whether or not the formula" + formula.toString()
                  //          + " is entailed by " + combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)).toString());
                  counter++;
                    if (classicalReasoner.query(
                        combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)), formula)) {
                           
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    return false;
                }
            }
            return true;
        
    }
        //Since we do not check the refinements of the infinite rank
    else if (rankFromWhichToRemove +1 == rankedKB.length){
       // System.out.println("We now check whether or not the formula" + formula.toString()
               //     + " is entailed by "
                  //  + combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)).toString());
                  counter++;
        if (classicalReasoner.query(
            combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)), formula)){
 
                return true;
            }
            else return false;
    }
    else{
        return false;
    }
        
    }

    static PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

}