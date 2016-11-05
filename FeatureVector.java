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

    public FeatureVector(Integer revLabel, String qryId) {
        label = revLabel;
        qid = qryId;

        features = new ArrayList<Double>(nFeatures);

        for (int i = 0; i < nFeatures; i++) {
            features.add(Double.NaN);
        }
    }

    public void normalize(FeatureVector minFV, FeatureVector maxFV) {
        for (int i = 0; i < features.size(); i++) {
            Double origVal = features.get(i);

            if (origVal.isNaN()) {
                features.set(i, 0.0);
            } else {
                Double minVal = minFV.get(i);
                Double maxVal = maxFV.get(i);
                features.set(i, (origVal - minVal) / (maxVal - minVal));
            }
        }
    }

    public String toString() {
        String row = null;

        row = String.format("%d qid:%s", label, qid);

        for (int i = 0; i < nFeatures; i++) {
            if (get(i).isNaN())
                continue;
            row += " " + features.get(i);
        }

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
