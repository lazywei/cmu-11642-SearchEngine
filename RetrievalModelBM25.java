/** 
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  An object that stores parameters for the Okapi BM25 retrieval model and
 *  indicates to the query operators how the query should be evaluated.
 */
public class RetrievalModelBM25 extends RetrievalModel {

    private double b, k1, k3;

    public RetrievalModelBM25(double k1, double b, double k3) {
        this.k1 = k1;
        this.b = b;
        this.k3 = k3;
    }

    public String defaultQrySopName() {
        return new String("#sum");
    }

    public double getB() {
        return this.b;
    }

    public double getK1() {
        return this.k1;
    }

    public double getK3() {
        return this.k3;
    }

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
        String fieldName = qIop.getField();
        int docid = qIop.docIteratorGetMatch();

        double k1 = this.getK1();
        double b = this.getB();

        int df = qIop.getDf();
        int tf = qIop.getMatchTf();

        long nDocs = this.getNumDocs(fieldName);
        long doclen = this.getDocLen(fieldName, docid);
        double avgDoclen = this.getAvgDocLen(fieldName);

        double logTerm = Math.max(0, Math.log((nDocs - df + 0.5) / (df + 0.5)));

        double tfTerm = tf / (tf + k1 * ((1 - b) + b * doclen / avgDoclen));
        double queryWeight = 1;

        return logTerm * tfTerm * queryWeight;
    }

}
