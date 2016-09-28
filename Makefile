# Adjust these parameters for each homework
HANDIN_FILES=*.java
REPORT_FILE=./reports/hw2/cchang3-HW2-Report.pdf
HANDIN_FILE=handin_hw2.zip
# No need to modify these flags
LUCENE_JARS=../lucene-4.3.0/*
CLASS_FLAG=-cp ".:$(LUCENE_JARS)"

all:
	javac $(CLASS_FLAG) -g *.java

run_toy:
	java $(CLASS_FLAG) QryEval params/toy

run_train:
	java $(CLASS_FLAG) QryEval params/train$(T)

run_exp:
	java $(CLASS_FLAG) QryEval params/expSt && \
	java $(CLASS_FLAG) QryEval params/expAnd && \
	java $(CLASS_FLAG) QryEval params/expOr

clean:
	rm *.class

handin:
	mkdir -p QryEval;
	cp $(HANDIN_FILES) QryEval/;
	cp $(REPORT_FILE) QryEval/;
	cp orgMakefile QryEval/Makefile;
	zip -r ../$(HANDIN_FILE) QryEval;
	rm -rf QryEval
