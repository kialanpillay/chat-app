JFLAGS = -g
JC = javac
BINDIR=./bin
SRCDIR=./src
DOCDIR=./javadocs

default:
	javac src/Connection.java && javac src/Server.java && javac src/Protocol.java && javac src/Client.java && javac src/Message.java

run:
	touch server/meta.txt
	java src/Server 8080

docs:
	javadoc  -classpath ${SRCDIR} -d ${DOCDIR} ${SRCDIR}/*.java

clean:
	rm -f ${SRCDIR}/*.class

cleandocs:
	rm -rf ${DOCDIR}/*
