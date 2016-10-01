import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestRetrievalModelIndri.class,
        TestRetrievalModelBM25.class
})

public class TestSuite {
    // the class remains empty,
    // used only as a holder for the above annotations
}
