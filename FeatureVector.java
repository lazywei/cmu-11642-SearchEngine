/*
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 *  Version 3.1.2.
 */
import java.io.*;
import java.util.*;

public class FeatureVector {

    private ArrayList<Double> features;
    private Integer label;
    private String qid;

    public FeatureVector(Integer revLabel, String qryId, ArrayList<Double> fs) {
        qid = qryId;
        label = revLabel;
        features = fs;
    }

    public FeatureVector(Integer revLabel, String qryId, ArrayList<Double> fs,
                         FeatureVector minFV, FeatureVector maxFV) {
        qid = qryId;
        label = revLabel;
        features = fs;

        for (int i = 0; i < fs.size(); i++) {
            if (fs.get(i).isNaN())
                continue;

            if (minFV.get(i).isNaN() || fs.get(i) <= minFV.get(i)) {
                minFV.set(i, fs.get(i));
            }

            if (maxFV.get(i).isNaN() || fs.get(i) >= maxFV.get(i)) {
                maxFV.set(i, fs.get(i));
            }
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

        row = String.format("%d qid:%d ", label, qid);

        row += features.get(0);

        return row;
    }

    public Double get(int i) {
        return features.get(i);
    }

    public void set(int i, Double val) {
        features.set(i, val);
    }
}
