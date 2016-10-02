/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.util.*;

/**
 *  The WAND operator for all retrieval models.
 */
public class QrySopIndriWand extends QrySop
    implements QryIFWeightable, QryIFIndriable {

    private ArrayList<Double> weights;

    public QrySopIndriWand() {
        this.weights = new ArrayList<Double>();
    }

    public void appendWeight(double weight) {
        this.weights.add(weight);
    }

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        if (r instanceof RetrievalModelIndri)
            return this.docIteratorHasMatchMin(r);
        else
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the WAND operator.");
    }

    /**
     *  Get a score for the document that docIteratorHasMatch matched.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScore (RetrievalModel r) throws IOException {
        if (r instanceof RetrievalModelIndri) {
            return this.getScoreIndri(r);
        } else {
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the WAND operator.");
        }
    }

    /**
     *  getScore for the Indri retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    private double getScoreIndri(RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            // this should never be called...
            return 0.0;
        } else {
            RetrievalModelIndri indri = (RetrievalModelIndri) r;
            ArrayList<Double> scores = new ArrayList<Double>();
            int docid = this.docIteratorGetMatch();

            for (Qry q_i: this.args) {
                if (q_i.docIteratorHasMatchCache() &&
                    q_i.docIteratorGetMatch() == docid) {
                    scores.add(((QrySop) q_i).getScore(r));
                } else {
                    scores.add(((QryIFIndriable) q_i).getDefaultIndriScore(r, docid));
                }
            }

            return indri.wandCombiner(scores, this.weights);
        }
    }

    /**
     *  getDefaultScore for the Indri retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getDefaultIndriScore(RetrievalModel r, int docid)
        throws IOException {
        RetrievalModelIndri indri = (RetrievalModelIndri) r;
        ArrayList<Double> scores = new ArrayList<Double>();

        for (Qry q_i: this.args) {
            scores.add(((QryIFIndriable) q_i).getDefaultIndriScore(r, docid));
        }

        return indri.wandCombiner(scores, this.weights);
    }

}
