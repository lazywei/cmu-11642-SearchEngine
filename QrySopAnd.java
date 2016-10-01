/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.util.*;

/**
 *  The AND operator for all retrieval models.
 */
public class QrySopAnd extends QrySop {

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        if (r instanceof RetrievalModelIndri)
            return this.docIteratorHasMatchMin(r);
        else
            return this.docIteratorHasMatchAll(r);
    }

    /**
     *  Get the i'th query argument.  The main value of this method
     *  is that it casts the argument to the correct type.
     *  @param i The index of the query argument to return.
     *  @return The query argument.
     */
    public QrySop getArgSop (int i) {
        return ((QrySop) this.args.get(i));
    }

    /**
     *  Get a score for the document that docIteratorHasMatch matched.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScore (RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean(r);
        } else if (r instanceof RetrievalModelRankedBoolean) {
            return this.getScoreRankedBoolean(r);
        } else if (r instanceof RetrievalModelIndri) {
            return this.getScoreIndri(r);
        } else {
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the AND operator.");
        }
    }

    /**
     *  Get the default score for the document that docIteratorHasMatch matched.
     *  This is particularly designed for Indri model
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getDefaultScore (RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelIndri) {
            return this.getDefaultScoreIndri(r);
        } else {
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the AND operator for getDefaultScore.");
        }
    }

    /**
     *  getScore for the UnrankedBoolean retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    private double getScoreUnrankedBoolean (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    /**
     *  getScore for the RankedBoolean retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    private double getScoreRankedBoolean (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double score = Double.MAX_VALUE;

            for (Qry q_i: this.args) {
                score = Math.min(score, ((QrySop) q_i).getScore (r));
            }

            return score;
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
            return this.getDefaultScoreIndri(r);
        } else {
            RetrievalModelIndri indri = (RetrievalModelIndri) r;
            ArrayList<Double> scores = new ArrayList<Double>();
            int docid = this.docIteratorGetMatch();


            for (Qry q_i: this.args) {
                if (q_i.docIteratorHasMatchCache() &&
                    q_i.docIteratorGetMatch() == docid) {
                    scores.add(((QrySop) q_i).getScore(r));
                } else {
                    scores.add(((QrySop) q_i).getDefaultScore(r));
                }
            }

            return indri.andCombiner(scores);
        }
    }

    /**
     *  getDefaultScore for the Indri retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    private double getDefaultScoreIndri(RetrievalModel r) throws IOException {
        RetrievalModelIndri indri = (RetrievalModelIndri) r;
        ArrayList<Double> scores = new ArrayList<Double>();

        for (Qry q_i: this.args) {
            scores.add(((QrySop) q_i).getDefaultScore(r));
        }

        return indri.andCombiner(scores);
    }

}
