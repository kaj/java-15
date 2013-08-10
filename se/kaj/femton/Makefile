CLASSPATH =

SRC	= FemtonApp.java
CLASSES = $(SRC:%.java=%.class) FemtonImage.class FemtonPane.class
DIR	= se/kaj/femton
JARFILE	= femton.jar

ifdef USEJIKES
JAVAC	= jikes -classpath /usr/local/java/lib/classes.zip:../../..
else
JAVAC	= javac -deprecation
endif
JAR	= jar cvf0

all:	$(JARFILE) .cvsignore

$(JARFILE): $(CLASSES)
	cd ../../.. && $(JAR) $(DIR)/$(JARFILE) $(CLASSES:%=$(DIR)/%)

$(CLASSES): $(SRC)
	${JAVAC} $(SRC)

clean:
	rm -f $(CLASSES)

.cvsignore: Makefile
	@echo Regenerating $@
	@echo $@ $(JARFILE) > $@
	@echo $(CLASSES) >> $@