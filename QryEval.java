/*
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 *  Version 3.1.2.
 */
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *  This software illustrates the architecture for the portion of a
 *  search engine that evaluates queries.  It is a guide for class
 *  homework assignments, so it emphasizes simplicity over efficiency.
 *  It implements an unranked Boolean retrieval model, however it is
 *  easily extended to other retrieval models.  For more information,
 *  see the ReadMe.txt file.
 */
public class QryEval {

    //  --------------- Constants and variables ---------------------

    private static final String USAGE =
        "Usage:  java QryEval paramFile\n\n";

    private static final String[] TEXT_FIELDS =
    { "body", "title", "url", "inlink" };


    //  --------------- Methods ---------------------------------------

    /**
     *  @param args The only argument is the parameter file name.
     *  @throws Exception Error accessing the Lucene index.
     */
    public static void main(String[] args) throws Exception {

        //  This is a timer that you may find useful.  It is used here to
        //  time how long the entire program takes, but you can move it
        //  around to time specific parts of your code.

        Timer timer = new Timer();
        timer.start ();

        //  Check that a parameter file is included, and that the required
        //  parameters are present.  Just store the parameters.  They get
        //  processed later during initialization of different system
        //  components.

        if (args.length < 1) {
            throw new IllegalArgumentException (USAGE);
        }

        Map<String, String> parameters = readParameterFile (args[0]);

        //  Open the index and initialize the retrieval model.

        System.out.println(parameters);
        Idx.open (parameters.get ("indexPath"));
        RetrievalModel model = initializeRetrievalModel (parameters);

        //  Perform experiments.
        processQueryFile(parameters, model);

        //  Clean up.

        timer.stop ();
        System.out.println ("Time:  " + timer);
    }

    /**
     *  Allocate the retrieval model and initialize it using parameters
     *  from the parameter file.
     *  @return The initialized retrieval model
     *  @throws IOException Error accessing the Lucene index.
     */
    private static RetrievalModel initializeRetrievalModel (Map<String, String> parameters)
        throws IOException {

        RetrievalModel model = null;
        String modelString = parameters.get("retrievalAlgorithm").toLowerCase();

        if (modelString.equals("unrankedboolean")) {
            model = new RetrievalModelUnrankedBoolean();
        } else if (modelString.equals("rankedboolean")) {
            model = new RetrievalModelRankedBoolean();
        } else if (modelString.equals("bm25")) {
            model = new RetrievalModelBM25(
                Double.parseDouble(parameters.get("BM25:k_1")),
                Double.parseDouble(parameters.get("BM25:b")),
                Double.parseDouble(parameters.get("BM25:k_3")));
        } else if (modelString.equals("indri")) {
            model = new RetrievalModelIndri(
                Integer.parseInt(parameters.get("Indri:mu")),
                Double.parseDouble(parameters.get("Indri:lambda")));
        } else {
            throw new IllegalArgumentException
                ("Unknown retrieval model " + parameters.get("retrievalAlgorithm"));
        }

        return model;
    }

    /**
     * Print a message indicating the amount of memory used. The caller can
     * indicate whether garbage collection should be performed, which slows the
     * program but reduces memory usage.
     *
     * @param gc
     *          If true, run the garbage collector before reporting.
     */
    public static void printMemoryUsage(boolean gc) {

        Runtime runtime = Runtime.getRuntime();

        if (gc)
            runtime.gc();

        System.out.println("Memory used:  "
                           + ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)) + " MB");
    }

    /**
     * Process one query.
     * @param qString A string that contains a query.
     * @param model The retrieval model determines how matching and scoring is done.
     * @return Search results
     * @throws IOException Error accessing the index
     */
    static ScoreList processQuery(String qString, RetrievalModel model)
        throws IOException {

        String defaultOp = model.defaultQrySopName ();
        qString = defaultOp + "(" + qString + ")";
        Qry q = QryParser.getQuery (qString);

        // Show the query that is evaluated

        System.out.println("    --> " + q);

        if (q != null) {

            ScoreList r = new ScoreList ();

            if (q.args.size () > 0) {		// Ignore empty queries

                q.initialize (model);

                while (q.docIteratorHasMatch (model)) {
                    int docid = q.docIteratorGetMatch ();
                    double score = ((QrySop) q).getScore (model);
                    r.add (docid, score);
                    q.docIteratorAdvancePast (docid);
                }
            }

            return r;
        } else
            return null;
    }

    /**
     *  Process the query file.
     *  @param queryFilePath
     *  @param model
     *  @throws IOException Error accessing the Lucene index.
     */
    static void processQueryFile(Map<String, String> parameters,
                                 RetrievalModel model)
        throws IOException {

        String queryFilePath = parameters.get("queryFilePath");
        String outputFilePath = parameters.get("trecEvalOutputPath");

        BufferedReader input = null;
        BufferedWriter output = null;

        // Deal with FB parameters (query expansion)
        Boolean doExpand = Boolean.parseBoolean(
            parameters.getOrDefault("fb", "false"));
        int fbDocs = 0;
        int fbTerms = 0;
        int fbMu = 0;
        double fbOrigWeight = 0.0;
        String fbInitialRankingFile = "";
        String fbExpansionQueryFile = "";
        BufferedWriter outputQry = null;

        if (doExpand) {
            fbDocs = Integer.parseInt(parameters.get("fbDocs"));
            fbTerms = Integer.parseInt(parameters.get("fbTerms"));
            fbMu = Integer.parseInt(parameters.get("fbMu"));
            fbOrigWeight = Double.parseDouble(parameters.get("fbOrigWeight"));
            fbInitialRankingFile = parameters.get("fbInitialRankingFile");
            fbExpansionQueryFile = parameters.get("fbExpansionQueryFile");

            if (fbInitialRankingFile != null) {
                // load ranking
            }
        }

        // Begin processing
        try {
            String qLine = null;

            input = new BufferedReader(new FileReader(queryFilePath));
            output = new BufferedWriter(new FileWriter(outputFilePath));

            if (doExpand) {
                outputQry = new BufferedWriter(new FileWriter(fbExpansionQueryFile));
            }

            //  Each pass of the loop processes one query.

            while ((qLine = input.readLine()) != null) {
                int d = qLine.indexOf(':');

                if (d < 0) {
                    throw new IllegalArgumentException
                        ("Syntax error:  Missing ':' in query line.");
                }

                printMemoryUsage(false);

                String qid = qLine.substring(0, d);
                String query = qLine.substring(d + 1);

                System.out.println("Query " + qLine);

                ScoreList r = null;

                r = processQuery(query, model);

                if (doExpand) {
                    r.sort();
                    r.truncate(fbDocs);

                    // get expanded query
                    String expandedQuery = expandQuery(r, fbDocs, fbTerms, fbMu);
                    String combinedQuery = "#wand(" +
                        fbOrigWeight + " #and(" + query + ") " +
                        (1.0 - fbOrigWeight) + " " + expandedQuery + ")";
                    r = processQuery(combinedQuery, model);

                    outputExpandedQuery(outputQry, qid, expandedQuery);
                }

                r.sort();
                r.truncate(100);

                if (r != null) {
                    outputResults(output, qid, r);
                    System.out.println();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            input.close();
            output.close();
            outputQry.close();
        }
    }

    /**
     * Print the query results.
     *
     * @param queryName
     *          Original query.
     * @param result
     *          A list of document ids and scores
     * @throws IOException Error accessing the Lucene index.
     */
    static void outputResults(BufferedWriter output, String queryName, ScoreList result) throws IOException {
        if (result.size() < 1) {
            output.write(queryName + "\tQ0\tdummy\t1\t0\trunID\n");
        } else {
            for (int i = 0; (i < result.size()) && (i < 100); i++) {
                output.write(queryName + "\tQ0\t" +
                             Idx.getExternalDocid(result.getDocid(i)) + "\t"
                             + (i+1) + "\t"   /* Rank */
                             + result.getDocidScore(i) + "\trunID\n");
            }
        }
    }

    /**
     * Generate feedback (expanded queries).
     *
     * @return The <queryId, expandedQry> map.
     * @throws IOException Error accessing the Lucene index.
     */
    static String expandQuery(ScoreList result, int fbDocs,
                              int fbTerms, int fbMu) throws IOException {
        Map<String, Double> termScores = new HashMap<String, Double>();

        String field = "body";
        long lenC = Idx.getSumOfFieldLengths(field);

        // Ryan explained the TermVector API to me.
        // Collect terms
        for (int i = 0; i < fbDocs; i++) {
            TermVector tv = new TermVector(result.getDocid(i), field);

            for (int j = 1; j < tv.stemsLength(); j++) {
                String stem = tv.stemString(j);
                if (stem.indexOf('.') < 0 && stem.indexOf(',') < 0 &&
                    !termScores.containsKey(stem)) {
                    termScores.put(stem, 0.0);
                }
            }
        }

        // Calculate scores
        for (int i = 0; i < fbDocs; i++) {
            TermVector tv = new TermVector(result.getDocid(i), field);
            int lenD = tv.positionsLength();

            for (String term: termScores.keySet()) {
                long ctf = Idx.getTotalTermFreq(field, term);
                int termIdx = tv.indexOfStem(term);
                int tf = 0;
                double oldScores = termScores.get(term);
                double probTD = 0.0;
                double logTerm = 0.0;

                if (termIdx > 0) {
                    tf = tv.stemFreq(termIdx);
                }

                probTD = ((double) tf) +
                    ((double) fbMu) * ((double) ctf) / ((double) lenC);
                probTD = probTD / ((double) (lenD + fbMu));

                logTerm = Math.log(((double) lenC) / ((double) ctf));

                termScores.put(term,
                               oldScores +
                               (probTD * result.getDocidScore(i) * logTerm));
            }
        }

        String expandedQuery = termScores
            .entrySet()
            .stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(fbTerms)
            .map((e1) -> String.valueOf(e1.getValue()) + " " + e1.getKey())
            .collect(Collectors.joining(" "));

        return "#wand(" + expandedQuery + ")";
    }

    /**
     * Output the expanded query.
     */
    static void outputExpandedQuery(BufferedWriter output, String qid, String expandedQuery) throws IOException {
        output.write(qid + ": " + expandedQuery + "\n");
    }

    /**
     *  Read the specified parameter file, and confirm that the required
     *  parameters are present.  The parameters are returned in a
     *  HashMap.  The caller (or its minions) are responsible for processing
     *  them.
     *  @return The parameters, in <key, value> format.
     */
    private static Map<String, String> readParameterFile (String parameterFileName)
        throws IOException {

        Map<String, String> parameters = new HashMap<String, String>();

        File parameterFile = new File (parameterFileName);

        if (! parameterFile.canRead ()) {
            throw new IllegalArgumentException
                ("Can't read " + parameterFileName);
        }

        Scanner scan = new Scanner(parameterFile);
        String line = null;
        do {
            line = scan.nextLine();
            String[] pair = line.split ("=");
            parameters.put(pair[0].trim(), pair[1].trim());
        } while (scan.hasNext());

        scan.close();

        if (! (parameters.containsKey ("indexPath") &&
               parameters.containsKey ("queryFilePath") &&
               parameters.containsKey ("trecEvalOutputPath") &&
               parameters.containsKey ("retrievalAlgorithm"))) {
            throw new IllegalArgumentException
                ("Required parameters were missing from the parameter file.");
        }

        return parameters;
    }

}
