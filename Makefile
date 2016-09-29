# Adjust these parameters for each homework
HW_N=HW2
HANDIN_FILES=*.java
REPORT_FILE=./reports/$(HW_N)/cchang3-$(HW_N)-Report.pdf
HANDIN_FILE=handin_$(HW_N).zip
# No need to modify these flags
LUCENE_JARS=../lucene-4.3.0/*
CLASS_FLAG=-cp ".:$(LUCENE_JARS)"

all:
	javac $(CLASS_FLAG) -g *.java

run_toy:
	java $(CLASS_FLAG) QryEval params/toy

run_train:
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Train-$(T).param

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
