JFLAGS = -g
JC = javac
BINDIR=./bin
SRCDIR=./src
DOCDIR=./javadocs

.SUFFIXES: .java .class

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JC) -d $(BINDIR)/ -cp $(BINDIR):$(SRCDIR) $<

CLASSES = Application.class Server.class Client.class


CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)
SRC_FILES=$(SRC:%.java=$(SRCDIR)/%.java)

default: $(CLASS_FILES)

docs:
	javadoc  -classpath ${BINDIR} -d ${DOCDIR} ${SRCDIR}/*.java

clean:
	rm -f ${BINDIR}/*.class

cleandocs:
	rm -rf ${DOCDIR}/*
