/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The OR operator for all retrieval models.
 */
public class QrySopOr extends QrySop {

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        return this.docIteratorHasMatchMin (r);
    }

    /**
     *  Get a score for the document that docIteratorHasMatch matched.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScore (RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean (r);
        } else if (r instanceof RetrievalModelRankedBoolean) {
            return this.getScoreRankedBoolean (r);
        } else {
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the OR operator.");
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
            double score = 0;

            // Only compare the minDocid's score. Some args' doc id might have
            // been advance to too far away, we should ignore them.
            int minDocid = Integer.MAX_VALUE;

            for (Qry q_i: this.args) {
                if (q_i.docIteratorHasMatchCache()) {
                    if (q_i.docIteratorGetMatch() < minDocid) {
                        minDocid = q_i.docIteratorGetMatch();
                        score = ((QrySop) q_i).getScore (r);
                    } else if (q_i.docIteratorGetMatch() == minDocid) {
                        score = Math.max(score, ((QrySop) q_i).getScore (r));
                    }
                    // else, this arg's doc id is beyond current, so ignore
                }
            }

            return score;
        }
    }

}
