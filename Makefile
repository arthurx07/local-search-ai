SRC := $(shell find src -name "*.java")
BIN := bin
LIB := lib
CP  := $(LIB)/*:$(BIN) # ClassPath

MAIN := Main   # or com.example.Main

all: $(BIN)/classes

$(BIN)/classes: $(SRC)
    mkdir -p $(BIN)
    javac -cp $(CP) -d $(BIN) $(SRC)
    touch $(BIN)/classes

run: all
    java -cp $(CP) $(MAIN)

clean:
    rm -rf $(BIN)
