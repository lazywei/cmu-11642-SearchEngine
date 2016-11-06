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

    private static Map<String, String> parameters;
    private static Boolean isLetor = false;
    private static Boolean doExpand = false;
    private static Map<String, Double> pageRank = null;

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

        initParameters(args[0]);
        isLetor = parameters.get("retrievalAlgorithm").equals("letor");
        doExpand = Boolean.parseBoolean(parameters.getOrDefault("fb", "false"));

        //  Open the index and initialize the retrieval model.

        System.out.println(parameters);
        Idx.open (parameters.get ("indexPath"));
        RetrievalModel model = initializeRetrievalModel();

        // Extract Features
        if (isLetor) {
            pageRank = readPageRank();

            Map<String, List<RelJudge>> relJudges = readRelJudges();

            BufferedReader inputReader = new BufferedReader(
                new FileReader(parameters.get("letor:trainingQueryFile")));
            BufferedWriter outputWriter = new BufferedWriter(
                new FileWriter(parameters.get("letor:trainingFeatureVectorsFile")));

            String trainQueryLine;
            while ((trainQueryLine = inputReader.readLine()) != null) {
                int d = trainQueryLine.indexOf(':');

                if (d < 0) {
                    throw new IllegalArgumentException
                        ("Syntax error:  Missing ':' in query line.");
                }

                String qid = trainQueryLine.substring(0, d);
                String query = trainQueryLine.substring(d + 1);

                generateFeatures(qid, query, relJudges.get(qid), outputWriter);
            }

            outputWriter.close();
        }

        // Call SVM
        exeSVM(true);

        // Perform experiments.
        processQueryFile(model);

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
    private static RetrievalModel initializeRetrievalModel ()
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
        } else if (modelString.equals("letor")) {
            model = new RetrievalModelBM25(
                Double.parseDouble(parameters.get("BM25:k_1")),
                Double.parseDouble(parameters.get("BM25:b")),
                Double.parseDouble(parameters.get("BM25:k_3")));
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

    // Load ranking from file
    static Map<String, ScoreList> loadRanking(String rankingFile)
        throws Exception {
        BufferedReader input = new BufferedReader(new FileReader(rankingFile));
        Map<String, ScoreList> scoreLists = new HashMap<String, ScoreList>();

        String rLine = null;

        while ((rLine = input.readLine()) != null) {
            String[] tokens = rLine.split("\\s");

            String qid = tokens[0];
            String externalDocid = tokens[2];
            int docid = Idx.getInternalDocid(externalDocid);
            Double score = Double.parseDouble(tokens[4]);

            ScoreList sl = null;
            if (scoreLists.containsKey(qid)) {
                sl = scoreLists.get(qid);
            } else {
                sl = new ScoreList();
                scoreLists.put(qid, sl);
            }

            sl.add(docid, score);
        }

        return scoreLists;
    }

    /**
     *  Process the query file.
     *  @param queryFilePath
     *  @param model
     *  @throws IOException Error accessing the Lucene index.
     */
    static void processQueryFile(RetrievalModel model)
        throws IOException {

        String queryFilePath = parameters.get("queryFilePath");
        String outputFilePath = parameters.get("trecEvalOutputPath");

        BufferedReader input = null;
        BufferedWriter output = null;

        // Deal with FB parameters (query expansion)
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
        }

        // Begin processing
        try {
            String qLine = null;

            input = new BufferedReader(new FileReader(queryFilePath));
            output = new BufferedWriter(new FileWriter(outputFilePath));

            Map<String, ScoreList> prerankedScoreLists = null;
            if (doExpand) {
                outputQry = new BufferedWriter(new FileWriter(fbExpansionQueryFile));
                if (fbInitialRankingFile != null) {
                    prerankedScoreLists = loadRanking(fbInitialRankingFile);
                }
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

                if (doExpand) {

                    if (prerankedScoreLists != null) {
                        r = prerankedScoreLists.get(qid);
                    } else {
                        r = processQuery(query, model);
                    }

                    r.sort();
                    r.truncate(fbDocs);

                    // get expanded query
                    String expandedQuery = expandQuery(r, fbDocs, fbTerms, fbMu);
                    String combinedQuery = "#wand(" +
                        fbOrigWeight + " #and(" + query + ") " +
                        (1.0 - fbOrigWeight) + " " + expandedQuery + ")";
                    r = processQuery(combinedQuery, model);
                    r.sort();
                    r.truncate(100);

                    outputExpandedQuery(outputQry, qid, expandedQuery);
                } else {
                    r = processQuery(query, model);
                    r.sort();
                    r.truncate(100);
                }

                if (isLetor) {
                    BufferedWriter letorOutput = new BufferedWriter(
                            new FileWriter(parameters.get("letor:testingFeatureVectorsFile")));
                    List<RelJudge> relJudges = RelJudge.fromScoreList(qid, r);
                    generateFeatures(qid, query, relJudges, letorOutput);
                    letorOutput.close();

                    exeSVM(false);

                    reRank(r);
                    r.sort();
                }

                if (r != null) {
                    outputResults(output, qid, r);
                    System.out.println();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            input.close();
            output.close();

            if (outputQry != null)
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
    private static void initParameters(String parameterFileName)
        throws IOException {

        parameters = new HashMap<String, String>();

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

        return;
    }

    private static void generateFeatures(String qid, String qryStr,
                                         List<RelJudge> relJudges,
                                         BufferedWriter outputWriter)
        throws IOException, Exception {

        List<String> featureVectors = new ArrayList<String>();

        Set<Integer> ignoreFeatures = new HashSet<Integer>();

        String[] qryStems = QryParser.tokenizeString(qryStr);

        FeatureVector minFV = new FeatureVector(-1, "minFV", "minFV");
        FeatureVector maxFV = new FeatureVector(-1, "maxFV", "maxFV");
        List<FeatureVector> fvList = new ArrayList<FeatureVector>();

        for (RelJudge relJudge: relJudges) {
            int docid = -1;
            try {
                // extDocid might not exist
                docid = Idx.getInternalDocid(relJudge.extDocid);
            } catch (Exception e) {
                continue;
            }

            FeatureVector fv = new FeatureVector(relJudge.label, qid, relJudge.extDocid);

            TermVector tvBody = new TermVector(docid, "body");
            TermVector tvTitle = new TermVector(docid, "title");
            TermVector tvUrl = new TermVector(docid, "url");
            TermVector tvInlink = new TermVector(docid, "inlink");

            // f1: spamScore
            fv.setWithMinMax(
                1, FeatureVector.spamScore(docid), minFV, maxFV);

            // f2: Url depth (# of /)
            fv.setWithMinMax(
                2, FeatureVector.urlDepth(docid), minFV, maxFV);

            // f3: FromWikipedia score
            fv.setWithMinMax(
                3, FeatureVector.fromWikiScore(docid), minFV, maxFV);

            // f4: pagerank
            fv.setWithMinMax(
                4, FeatureVector.pr(pageRank, relJudge.extDocid),
                minFV, maxFV);

            // f5: BM25 Body
            fv.setWithMinMax(
                5, FeatureVector.bm25(tvBody, qryStems), minFV, maxFV);

            // f5: BM25 Title
            fv.setWithMinMax(
                8, FeatureVector.bm25(tvTitle, qryStems), minFV, maxFV);

            // f5: BM25 Url
            fv.setWithMinMax(
                11, FeatureVector.bm25(tvUrl, qryStems), minFV, maxFV);

            // f5: BM25 Inlink
            fv.setWithMinMax(
                14, FeatureVector.bm25(tvInlink, qryStems), minFV, maxFV);

            fvList.add(fv);
        }

        // normalize
        for (FeatureVector fv: fvList) {
            fv.normalize(minFV, maxFV);
        }

        outputFeatureVectors(fvList, outputWriter);
    }

    private static Map<String, Double> readPageRank()
        throws IOException {
        Map<String, Double> pageRank = new HashMap<String, Double>();
        BufferedReader br = new BufferedReader(
            new FileReader(parameters.get("letor:pageRankFile")));

        String line;
        while ((line = br.readLine()) != null) {
            String[] vals = line.split("\\s");
            Double pr = Double.parseDouble(vals[1]);

            pageRank.put(vals[0], pr);
        }

        return pageRank;
    }

    private static Map<String, List<RelJudge>> readRelJudges()
        throws IOException {
        Map<String, List<RelJudge>> relJudges =
            new HashMap<String, List<RelJudge>>();

        FileReader in = new FileReader(
            parameters.get("letor:trainingQrelsFile"));
        BufferedReader br = new BufferedReader(in);

        String line;
        while ((line = br.readLine()) != null) {
            String qid = line.split("\\s")[0];
            String extDocid = line.split("\\s")[2];
            Integer label = Integer.parseInt(line.split("\\s")[3]);

            if (!relJudges.containsKey(qid))
                relJudges.put(qid, new ArrayList<RelJudge>());

            relJudges.get(qid).add(new RelJudge(qid, extDocid, label));
        }

        return relJudges;
    }

    private static void outputFeatureVectors(List<FeatureVector> fvList, BufferedWriter output)
        throws IOException {
        if (fvList.size() < 1) {
            output.write("NONE DUMMY ERROR!\n");
        } else {
            for (int i = 0; i < fvList.size(); i++) {
                output.write(fvList.get(i).toString() + "\n");
            }
        }
    }

    private static void exeSVM(boolean isTrain) throws Exception {
        // runs svm_rank_learn from within Java to train the model
        // execPath is the location of the svm_rank_learn utility, 
        // which is specified by letor:svmRankLearnPath in the parameter file.
        // FEAT_GEN.c is the value of the letor:c parameter.

        String[] cmdOpts = null;
        if (isTrain) {
            cmdOpts = new String[] {
                parameters.get("letor:svmRankLearnPath"),
                "-c", parameters.get("letor:svmRankParamC"),
                parameters.get("letor:trainingFeatureVectorsFile"),
                parameters.get("letor:svmRankModelFile") };
        } else {
            cmdOpts = new String[] {
                parameters.get("letor:svmRankClassifyPath"),
                parameters.get("letor:testingFeatureVectorsFile"),
                parameters.get("letor:svmRankModelFile"),
                parameters.get("letor:testingDocumentScores") };
        }

        Process cmdProc = Runtime.getRuntime().exec(cmdOpts);

        // The stdout/stderr consuming code MUST be included.
        // It prevents the OS from running out of output buffer space and stalling.

        // consume stdout and print it out for debugging purposes
        BufferedReader stdoutReader = new BufferedReader(
            new InputStreamReader(cmdProc.getInputStream()));
        String line;
        while ((line = stdoutReader.readLine()) != null) {
            // System.out.println(line);
        }
        // consume stderr and print it for debugging purposes
        BufferedReader stderrReader = new BufferedReader(
            new InputStreamReader(cmdProc.getErrorStream()));
        while ((line = stderrReader.readLine()) != null) {
            // System.out.println(line);
        }

        // get the return value from the executable. 0 means success, non-zero 
        // indicates a problem
        int retValue = cmdProc.waitFor();
        if (retValue != 0) {
            throw new Exception("SVM Rank crashed.");
        }
    }

    private static void reRank(ScoreList r) throws IOException {
        BufferedReader in = new BufferedReader(
            new FileReader(parameters.get("letor:testingDocumentScores")));

        String line;

        int i = 0;
        while((line = in.readLine()) != null) {
            Double svmScore = Double.parseDouble(line);
            r.setDocidScore(i, svmScore);
            i += 1;
        }

        in.close();
    }
}

class RelJudge {
    public String qid;
    public String extDocid;
    public Integer label;

    public RelJudge(String qid, String extDocid, Integer label) {
        this.qid = qid;
        this.extDocid = extDocid;
        this.label = label;
    }

    public static List<RelJudge> fromScoreList(String qid, ScoreList r) {
        List<RelJudge> relJudges = new ArrayList<RelJudge>();

        for (int i = 0; i < r.size(); i++) {
            relJudges.add(new RelJudge(qid, r.getExternalId(i), 0));
        }

        return relJudges;
    }
}
