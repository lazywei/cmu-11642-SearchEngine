/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.lang.IllegalArgumentException;

/**
 *  The SCORE operator for all retrieval models.
 */
public class QrySopScore extends QrySop implements QryIFIndriable {

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
        return this.docIteratorHasMatchFirst (r);
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

        } else if (r instanceof RetrievalModelBM25) {
            return this.getScoreBM25(r);

        } else if (r instanceof RetrievalModelIndri) {
            return this.getScoreIndri(r);

        } else {
            throw new IllegalArgumentException
                (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
    }

    /**
     *  getScore for the Unranked retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScoreUnrankedBoolean (RetrievalModel r) throws IOException {
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
            Qry q = this.args.get (0);
            return ((QryIop) q).getMatchTf();
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
            RetrievalModelBM25 rBM25 = (RetrievalModelBM25) r;
            QryIop qIop = (QryIop)(this.args.get(0));

            return rBM25.calculateScore(qIop);
        }
    }

    /**
     *  getScore for the Indri model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    private double getScoreIndri(RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            // this should never be invoked ...
            return 0.0;
        } else {
            RetrievalModelIndri rIndri = (RetrievalModelIndri) r;
            QryIop qIop = (QryIop)(this.args.get(0));

            return rIndri.getScore(qIop);
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
        RetrievalModelIndri rIndri = (RetrievalModelIndri) r;
        QryIop qIop = (QryIop)(this.args.get(0));

        return rIndri.getDefaultScore(qIop, docid);
    }

    /**
     *  Initialize the query operator (and its arguments), including any
     *  internal iterators.  If the query operator is of type QryIop, it
     *  is fully evaluated, and the results are stored in an internal
     *  inverted list that may be accessed via the internal iterator.
     *  @param r A retrieval model that guides initialization
     *  @throws IOException Error accessing the Lucene index.
     */
    public void initialize (RetrievalModel r) throws IOException {

        Qry q = this.args.get (0);
        q.initialize (r);
    }

}
