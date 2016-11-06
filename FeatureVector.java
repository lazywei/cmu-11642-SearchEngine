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
}
