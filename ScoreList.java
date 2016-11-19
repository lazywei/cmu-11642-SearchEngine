/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */
import java.io.*;
import java.util.*;

/**
 *  This class implements the document score list data structure
 *  and provides methods for accessing and manipulating them.
 */
public class ScoreList {

    //  A utility class to create a <internalDocid, externalDocid, score>
    //  object.

    private class ScoreListEntry {
        private int docid;
        private String externalId;
        private double score;

        private ScoreListEntry(int internalDocid, double score) {
            this.docid = internalDocid;
            this.score = score;

            try {
                this.externalId = Idx.getExternalDocid (this.docid);
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     *  A list of document ids and scores. 
     */
    private ArrayList<ScoreListEntry> scores = new ArrayList<ScoreListEntry>();

    /**
     *  Append a document score to a score list.
     *  @param docid An internal document id.
     *  @param score The document's score.
     */
    public void add(int docid, double score) {
        scores.add(new ScoreListEntry(docid, score));
    }

    /**
     *  Get the internal docid of the n'th entry.
     *  @param n The index of the requested document.
     *  @return The internal document id.
     */
    public int getDocid(int n) {
        return this.scores.get(n).docid;
    }

    /**
     *  Get the internal docid of the n'th entry.
     *  @param n The index of the requested document.
     *  @return The internal document id.
     */
    public String getExternalId(int n) {
        return this.scores.get(n).externalId;
    }

    /**
     *  Get the score of the n'th entry.
     *  @param n The index of the requested document score.
     *  @return The document's score.
     */
    public double getDocidScore(int n) {
        return this.scores.get(n).score;
    }

    /**
     *  Set the score of the n'th entry.
     *  @param n The index of the score to change.
     *  @param score The new score.
     */
    public void setDocidScore(int n, double score) {
        this.scores.get(n).score = score;
    }

    /**
     *  Get the size of the score list.
     *  @return The size of the posting list.
     */
    public int size() {
        return this.scores.size();
    }

    /*
     *  Compare two ScoreListEntry objects.  Sort by score, then
     *  internal docid.
     */
    public class ScoreListComparator implements Comparator<ScoreListEntry> {

        @Override
        public int compare(ScoreListEntry s1, ScoreListEntry s2) {
            if (s1.score > s2.score)
                return -1;
            if (s1.score < s2.score)
                return 1;

            return s1.externalId.compareTo(s2.externalId);
        }
    }

    /**
     *  Sort the list by score and external document id.
     */
    public void sort () {
        Collections.sort(this.scores, new ScoreListComparator());
    }

    /**
     * Reduce the score list to the first num results to save on RAM.
     *
     * @param num Number of results to keep.
     */
    public void truncate(int num) {
        ArrayList<ScoreListEntry> truncated = new ArrayList<ScoreListEntry>(
            this.scores.subList(0, Math.min(num, scores.size())));

        this.scores.clear();
        this.scores = truncated;
    }

    // do this after truncate
    public Double getSumScore(HashMap<Integer, Double> origRHash) {
        double sum = 0.0;
        for (ScoreListEntry sEntry: this.scores) {
            if (origRHash != null && !origRHash.containsKey(sEntry.docid))
                continue;
            else
                sum += sEntry.score;
        }

        return sum;
    }

    public void normalize(double maxSum) {
        for (ScoreListEntry sEntry: this.scores) {
            sEntry.score = sEntry.score / maxSum;
        }
    }

    // do this after truncate
    public static void normalize(ScoreList origR, ArrayList<ScoreList> intentsR) {
        HashMap<Integer, Double> origRHash = origR.toHashMap();
        double maxSum = origR.getSumScore(null);
        for (ScoreList sl : intentsR) {
            maxSum = Math.max(maxSum, sl.getSumScore(origRHash));
        }

        for (ScoreList sl : intentsR) {
            sl.normalize(maxSum);
        }
        origR.normalize(maxSum);
    }

    public HashMap<Integer, Double> toHashMap() {
        HashMap<Integer, Double> scoreMap = new HashMap<Integer, Double>();

        for (ScoreListEntry sEntry: this.scores) {
            scoreMap.put(sEntry.docid, sEntry.score);
        }

        return scoreMap;
    }

    // public void truncateByOrigRanking(ScoreList origR) {
    //     HashMap<Integer, Double> origRHash = origR.toHashMap();
    //     ArrayList<ScoreListEntry> oldScores = this.scores;
    //     this.scores = new ArrayList<ScoreListEntry>();

    //     for (int i = 0; i < oldScores.size(); i++) {
    //         if (origRHash.containsKey(oldScores.get(i).docid)) {
    //             this.scores.add(oldScores.get(i));
    //         }
    //     }
    // }
}
