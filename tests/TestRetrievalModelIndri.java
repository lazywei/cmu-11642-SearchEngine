import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.io.*;
import java.util.*;

public class TestRetrievalModelIndri {
    private RetrievalModel r;
    private Qry q;

    @BeforeClass
    public static void oneTimeSetUp() throws IOException {
        Idx.open("INPUT_DIR/toy/index");
    }

    @Before
    public void setUp() throws IOException {
        this.r = (RetrievalModel) new RetrievalModelIndri(2500, 0.4);
        this.q = QryParser.getQuery("#and(pie doctor)");
        this.q.initialize(this.r);
    }

    @Test
    public void testInit() {
        RetrievalModelIndri r = new RetrievalModelIndri(2500, 0.4);
        assertEquals(2500, r.getMu());
        assertEquals(0.4, r.getLambda(), 1e-6);
    }

    @Test
    public void testDefaultScore() {
        assertEquals(1.0, ((RetrievalModelIndri) this.r).getDefaultScore(), 1e-6);
    }

    @Test
    public void testActualScore() {
    }

    @Test
    public void testIndriAndCombiner() throws IOException {
        ArrayList<Double> scores = new ArrayList<Double>();
        scores.add(2.0);
        scores.add(2.0);
        assertEquals(
            2.0,
            ((RetrievalModelIndri) this.r).andCombiner(scores),
            1e-6);
    }

    @Test
    public void testIndriAndMatch() throws IOException {
        // check the strategy is matching the min docid
        assertEquals(true, this.q.docIteratorHasMatch(r));
        assertEquals(0, this.q.docIteratorGetMatch());
    }


    // @Test
    // public void testIndriAndCombiner() throws IOException {
    //     QrySopAnd qAnd = (QrySopAnd) this.q;

    //     double scoreAnd = qAnd.getScore(r);
    //     double scorePie = qAnd.getArgSop(0).getScore(r);
    //     double scoreDoctor = qAnd.getArgSop(1).getScore(r);

    //     // Check the geometric mean is correctly performed
    //     assertEquals(Math.sqrt(scorePie) * Math.sqrt(scoreDoctor),
    //                  scoreAnd, 1e-6);
    // }
}
