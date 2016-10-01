import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.io.*;
import java.util.*;

public class TestQryParser {

    @Test
    public void testWsum() throws IOException, IllegalArgumentException {
        Qry q = QryParser.getQuery("#WSUM(0.1 word1-word2 0.2 word2)");
    }

}
