import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestRetrievalModelBM25 {
    @Test
    public void calculateScore() {
        RetrievalModelBM25 bm25 = new RetrievalModelBM25(1.2, 0.75, 0.0);
        assertEquals(1.2, bm25.getK1(), 1e-5);
        assertEquals(0.75, bm25.getB(), 1e-6);
    }
}
