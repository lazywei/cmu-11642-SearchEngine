/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */
import java.io.*;
import java.util.*;

/**
 *  The NEAR operator for all retrieval models.
 */
public class QryIopNear extends QryIop {

    private int opDistance;
    public QryIopNear(int opDistance) {
        System.out.println("opDistance = " + opDistance);
        this.opDistance = opDistance;
    }

    /**
     *  Evaluate the query operator; the result is an internal inverted
     *  list that may be accessed via the internal iterators.
     *  @throws IOException Error accessing the Lucene index.
     */
    protected void evaluate () throws IOException {

        //  Create an empty inverted list.  If there are no query arguments,
        //  that's the final result.

        this.invertedList = new InvList (this.getField());

        if (args.size () == 0) {
            return;
        }

        while (this.docIteratorHasMatchAll(null)) {
            // Get current comparing docid.
            // Since QryIop's docIteratorGetMatch will go directly into its
            // invertedList, which has not yet constructed in this case, we will
            // need to use children's invertedList to get the docid.
            int docid = this.args.get(0).docIteratorGetMatch();


            //  Create a new posting that satisfies the location requirement.
            List<Integer> positions = new ArrayList<Integer>();

            while(true) {

                // --- Check if any sub-query's location iterator has finished.
                boolean allLocHasMatch = true;
                for (Qry q_i: this.args) {
                    if (! ((QryIop) q_i).locIteratorHasMatch()) {
                        allLocHasMatch = false;
                        break;
                    }
                }

                // Some query has exhausted its location iterator for current
                // docid, so exit the loop.
                if (! allLocHasMatch)
                    break;
                // --- Done Checking ---

                // --- Start find location that satisfies the requirement.
                Qry q_0 = this.args.get(0);
                int prevLoc = ((QryIop) q_0).locIteratorGetMatch();
                boolean matchFound = true;

                for (int i=1; i<this.args.size(); i++) {
                    Qry q_i = this.args.get(i);
                    int currLoc = ((QryIop) q_i).locIteratorGetMatch();

                    if (validDistance(prevLoc, currLoc)) {
                        prevLoc = currLoc;
                    } else {
                        matchFound = false;
                        ((QryIop) q_i).locIteratorAdvance();
                        break;
                    }
                }

                if (matchFound) {
                    positions.add(prevLoc);
                    for (int i=0; i<this.args.size(); i++) {
                        Qry q_i = this.args.get(i);
                        ((QryIop) q_i).locIteratorAdvance();
                    }
                }
            }

            if (positions.size() > 0)
                this.invertedList.appendPosting (docid, positions);

            // Advance all child queries. Since QryIop's docIteratorAdvancePast
            // is not recursive, we need to advance each children manually
            for (int i=0; i<this.args.size(); i++) {
                Qry q_i = this.args.get(i);
                ((QryIop) q_i).docIteratorAdvancePast(docid);
            }
        }
    }

    private boolean validDistance(int loc1, int loc2) {
        int diff = loc2 - loc1;
        return (diff >= 0) && (diff <= this.opDistance);
    }

}
