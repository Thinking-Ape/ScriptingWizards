package main.model.statement;

import java.util.List;

public class SimpleStatement extends Statement {

    SimpleStatement(StatementType statementType) {
        this.statementType = statementType;
    }
    public SimpleStatement() {
        this.statementType = StatementType.SIMPLE;
    }

    public int getActualSize() {
        return getCode().equals("") ? 0 : 1;
    }

    @Override
    public String getAllText() {
        return getCode();
    }
    public int getDepth(){
        return parentStatement == null ? 1 : parentStatement.getDepth() + 1;
    }

    @Override
    public String getCode() {
        return "";
    }

    @Override
    public List<String> getCodeLines() {
        return List.of(getCode());
    }


    @Override
    public boolean isComplex() {
        return false;
    }

}
