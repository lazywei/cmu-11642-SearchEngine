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
    public void testScore() throws IOException {
        QryIop q = new QryIopTerm("pie");
        q.initialize(this.r);

        double len_d = 15;
        double len_c = 49;
        double ctf = 6;
        double tf = 2;
        double lambda = 0.4;
        double mu = 2500;

        assertEquals(
            (1 - lambda)*((0 + mu * (ctf) / (len_c)) / (len_d + mu) )
            + lambda * (ctf / len_c),
            ((RetrievalModelIndri) this.r).getDefaultScore(q, 0),
            1e-6);

        if (q.docIteratorHasMatch(this.r)) {
            assertEquals(
                (1 - lambda)*((tf + mu * (ctf) / (len_c)) / (len_d + mu) )
                + lambda * (ctf / len_c),
                ((RetrievalModelIndri) this.r).getScore(q),
                1e-6);
        }
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
    public void testIndriWandCombiner() throws IOException {
        ArrayList<Double> scores = new ArrayList<Double>();
        scores.add(27.0);
        scores.add(8.0);

        ArrayList<Double> weights = new ArrayList<Double>();
        weights.add(2.0);
        weights.add(1.0);

        assertEquals(
            18.0,
            ((RetrievalModelIndri) this.r).wandCombiner(scores, weights),
            1e-6);
    }

    @Test
    public void testIndriWsumCombiner() throws IOException {
        ArrayList<Double> scores = new ArrayList<Double>();
        scores.add(27.0);
        scores.add(8.0);

        ArrayList<Double> weights = new ArrayList<Double>();
        weights.add(2.0);
        weights.add(1.0);

        assertEquals(
            11.0,
            ((RetrievalModelIndri) this.r).wsumCombiner(scores, weights),
            1e-6);
    }

    @Test
    public void testIndriAndMatch() throws IOException {
        // check the strategy is matching the min docid
        assertEquals(true, this.q.docIteratorHasMatch(r));
        assertEquals(0, this.q.docIteratorGetMatch());
    }

    // @Test
    // public void testIndriAndWithNear() throws IOException {
    //     // check the strategy is matching the min docid
    //     Qry q = QryParser.getQuery("#and(toy1.url #NEAR/2(health benefits))");
    //     q.initialize(this.r);
    //     q.docIteratorHasMatch(this.r);

    //     QrySop qSop = (QrySop) q;
    //     qSop.getScore(this.r);

    //     q.docIteratorAdvanceTo(1);
    //     q.docIteratorHasMatch(this.r);
    //     qSop.getScore(this.r);
    // }
}
