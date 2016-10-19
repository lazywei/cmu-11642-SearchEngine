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

# run_exp: all
# 	java $(CLASS_FLAG) QryEval params/$(HW_N)/$(HW_N)-Exp-5-4.param

clean:
	rm -f *.class ./tests/*.class

handin:
	mkdir -p QryEval;
	cp $(HANDIN_FILES) QryEval/;
	cp $(REPORT_FILE) QryEval/;
	cp orgMakefile QryEval/Makefile;
	zip -r ../$(ZIP_FILE) QryEval;
	rm -rf QryEval
