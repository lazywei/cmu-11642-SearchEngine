/*
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 *  Version 3.1.2.
 */
import java.io.*;
import java.util.*;

public class FeatureVector {

    public static int nFeatures = 18;

    private ArrayList<Double> features;
    private Integer label;
    private String qid;
    private String extDocid;

    public FeatureVector(Integer label, String qid, String extDocid) {
        this.label = label;
        this.qid = qid;
        this.extDocid = extDocid;

        this.features = new ArrayList<Double>(nFeatures);

        // first features is dummy, not used. This is for easier indexing
        for (int i = 0; i <= nFeatures; i++) {
            this.features.add(Double.NaN);
        }
    }

    public void normalize(FeatureVector minFV, FeatureVector maxFV) {
        for (int i = 1; i <= nFeatures; i++) {
            // if (ignoreFeatures.contains(i))
            //     continue;

            Double origVal = features.get(i);
            Double minVal = minFV.get(i);
            Double maxVal = maxFV.get(i);

            if (minVal.isNaN()) {
                features.set(i, Double.NaN);
            } else if (minVal == maxVal || origVal.isNaN()) {
                features.set(i, 0.0);
            } else {
                features.set(i, (origVal - minVal) / (maxVal - minVal));
            }
        }
    }

    public String toString() {
        String row = null;

        row = String.format("%d qid:%s", label, qid);

        for (int i = 1; i <= nFeatures; i++) {
            if (get(i).isNaN())
                continue;
            row += String.format(" %d:%f", i, features.get(i));
        }

        row += " # " + extDocid;

        return row;
    }

    public Double get(int i) {
        return features.get(i);
    }

    public void set(int i, Double val) {
        features.set(i, val);
    }

    public void setWithMinMax(int i, Double val,
                              FeatureVector minFV, FeatureVector maxFV) {
        set(i, val);

        if (minFV.get(i).isNaN() || val <= minFV.get(i)) {
            minFV.set(i, val);
        }

        if (maxFV.get(i).isNaN() || val >= maxFV.get(i)) {
            maxFV.set(i, val);
        }
    }

    // f1
    public static Double spamScore(int docid) throws IOException {
        return Double.parseDouble(Idx.getAttribute("score", docid));
    }

    // f2
    public static Double urlDepth(int docid) throws IOException {
        String rawUrl = Idx.getAttribute ("rawUrl", docid);

        // remove prefix protocal
        rawUrl = rawUrl.split("://")[1];

        double cnt = 0.0;
        for (int i = 0; i < rawUrl.length(); i++) {
            if (rawUrl.charAt(i) == '/') {
                cnt += 1.0;
            }
        }

        return cnt;
    }

    // f3
    public static Double fromWikiScore(int docid) throws IOException {
        String rawUrl = Idx.getAttribute ("rawUrl", docid);

        int d = rawUrl.indexOf("wikipedia.org");

        if (d >= 0) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    // f4
    public static Double pr(Map<String, Double> pageRank, String extDocid) {
        return pageRank.getOrDefault(extDocid, Double.NaN);
    }

    // f5
    public static Double bm25(TermVector tv, String[] qryStems)
        throws IOException {
        double score = 0.0;

        double k1 = 1.2;
        double b = 0.75;
        // double k3 = 0.0;
        double nDocs = (double) Idx.getNumDocs();
        double doclen = (double) tv.positionsLength();
        double avgDoclen =
            ((double) Idx.getSumOfFieldLengths(tv.fieldName) /
             (double) Idx.getDocCount(tv.fieldName));

        if (doclen == 0.0) {
            return Double.NaN;
        }

        for (String qryStem: qryStems) {
            int stemIdx = tv.indexOfStem(qryStem);
            if (stemIdx > 0) {
                double df = (double) tv.stemDf(stemIdx);
                double tf = (double) tv.stemFreq(stemIdx);

                double rjs = Math.max(0, Math.log((nDocs - df + 0.5) /
                                                  (df + 0.5)));
                double tfTerm = tf / (tf + k1 * ((1 - b) + b * doclen / avgDoclen));

                score += rjs * tfTerm;

            }
        }

        return score;
    }
}
