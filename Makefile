# Adjust these parameters for each homework
HW_N=HW5
HANDIN_FILES=*.java
REPORT_FILE=./reports/$(HW_N)/cchang3-$(HW_N)-Report.pdf
ZIP_FILE=handin_$(HW_N).zip
# No need to modify these flags
LUCENE_JARS=../lucene-4.3.0/*
JUNIT_JARS=../junit/*
CLASS_FLAG=-cp ".:./tests:$(LUCENE_JARS):$(JUNIT_JARS)"

## General compile tasks

classes = $(patsubst %.java,%.class,$(wildcard *.java))
test_classes = $(patsubst %.java,%.class,$(wildcard ./tests/*.java))

all: $(classes)

test: all $(test_classes)
	java $(CLASS_FLAG) org.junit.runner.JUnitCore TestSuite

%.class: %.java
	javac $(CLASS_FLAG) -g $<

## Runner tasks

run_toy: all
	java $(CLASS_FLAG) QryEval params/toy

run_train: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Train-$(T).param

inspect: all
	java $(CLASS_FLAG) InspectIndex -index INPUT_DIR/index -list-stats

exp1: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp1-BM25-PM2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp1-BM25-X.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp1-BM25.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp1-Indri-PM2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp1-Indri-X.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp1-Indri.param

exp3bm25: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-PM2-0.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-PM2-10.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-PM2-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-PM2-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-PM2-6.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-PM2-8.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-X-0.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-X-10.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-X-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-X-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-X-6.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-BM25-X-8.param

exp3indri: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-PM2-0.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-PM2-10.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-PM2-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-PM2-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-PM2-6.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-PM2-8.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-X-0.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-X-10.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-X-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-X-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-X-6.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp3-Indri-X-8.param

exp4bm25pm2: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-PM2-1.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-PM2-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-PM2-3.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-PM2-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-PM2-5.param

exp4bm25x: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-X-1.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-X-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-X-3.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-X-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-BM25-X-5.param

exp4indripm2: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-PM2-1.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-PM2-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-PM2-3.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-PM2-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-PM2-5.param

exp4indrix: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-X-1.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-X-2.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-X-3.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-X-4.param && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/Exp4-Indri-X-5.param

clean:
	rm -f *.class ./tests/*.class

handin:
	mkdir -p QryEval;
	cp $(HANDIN_FILES) QryEval/;
	cp $(REPORT_FILE) QryEval/;
	cp orgMakefile QryEval/Makefile;
	zip -r ../$(ZIP_FILE) QryEval;
	rm -rf QryEval
