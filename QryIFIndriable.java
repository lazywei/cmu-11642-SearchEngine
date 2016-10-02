import java.io.*;

public interface QryIFIndriable {
    /**
     *  Get a default indri score for the document=docid.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getDefaultIndriScore(RetrievalModel r, int docid)
        throws IOException;
}
