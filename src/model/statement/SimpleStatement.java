package model.statement;

public class SimpleStatement implements Statement {

//    int depth;
    StatementType statementType;
    ComplexStatement parentStatement;

    SimpleStatement(StatementType statementType) {
//        this.expressionTree = new ExpressionTree(new ExpressionLeaf(variable,depth+1), ExpressionType.SINGLE,new ExpressionLeaf(value,depth+1),depth);
        this.statementType = statementType;
//        this.depth = depth;
    }
    public SimpleStatement() {
//        this.expressionTree = new ExpressionTree(new ExpressionLeaf(variable,depth+1), ExpressionType.SINGLE,new ExpressionLeaf(value,depth+1),depth);
        this.statementType = StatementType.SIMPLE;
//        this.depth = depth;
    }

    @Override
    public ComplexStatement getParentStatement() {
        return parentStatement;
    }

    @Override
    public void setParentStatement(ComplexStatement parentStatement) {
        this.parentStatement = parentStatement;
    }

    @Override
    public Statement nextStatement() {
        parentStatement.skip();
        return this;
    }
//
//    @Override
//    public void print() {
//        System.out.print(expressionTree.getValue());
//    }

//    public abstract void print();

    public int getActualSize() {
        return 1;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public int getDepth(){
        return parentStatement == null ? 1 : parentStatement.getDepth() + 1;
    }


    @Override
    public String getText() {
        return "";
    }


    @Override
    public String print() {
        return getText();
    }

//    @Override
//    public void resetCounter() {
//        //do nothing
//    }

    @Override
    public boolean isComplex() {
        return false;
    }


}
