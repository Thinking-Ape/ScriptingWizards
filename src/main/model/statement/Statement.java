package main.model.statement;

import java.util.List;

public interface Statement {

    StatementType getStatementType();
    String print();
    int getActualSize(); //TODO: eventuell entfernen
    int getDepth();
    Statement nextStatement();
    boolean isComplex();
//    void resetCounter();
    void setParentStatement(ComplexStatement parentStatement);
    ComplexStatement getParentStatement();
    String getText();

    List<String> getCodeLines();
    //TODO: maybe move methods to complex statements?
    //TODO: eventuell eine parse Methode, die die Methoden in OldCodeParser ersetzt.
}
