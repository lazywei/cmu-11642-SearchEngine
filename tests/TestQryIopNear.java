import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.io.*;
import java.util.*;

public class TestQryIopNear {
    private RetrievalModel r;
    private Qry q;

    @BeforeClass
    public static void oneTimeSetUp() throws IOException {
        Idx.open("INPUT_DIR/toy/index");
    }

    @Before
    public void setUp() throws IOException {
        this.r = (RetrievalModel) new RetrievalModelIndri(2500, 0.4);
        this.q = QryParser.getQuery("#near/2(apple pie)");
        this.q.initialize(this.r);
    }

    @Test
    public void testEvaluate() {
        QryIopNear qNear = (QryIopNear) this.q;
        qNear.docIteratorAdvanceTo(1);
        assertEquals(1, qNear.getMatchTf());
    }
}
