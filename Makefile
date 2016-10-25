# Adjust these parameters for each homework
HW_N=HW3
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

run_exp1: all
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp1-1.param > \
		logs/$(HW_N)-Exp1-1.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp1-2.param > \
		logs/$(HW_N)-Exp1-2.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp1-4.param > \
		logs/$(HW_N)-Exp1-4.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp1-5.param > \
		logs/$(HW_N)-Exp1-5.log

run_tmp_exp: all
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp3-40.param > \
		logs/$(HW_N)-Exp3-40.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp5-bow.param > \
		logs/$(HW_N)-Exp5-bow.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp5-sdm.param > \
		logs/$(HW_N)-Exp5-sdm.log

run_exp4: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp3-50.param > \
		logs/$(HW_N)-Exp3-50.log && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp4-0.param > \
		logs/$(HW_N)-Exp4-0.log && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp4-2.param > \
		logs/$(HW_N)-Exp4-2.log && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp4-4.param > \
		logs/$(HW_N)-Exp4-4.log && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp4-6.param > \
		logs/$(HW_N)-Exp4-6.log && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp4-8.param > \
		logs/$(HW_N)-Exp4-8.log && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp4-10.param > \
		logs/$(HW_N)-Exp4-10.log

run_exp3: all
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp3-40.param > \
		logs/$(HW_N)-Exp3-40.log

run_exp2: all
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp2-10.param > \
		logs/$(HW_N)-Exp2-10.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp2-20.param > \
		logs/$(HW_N)-Exp2-20.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp2-30.param > \
		logs/$(HW_N)-Exp2-30.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp2-40.param > \
		logs/$(HW_N)-Exp2-40.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp2-50.param > \
		logs/$(HW_N)-Exp2-50.log && \
	echo "." && \
	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp2-100.param > \
		logs/$(HW_N)-Exp2-100.log

clean:
	rm -f *.class ./tests/*.class

handin:
	mkdir -p QryEval;
	cp $(HANDIN_FILES) QryEval/;
	cp $(REPORT_FILE) QryEval/;
	cp orgMakefile QryEval/Makefile;
	zip -r ../$(ZIP_FILE) QryEval;
	rm -rf QryEval
