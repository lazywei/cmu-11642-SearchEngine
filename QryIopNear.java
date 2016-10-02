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

            if (docid == 172664) {
                for (Qry q_i: this.args) {
                    System.out.println(q_i);
                    while ( ((QryIop) q_i).locIteratorHasMatch()) {
                        System.out.println(((QryIop) q_i).locIteratorGetMatch());
                        ((QryIop) q_i).locIteratorAdvance();
                    }
                }
                System.out.println("---");
                for (Qry q_i: this.args) {
                    ((QryIop) q_i).docIteratorAdvanceTo(docid);
                }
            }

            while(true) {

                // --- Check if any sub-query's location iterator has finished.
                boolean allLocHasMatch = true;

                // If the first arg is done, we all done
                if (! ((QryIop) this.args.get(0)).locIteratorHasMatch())
                    break;

                for (int i = 1; i < this.args.size(); i++) {
                    QryIop prevQryIop = (QryIop) this.args.get(i-1);
                    QryIop currQryIop = (QryIop) this.args.get(i);


                    currQryIop.locIteratorAdvancePast(
                        prevQryIop.locIteratorGetMatch());

                    if (! currQryIop.locIteratorHasMatch()) {
                        if (docid == 172664) {
                            System.out.println(i + "-th has no match");
                        }
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

                if (docid == 172664) {
                    System.out.println("0-th arg loc = " + prevLoc);
                }
                for (int i=1; i<this.args.size(); i++) {
                    Qry q_i = this.args.get(i);
                    int currLoc = ((QryIop) q_i).locIteratorGetMatch();

                    if (docid == 172664) {
                        System.out.format("%d-th arg loc = %d\n", i, currLoc);
                    }

                    if (currLoc > prevLoc &&
                        (currLoc - prevLoc) <= this.opDistance) {
                        prevLoc = currLoc;
                    } else {
                        matchFound = false;
                        break;
                    }
                }

                if (matchFound) {
                    // When match, all loc iter need to be advanced as one
                    // location can only be matched once.
                    positions.add(prevLoc);
                    for (int i=0; i<this.args.size(); i++) {
                        Qry q_i = this.args.get(i);
                        ((QryIop) q_i).locIteratorAdvance();
                    }
                } else {
                    ((QryIop) this.minLocQry()).locIteratorAdvance();
                }
            }

            if (positions.size() > 0)
                this.invertedList.appendPosting(docid, positions);

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
        return (diff > 0) && (diff <= this.opDistance);
    }

    /**
     *  Helper function, get the i-th args which has the minimal loc iter
     *  location.
     */
    private Qry minLocQry() {
        int idxOfMinLoc = 0;

        for (int i=1; i<this.args.size(); i++) {
            Qry q_i = this.args.get(i);
            if (locIsGeqThan(this.args.get(idxOfMinLoc),
                                 this.args.get(i))) {
                idxOfMinLoc = i;
            }
        }
        return this.args.get(idxOfMinLoc);
    }

    /**
     *  Helper function, check whether the current loc iter's location of q1 is
     *  greater or equal than q2 or not.
     */
    private boolean locIsGeqThan(Qry q1, Qry q2) {
        return (((QryIop) q1).locIteratorGetMatch()
                >= ((QryIop) q2).locIteratorGetMatch());
    }

}
