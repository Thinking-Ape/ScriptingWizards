package main.model.statement;

import java.util.List;

public interface Statement {

    StatementType getStatementType();
    String print();
    int getActualSize();
    int getDepth();
    boolean isComplex();
    void setParentStatement(ComplexStatement parentStatement);
    ComplexStatement getParentStatement();
    String getText();
    List<String> getCodeLines();
}
