@echo off

javac -d build -Xlint:none src\parser\*.java src\semantic\SymbolTable\*.java src\semantic\*.java src\syntaxtree\*.java src\visitor\*.java src\frame\*.java src\mips\*.java src\Temp\*.java src\util\*.java src\Translate\*.java src\Tree\*.java src\canon\*.java src\Assem\*.java src\FlowGraph\*.java src\Graph\*.java src\RegAlloc\*.java src\Main.java

echo Compilation done successfully.
