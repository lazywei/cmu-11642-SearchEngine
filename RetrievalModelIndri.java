/** 
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.util.*;

/**
 *  An object that stores parameters for the Indri retrieval model and
 *  indicates to the query operators how the query should be evaluated.
 */
public class RetrievalModelIndri extends RetrievalModel {

    private int mu;
    private double lambda;

    public RetrievalModelIndri(int mu, double lambda) {
        this.mu = mu;
        this.lambda = lambda;
    }

    public String defaultQrySopName() {
        return new String("#and");
    }

    public int getMu() {
        return this.mu;
    }

    public double getLambda() {
        return this.lambda;
    }

    public double getScore(QryIop qIop) throws IOException {
        String fieldName = qIop.getField();
        int docid = qIop.docIteratorGetMatch();

        return this.queryLikelihood(qIop.getMatchTf(), qIop.getCtf(),
                                    fieldName, docid);
    }

    public double getDefaultScore(QryIop qIop, int docid) throws IOException {
        String fieldName = qIop.getField();

        return this.queryLikelihood(0, qIop.getCtf(), fieldName, docid);
    }

    // combine scores for AND
    public double andCombiner(ArrayList<Double> scores) {
        double geoMeanPow = 1/(double)(scores.size());
        double result = 1;

        for (Double score: scores) {
            result *= Math.pow(score, geoMeanPow);
        }

        return result;
    }

    // combine scores for WAND
    public double wandCombiner(ArrayList<Double> scores,
                               ArrayList<Double> weights) {
        double result = 1;
        double totalWeight = 0.0;

        for (int i = 0; i < weights.size(); i++)
            totalWeight += weights.get(i);

        for (int i = 0; i < scores.size(); i++) {
            result *= Math.pow(scores.get(i), weights.get(i) / totalWeight);
        }

        return result;
    }

    // combine scores for WSUM
    public double wsumCombiner(ArrayList<Double> scores,
                               ArrayList<Double> weights) {
        double result = 0;
        double totalWeight = 0.0;

        for (int i = 0; i < weights.size(); i++)
            totalWeight += weights.get(i);

        for (int i = 0; i < scores.size(); i++) {
            result += Math.pow(scores.get(i), weights.get(i) / totalWeight);
        }

        return result;
    }

    private double queryLikelihood(int tf, int ctf,
                                   String fieldName, int docid) throws IOException {
        double len_d = (double)(Idx.getFieldLength(fieldName, docid));
        double len_c = (double)(Idx.getSumOfFieldLengths(fieldName));

        double pMLE = (double)ctf / len_c;
        double smoothed = ((double)tf + this.getMu() * pMLE) / (len_d + this.getMu());
        // System.out.println("");
        // System.out.format(
        //     "(%d) tf = %d, ctf = %d, len_d = %f, len_c = %f, fieldName = %s\n",
        //     docid, tf, ctf, len_d, len_c, fieldName);
        // System.out.println(pMLE);
        //     System.out.println(smoothed);
        return (1 - this.getLambda()) * smoothed + this.getLambda()*pMLE;
    }
}
