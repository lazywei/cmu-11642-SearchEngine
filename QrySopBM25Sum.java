/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.lang.IllegalArgumentException;

/**
 *  The SUM operator for all Okapi BM25 models.
 */
public class QrySopBM25Sum extends QrySop {

    /**
     *  Document-independent values that should be determined just once.
     *  Some retrieval models have these, some don't.
     */

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        return this.docIteratorHasMatchMin(r);
    }

    /**
     *  Get a score for the document that docIteratorHasMatch matched.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScore(RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelBM25) {
            return this.getScoreBM25 (r);
        } else {
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the SUM operator.");
        }
    }

    /**
     *  getScore for the Okapi BM25 model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScoreBM25 (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double score = 0;
            int docid = this.docIteratorGetMatch();

            /**
             * We are using docIteratorHasMatchMin, which means we might
             * have such situation:
             * q1: (doc1, doc3, doc5)
             * q2: (doc1, doc2, doc4)
             * q3: (doc2, doc3, doc6)
             * since we are using document-at-a-time, the first match would
             * be (q1: doc1), (q2: doc1), (q3: doc2). But we only want to
             * calculate the scores for q1 and q2 with respect to doc1, so we
             * should check if the query's matched document is the one we
             * actually want to calculate with.
             */
            for (Qry q_i: this.args) {
                if (q_i.docIteratorHasMatchCache() &&
                    q_i.docIteratorGetMatch() == docid) {
                    score += ((QrySop) q_i).getScore(r);
                }
            }

            return score;
        }
    }
}
