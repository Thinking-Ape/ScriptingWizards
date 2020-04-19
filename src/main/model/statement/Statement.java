package main.model.statement;

import java.util.List;

public abstract class Statement {

    StatementType statementType;
    ComplexStatement parentStatement;


    public final StatementType getStatementType(){
        return statementType;
    }
    public final ComplexStatement getParentStatement(){return parentStatement;}

    final void setParentStatement(ComplexStatement parentStatement){
        this.parentStatement = parentStatement;
    }

    abstract public int getActualSize();
    abstract public int getDepth();
    abstract public boolean isComplex();
    abstract public String getCode();
    abstract public List<String> getCodeLines();
    abstract public String getAllText();
}
