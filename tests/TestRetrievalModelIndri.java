import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestRetrievalModelIndri {

    /**
     *  RetrievalModelIndri
     */
    @Test
    public void indriModelInit() {
        RetrievalModelIndri r = new RetrievalModelIndri(2500, 0.4);
        assertEquals(2500, r.getMu());
        assertEquals(0.4, r.getLambda(), 1e-5);
    }
}
