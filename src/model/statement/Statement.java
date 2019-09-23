package model.statement;

public interface Statement {

    StatementType getStatementType();
    String print() throws IllegalAccessException;
    int getActualSize(); //TODO: eventuell entfernen
    int getDepth();
    Statement nextStatement();
    boolean isComplex();
//    void resetCounter();
    void setParentStatement(ComplexStatement parentStatement);
    ComplexStatement getParentStatement();
    String getText();
    //TODO: maybe move methods to complex statements?
    //TODO: eventuell eine parse Methode, die die Methoden in CodeParser ersetzt.
}
