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

    public double getDefaultScore() {
        return 1.0;
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

    private long getFieldLength(String fieldName) throws IOException  {
        return Idx.getSumOfFieldLengths(fieldName);
    }

    ///////////////////////////////////

    public long getNumDocs(String fieldName) throws IOException  {
        return Idx.getDocCount(fieldName);
    }

    public double getAvgDocLen(String fieldName) throws IOException  {
        long totalLen = Idx.getSumOfFieldLengths(fieldName);
        long nDocs = this.getNumDocs(fieldName);

        return ((double)totalLen / (double)nDocs);
    }

    public long getDocLen(String fieldName, int docid) throws IOException  {
        return Idx.getFieldLength(fieldName, docid);
    }

    public double calculateScore(QryIop qIop) throws IOException {
        return 1;
    }


}
