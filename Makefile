SRC := $(shell find src -name "*.java")
BIN := bin
LIB := lib
CP  := $(LIB)/*:$(BIN) # ClassPath

MAIN := rescate.Main
JAR  := programa.jar

all: $(BIN)/classes

$(BIN)/classes: $(SRC)
	mkdir -p $(BIN)
	javac -cp $(CP) -d $(BIN) $(SRC)
	touch $(BIN)/classes

# Usar con: make run ARGS="--grupos 100 --estado greedy etc."
run: all
	java -cp $(CP) $(MAIN) $(ARGS)

jar: all
	echo "Main-Class: $(MAIN)" > MANIFEST.MF
	echo "Class-Path: ." >> MANIFEST.MF

	mkdir -p tmp_libs
	for f in $(LIB)/*.jar; do \
    ( cd tmp_libs && jar xf ../$$f ); \
	done

	jar cfm $(JAR) MANIFEST.MF -C $(BIN) . -C tmp_libs .
	rm -rf MANIFEST.MF tmp_libs

	@echo "JAR creado: $(JAR)"

run-jar: jar
	java -jar $(JAR) $(ARGS)

clean:
	rm -rf $(BIN)
