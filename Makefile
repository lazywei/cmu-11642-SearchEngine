LUCENE_JARS=../../lucene-4.3.0/*
CLASS_FLAG=-cp ".:$(LUCENE_JARS)"

all:
	javac $(CLASS_FLAG) -g *.java

run_toy:
	java $(CLASS_FLAG) QryEval params/toy

run_train:
	java $(CLASS_FLAG) QryEval params/train$(T)

clean:
	rm *.class

handin:
	mkdir -p QryEval;
	cp *.java QryEval;
	cp orgMakefile QryEval/Makefile;
	zip -r ../handin.zip QryEval;
	rm -rf QryEval
